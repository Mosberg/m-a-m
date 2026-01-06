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
 * Features implemented: dependency checking, hot-reload, difficulty presets, compression helpers,
 * inheritance/templates resolution.
 *
 * TODO: Add spell compatibility checking (strict schema versioning) TODO: Implement spell
 * variant/modification system TODO: Add spell versioning and migration system
 */
public class SpellRegistry {
    private static final Map<Identifier, Spell> SPELLS = new HashMap<>();
    private static final int SUPPORTED_SPELL_FORMAT = 1; // for future schema evolution
    private static int cachedMaxTier = -1;
    private static List<Spell> cachedMaxTierList;
    private static DifficultyPreset currentDifficulty = DifficultyPreset.NORMAL;

    /**
     * Difficulty presets that scale spell effectiveness.
     */
    public enum DifficultyPreset {
        EASY(0.8f, 1.2f, 0.8f, "Easy - Lower costs, higher damage"), NORMAL(1.0f, 1.0f, 1.0f,
                "Normal - Balanced gameplay"), HARD(1.3f, 0.85f, 1.2f,
                        "Hard - Higher costs, lower damage"), NIGHTMARE(1.5f, 0.7f, 1.5f,
                                "Nightmare - Challenging gameplay");

        private final float manaCostMultiplier;
        private final float damageMultiplier;
        private final float cooldownMultiplier;
        private final String description;

        DifficultyPreset(float manaCostMultiplier, float damageMultiplier, float cooldownMultiplier,
                String description) {
            this.manaCostMultiplier = manaCostMultiplier;
            this.damageMultiplier = damageMultiplier;
            this.cooldownMultiplier = cooldownMultiplier;
            this.description = description;
        }

        public float getManaCostMultiplier() {
            return manaCostMultiplier;
        }

        public float getDamageMultiplier() {
            return damageMultiplier;
        }

        public float getCooldownMultiplier() {
            return cooldownMultiplier;
        }

        public String getDescription() {
            return description;
        }
    }

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

                if (spell != null && validateSpell(spell) && isCompatible(spell)
                        && checkSpellDependencies(spell)) {
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

        // Resolve simple inheritance/templates after initial load
        resolveInheritance();

        MAM.LOGGER.info("Spell loading complete: {} loaded, {} failed", loaded, failed);
    }

    /**
     * Manually trigger a spell reload. Useful for development and testing. Can be called from a
     * command or debug interface.
     *
     * @param manager Resource manager to load spells from
     * @return Number of spells successfully loaded
     */
    public static int hotReload(ResourceManager manager) {
        int beforeCount = SPELLS.size();
        MAM.LOGGER.info("Hot-reloading spells... (current: {})", beforeCount);

        loadSpells(manager);

        int afterCount = SPELLS.size();
        MAM.LOGGER.info("Hot-reload complete: {} spells loaded (was {})", afterCount, beforeCount);

        return afterCount;
    }

    /**
     * Check if hot-reload is enabled (always true for development). This can be expanded to check
     * for dev/production mode.
     */
    public static boolean isHotReloadEnabled() {
        // For now, always enabled. Can be expanded with config option.
        return true;
    }

    /**
     * Set the current difficulty preset.
     *
     * @param preset Difficulty preset to apply
     */
    public static void setDifficulty(DifficultyPreset preset) {
        currentDifficulty = preset;
        MAM.LOGGER.info("Difficulty set to: {} - {}", preset.name(), preset.getDescription());
    }

    /**
     * Get the current difficulty preset.
     */
    public static DifficultyPreset getDifficulty() {
        return currentDifficulty;
    }

    /**
     * Apply difficulty scaling to a spell's mana cost.
     */
    public static float getScaledManaCost(float baseCost) {
        return baseCost * currentDifficulty.getManaCostMultiplier();
    }

    /**
     * Apply difficulty scaling to a spell's damage.
     */
    public static float getScaledDamage(float baseDamage) {
        return baseDamage * currentDifficulty.getDamageMultiplier();
    }

