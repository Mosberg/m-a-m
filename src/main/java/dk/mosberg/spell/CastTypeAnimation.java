package dk.mosberg.spell;

import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.mosberg.MAM;

/**
 * Animation configuration for spell cast types, providing per-type animation details.
 *
 * <p>
 * Allows customization of animations based on cast type:
 * <ul>
 * <li>Duration in ticks (how long animation plays)
 * <li>Speed multiplier (playback speed: 0.5x - 2.0x)
 * <li>Animation key (identifier for animation resource)
 * <li>Particle effects (particle type, color, count)
 * <li>Sound effect (sound event, pitch, volume)
 * <li>Charge animation (for channeled spells)
 * </ul>
 *
 * <p>
 * Example animation config for PROJECTILE cast type:
 *
 * <pre>
 * {
 *   "castType": "projectile",
 *   "durationTicks": 12,
 *   "speedMultiplier": 1.0,
 *   "animationKey": "mam:projectile_cast",
 *   "particles": {
 *     "type": "small_flame",
 *     "color": "FF6600",
 *     "count": 8
 *   },
 *   "sound": {
 *     "event": "mam:cast_projectile",
 *     "pitch": 1.0,
 *     "volume": 0.8
 *   },
 *   "chargeAnimation": {
 *     "enabled": false
 *   }
 * }
 * </pre>
 */
public record CastTypeAnimation(SpellCastType castType, int durationTicks, float speedMultiplier,
        String animationKey, Optional<ParticleEffect> particles, Optional<SoundEffect> sound,
        Optional<ChargeAnimation> chargeAnimation) {

    /**
     * Particle effect configuration.
     *
     * @param type particle type identifier (e.g., "small_flame")
     * @param color RGB hex color (e.g., "FF6600" for orange)
     * @param count number of particles to spawn
     */
    public record ParticleEffect(String type, String color, int count) {

        @SuppressWarnings("null")
        public static final Codec<ParticleEffect> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.STRING.fieldOf("type").forGetter(ParticleEffect::type),
                                Codec.STRING
                                        .optionalFieldOf("color", "FFFFFF")
                                        .forGetter(ParticleEffect::color),
                                Codec.INT.optionalFieldOf("count", 5)
                                        .forGetter(ParticleEffect::count))
                        .apply(instance, ParticleEffect::new));
    }

    /**
     * Sound effect configuration.
     *
     * @param event sound event ID (e.g., "mam:cast_projectile")
     * @param pitch pitch multiplier (0.5 - 2.0, where 1.0 is normal)
     * @param volume volume level (0.0 - 1.0)
     */
    public record SoundEffect(String event, float pitch, float volume) {

        @SuppressWarnings("null")
        public static final Codec<SoundEffect> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.STRING.fieldOf("event").forGetter(SoundEffect::event),
                                Codec.FLOAT
                                        .optionalFieldOf("pitch", 1.0f)
                                        .forGetter(SoundEffect::pitch),
                                Codec.FLOAT.optionalFieldOf("volume", 0.8f)
                                        .forGetter(SoundEffect::volume))
                        .apply(instance, SoundEffect::new));
    }

    /**
     * Charge animation configuration for channeled spells.
     *
     * <p>
     * Used for BEAM, CHANNEL cast types that build up before releasing.
     *
     * @param enabled whether charge animation is used
     * @param chargeDurationTicks max duration for charge phase
     * @param chargeSpeedMultiplier how fast to animate during charge
     */
    public record ChargeAnimation(boolean enabled, int chargeDurationTicks,
            float chargeSpeedMultiplier) {

        @SuppressWarnings("null")
        public static final Codec<ChargeAnimation> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.BOOL.optionalFieldOf("enabled", false)
                                .forGetter(ChargeAnimation::enabled),
                                Codec.INT.optionalFieldOf("chargeDurationTicks", 20)
                                        .forGetter(ChargeAnimation::chargeDurationTicks),
                                Codec.FLOAT.optionalFieldOf("chargeSpeedMultiplier", 0.8f)
                                        .forGetter(ChargeAnimation::chargeSpeedMultiplier))
                        .apply(instance, ChargeAnimation::new));
    }

    /** Codec for CastTypeAnimation */
    @SuppressWarnings("null")
    public static final Codec<CastTypeAnimation> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.xmap(str -> {
                try {
                    return SpellCastType.valueOf(str.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    MAM.LOGGER.warn("Invalid cast type '{}' in animation, defaulting to PROJECTILE",
                            str);
                    return SpellCastType.PROJECTILE;
                }
            }, SpellCastType::name).fieldOf("castType").forGetter(CastTypeAnimation::castType),
                    Codec.INT.optionalFieldOf("durationTicks", 12)
                            .forGetter(CastTypeAnimation::durationTicks),
                    Codec.FLOAT.optionalFieldOf("speedMultiplier", 1.0f)
                            .forGetter(CastTypeAnimation::speedMultiplier),
                    Codec.STRING.fieldOf("animationKey").forGetter(CastTypeAnimation::animationKey),
                    ParticleEffect.CODEC.optionalFieldOf("particles")
                            .forGetter(CastTypeAnimation::particles),
                    SoundEffect.CODEC.optionalFieldOf("sound").forGetter(CastTypeAnimation::sound),
                    ChargeAnimation.CODEC.optionalFieldOf("chargeAnimation")
                            .forGetter(CastTypeAnimation::chargeAnimation))
                    .apply(instance, CastTypeAnimation::new));

    /**
     * Validates animation configuration.
     *
     * @return true if cast type is non-null, duration >= 0, speed > 0, animation key non-empty
     */
    public boolean isValid() {
        boolean castTypeValid = castType != null;
        boolean durationValid = durationTicks >= 0;
        boolean speedValid = speedMultiplier > 0f;
        boolean keyValid = animationKey != null && !animationKey.isEmpty();

        return castTypeValid && durationValid && speedValid && keyValid;
    }

    /**
     * Gets the effective animation duration considering speed multiplier.
     *
     * @return duration in ticks adjusted by speed (duration / speedMultiplier)
     */
    public int getEffectiveDuration() {
        return Math.max(1, (int) (durationTicks / speedMultiplier));
    }

    /**
     * Creates a basic animation config with minimal setup.
     *
     * @param castType spell cast type
     * @param key animation key identifier
     * @return animation with default duration 12 ticks, speed 1.0x
     */
    public static CastTypeAnimation create(SpellCastType castType, String key) {
        return new CastTypeAnimation(castType, 12, 1.0f, key, Optional.empty(), Optional.empty(),
                Optional.empty());
    }

    @Override
    public String toString() {
        return String.format(
                "CastTypeAnimation{type=%s, duration=%d ticks, speed=%.2fx, key=%s, particles=%s, sound=%s, charge=%s}",
                castType, durationTicks, speedMultiplier, animationKey, particles.isPresent(),
                sound.isPresent(), chargeAnimation.map(ChargeAnimation::enabled).orElse(false));
    }
}
