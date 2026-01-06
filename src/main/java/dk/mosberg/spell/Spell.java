package dk.mosberg.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Represents a spell loaded from JSON data.
 */
public class Spell {

    public static final Codec<Spell> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.fieldOf("id").forGetter(spell -> spell.id),
                    Codec.STRING.fieldOf("name").forGetter(spell -> spell.name),
                    Codec.STRING.fieldOf("school")
                            .forGetter(spell -> spell.school.name().toLowerCase()),
                    Codec.STRING
                            .optionalFieldOf("description", "")
                            .forGetter(spell -> spell.description),
                    Codec.STRING
                            .fieldOf("castType")
                            .forGetter(spell -> spell.castType.name().toLowerCase()),
                    Codec.FLOAT.fieldOf("manaCost").forGetter(spell -> spell.manaCost),
                    Codec.FLOAT.optionalFieldOf("castTime", 1.0f)
                            .forGetter(spell -> spell.castTime),
                    Codec.FLOAT.optionalFieldOf("cooldown", 0.0f)
                            .forGetter(spell -> spell.cooldown),
                    Codec.INT.fieldOf("tier").forGetter(spell -> spell.tier),
                    Codec.INT.optionalFieldOf("requiredLevel", 1)
                            .forGetter(spell -> spell.requiredLevel),
                    Codec.FLOAT.optionalFieldOf("damage", 0.0f).forGetter(spell -> spell.damage),
                    Codec.FLOAT.optionalFieldOf("range", 30.0f).forGetter(spell -> spell.range),
                    Codec.FLOAT
                            .optionalFieldOf("projectileSpeed", 1.0f)
                            .forGetter(spell -> spell.projectileSpeed),
                    Codec.FLOAT.optionalFieldOf("aoeRadius", 0.0f)
                            .forGetter(spell -> spell.aoeRadius),
                    Codec.FLOAT.optionalFieldOf("knockback", 0.0f)
                            .forGetter(spell -> spell.knockback),
                    StatusEffectEntry.CODEC.listOf().optionalFieldOf("statusEffects", List.of())
                            .forGetter(spell -> spell.statusEffects),
                    Codec.unboundedMap(Codec.STRING, Codec.FLOAT)
                            .optionalFieldOf("customData", java.util.Map.of())
                            .forGetter(spell -> spell.customData),
                    Codec.STRING.optionalFieldOf("sound", "").forGetter(spell -> spell.sound),
                    VfxData.CODEC.optionalFieldOf("vfx")
                            .forGetter(spell -> Optional.ofNullable(spell.vfx)))
            .apply(instance, Spell::new));

    private final Identifier id;
    private final String name;
    private final SpellSchool school;
    private final String description;
    private final SpellCastType castType;
    private final float manaCost;
    private final float castTime;
    private final float cooldown;
    private final int tier;
    private final int requiredLevel;
    private final float damage;
    private final float range;
    private final float projectileSpeed;
    private final float aoeRadius;
    private final float knockback;
    private final List<StatusEffectEntry> statusEffects;
    private final java.util.Map<String, Float> customData;
    private final String sound;
    private final VfxData vfx;

    public Spell(Identifier id, String name, String school, String description, String castType,
            float manaCost, float castTime, float cooldown, int tier, int requiredLevel,
            float damage, float range, float projectileSpeed, float aoeRadius, float knockback,
            List<StatusEffectEntry> statusEffects, java.util.Map<String, Float> customData,
            String sound, Optional<VfxData> vfx) {
        this.id = id;
        this.name = name;
        this.school = SpellSchool.valueOf(school.toUpperCase());
        this.description = description;
        this.castType = SpellCastType.valueOf(castType.toUpperCase());
        this.manaCost = manaCost;
        this.castTime = castTime;
        this.cooldown = cooldown;
        this.tier = tier;
        this.requiredLevel = requiredLevel;
        this.damage = damage;
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.aoeRadius = aoeRadius;
        this.knockback = knockback;
        this.statusEffects = statusEffects;
        this.customData = customData;
        this.sound = sound;
        this.vfx = vfx.orElse(null);
    }

    // Getters
    public Identifier getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SpellSchool getSchool() {
        return school;
    }

    public String getDescription() {
        return description;
    }

    public SpellCastType getCastType() {
        return castType;
    }

    public float getManaCost() {
        return manaCost;
    }

    public float getCastTime() {
        return castTime;
    }

    public float getCooldown() {
        return cooldown;
    }

    public int getTier() {
        return tier;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public float getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public float getAoeRadius() {
        return aoeRadius;
    }

    public float getKnockback() {
        return knockback;
    }

    public List<StatusEffectEntry> getStatusEffects() {
        return statusEffects;
    }

    public java.util.Map<String, Float> getCustomData() {
        return customData;
    }

    public String getSound() {
        return sound;
    }

    public VfxData getVfx() {
        return vfx;
    }

    public String getTranslationKey() {
        return "spell." + id.getNamespace() + "." + id.getPath();
    }

    // Status effect entry for serialization
    public record StatusEffectEntry(String effect, int duration, int amplifier) {
        public static final Codec<StatusEffectEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.STRING.fieldOf("effect").forGetter(StatusEffectEntry::effect),
                                Codec.INT.fieldOf("duration")
                                        .forGetter(StatusEffectEntry::duration),
                                Codec.INT.optionalFieldOf("amplifier", 0)
                                        .forGetter(StatusEffectEntry::amplifier))
                        .apply(instance, StatusEffectEntry::new));

        public Optional<RegistryEntry<StatusEffect>> getStatusEffect() {
            return Registries.STATUS_EFFECT.getEntry(Identifier.tryParse(effect));
        }
    }

    // VFX data for spell visual effects
    public record VfxData(String particleType, int particleCount, String color) {
        public static final Codec<VfxData> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.STRING.fieldOf("particleType").forGetter(VfxData::particleType),
                        Codec.INT.optionalFieldOf("particleCount", 10)
                                .forGetter(VfxData::particleCount),
                        Codec.STRING.fieldOf("color").forGetter(VfxData::color))
                .apply(instance, VfxData::new));

        public int getColorInt() {
            try {
                return Integer.parseInt(color, 16);
            } catch (NumberFormatException e) {
                return 0xFFFFFF; // Default to white
            }
        }
    }
}
