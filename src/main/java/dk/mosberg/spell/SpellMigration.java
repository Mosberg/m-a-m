package dk.mosberg.spell;

import java.util.Comparator;
import com.mojang.serialization.Dynamic;

/**
 * Spell version migration system for handling evolving spell JSON formats.
 *
 * <p>
 * Manages migrations from one spell format version to another, enabling:
 * <ul>
 * <li>Gradual API evolution without breaking old spell definitions
 * <li>Automatic data transformation for old spell JSON files
 * <li>Migration logging and validation
 * <li>Rollback prevention (only forward migrations supported)
 * </ul>
 *
 * <p>
 * Example migration: Version 1 → Version 2 adds "projectileType" field
 * 
 * <pre>
 * SpellMigration migration = SpellMigration.create(1, 2, dynamic -> {
 *     if (!dynamic.get("projectileType").isPresent()) {
 *         return dynamic.set("projectileType", "standard");
 *     }
 *     return dynamic;
 * });
 * </pre>
 *
 * <p>
 * Migrations are applied sequentially from current version to target version. Each migration must
 * be idempotent (applying twice yields same result as applying once).
 */
public class SpellMigration {

    private final int fromVersion;
    private final int toVersion;
    private final MigrationFunction function;
    private final String description;

    /**
     * Function that transforms spell data from one version to the next.
     *
     * <p>
     * Implementations must:
     * <ul>
     * <li>Handle missing fields gracefully (add defaults)
     * <li>Be idempotent (safe to apply multiple times)
     * <li>Return modified Dynamic with updated data
     * <li>Log any transformations applied
     * </ul>
     */
    @FunctionalInterface
    public interface MigrationFunction {
        /**
         * Migrates spell data to next version.
         *
         * @param spellData spell JSON data (wrapped in Dynamic)
         * @return transformed spell data
         */
        Dynamic<?> migrate(Dynamic<?> spellData);
    }

    /**
     * Creates a migration from one version to the next.
     *
     * @param fromVersion source version (must be < toVersion)
     * @param toVersion target version (must be > fromVersion)
     * @param function transformation function
     * @param description description of changes in this migration
     */
    public SpellMigration(int fromVersion, int toVersion, MigrationFunction function,
            String description) {
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.function = function;
        this.description = description;
    }

    /**
     * Creates a migration with minimal setup (default description).
     *
     * @param fromVersion source version
     * @param toVersion target version
     * @param function transformation function
     * @return migration instance
     */
    public static SpellMigration create(int fromVersion, int toVersion,
            MigrationFunction function) {
        return new SpellMigration(fromVersion, toVersion, function,
                String.format("Version %d → %d", fromVersion, toVersion));
    }

    /**
     * Validates migration constraints.
     *
     * @return true if from < to, and function is non-null
     */
    public boolean isValid() {
        return fromVersion < toVersion && function != null;
    }

    /**
     * Applies this migration to spell data.
     *
     * @param spellData spell JSON data
     * @return migrated spell data
     */
    public Dynamic<?> apply(Dynamic<?> spellData) {
        return function.migrate(spellData);
    }

    public int getFromVersion() {
        return fromVersion;
    }

    public int getToVersion() {
        return toVersion;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Compares migrations by source version (for ordering).
     *
     * @return comparator ordering by fromVersion ascending
     */
    public static Comparator<SpellMigration> byFromVersion() {
        return Comparator.comparingInt(SpellMigration::getFromVersion);
    }

    @Override
    public String toString() {
        return String.format("SpellMigration{%d→%d: %s}", fromVersion, toVersion, description);
    }
}
