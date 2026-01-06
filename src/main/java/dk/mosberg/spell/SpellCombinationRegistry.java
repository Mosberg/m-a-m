package dk.mosberg.spell;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;
import net.minecraft.util.Identifier;

/**
 * Registry for spell combinations/fusions: recipes for combining multiple spells into a single
 * enhanced spell.
 *
 * Enables depth in spell progression: - Players learn combination recipes from quests, leveling,
 * unlocks - Combining spells in correct sequence triggers the fusion - Resulting fused spell is
 * more powerful but has cooldown/cost penalties
 *
 * Example combinations: - Fire Strike + Air Strike = Plasma Strike (damage boost, AOE) - Water Heal
 * + Earth Wall = Regeneration Field (healing + protection) - Summon Ally + Fire Strike = Infernal
 * Minion (minion with fire attacks)
 */
public class SpellCombinationRegistry {

    // Map from combination ID to the recipe
    private static final Map<Identifier, SpellCombination> COMBINATIONS = new HashMap<>();

    // Map from output spell ID to list of combinations that produce it
    // For quick lookup: "which combinations create this spell?"
    private static final Map<Identifier, List<SpellCombination>> OUTPUT_TO_COMBINATIONS =
            new HashMap<>();

    private SpellCombinationRegistry() {}

    /**
     * Register a spell combination recipe.
     *
     * @param combination The combination to register
     * @throws IllegalArgumentException if combination is invalid or ID already exists
     */
    public static void register(SpellCombination combination) {
        if (!combination.isValid()) {
            throw new IllegalArgumentException("Invalid spell combination: " + combination.id());
        }

        if (COMBINATIONS.containsKey(combination.id())) {
            throw new IllegalArgumentException(
                    "Spell combination already registered: " + combination.id());
        }

        COMBINATIONS.put(combination.id(), combination);

        // Index by output spell
        OUTPUT_TO_COMBINATIONS
                .computeIfAbsent(combination.outputSpell(), k -> new java.util.ArrayList<>())
                .add(combination);

        MAM.LOGGER.info("Registered spell combination '{}' producing '{}'", combination.id(),
                combination.outputSpell());
    }

    /**
     * Get a combination by ID.
     */
    public static Optional<SpellCombination> getCombination(Identifier id) {
        return Optional.ofNullable(COMBINATIONS.get(id));
    }

    /**
     * Get all combinations that produce a specific output spell.
     *
     * @param outputSpellId The output spell's ID
     * @return List of combinations producing this spell
     */
    public static List<SpellCombination> getCombinationsProducing(Identifier outputSpellId) {
        return OUTPUT_TO_COMBINATIONS.getOrDefault(outputSpellId, List.of());
    }

    /**
     * Find a matching combination for a sequence of recently cast spells.
     *
     * Searches all combinations to find the first match. If multiple combinations match, returns
     * the first registered one.
     *
     * @param recentCasts List of recently cast spell IDs (in order)
     * @return Optional containing the matching combination if found
     */
    public static Optional<SpellCombination> findMatching(List<Identifier> recentCasts) {
        return COMBINATIONS.values().stream().filter(combo -> combo.matches(recentCasts))
                .findFirst();
    }

    /**
     * Get all registered combinations.
     */
    public static Collection<SpellCombination> getAll() {
        return java.util.Collections.unmodifiableCollection(COMBINATIONS.values());
    }

    /**
     * Get count of registered combinations.
     */
    public static int getCount() {
        return COMBINATIONS.size();
    }

    /**
     * Clear all registered combinations. Used for reloading.
     */
    public static void clear() {
        COMBINATIONS.clear();
        OUTPUT_TO_COMBINATIONS.clear();
        MAM.LOGGER.info("Cleared all spell combinations");
    }

    /**
     * Load a combination from JSON data. Called during spell data loading.
     *
     * @param id The combination's identifier
     * @param data JSON data for the combination
     * @return The loaded combination
     */
    public static SpellCombination load(Identifier id, com.mojang.serialization.Dynamic<?> data) {
        try {
            var decoded = SpellCombination.CODEC.parse(data).result();
            if (decoded.isPresent()) {
                return decoded.get();
            }
        } catch (Exception e) {
            MAM.LOGGER.error("Failed to parse spell combination {}: {}", id, e.getMessage());
        }

        throw new IllegalArgumentException("Failed to load spell combination: " + id);
    }
}
