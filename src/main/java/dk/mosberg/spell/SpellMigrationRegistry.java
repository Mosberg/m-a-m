package dk.mosberg.spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import com.mojang.serialization.Dynamic;
import dk.mosberg.MAM;

/**
 * Registry for spell format version migrations, enabling backward compatibility with older spell
 * JSON formats.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Register version migrations
 * <li>Migrate spell data from old versions to current version
 * <li>Get migration path between versions
 * <li>List available migrations
 * <li>Query target/current version
 * </ul>
 *
 * <p>
 * Migrations must form a connected graph (no gaps): can't go from v1 to v3 without v2.
 *
 * <p>
 * Example usage:
 * 
 * <pre>
 * SpellMigrationRegistry.register(SpellMigration.create(1, 2, this::migrateV1ToV2));
 * SpellMigrationRegistry.register(SpellMigration.create(2, 3, this::migrateV2ToV3));
 *
 * // Spell with formatVersion=1 automatically migrated to 3
 * Dynamic<?> spellData = SpellMigrationRegistry.migrateToLatest(spellJson, 1);
 * </pre>
 */
public class SpellMigrationRegistry {

    /** Current target format version (spells at or below are considered current) */
    private static final int CURRENT_VERSION = 3;

    /** Map of migration: (fromVersion, toVersion) -> SpellMigration */
    private static final Map<Integer, SpellMigration> MIGRATIONS_BY_FROM_VERSION = new TreeMap<>();

    /** Ordered list of all migrations (for easy iteration) */
    private static final List<SpellMigration> MIGRATION_CHAIN = new ArrayList<>();

    /**
     * Registers a spell format migration.
     *
     * <p>
     * Validates that:
     * <ul>
     * <li>Migration is valid (from < to)
     * <li>No duplicate migration for same from version
     * <li>Migrations form a connected chain (no gaps)
     * </ul>
     *
     * @param migration migration to register
     * @return true if registered successfully
     */
    public static boolean register(SpellMigration migration) {
        // Validate migration
        if (!migration.isValid()) {
            MAM.LOGGER.warn("Cannot register invalid spell migration: {}", migration);
            return false;
        }

        // Check for duplicate
        if (MIGRATIONS_BY_FROM_VERSION.containsKey(migration.getFromVersion())) {
            MAM.LOGGER.warn("Migration from version {} already registered",
                    migration.getFromVersion());
            return false;
        }

        // Store migration
        MIGRATIONS_BY_FROM_VERSION.put(migration.getFromVersion(), migration);
        MIGRATION_CHAIN.add(migration);
        MIGRATION_CHAIN.sort(SpellMigration.byFromVersion());

        MAM.LOGGER.info("Registered spell migration: {}", migration);
        return true;
    }

    /**
     * Gets the migration from a specific version.
     *
     * @param fromVersion version to migrate from
     * @return Optional containing migration if registered
     */
    public static Optional<SpellMigration> getMigration(int fromVersion) {
        return Optional.ofNullable(MIGRATIONS_BY_FROM_VERSION.get(fromVersion));
    }

