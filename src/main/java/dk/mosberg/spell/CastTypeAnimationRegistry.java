package dk.mosberg.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;

/**
 * Registry for spell cast type animation configurations.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Register animation configs per cast type
 * <li>Lookup animations by cast type
 * <li>Get default animations
 * <li>List all available animations
 * </ul>
 *
 * <p>
 * Animations are per-cast-type, not per-spell - all spells of type PROJECTILE share the same base
 * animation, but individual spells can override in their own animation data.
 */
public class CastTypeAnimationRegistry {

    /** Map of cast type -> CastTypeAnimation */
    private static final Map<SpellCastType, CastTypeAnimation> ANIMATIONS = new HashMap<>();

    /**
     * Registers an animation configuration for a cast type.
     *
     * <p>
     * Validates the animation and rejects invalid entries:
     * <ul>
     * <li>Cast type must be non-null
     * <li>Animation key must be non-empty
     * <li>Duration must be >= 0
     * </ul>
     *
     * @param animation animation configuration to register
     * @return true if registered successfully, false if validation failed
     */
    public static boolean register(CastTypeAnimation animation) {
        // Validate animation
        if (!animation.isValid()) {
            MAM.LOGGER.warn("Cannot register invalid cast type animation: {}", animation);
            return false;
        }

        // Check for existing animation
        if (ANIMATIONS.containsKey(animation.castType())) {
            MAM.LOGGER.warn("Animation for cast type '{}' already registered, replacing",
                    animation.castType());
        }

        // Store animation
        ANIMATIONS.put(animation.castType(), animation);

        MAM.LOGGER.info("Registered animation for cast type '{}': {}", animation.castType(),
                animation.animationKey());
        return true;
    }

    /**
     * Gets the animation configuration for a cast type.
     *
     * @param castType spell cast type
     * @return Optional containing animation if registered
     */
    public static Optional<CastTypeAnimation> getAnimation(SpellCastType castType) {
        return Optional.ofNullable(ANIMATIONS.get(castType));
    }

    /**
     * Gets the animation key for a cast type (for resource lookup).
     *
     * @param castType spell cast type
     * @return animation key, or empty string if not found
     */
    public static String getAnimationKey(SpellCastType castType) {
        return getAnimation(castType).map(CastTypeAnimation::animationKey).orElse("");
    }

    /**
     * Gets the duration for a cast type animation.
     *
     * @param castType spell cast type
     * @return duration in ticks, or 12 if not found
     */
    public static int getDuration(SpellCastType castType) {
        return getAnimation(castType).map(CastTypeAnimation::durationTicks).orElse(12);
    }

    /**
     * Gets the speed multiplier for a cast type animation.
     *
     * @param castType spell cast type
     * @return speed multiplier, or 1.0f if not found
     */
    public static float getSpeedMultiplier(SpellCastType castType) {
        return getAnimation(castType).map(CastTypeAnimation::speedMultiplier).orElse(1.0f);
    }

    /**
     * Lists all registered animations.
     *
     * @return list of all animation configurations
     */
    public static List<CastTypeAnimation> getAllAnimations() {
        return ANIMATIONS.values().stream().toList();
    }

    /**
     * Gets count of registered animations.
     *
     * @return number of registered animations
     */
    public static int getAnimationCount() {
        return ANIMATIONS.size();
    }

    /**
     * Checks if an animation is registered for a cast type.
     *
     * @param castType spell cast type
     * @return true if animation configured
     */
    public static boolean hasAnimation(SpellCastType castType) {
        return ANIMATIONS.containsKey(castType);
    }

    /**
     * Clears all registered animations (useful for reloading).
     */
    public static void clear() {
        ANIMATIONS.clear();
        MAM.LOGGER.info("Cleared cast type animation registry");
    }

    /**
     * Registers all default cast type animations.
     *
     * <p>
     * Default animations for all 10 cast types:
     * <ul>
     * <li>PROJECTILE - 12 ticks, standard cast
     * <li>AOE - 16 ticks, area effect impact
     * <li>UTILITY - 8 ticks, instant utility effect
     * <li>RITUAL - 20 ticks, with charge animation (channeled)
     * <li>SYNERGY - 18 ticks, synchronized multi-school effect
     * <li>BEAM - 20 ticks, with charge animation (continuous)
     * <li>SELF_CAST - 6 ticks, quick personal buff
     * <li>TRAP - 15 ticks, placement animation
     * <li>SUMMON - 30 ticks, summoning ritual
     * <li>TRANSFORM - 24 ticks, dramatic transformation
     * </ul>
     *
     * <p>
     * Called during mod initialization.
     */
    public static void registerDefaults() {
        MAM.LOGGER.info("Registering default cast type animations");

        // PROJECTILE - standard fireball-style cast
        register(CastTypeAnimation.create(SpellCastType.PROJECTILE, "mam:projectile_cast"));

        // AOE - area effect impact
        register(new CastTypeAnimation(SpellCastType.AOE, 16, 1.0f, "mam:aoe_cast",
                Optional.empty(), Optional.empty(), Optional.empty()));

        // UTILITY - instant/self-centered effect
        register(CastTypeAnimation.create(SpellCastType.UTILITY, "mam:utility_cast"));

        // RITUAL - channeled ritual with charging
        register(new CastTypeAnimation(SpellCastType.RITUAL, 20, 1.0f, "mam:ritual_cast",
                Optional.empty(), Optional.empty(),
                Optional.of(new CastTypeAnimation.ChargeAnimation(true, 60, 0.8f))));

        // SYNERGY - synchronized multi-school effect
        register(new CastTypeAnimation(SpellCastType.SYNERGY, 18, 1.0f, "mam:synergy_cast",
                Optional.empty(), Optional.empty(), Optional.empty()));

        // BEAM - continuous laser, with charging
        register(new CastTypeAnimation(SpellCastType.BEAM, 20, 1.0f, "mam:beam_cast",
                Optional.empty(), Optional.empty(),
                Optional.of(new CastTypeAnimation.ChargeAnimation(true, 60, 0.8f))));

        // SELF_CAST - quick buff on player
        register(CastTypeAnimation.create(SpellCastType.SELF_CAST, "mam:self_cast"));

        // TRAP - place trap on ground
        register(CastTypeAnimation.create(SpellCastType.TRAP, "mam:trap_placement"));

        // SUMMON - summoning ritual (slower, more dramatic)
        register(new CastTypeAnimation(SpellCastType.SUMMON, 30, 0.9f, "mam:summon_ritual",
                Optional.empty(), Optional.empty(), Optional.empty()));

        // TRANSFORM - metamorphosis (quick but impressive)
        register(new CastTypeAnimation(SpellCastType.TRANSFORM, 24, 1.0f, "mam:transform_effect",
                Optional.empty(), Optional.empty(), Optional.empty()));

        MAM.LOGGER.info("Registered {} default cast type animations", getAnimationCount());
    }
}
