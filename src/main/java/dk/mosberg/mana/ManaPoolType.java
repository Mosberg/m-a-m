package dk.mosberg.mana;

/**
 * Four mana pool types in the advanced mana system.
 *
 * Features implemented: Pool affinity system, pool conversion mechanics, regional variations.
 * 
 * TODO: Implement pool linking (shared pools for teams/guilds)
 */
public enum ManaPoolType {
    PERSONAL(250, 0.5f, "Personal", 0xFF4A90E2, 1.0f, 1.0f, 1.0f), // Blue - balanced
    AURA(500, 0.25f, "Aura", 0xFF9B59B6, 0.9f, 1.1f, 0.95f), // Purple - extended range, slower
                                                             // cooldown
    RESERVE(1000, 0.1f, "Reserve", 0xFFE74C3C, 1.2f, 0.9f, 1.1f), // Red - high damage, fast
                                                                  // cooldown, lower range
    SKILL(150, 0.8f, "Skill", 0xFF2ECC71, 1.05f, 1.05f, 0.85f); // Green - precision casting, fast
                                                                // cooldown, balanced stats

    private final int defaultCapacity;
    private final float defaultRegenRate;
    private final String displayName;
    private final int color;
    private final float damageModifier;
    private final float rangeModifier;
    private final float cooldownModifier;

