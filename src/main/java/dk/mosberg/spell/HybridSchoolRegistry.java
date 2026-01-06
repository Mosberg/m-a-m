package dk.mosberg.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;

/**
 * Registry for all hybrid (mixed-element) schools in the mod.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Register new hybrid schools
 * <li>Lookup hybrid schools by ID or parent combo
 * <li>List all available hybrid schools
 * <li>Check if player can unlock specific hybrid school
 * </ul>
 *
 * <p>
 * Hybrid schools are data-driven: can be loaded from JSON or registered programmatically.
 */
public class HybridSchoolRegistry {

    /** Map of hybrid school ID -> HybridSchool */
    private static final Map<String, HybridSchool> HYBRID_SCHOOLS = new HashMap<>();

    /** Map of parent1+parent2 combo -> HybridSchool (for quick lookup) */
    private static final Map<String, HybridSchool> HYBRID_BY_PARENTS = new HashMap<>();

    /**
     * Registers a new hybrid school.
     *
     * <p>
     * Validates the hybrid school and rejects invalid combinations:
     * <ul>
     * <li>Must have two different parent schools
     * <li>Cannot have duplicate ID
     * <li>Must meet stat multiplier constraints
     * </ul>
     *
     * @param hybrid hybrid school to register
     * @return true if registered successfully, false if validation failed
     */
    public static boolean register(HybridSchool hybrid) {
        // Validate hybrid
        if (!hybrid.isValid()) {
            MAM.LOGGER.warn("Cannot register invalid hybrid school: {}", hybrid);
            return false;
        }

        // Check for duplicate ID
        if (HYBRID_SCHOOLS.containsKey(hybrid.id())) {
            MAM.LOGGER.warn("Hybrid school ID '{}' already registered", hybrid.id());
            return false;
        }

        // Store by ID
        HYBRID_SCHOOLS.put(hybrid.id(), hybrid);

        // Create parent combination key: alphabetically ordered for consistency
        String[] parents = {hybrid.parent1().name(), hybrid.parent2().name()};
        java.util.Arrays.sort(parents);
        String parentKey = parents[0] + "_" + parents[1];

        // Store by parent combo
        if (HYBRID_BY_PARENTS.containsKey(parentKey)) {
            MAM.LOGGER.warn("Hybrid school for combo {}+{} already exists: {}", hybrid.parent1(),
                    hybrid.parent2(), HYBRID_BY_PARENTS.get(parentKey).id());
        }
        HYBRID_BY_PARENTS.put(parentKey, hybrid);

        MAM.LOGGER.info("Registered hybrid school '{}' combining {} + {}", hybrid.id(),
                hybrid.parent1(), hybrid.parent2());
        return true;
    }

    /**
     * Gets a hybrid school by its ID.
     *
     * @param id hybrid school identifier
     * @return Optional containing hybrid school if found
     */
    public static Optional<HybridSchool> getHybridSchool(String id) {
        return Optional.ofNullable(HYBRID_SCHOOLS.get(id));
    }

    /**
     * Gets the hybrid school that combines two parent schools.
     *
     * <p>
     * Example: getHybridForParents(FIRE, WATER) → Vapor school
     *
     * @param school1 first parent school
     * @param school2 second parent school
     * @return Optional containing hybrid school, empty if not found
     */
    public static Optional<HybridSchool> getHybridForParents(SpellSchool school1,
            SpellSchool school2) {
        // Create parent key (order-independent)
        String[] parents = {school1.name(), school2.name()};
        java.util.Arrays.sort(parents);
        String parentKey = parents[0] + "_" + parents[1];

        return Optional.ofNullable(HYBRID_BY_PARENTS.get(parentKey));
    }

    /**
     * Lists all hybrid schools that have a specific parent school.
     *
     * <p>
     * Example: getHybridsWithParent(FIRE) → [Vapor, Magma, Lava]
     *
     * @param school parent school to search for
     * @return list of hybrid schools containing this parent
     */
    public static List<HybridSchool> getHybridsWithParent(SpellSchool school) {
        return HYBRID_SCHOOLS.values().stream()
                .filter(h -> h.parent1().equals(school) || h.parent2().equals(school))
                .sorted(HybridSchool.byDisplayName()).toList();
    }

    /**
     * Checks if a player can unlock a hybrid school based on parent proficiency.
     *
     * @param hybrid hybrid school to unlock
     * @param proficiency1 player proficiency in parent1 school (0-100)
     * @param proficiency2 player proficiency in parent2 school (0-100)
     * @return true if player meets both proficiency requirements
     */
    public static boolean canUnlock(HybridSchool hybrid, int proficiency1, int proficiency2) {
        return hybrid.meetsRequirements(proficiency1, proficiency2);
    }

    /**
     * Lists all available hybrid schools.
     *
     * @return list of all registered hybrid schools, sorted by display name
     */
    public static List<HybridSchool> getAllHybrids() {
        return HYBRID_SCHOOLS.values().stream().sorted(HybridSchool.byDisplayName()).toList();
    }

    /**
     * Gets count of registered hybrid schools.
     *
     * @return number of registered hybrids
     */
    public static int getHybridCount() {
        return HYBRID_SCHOOLS.size();
    }

    /**
     * Clears all registered hybrid schools (useful for testing/reloading).
     */
    public static void clear() {
        HYBRID_SCHOOLS.clear();
        HYBRID_BY_PARENTS.clear();
        MAM.LOGGER.info("Cleared hybrid school registry");
    }

    /**
     * Registers all default hybrid schools.
     *
     * <p>
     * Default hybrids (6 total):
     * <ul>
     * <li>Vapor: Fire + Water (explosive steam)
     * <li>Magma: Fire + Air (heat waves)
     * <li>Mud: Water + Earth (slow, heavy)
     * <li>Sand: Air + Earth (dust storms)
     * <li>Lava: Fire + Earth (molten, destructive)
     * <li>Ice: Water + Air (frozen, slowing)
     * </ul>
     *
     * <p>
     * Called during mod initialization.
     */
    public static void registerDefaults() {
        MAM.LOGGER.info("Registering default hybrid schools");

        // Vapor: Fire + Water - balanced, explosive
        register(HybridSchool.create("vapor", "Vapor School", SpellSchool.FIRE, SpellSchool.WATER,
                50));

        // Magma: Fire + Air - hot, fast, scattered
        register(HybridSchool.create("magma", "Magma School", SpellSchool.FIRE, SpellSchool.AIR,
                50));

        // Mud: Water + Earth - slow, heavy, defensive
        register(
                HybridSchool.create("mud", "Mud School", SpellSchool.WATER, SpellSchool.EARTH, 50));

        // Sand: Air + Earth - wind-based, abrading, defensive
        register(
                HybridSchool.create("sand", "Sand School", SpellSchool.AIR, SpellSchool.EARTH, 50));

        // Lava: Fire + Earth - molten, destructive, slow
        register(HybridSchool.create("lava", "Lava School", SpellSchool.FIRE, SpellSchool.EARTH,
                50));

        // Ice: Water + Air - frozen, slowing, fast
        register(HybridSchool.create("ice", "Ice School", SpellSchool.WATER, SpellSchool.AIR, 50));

        MAM.LOGGER.info("Registered {} default hybrid schools", getHybridCount());
    }
}
