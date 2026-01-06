package dk.mosberg.mana;

/**
 * Three mana pool types in the three-pool system.
 *
 * TODO: Add fourth pool type for special mechanics (overflow, burnout, or skill-based) TODO:
 * Implement pool affinity system (players born with pool preferences) TODO: Add pool conversion
 * mechanics (convert between pools with penalties) TODO: Implement pool linking (shared pools for
 * teams/guilds) TODO: Add regional pool type variations (biome-based mana types) TODO: Implement
 * pool thresholds for special abilities (unlock at % full) TODO: Add pool attribute modifiers
 * (affects spell damage, range, cooldown per pool) TODO: Implement pool combo bonuses (using
 * multiple pools in sequence)
 */
public enum ManaPoolType {
    PERSONAL(250, 0.5f, "Personal", 0xFF4A90E2, 1.0f, 1.0f, 1.0f), // Blue - balanced
    AURA(500, 0.25f, "Aura", 0xFF9B59B6, 0.9f, 1.1f, 0.95f), // Purple - extended range, slower
                                                             // cooldown
    RESERVE(1000, 0.1f, "Reserve", 0xFFE74C3C, 1.2f, 0.9f, 1.1f); // Red - high damage, fast
                                                                  // cooldown, lower range

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
     * (building power) - Reserve → Personal: 1.25x (full cycle bonus) - Same pool repeatedly: 0.95x
     * (penalty for monotone casting)
     */
    public float getComboBonus(ManaPoolType previousPool) {
        if (previousPool == this) {
            return 0.95f; // Penalty for using same pool consecutively
        }

        return switch (this) {
            case PERSONAL -> previousPool == RESERVE ? 1.25f : 1.1f;
            case AURA -> previousPool == PERSONAL ? 1.15f : 1.1f;
            case RESERVE -> previousPool == AURA ? 1.2f : 1.1f;
        };
    }

    /**
     * Get the next pool in the optimal rotation sequence for maximum combo bonuses. Personal → Aura
     * → Reserve → Personal (full cycle)
     */
    public ManaPoolType getOptimalNext() {
        return switch (this) {
            case PERSONAL -> AURA;
            case AURA -> RESERVE;
            case RESERVE -> PERSONAL;
        };
    }

    /**
     * Check if this pool type is compatible with the given spell tier threshold. Personal: All
     * tiers, Aura: Tier 2+, Reserve: Tier 3+
     */
    public boolean isAvailableForTier(int tier) {
        return switch (this) {
            case PERSONAL -> tier >= 1;
            case AURA -> tier >= 2;
            case RESERVE -> tier >= 3;
        };
    }

    /**
     * Get the efficiency multiplier based on current fullness percentage. Reserve pool: 1.3x when >
     * 75% full (power storage) Aura pool: 1.15x when 25-75% (balanced) Personal pool: 1.2x when <
     * 50% full (desperation bonus)
     */
    public float getEfficiencyAtCapacity(float percentFull) {
        return switch (this) {
            case PERSONAL -> percentFull < 0.5f ? 1.2f : 1.0f;
            case AURA -> (percentFull >= 0.25f && percentFull <= 0.75f) ? 1.15f : 1.0f;
            case RESERVE -> percentFull > 0.75f ? 1.3f : 1.0f;
        };
    }

    public String getTranslationKey() {
        return "mana.mam.pool." + name().toLowerCase();
    }
}
