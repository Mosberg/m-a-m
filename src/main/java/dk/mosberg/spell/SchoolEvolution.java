package dk.mosberg.spell;

import java.util.EnumMap;
import java.util.Map;

/**
 * School Evolution System: unlocks new mechanics and bonuses at higher spell tiers (2, 3, 4).
 *
 * As players progress through tiers, schools gain new abilities: - Tier 1 (Novice): Base mechanics
 * only - Tier 2 (Apprentice): +1 evolution feature per school - Tier 3 (Adept): +2 evolution
 * features - Tier 4 (Master): Full evolution with all mechanics
 *
 * Evolution types per school: - Fire: Ignite (spreads to nearby enemies), Flame Wave (AOE
 * enhancement) - Water: Refresh (heals caster), Cascade (chains to multiple targets) - Air:
 * Momentum (stacks with repeated casts), Gust (pushes enemies back) - Earth: Fortify (temporary
 * shield), Roots (immobilizes enemies)
 */
public class SchoolEvolution {

    /**
     * Evolution unlocks per spell tier.
     */
    public enum EvolutionType {
        FIRE_IGNITE("Ignite", "Spells spread fire to nearby enemies"), FIRE_FLAME_WAVE("Flame Wave",
                "Spells create AOE wave of flames"),

        WATER_REFRESH("Refresh", "Spells heal the caster"), WATER_CASCADE("Cascade",
                "Spells chain to multiple targets"),

        AIR_MOMENTUM("Momentum", "Stacks with repeated casts for more damage"), AIR_GUST("Gust",
                "Spells push enemies away"),

        EARTH_FORTIFY("Fortify", "Creates temporary protective shield"), EARTH_ROOTS("Roots",
                "Spells immobilize enemies");

        private final String displayName;
        private final String description;

        EvolutionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Map of school to evolution unlocks by tier. Tier 1 = no evolutions (base), Tier 2+ = unlocked
     * evolutions
     */
    @SuppressWarnings("null")
    private static final Map<SpellSchool, Map<Integer, EvolutionType>> EVOLUTION_UNLOCKS =
            new EnumMap<>(SpellSchool.class);

    static {
        // Initialize evolutions for each school
        initializeEvolutions();
    }

    private static void initializeEvolutions() {
        // Fire School Evolutions
        Map<Integer, EvolutionType> fireEvolutions = new java.util.HashMap<>();
        fireEvolutions.put(2, EvolutionType.FIRE_IGNITE); // Tier 2 unlock
        fireEvolutions.put(3, EvolutionType.FIRE_FLAME_WAVE); // Tier 3 unlock
        EVOLUTION_UNLOCKS.put(SpellSchool.FIRE, fireEvolutions);

        // Water School Evolutions
        Map<Integer, EvolutionType> waterEvolutions = new java.util.HashMap<>();
        waterEvolutions.put(2, EvolutionType.WATER_REFRESH);
        waterEvolutions.put(3, EvolutionType.WATER_CASCADE);
        EVOLUTION_UNLOCKS.put(SpellSchool.WATER, waterEvolutions);

        // Air School Evolutions
        Map<Integer, EvolutionType> airEvolutions = new java.util.HashMap<>();
        airEvolutions.put(2, EvolutionType.AIR_MOMENTUM);
        airEvolutions.put(3, EvolutionType.AIR_GUST);
        EVOLUTION_UNLOCKS.put(SpellSchool.AIR, airEvolutions);

        // Earth School Evolutions
        Map<Integer, EvolutionType> earthEvolutions = new java.util.HashMap<>();
        earthEvolutions.put(2, EvolutionType.EARTH_FORTIFY);
        earthEvolutions.put(3, EvolutionType.EARTH_ROOTS);
        EVOLUTION_UNLOCKS.put(SpellSchool.EARTH, earthEvolutions);
    }

    /**
     * Get the evolution unlocked for a school at a specific tier.
     *
     * @param school The spell school
     * @param tier The spell tier (1-4)
     * @return Optional containing the evolution if available at this tier
     */
    public static java.util.Optional<EvolutionType> getEvolutionAtTier(SpellSchool school,
            int tier) {
        if (tier < 2 || tier > 4) {
            return java.util.Optional.empty();
        }

        Map<Integer, EvolutionType> schoolEvolutions =
                EVOLUTION_UNLOCKS.getOrDefault(school, new java.util.HashMap<>());
        return java.util.Optional.ofNullable(schoolEvolutions.get(tier));
    }

    /**
     * Check if a school has an evolution unlocked at the given tier.
     */
    public static boolean hasEvolution(SpellSchool school, int tier) {
        return getEvolutionAtTier(school, tier).isPresent();
    }

    /**
     * Get all evolutions unlocked up to and including a given tier.
     *
     * @param school The spell school
     * @param maxTier Maximum tier to check (1-4)
     * @return List of all evolutions available at/below this tier
     */
    public static java.util.List<EvolutionType> getEvolutionsUpToTier(SpellSchool school,
            int maxTier) {
        var evolutions = new java.util.ArrayList<EvolutionType>();

        for (int tier = 2; tier <= Math.min(maxTier, 4); tier++) {
            getEvolutionAtTier(school, tier).ifPresent(evolutions::add);
        }

        return evolutions;
    }

    /**
     * Get the tier at which an evolution is first unlocked.
     *
     * @param evolution The evolution to find
     * @return The tier (2, 3, or 4) where this evolution unlocks, or 0 if not found
     */
    public static int getTierForEvolution(EvolutionType evolution) {
        for (Map<Integer, EvolutionType> schoolEvolutions : EVOLUTION_UNLOCKS.values()) {
            for (Map.Entry<Integer, EvolutionType> entry : schoolEvolutions.entrySet()) {
                if (entry.getValue() == evolution) {
                    return entry.getKey();
                }
            }
        }
        return 0;
    }

    /**
     * Apply evolution bonuses to a spell's damage/effects based on tier and school.
     *
     * @param baseValue Base damage or effect value
     * @param school The spell's school
     * @param tier The spell's tier
     * @return Modified value with evolution bonuses applied
     */
    public static float applyEvolutionBonus(float baseValue, SpellSchool school, int tier) {
        if (tier < 2) {
            return baseValue;
        }

        // Each evolution adds a cumulative bonus
        int evolutionCount = getEvolutionsUpToTier(school, tier).size();
        float bonusPerEvolution = 0.1f; // 10% bonus per evolution

        return baseValue * (1.0f + bonusPerEvolution * evolutionCount);
    }
}