    ManaPoolType(int defaultCapacity, float defaultRegenRate, String displayName, int color,
            float damageModifier, float rangeModifier, float cooldownModifier) {
        this.defaultCapacity = defaultCapacity;
        this.defaultRegenRate = defaultRegenRate;
        this.displayName = displayName;
        this.color = color;
        this.damageModifier = damageModifier;
        this.rangeModifier = rangeModifier;
        this.cooldownModifier = cooldownModifier;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public float getDefaultRegenRate() {
        return defaultRegenRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    /**
     * Get the damage modifier applied when casting from this pool. Personal: 1.0x (normal), Aura:
     * 0.9x (reduced), Reserve: 1.2x (boosted)
     */
    public float getDamageModifier() {
        return damageModifier;
    }

    /**
     * Get the range modifier applied when casting from this pool. Personal: 1.0x (normal), Aura:
     * 1.1x (extended), Reserve: 0.9x (reduced)
     */
    public float getRangeModifier() {
        return rangeModifier;
    }

    /**
     * Get the cooldown modifier applied when casting from this pool. Personal: 1.0x (normal), Aura:
     * 0.95x (faster), Reserve: 1.1x (slower)
     */
    public float getCooldownModifier() {
        return cooldownModifier;
    }

    /**
     * Get the combo bonus multiplier when this pool is used after the previous pool. Rewards
     * strategic pool rotation: - Personal → Aura: 1.15x (smooth transition) - Aura → Reserve: 1.2x
     * (building power) - Reserve → Skill: 1.25x - Skill → Personal: 1.3x (full cycle bonus) - Same
     * pool repeatedly: 0.95x (penalty for monotone casting)
     */
    public float getComboBonus(ManaPoolType previousPool) {
        if (previousPool == this) {
            return 0.95f; // Penalty for using same pool consecutively
        }

        return switch (this) {
            case PERSONAL -> previousPool == SKILL ? 1.3f : 1.1f;
            case AURA -> previousPool == PERSONAL ? 1.15f : 1.1f;
            case RESERVE -> previousPool == AURA ? 1.2f : 1.1f;
            case SKILL -> previousPool == RESERVE ? 1.25f : 1.1f;
        };
    }

    /**
     * Get the next pool in the optimal rotation sequence for maximum combo bonuses. Personal → Aura
     * → Reserve → Skill → Personal (full cycle)
     */
    public ManaPoolType getOptimalNext() {
        return switch (this) {
            case PERSONAL -> AURA;
            case AURA -> RESERVE;
            case RESERVE -> SKILL;
            case SKILL -> PERSONAL;
        };
    }

    /**
     * Check if this pool type is compatible with the given spell tier threshold. Personal: All
     * tiers, Aura: Tier 2+, Reserve: Tier 3+, Skill: Tier 4 (master)
     */
    public boolean isAvailableForTier(int tier) {
        return switch (this) {
            case PERSONAL -> tier >= 1;
            case AURA -> tier >= 2;
            case RESERVE -> tier >= 3;
            case SKILL -> tier >= 4;
        };
    }

    /**
     * Get the efficiency multiplier based on current fullness percentage. Reserve pool: 1.3x when >
     * 75% full (power storage) Aura pool: 1.15x when 25-75% (balanced) Personal pool: 1.2x when <
     * 50% full (desperation bonus) Skill pool: 1.25x when exactly 40-60% (precision sweet spot)
     */
    public float getEfficiencyAtCapacity(float percentFull) {
        return switch (this) {
            case PERSONAL -> percentFull < 0.5f ? 1.2f : 1.0f;
            case AURA -> (percentFull >= 0.25f && percentFull <= 0.75f) ? 1.15f : 1.0f;
            case RESERVE -> percentFull > 0.75f ? 1.3f : 1.0f;
            case SKILL -> (percentFull >= 0.4f && percentFull <= 0.6f) ? 1.25f : 0.95f;
        };
    }

    public String getTranslationKey() {
        return "mana.mam.pool." + name().toLowerCase();
    }

    /**
     * Get the conversion efficiency when transferring mana from this pool to the target pool. Some
     * conversions are more efficient than others based on pool compatibility.
     *
     * @param targetPool Pool to convert mana into
     * @return Conversion efficiency (0.5 - 1.0), lower means more loss
     */
    public float getConversionEfficiency(ManaPoolType targetPool) {
        if (this == targetPool)
            return 1.0f; // No loss for same pool

        return switch (this) {
            case PERSONAL -> switch (targetPool) {
                case AURA -> 0.85f; // Personal adapts well to Aura
                case RESERVE -> 0.7f; // Less efficient to Reserve
                case SKILL -> 0.75f; // Moderate to Skill
                default -> 0.5f;
            };
            case AURA -> switch (targetPool) {
                case PERSONAL -> 0.8f; // Good conversion to Personal
                case RESERVE -> 0.9f; // Excellent to Reserve
                case SKILL -> 0.65f; // Poor to Skill (different nature)
                default -> 0.5f;
            };
            case RESERVE -> switch (targetPool) {
                case PERSONAL -> 0.6f; // Difficult to convert to Personal
                case AURA -> 0.85f; // Good to Aura
                case SKILL -> 0.7f; // Moderate to Skill
                default -> 0.5f;
            };
            case SKILL -> switch (targetPool) {
                case PERSONAL -> 0.75f; // Moderate to Personal
                case AURA -> 0.6f; // Difficult to Aura
                case RESERVE -> 0.8f; // Good to Reserve
                default -> 0.5f;
            };
        };
    }

    /**
     * Get the mana cost multiplier for converting between pools. Conversion requires spending mana
     * as a cost.
     *
     * @param targetPool Pool to convert into
     * @return Mana cost as percentage of amount being converted (0.1 = 10% cost)
     */
    public float getConversionCost(ManaPoolType targetPool) {
        if (this == targetPool)
            return 0.0f;

        float efficiency = getConversionEfficiency(targetPool);
        // Higher efficiency = lower cost
        // 0.85 efficiency = 15% cost, 0.6 efficiency = 40% cost
        return Math.max(0.05f, (1.0f - efficiency) * 0.5f);
    }

    /**
     * Check if conversion between these pools is allowed. All conversions are allowed but some are
     * inefficient.
     */
    public boolean canConvertTo(ManaPoolType targetPool) {
        return this != targetPool; // Can convert to any different pool
    }

    /**
     * Get the cooldown (in ticks) required before converting from this pool again. Prevents rapid
     * pool conversion exploitation.
     */
    public int getConversionCooldown() {
        return switch (this) {
            case PERSONAL -> 100; // 5 seconds
            case AURA -> 200; // 10 seconds
            case RESERVE -> 300; // 15 seconds
            case SKILL -> 150; // 7.5 seconds
        };
    }

    /**
     * Regional variations: pools behave differently in certain dimensions/regions. Provides small,
     * thematic modifiers to regen and damage output.
     */
    public float getRegionalRegenModifier(String dimension, boolean isUnderwater) {
        if (dimension == null)
            return 1.0f;
        String dim = dimension.toLowerCase();
        switch (this) {
            case PERSONAL:
                if (isUnderwater)
                    return 0.9f;
                return 1.0f;
            case AURA:
                if (dim.contains("end"))
                    return 1.2f; // The End amplifies aura presence
                if (isUnderwater)
                    return 0.95f;
                return 1.0f;
            case RESERVE:
                if (dim.contains("nether"))
                    return 1.15f; // Nether heat fuels reserves
                if (isUnderwater)
                    return 0.85f;
                return 1.0f;
            case SKILL:
                if (dim.contains("overworld"))
                    return 1.1f; // Familiar lands assist precision
                return 1.0f;
        }
    }

    public float getRegionalDamageModifier(String dimension, boolean isUnderwater) {
        if (dimension == null)
            return 1.0f;
        String dim = dimension.toLowerCase();
        switch (this) {
            case PERSONAL:
                return 1.0f;
            case AURA:
                if (dim.contains("end"))
                    return 1.1f;
                return 1.0f;
            case RESERVE:
                if (dim.contains("nether"))
                    return 1.2f;
                if (isUnderwater)
                    return 0.9f;
                return 1.0f;
            case SKILL:
                if (dim.contains("overworld"))
                    return 1.05f;
                return 1.0f;
        }
    }

    /**
     * Subtle color shift for UI based on region (packed ARGB). Keeps theme recognizable.
     */
    public int getRegionalColor(String dimension) {
        if (dimension == null)
            return color;
        String dim = dimension.toLowerCase();
        int shift = 0x00000000;
        if (dim.contains("nether"))
            shift = 0x00100000; // warmer
        else if (dim.contains("end"))
            shift = 0x00001010; // cooler/paler
        else if (dim.contains("overworld"))
            shift = 0x00000500; // subtle green
        int r = Math.min(255, ((color >> 16) & 0xFF) + ((shift >> 16) & 0xFF));
        int g = Math.min(255, ((color >> 8) & 0xFF) + ((shift >> 8) & 0xFF));
        int b = Math.min(255, (color & 0xFF) + (shift & 0xFF));
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /**
     * Get capacity bonus for having affinity with this pool type. Players with pool affinity gain
     * bonus max capacity.
     *
     * @param affinityStrength Player's affinity strength (0.0 = none, 1.0 = max)
     * @return Additional capacity added to base (0-50% bonus)
     */
    public float getAffinityCapacityBonus(float affinityStrength) {
        float strength = Math.max(0.0f, Math.min(1.0f, affinityStrength));
        // Weak affinity (0.3): +15% capacity, Max affinity (1.0): +50% capacity
        return defaultCapacity * (0.5f * strength);
    }

    /**
     * Get regeneration rate bonus for having affinity with this pool type.
     *
     * @param affinityStrength Player's affinity strength (0.0 = none, 1.0 = max)
     * @return Multiplier applied to base regen rate (1.0-1.5x)
     */
    public float getAffinityRegenBonus(float affinityStrength) {
        float strength = Math.max(0.0f, Math.min(1.0f, affinityStrength));
        // 1.0 + (0.5 * strength) = 1.0-1.5x regen multiplier
        return 1.0f + (0.5f * strength);
    }

    /**
     * Get penalty for using non-affinity pool. Players using pools they don't have affinity for
     * receive small penalty.
     *
     * @param playerAffinityPool The pool type player has affinity for (null if none)
     * @return Efficiency multiplier (0.9-1.0x)
     */
    public float getNonAffinityPenalty(ManaPoolType playerAffinityPool) {
        if (playerAffinityPool == null) {
            return 1.0f; // No penalty if no affinity
        }

        if (this == playerAffinityPool) {
            return 1.0f; // No penalty for matching pool
        }

        // Small penalty for non-affinity pools
        return 0.9f; // 10% efficiency reduction
    }

    /**
     * Determine starting affinity pool for new players based on playstyle preference. This is a
     * recommendation, not a restriction.
     */
    public static ManaPoolType getRecommendedAffinity(String playstylePreference) {
        return switch (playstylePreference.toLowerCase()) {
            case "aggressive", "damage" -> RESERVE; // High damage playstyle
            case "support", "team" -> AURA; // Extended range, team support
            case "balanced", "adaptive" -> PERSONAL; // Balanced approach
            case "precision", "technical" -> SKILL; // Precision casting
            default -> PERSONAL; // Default to balanced
        };
    }

    /**
     * Check if player can have affinity for multiple pools simultaneously. Only one primary
     * affinity allowed, but weak secondary affinity possible at high levels.
     */
    public static boolean canHaveSecondaryAffinity(int playerLevel) {
        return playerLevel >= 50; // Unlock secondary affinity at level 50
    }
}
