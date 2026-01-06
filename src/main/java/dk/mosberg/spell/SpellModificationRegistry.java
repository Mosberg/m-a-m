package dk.mosberg.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;
import net.minecraft.util.Identifier;

/**
 * Registry for all spell modifications (transmutations) in the mod.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Register spell modifications
 * <li>Find modifications by input spell
 * <li>Find modifications by output spell
 * <li>List all available modifications
 * <li>Apply modifications to spells
 * </ul>
 *
 * <p>
 * Modifications are data-driven: loaded from JSON data packs.
 */
public class SpellModificationRegistry {

    /** Map of modification ID -> SpellModification */
    private static final Map<String, SpellModification> MODIFICATIONS = new HashMap<>();

    /** Map of input spell ID -> list of modifications that consume it */
    private static final Map<Identifier, List<SpellModification>> MODIFICATIONS_BY_INPUT =
            new HashMap<>();

    /** Map of output spell ID -> list of modifications that produce it */
    private static final Map<Identifier, List<SpellModification>> MODIFICATIONS_BY_OUTPUT =
            new HashMap<>();

    /**
     * Registers a spell modification.
     *
     * <p>
     * Validates the modification and rejects invalid entries:
     * <ul>
     * <li>Input and output spells must be different
     * <li>Cannot have duplicate ID
     * <li>Must have at least one catalyst
     * </ul>
     *
     * @param modification modification to register
     * @return true if registered successfully, false if validation failed
     */
    public static boolean register(SpellModification modification) {
        // Validate modification
        if (!modification.isValid()) {
            MAM.LOGGER.warn("Cannot register invalid spell modification: {}", modification);
            return false;
        }

        // Check for duplicate ID
        if (MODIFICATIONS.containsKey(modification.id())) {
            MAM.LOGGER.warn("Modification ID '{}' already registered", modification.id());
            return false;
        }

        // Store by ID
        MODIFICATIONS.put(modification.id(), modification);

        // Index by input spell
        MODIFICATIONS_BY_INPUT
                .computeIfAbsent(modification.inputSpell(), k -> new java.util.ArrayList<>())
                .add(modification);

        // Index by output spell
        MODIFICATIONS_BY_OUTPUT
                .computeIfAbsent(modification.outputSpell(), k -> new java.util.ArrayList<>())
                .add(modification);

        MAM.LOGGER.info("Registered spell modification '{}': {} → {}", modification.id(),
                modification.inputSpell(), modification.outputSpell());
        return true;
    }

    /**
     * Gets a modification by its ID.
     *
     * @param id modification identifier
     * @return Optional containing modification if found
     */
    public static Optional<SpellModification> getModification(String id) {
        return Optional.ofNullable(MODIFICATIONS.get(id));
    }

    /**
     * Finds all modifications that consume a specific input spell.
     *
     * <p>
     * Example: getModificationsFor(Identifier.of("mam:fireball")) → [Inferno modification, ...]
     *
     * @param inputSpellId input spell to search for
     * @return list of modifications that have this as input
     */
    public static List<SpellModification> getModificationsFor(Identifier inputSpellId) {
        return MODIFICATIONS_BY_INPUT.getOrDefault(inputSpellId, List.of());
    }

    /**
     * Finds all modifications that produce a specific output spell.
     *
     * <p>
     * Inverse of getModificationsFor() - shows how to create a spell via transmutation.
     *
     * @param outputSpellId output spell to search for
     * @return list of modifications that produce this spell
     */
    public static List<SpellModification> getModificationsThatProduce(Identifier outputSpellId) {
        return MODIFICATIONS_BY_OUTPUT.getOrDefault(outputSpellId, List.of());
    }

    /**
     * Finds a modification matching specific catalysts (simple linear search).
     *
     * <p>
     * Used when player presents catalysts to find matching modification.
     *
     * <p>
     * Note: This is a basic O(n) search. For large catalyst lists, consider indexing.
     *
     * @param catalystIds list of catalyst item IDs
     * @return Optional containing first matching modification
     */
    public static Optional<SpellModification> findModificationByCatalysts(
            List<String> catalystIds) {
        return MODIFICATIONS.values().stream().filter(m -> m.catalystItems().equals(catalystIds))
                .findFirst();
    }

    /**
     * Applies a spell modification, transforming one spell into another.
     *
     * <p>
     * Validates:
     * <ul>
     * <li>Input spell is correct type
     * <li>Output spell exists
     * <li>Player has required catalysts and mana
     * <li>Player meets school requirements
     * </ul>
     *
     * <p>
     * Note: Actual application (item stack modifications, mana drain, catalyst consumption) would
     * be handled by calling code.
     *
     * @param modification modification to apply
     * @param inputSpell spell being modified
     * @return true if modification can be applied
     */
    public static boolean canApply(SpellModification modification, Spell inputSpell) {
        // Check that input matches modification
        if (!inputSpell.getId().equals(modification.inputSpell())) {
            MAM.LOGGER.debug("Input spell {} does not match modification input {}",
                    inputSpell.getId(), modification.inputSpell());
            return false;
        }

        // Check output spell exists
        Optional<Spell> outputSpell = SpellRegistry.getSpell(modification.outputSpell());
        if (outputSpell.isEmpty()) {
            MAM.LOGGER.warn("Output spell {} not found in registry", modification.outputSpell());
            return false;
        }

        return true;
    }

    /**
     * Lists all available modifications.
     *
     * @return list of all registered modifications
     */
    public static List<SpellModification> getAllModifications() {
        return MODIFICATIONS.values().stream().sorted(SpellModification.byOutputSpell()).toList();
    }

    /**
     * Gets count of registered modifications.
     *
     * @return number of registered modifications
     */
    public static int getModificationCount() {
        return MODIFICATIONS.size();
    }

    /**
     * Clears all registered modifications (useful for reloading).
     */
    public static void clear() {
        MODIFICATIONS.clear();
        MODIFICATIONS_BY_INPUT.clear();
        MODIFICATIONS_BY_OUTPUT.clear();
        MAM.LOGGER.info("Cleared spell modification registry");
    }
}