    /**
     * Migrates spell data from old version to current version.
     *
     * <p>
     * Applies migrations sequentially from current version to CURRENT_VERSION. Handles:
     * <ul>
     * <li>Spells already at current version (no migration)
     * <li>Spells from earlier versions (applies chain of migrations)
     * <li>Spells from future versions (logs warning, returns unchanged)
     * </ul>
     *
     * @param spellData spell JSON data
     * @param formatVersion current version of spell data
     * @return migrated spell data (at CURRENT_VERSION)
     */
    public static Dynamic<?> migrateToLatest(Dynamic<?> spellData, int formatVersion) {
        // Already at current version
        if (formatVersion >= CURRENT_VERSION) {
            if (formatVersion > CURRENT_VERSION) {
                MAM.LOGGER.warn("Spell has newer format version {} than current {} - may lose data",
                        formatVersion, CURRENT_VERSION);
            }
            return spellData;
        }

        // Apply migrations sequentially
        Dynamic<?> result = spellData;
        int currentVersion = formatVersion;

        for (SpellMigration migration : MIGRATION_CHAIN) {
            if (migration.getFromVersion() >= currentVersion
                    && migration.getFromVersion() < CURRENT_VERSION) {
                // Check for migration gap
                if (migration.getFromVersion() != currentVersion) {
                    MAM.LOGGER.warn("Migration gap: v{} → v{}, expected continuous chain",
                            currentVersion, migration.getFromVersion());
                    return result;
                }

                // Apply migration
                MAM.LOGGER.debug("Applying spell migration: v{} → v{}", currentVersion,
                        migration.getToVersion());
                result = migration.apply(result);
                currentVersion = migration.getToVersion();
            }
        }

        // Update format version in data
        result = result.set("formatVersion", result.createInt(CURRENT_VERSION));

        if (currentVersion == CURRENT_VERSION) {
            MAM.LOGGER.debug("Migrated spell from v{} to current v{}", formatVersion,
                    CURRENT_VERSION);
        } else {
            MAM.LOGGER.warn("Incomplete migration: v{} → v{}, expected v{}", formatVersion,
                    currentVersion, CURRENT_VERSION);
        }

        return result;
    }

    /**
     * Gets the migration path between two versions.
     *
     * @param fromVersion source version
     * @param toVersion target version
     * @return list of migrations to apply (empty if not possible)
     */
    public static List<SpellMigration> getPath(int fromVersion, int toVersion) {
        List<SpellMigration> path = new ArrayList<>();

        if (fromVersion >= toVersion) {
            return path; // Backward migration not supported
        }

        for (SpellMigration m : MIGRATION_CHAIN) {
            if (m.getFromVersion() >= fromVersion && m.getToVersion() <= toVersion) {
                path.add(m);
            }
        }

        return path;
    }

    /**
     * Lists all registered migrations.
     *
     * @return list of all migrations in version order
     */
    public static List<SpellMigration> getAllMigrations() {
        return new ArrayList<>(MIGRATION_CHAIN);
    }

    /**
     * Gets count of registered migrations.
     *
     * @return number of registered migrations
     */
    public static int getMigrationCount() {
        return MIGRATIONS_BY_FROM_VERSION.size();
    }

    /**
     * Gets the current target format version.
     *
     * @return current version number
     */
    public static int getCurrentVersion() {
        return CURRENT_VERSION;
    }

    /**
     * Clears all registered migrations (useful for testing).
     */
    public static void clear() {
        MIGRATIONS_BY_FROM_VERSION.clear();
        MIGRATION_CHAIN.clear();
        MAM.LOGGER.info("Cleared spell migration registry");
    }

    /**
     * Registers default spell format migrations.
     *
     * <p>
     * Default migrations:
     * <ul>
     * <li>v1 → v2: Adds "variantIds" list field (empty list for base spells)
     * <li>v2 → v3: Adds "poolLinks" for mana pool linking support
     * </ul>
     *
     * <p>
     * Called during mod initialization.
     */
    public static void registerDefaults() {
        MAM.LOGGER.info("Registering default spell format migrations");

        // Migration 1 → 2: Add variantIds field
        register(new SpellMigration(1, 2, spellData -> {
            // Only add variantIds if not present
            if (spellData.get("variantIds").result().isEmpty()) {
                return spellData.set("variantIds",
                        spellData.createList(java.util.stream.Stream.of()));
            }
            return spellData;
        }, "Add variantIds list field for spell variants"));

        // Migration 2 → 3: Add poolLinks field
        register(new SpellMigration(2, 3, spellData -> {
            // Only add poolLinks if not present
            if (spellData.get("poolLinks").result().isEmpty()) {
                return spellData.set("poolLinks",
                        spellData.createList(java.util.stream.Stream.of()));
            }
            return spellData;
        }, "Add poolLinks list field for mana pool linking"));

        MAM.LOGGER.info("Registered {} default spell migrations", getMigrationCount());
    }
}