    /**
     * Apply difficulty scaling to a spell's cooldown.
     */
    public static float getScaledCooldown(float baseCooldown) {
        return baseCooldown * currentDifficulty.getCooldownMultiplier();
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
     * Basic compatibility check placeholder. Currently validates tier range and reserved for schema
     * format checks. Extend to enforce schema versions when spells start specifying it.
     */
    private static boolean isCompatible(Spell spell) {
        // Placeholder for future: when Spell exposes optional format, reject unsupported.
        // Example:
        // if (spell.getFormat().isPresent() && spell.getFormat().getAsInt() !=
        // SUPPORTED_SPELL_FORMAT) {
        // MAM.LOGGER.warn("Skipping spell {} due to incompatible format {} (supported {})",
        // spell.getId(), spell.getFormat().getAsInt(), SUPPORTED_SPELL_FORMAT);
        // return false;
        // }
        return true;
    }

    /**
     * Resolve simple inheritance: child spells may declare a parent via optional field 'parent'.
     * Child inherits tags, vfx, sound, status effects, and custom data keys it doesn't define.
     * Numeric fields are not overridden to avoid ambiguity with defaults.
     */
    private static void resolveInheritance() {
        boolean changed;
        int passes = 0;
        do {
            changed = false;
            passes++;
            for (var entry : new java.util.ArrayList<>(SPELLS.entrySet())) {
                Spell child = entry.getValue();
                var parentOpt = child.getParent();
                if (parentOpt.isEmpty())
                    continue;
                Spell parent = SPELLS.get(parentOpt.get());
                if (parent == null) {
                    MAM.LOGGER.warn("Spell {} declares missing parent {}", child.getId(),
                            parentOpt.get());
                    continue;
                }

                // Merge tags (union)
                java.util.Set<String> tags = new java.util.LinkedHashSet<>(parent.getTags());
                tags.addAll(child.getTags());

                // Merge status effects (inherit if child has none)
                java.util.List<Spell.StatusEffectEntry> effects =
                        child.getStatusEffects().isEmpty() ? parent.getStatusEffects()
                                : child.getStatusEffects();

                // Merge custom data (parent keys fill gaps)
                java.util.Map<String, Float> custom =
                        new java.util.HashMap<>(parent.getCustomData());
                custom.putAll(child.getCustomData());

                // Inherit sound if child empty
                String sound =
                        (child.getSound() == null || child.getSound().isEmpty()) ? parent.getSound()
                                : child.getSound();

                // Inherit VFX if child none
                var vfx = child.getVfxOptional().isPresent() ? child.getVfxOptional()
                        : parent.getVfxOptional();

                // Inherit animation if child none
                var anim = child.getAnimationOptional().isPresent() ? child.getAnimationOptional()
                        : parent.getAnimationOptional();

                Spell merged = new Spell(child.getId(), child.getName(), child.getSchool().name(),
                        child.getDescription(), child.getCastType().name(), child.getManaCost(),
                        child.getCastTime(), child.getCooldown(), child.getTier(),
                        child.getRequiredLevel(), child.getDamage(), child.getRange(),
                        child.getProjectileSpeed(), child.getAoeRadius(), child.getKnockback(),
                        effects, custom, sound, vfx, new java.util.ArrayList<>(tags),
                        java.util.Optional.of(child.getRarity().name()), child.getParent(), anim);

                if (merged != child) {
                    SPELLS.put(child.getId(), merged);
                    changed = true;
                }
            }
        } while (changed && passes < 5);
    }

    /**
     * Network compression helpers for spells using JSON + GZIP. Useful for lightweight sync.
     */
    public static Optional<byte[]> compressSpell(Spell spell) {
        try {
            var json = Spell.CODEC.encodeStart(JsonOps.INSTANCE, spell).result().orElseThrow();
            byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(baos)) {
                gzip.write(bytes);
            }
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            MAM.LOGGER.error("Failed compressing spell {}: {}", spell.getId(), e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<Spell> decompressSpell(byte[] compressed) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressed);
            try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(bais)) {
                String json = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                return Spell.CODEC
                        .parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(json))
                        .result();
            }
        } catch (Exception e) {
            MAM.LOGGER.error("Failed decompressing spell: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static Optional<byte[]> compressSpells(Collection<Spell> spells) {
        try {
            // Encode as array of spell JSON objects
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (Spell s : spells) {
                var json = Spell.CODEC.encodeStart(JsonOps.INSTANCE, s).result();
                json.ifPresent(j -> arr.add((com.google.gson.JsonElement) j));
            }
            byte[] bytes = arr.toString().getBytes(StandardCharsets.UTF_8);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (java.util.zip.GZIPOutputStream gzip = new java.util.zip.GZIPOutputStream(baos)) {
                gzip.write(bytes);
            }
            return Optional.of(baos.toByteArray());
        } catch (Exception e) {
            MAM.LOGGER.error("Failed compressing spells: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static List<Spell> decompressSpells(byte[] compressed) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(compressed);
            try (java.util.zip.GZIPInputStream gzip = new java.util.zip.GZIPInputStream(bais)) {
                String json = new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
                com.google.gson.JsonArray arr =
                        com.google.gson.JsonParser.parseString(json).getAsJsonArray();
                java.util.ArrayList<Spell> list = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement el : arr) {
                    Spell.CODEC.parse(JsonOps.INSTANCE, el).result().ifPresent(list::add);
                }
                return list;
            }
        } catch (Exception e) {
            MAM.LOGGER.error("Failed decompressing spells: {}", e.getMessage());
            return List.of();
        }
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
