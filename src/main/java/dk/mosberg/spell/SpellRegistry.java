package dk.mosberg.spell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Registry for spells loaded from data packs.
 *
 * TODO: Implement spell validation on load (check required fields) TODO: Add spell compatibility
 * checking (version, dependencies) TODO: Implement hot-reload for spells during development TODO:
 * Add spell inheritance/templates system TODO: Implement spell balancing presets (easy, normal,
 * hard) TODO: Add spell tag system for categorization and filtering TODO: Implement spell
 * compression for network transfer TODO: Add spell dependency resolution (spells requiring other
 * spells) TODO: Implement spell variant/modification system TODO: Add spell versioning and
 * migration system
 */
public class SpellRegistry {
    private static final Map<Identifier, Spell> SPELLS = new HashMap<>();
    private static int cachedMaxTier = -1;
    private static List<Spell> cachedMaxTierList;

    @SuppressWarnings("deprecation")
    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @SuppressWarnings("null")
                    @Override
                    public @NotNull Identifier getFabricId() {
                        return Identifier.of(MAM.MOD_ID, "spells");
                    }

                    @Override
                    public void reload(@NotNull ResourceManager manager) {
                        loadSpells(manager);
                    }
                });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MAM.LOGGER.info("Loaded {} spells", SPELLS.size());
        });

        MAM.LOGGER.info("Registered spell registry");
    }

    private static void loadSpells(ResourceManager manager) {
        SPELLS.clear();
        cachedMaxTier = -1;
        cachedMaxTierList = null;
        int loaded = 0;
        int failed = 0;

        // Find all spell JSON files in data/{namespace}/spells/**/*.json
        Map<Identifier, Resource> resources =
                manager.findResources("spells", id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();

            // Convert file path to spell ID: data/mam/spells/air/air_strike.json -> mam:air_strike
            String path = fileId.getPath();
            // Remove "spells/" prefix and ".json" suffix
            String spellPath = path.substring("spells/".length(), path.length() - ".json".length());
            // Get just the filename (last part after /)
            String spellName = spellPath.substring(spellPath.lastIndexOf('/') + 1);
            Identifier spellId = Identifier.of(fileId.getNamespace(), spellName);

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(entry.getValue().getInputStream(),
                            StandardCharsets.UTF_8))) {

                JsonElement json = JsonParser.parseReader(reader);
                Spell spell = Spell.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(
                        error -> MAM.LOGGER.error("Failed to parse spell {}: {}", spellId, error))
                        .orElse(null);

                if (spell != null && validateSpell(spell)) {
                    SPELLS.put(spell.getId(), spell);
                    loaded++;
                    MAM.LOGGER.debug("Loaded spell: {}", spell.getId());
                } else {
                    if (spell != null) {
                        MAM.LOGGER.error("Spell {} failed validation", spellId);
                    }
                    failed++;
                }
            } catch (Exception e) {
                MAM.LOGGER.error("Failed to load spell from {}: {}", fileId, e.getMessage());
                failed++;
            }
        }

        MAM.LOGGER.info("Spell loading complete: {} loaded, {} failed", loaded, failed);
    }

    public static Optional<Spell> getSpell(Identifier id) {
        return Optional.ofNullable(SPELLS.get(id));
    }

    public static Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(SPELLS.values());
    }

    public static List<Spell> getSpellsBySchool(SpellSchool school) {
        if (SPELLS.isEmpty())
            return List.of();
        return SPELLS.values().stream().filter(spell -> spell.getSchool() == school)
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    public static List<Spell> getSpellsByTier(int tier) {
        if (SPELLS.isEmpty())
            return List.of();
        return SPELLS.values().stream().filter(spell -> spell.getTier() == tier).toList();
    }

    public static List<Spell> getSpellsBySchoolAndMaxTier(SpellSchool school, int maxTier) {
        return SPELLS.values().stream()
                .filter(spell -> spell.getSchool() == school && spell.getTier() <= maxTier)
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    /**
     * Get all spells across all schools up to a maximum tier level (cached for performance)
     *
     * @param maxTier Maximum tier level (inclusive)
     * @return List of spells sorted by tier
     */
    public static List<Spell> getSpellsByMaxTier(int maxTier) {
        // Cache result for repeated queries with same tier
        if (cachedMaxTier == maxTier && cachedMaxTierList != null) {
            return cachedMaxTierList;
        }

        List<Spell> result = SPELLS.values().stream().filter(spell -> spell.getTier() <= maxTier)
                .sorted(Comparator.comparingInt(Spell::getTier)
                        .thenComparing(spell -> spell.getSchool().toString()))
                .toList();

        cachedMaxTier = maxTier;
        cachedMaxTierList = result;
        return result;
    }

    /**
     * Get spells filtered by tags.
     *
     * @param tag Tag to filter by
     * @return List of spells with the specified tag
     */
    public static List<Spell> getSpellsByTag(String tag) {
        return SPELLS.values().stream().filter(spell -> spell.getTags().contains(tag))
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    /**
     * Get spells filtered by multiple tags (must have all tags).
     *
     * @param tags Tags to filter by
     * @return List of spells with all specified tags
     */
    public static List<Spell> getSpellsByTags(List<String> tags) {
        return SPELLS.values().stream().filter(spell -> spell.getTags().containsAll(tags))
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    /**
     * Get spells by cast type.
     *
     * @param castType Cast type to filter by
     * @return List of spells with the specified cast type
     */
    public static List<Spell> getSpellsByCastType(SpellCastType castType) {
        return SPELLS.values().stream().filter(spell -> spell.getCastType() == castType)
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    /**
     * Validate spell data for required fields and reasonable values.
     *
     * @param spell Spell to validate
     * @return true if spell passes validation
     */
    private static boolean validateSpell(Spell spell) {
        // Check required fields
        if (spell.getId() == null) {
            MAM.LOGGER.error("Spell missing ID");
            return false;
        }
        if (spell.getName() == null || spell.getName().isEmpty()) {
            MAM.LOGGER.error("Spell {} missing name", spell.getId());
            return false;
        }
        if (spell.getSchool() == null) {
            MAM.LOGGER.error("Spell {} missing school", spell.getId());
            return false;
        }
        if (spell.getCastType() == null) {
            MAM.LOGGER.error("Spell {} missing cast type", spell.getId());
            return false;
        }

        // Check reasonable value ranges
        if (spell.getManaCost() < 0) {
            MAM.LOGGER.error("Spell {} has negative mana cost: {}", spell.getId(),
                    spell.getManaCost());
            return false;
        }
        if (spell.getTier() < 1 || spell.getTier() > 4) {
            MAM.LOGGER.error("Spell {} has invalid tier: {} (must be 1-4)", spell.getId(),
                    spell.getTier());
            return false;
        }
        if (spell.getCooldown() < 0) {
            MAM.LOGGER.error("Spell {} has negative cooldown: {}", spell.getId(),
                    spell.getCooldown());
            return false;
        }
        if (spell.getDamage() < 0) {
            MAM.LOGGER.error("Spell {} has negative damage: {}", spell.getId(), spell.getDamage());
            return false;
        }
        if (spell.getRange() <= 0) {
            MAM.LOGGER.error("Spell {} has invalid range: {}", spell.getId(), spell.getRange());
            return false;
        }

        MAM.LOGGER.debug("Spell {} passed validation", spell.getId());
        return true;
    }

    /**
     * Check if a spell's dependencies are met (prerequisite spells exist). Dependencies are
     * specified in spell tags prefixed with "requires:". Example: ["offensive",
     * "requires:fire_bolt"]
     *
     * @param spell Spell to check dependencies for
     * @return true if all dependencies are met
     */
    public static boolean checkSpellDependencies(Spell spell) {
        for (String tag : spell.getTags()) {
            if (tag.startsWith("requires:")) {
                String requiredSpellName = tag.substring("requires:".length());
                Identifier requiredSpellId =
                        Identifier.of(spell.getId().getNamespace(), requiredSpellName);

                if (!SPELLS.containsKey(requiredSpellId)) {
                    MAM.LOGGER.warn("Spell {} has unmet dependency: {}", spell.getId(),
                            requiredSpellId);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get all spells that depend on the specified spell.
     *
     * @param spellId Spell identifier
     * @return List of spells that require this spell
     */
    public static List<Spell> getSpellDependents(Identifier spellId) {
        String dependencyTag = "requires:" + spellId.getPath();
        return SPELLS.values().stream().filter(spell -> spell.getTags().contains(dependencyTag))
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    /**
     * Check if a player has access to a spell based on dependencies. This method checks if all
     * prerequisite spells are in the known spells list.
     *
     * @param spell Spell to check
     * @param knownSpells List of spell IDs the player has learned
     * @return true if player can access this spell
     */
    public static boolean canAccessSpell(Spell spell, List<Identifier> knownSpells) {
        for (String tag : spell.getTags()) {
            if (tag.startsWith("requires:")) {
                String requiredSpellName = tag.substring("requires:".length());
                Identifier requiredSpellId =
                        Identifier.of(spell.getId().getNamespace(), requiredSpellName);

                if (!knownSpells.contains(requiredSpellId)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get all spells accessible with the given known spells and tier limit.
     *
     * @param knownSpells List of spell IDs the player has learned
     * @param maxTier Maximum tier the player can access
     * @return List of spells available to the player
     */
    public static List<Spell> getAccessibleSpells(List<Identifier> knownSpells, int maxTier) {
        return SPELLS.values().stream().filter(spell -> spell.getTier() <= maxTier)
                .filter(spell -> canAccessSpell(spell, knownSpells))
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }
}
