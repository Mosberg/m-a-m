package dk.mosberg.spell;

/**
 * The four spell schools.
 *
 * Features implemented: School affinity system.
 *
 * TODO: Implement school-based enchantments (spellbooks/staffs specialize in school) TODO:
 * Implement school evolution (higher tiers unlock new mechanics) TODO: Add school combinations
 * (hybrid schools for mixed-element spells) TODO: Implement school prestige/mastery system (unlock
 * bonuses)
 */
public enum SpellSchool {
    AIR("Air", 0x87CEEB, 1.0f, 0.9f, 0.95f), EARTH("Earth", 0x8B4513, 1.2f, 1.1f, 1.2f), FIRE(
            "Fire", 0xFF4500, 1.3f, 1.0f, 1.0f), WATER("Water", 0x1E90FF, 0.9f, 0.8f, 0.9f);

    private final String displayName;
    private final int color;
    private final float damageMultiplier;
    private final float manaCostMultiplier;
    private final float cooldownMultiplier;

    SpellSchool(String displayName, int color, float damageMultiplier, float manaCostMultiplier,
            float cooldownMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.damageMultiplier = damageMultiplier;
        this.manaCostMultiplier = manaCostMultiplier;
        this.cooldownMultiplier = cooldownMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    /**
     * Get the damage multiplier for this spell school. Fire: 1.3x (high damage), Earth: 1.2x
     * (moderate), Air: 1.0x (normal), Water: 0.9x (healing focus)
     */
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Get the mana cost multiplier for this spell school. Water: 0.8x (efficient), Air: 0.9x (low
     * cost), Fire: 1.0x (normal), Earth: 1.1x (expensive)
     */
    public float getManaCostMultiplier() {
        return manaCostMultiplier;
    }

    /**
     * Get the cooldown multiplier for this spell school. Air: 0.95x (quick), Water: 0.9x (fast),
     * Fire: 1.0x (normal), Earth: 1.2x (slow)
     */
    public float getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    /**
     * Get the effectiveness multiplier when this school attacks the target school. Returns > 1.0
     * for advantage, < 1.0 for disadvantage, 1.0 for neutral.
     *
     * Rock-paper-scissors system: - Fire beats Air (fire spreads with wind) - 1.25x - Air beats
     * Earth (erosion) - 1.25x - Earth beats Water (absorbs) - 1.25x - Water beats Fire
     * (extinguishes) - 1.25x
     */
    public float getEffectivenessAgainst(SpellSchool target) {
        if (this == FIRE && target == AIR)
            return 1.25f;
        if (this == AIR && target == EARTH)
            return 1.25f;
        if (this == EARTH && target == WATER)
            return 1.25f;
        if (this == WATER && target == FIRE)
            return 1.25f;

        // Reverse relationships (weak against)
        if (this == AIR && target == FIRE)
            return 0.75f;
        if (this == EARTH && target == AIR)
            return 0.75f;
        if (this == WATER && target == EARTH)
            return 0.75f;
        if (this == FIRE && target == WATER)
            return 0.75f;

        return 1.0f; // Neutral (same school or no relationship)
    }

    /**
     * Get the school this school is strong against (deals 1.25x damage).
     */
    public SpellSchool getStrongAgainst() {
        return switch (this) {
            case FIRE -> AIR;
            case AIR -> EARTH;
            case EARTH -> WATER;
            case WATER -> FIRE;
        };
    }

    /**
     * Get the school this school is weak against (deals 0.75x damage).
     */
    public SpellSchool getWeakAgainst() {
        return switch (this) {
            case FIRE -> WATER;
            case AIR -> FIRE;
            case EARTH -> AIR;
            case WATER -> EARTH;
        };
    }

    /**
     * Check if this school has environmental advantages in the given conditions.
     *
     * @param isRaining Whether it's currently raining
     * @param isUnderwater Whether the caster is underwater
     * @param isInNether Whether the caster is in the Nether
     * @param isOnGround Whether the caster is on solid ground
     * @return Effectiveness multiplier based on environment (0.8 - 1.3)
     */
    public float getEnvironmentalModifier(boolean isRaining, boolean isUnderwater,
            boolean isInNether, boolean isOnGround) {
        return switch (this) {
            case FIRE -> {
                if (isUnderwater || isRaining)
                    yield 0.8f; // Fire weakened by water
                if (isInNether)
                    yield 1.3f; // Fire boosted in Nether
                yield 1.0f;
            }
            case WATER -> {
                if (isUnderwater)
                    yield 1.3f; // Water boosted underwater
                if (isRaining)
                    yield 1.15f; // Water boosted in rain
                if (isInNether)
                    yield 0.8f; // Water weakened in Nether
                yield 1.0f;
            }
            case AIR -> {
                if (!isOnGround)
                    yield 1.2f; // Air boosted when airborne
                if (isUnderwater)
                    yield 0.85f; // Air weakened underwater
                yield 1.0f;
            }
            case EARTH -> {
                if (isOnGround)
                    yield 1.15f; // Earth boosted on solid ground
                if (!isOnGround)
                    yield 0.9f; // Earth weakened when airborne
                yield 1.0f;
            }
        };
    }

    /**
     * Get description of environmental advantages for this school.
     */
    public String getEnvironmentalDescription() {
        return switch (this) {
            case FIRE -> "Powerful in Nether, weak in water/rain";
            case WATER -> "Powerful underwater and in rain, weak in Nether";
            case AIR -> "Powerful when airborne, weak underwater";
            case EARTH -> "Powerful on solid ground, weak when airborne";
        };
    }

    /**
     * Calculate player affinity bonus for this school. Players can have natural affinity for
     * certain schools (0.8x - 1.3x multiplier).
     *
     * @param playerAffinitySchool The school the player has affinity for (null if none)
     * @param playerAffinityStrength Affinity strength (0.0 = none, 1.0 = max)
     * @return Affinity multiplier applied to spell effectiveness
     */
    public float getAffinityMultiplier(SpellSchool playerAffinitySchool,
            float playerAffinityStrength) {
        if (playerAffinitySchool == null || playerAffinityStrength <= 0.0f) {
            return 1.0f; // No affinity
        }

        // Clamp strength to 0.0-1.0
        float strength = Math.max(0.0f, Math.min(1.0f, playerAffinityStrength));

        if (this == playerAffinitySchool) {
            // Matching affinity: 1.0 + (0.3 * strength) = 1.0-1.3x
            return 1.0f + (0.3f * strength);
        }

        // Opposite school (based on weakness cycle)
        SpellSchool oppositeSchool = getWeakAgainst();
        if (playerAffinitySchool == oppositeSchool) {
            // Opposing affinity: 1.0 - (0.2 * strength) = 0.8-1.0x
            return 1.0f - (0.2f * strength);
        }

        // Adjacent schools (neutral): minor penalty
        // 1.0 - (0.1 * strength) = 0.9-1.0x
        return 1.0f - (0.1f * strength);
    }

    /**
     * Get recommended affinity strength for a player starting with this school. Used for character
     * creation or tutorial guidance.
     */
    public static float getRecommendedAffinityStrength(int playerLevel) {
        if (playerLevel < 10)
            return 0.3f; // Weak affinity
        if (playerLevel < 25)
            return 0.5f; // Moderate affinity
        if (playerLevel < 50)
            return 0.7f; // Strong affinity
        return 1.0f; // Master affinity
    }

    /**
     * Check if player can change their school affinity. Affinity becomes locked after significant
     * progression.
     */
    public static boolean canChangeAffinity(int playerLevel, float currentStrength) {
        // Can only change before level 20 or if affinity is weak
        return playerLevel < 20 || currentStrength < 0.5f;
    }

    public String getTranslationKey() {
        return "school.mam." + name().toLowerCase();
    }
}
