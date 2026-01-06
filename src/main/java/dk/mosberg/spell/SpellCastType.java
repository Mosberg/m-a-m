package dk.mosberg.spell;

/**
 * The ten spell cast types supported by the magic system. All features implemented: BEAM,
 * SELF-CAST, TRAP, SUMMON, TRANSFORM types with cooldown multipliers, VFX customization, tier
 * restrictions, and channeling support.
 */
public enum SpellCastType {
    PROJECTILE("Projectile", "Fires a projectile towards the target", 1.0f), AOE("Area of Effect",
            "Affects all entities in an area", 1.5f), UTILITY("Utility",
                    "Provides buffs, teleportation, or other utility", 1.2f), RITUAL("Ritual",
                            "Requires channeling time and consumes mana over time",
                            2.0f), SYNERGY("Synergy", "Combines effects from multiple schools",
                                    1.8f), BEAM("Beam", "Continuous laser attack until released",
                                            0.5f), SELF_CAST("Self-Cast",
                                                    "Buffs or debuffs applied to the caster",
                                                    0.8f), TRAP("Trap",
                                                            "Placed on ground and triggered by entities",
                                                            1.3f), SUMMON("Summon",
                                                                    "Spawns entities to fight for the player",
                                                                    2.5f), TRANSFORM("Transform",
                                                                            "Temporarily changes player form or abilities",
                                                                            3.0f);

    private final String displayName;
    private final String description;
    private final float cooldownMultiplier;

    SpellCastType(String displayName, String description, float cooldownMultiplier) {
        this.displayName = displayName;
        this.description = description;
        this.cooldownMultiplier = cooldownMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the cooldown multiplier for this cast type. Beam: 0.5x (rapid), Self-Cast: 0.8x (fast),
     * Projectile: 1.0x (normal), Utility: 1.2x, Trap: 1.3x, AOE: 1.5x, Synergy: 1.8x, Ritual: 2.0x,
     * Summon: 2.5x (slow), Transform: 3.0x (very slow)
     */
    public float getCooldownMultiplier() {
        return cooldownMultiplier;
    }

    /**
     * Get the minimum tier required to use this cast type. Basic types (Projectile, AOE, Self-Cast)
     * available from tier 1. Advanced types (Utility, Trap, Beam) require tier 2. Complex types
     * (Ritual, Synergy) require tier 3. Master types (Summon, Transform) require tier 4.
     */
    public int getMinimumTier() {
        return switch (this) {
            case PROJECTILE, AOE, SELF_CAST -> 1;
            case UTILITY, TRAP, BEAM -> 2;
            case RITUAL, SYNERGY -> 3;
            case SUMMON, TRANSFORM -> 4;
        };
    }

    /**
     * Check if this cast type is available at the given tier.
     */
    public boolean isAvailableAtTier(int tier) {
        return tier >= getMinimumTier();
    }

    /**
     * Get the VFX particle type identifier for this cast type. Used for rendering spell casting
     * animations.
     */
    public String getVfxParticleType() {
        return switch (this) {
            case PROJECTILE -> "spell_projectile";
            case AOE -> "explosion_particles";
            case UTILITY -> "enchantment_glyphs";
            case RITUAL -> "ritual_circle";
            case SYNERGY -> "rainbow_sparkles";
            case BEAM -> "laser_beam";
            case SELF_CAST -> "aura_pulse";
            case TRAP -> "rune_marker";
            case SUMMON -> "summoning_portal";
            case TRANSFORM -> "transformation_swirl";
        };
    }

    /**
     * Get the animation duration in ticks for this cast type. Determines how long the casting
     * animation plays.
     */
    public int getAnimationDuration() {
        return switch (this) {
            case PROJECTILE, BEAM -> 10; // 0.5 seconds
            case AOE, SELF_CAST -> 15; // 0.75 seconds
            case UTILITY, TRAP -> 20; // 1 second
            case RITUAL, SYNERGY -> 40; // 2 seconds
            case SUMMON -> 60; // 3 seconds
            case TRANSFORM -> 80; // 4 seconds
        };
    }

    /**
     * Check if this cast type requires continuous channeling.
     */
    public boolean requiresChanneling() {
        return this == BEAM || this == RITUAL;
    }

    public String getTranslationKey() {
        return "spell.mam.cast_type." + name().toLowerCase();
    }
}
