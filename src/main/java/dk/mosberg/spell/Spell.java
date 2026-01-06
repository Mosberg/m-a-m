package dk.mosberg.spell;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Represents a spell loaded from JSON data.
 */
public class Spell {

    @SuppressWarnings({"unused", "null"})
    private static final Codec<SpellSchool> SCHOOL_CODEC = Codec.STRING
            .xmap(s -> SpellSchool.valueOf(s.toUpperCase()), school -> school.name().toLowerCase());

    @SuppressWarnings({"unused", "null"})
    private static final Codec<SpellCastType> CAST_TYPE_CODEC = Codec.STRING.xmap(
            c -> SpellCastType.valueOf(c.toUpperCase()), castType -> castType.name().toLowerCase());

    public static final Codec<Spell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(Spell::getId),
            Codec.STRING.fieldOf("name").forGetter(Spell::getName),
            Codec.STRING.fieldOf("school").forGetter((Spell spell) -> spell.getSchool().name()),
            Codec.STRING.optionalFieldOf("description", "").forGetter(Spell::getDescription),
            Codec.STRING.fieldOf("castType").forGetter((Spell spell) -> spell.getCastType().name()),
            Codec.FLOAT.fieldOf("manaCost").forGetter(Spell::getManaCost),
            Codec.FLOAT.optionalFieldOf("castTime", 1.0f).forGetter(Spell::getCastTime),
            Codec.FLOAT.optionalFieldOf("cooldown", 0.0f).forGetter(Spell::getCooldown),
            Codec.INT.fieldOf("tier").forGetter(Spell::getTier),
            Codec.INT.optionalFieldOf("requiredLevel", 1).forGetter(Spell::getRequiredLevel),
            Codec.FLOAT.optionalFieldOf("damage", 0.0f).forGetter(Spell::getDamage),
            Codec.FLOAT.optionalFieldOf("range", 30.0f).forGetter(Spell::getRange),
            Codec.FLOAT.optionalFieldOf("projectileSpeed", 1.0f)
                    .forGetter(Spell::getProjectileSpeed),
            Codec.FLOAT.optionalFieldOf("aoeRadius", 0.0f).forGetter(Spell::getAoeRadius),
            Codec.FLOAT.optionalFieldOf("knockback", 0.0f).forGetter(Spell::getKnockback),
            Codec.STRING.optionalFieldOf("sound", "").forGetter(Spell::getSound))
            .apply(instance,
                    (id, name, school, desc, castType, manaCost, castTime, cooldown, tier,
                            requiredLevel, damage, range, projectileSpeed, aoeRadius, knockback,
                            sound) -> new Spell(id, name, school, desc, castType, manaCost,
                                    castTime, cooldown, tier, requiredLevel, damage, range,
                                    projectileSpeed, aoeRadius, knockback, List.of(),
                                    java.util.Map.of(), sound, Optional.empty())));

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
    private final List<String> tags;

    public Spell(Identifier id, String name, String school, String description, String castType,
            float manaCost, float castTime, float cooldown, int tier, int requiredLevel,
            float damage, float range, float projectileSpeed, float aoeRadius, float knockback,
            List<StatusEffectEntry> statusEffects, java.util.Map<String, Float> customData,
            String sound, Optional<VfxData> vfx) {
        this(id, name, school, description, castType, manaCost, castTime, cooldown, tier,
                requiredLevel, damage, range, projectileSpeed, aoeRadius, knockback, statusEffects,
                customData, sound, vfx, List.of());
    }

    public Spell(Identifier id, String name, String school, String description, String castType,
            float manaCost, float castTime, float cooldown, int tier, int requiredLevel,
            float damage, float range, float projectileSpeed, float aoeRadius, float knockback,
            List<StatusEffectEntry> statusEffects, java.util.Map<String, Float> customData,
            String sound, Optional<VfxData> vfx, List<String> tags) {
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
        this.tags = new java.util.ArrayList<>(tags);
    }

    /**
     * Factory method for codec deserialization. Converts string representations to enum values.
     */
    public static Spell create(Identifier id, String name, String school, String description,
            String castType, float manaCost, float castTime, float cooldown, int tier,
            int requiredLevel, float damage, float range, float projectileSpeed, float aoeRadius,
            float knockback, List<StatusEffectEntry> statusEffects,
            java.util.Map<String, Float> customData, String sound, Optional<VfxData> vfx) {
        return new Spell(id, name, school, description, castType, manaCost, castTime, cooldown,
                tier, requiredLevel, damage, range, projectileSpeed, aoeRadius, knockback,
                statusEffects, customData, sound, vfx);
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

    public Optional<VfxData> getVfxOptional() {
        return Optional.ofNullable(vfx);
    }

    public String getTranslationKey() {
        return "spell." + id.getNamespace() + "." + id.getPath();
    }

    public List<String> getTags() {
        return new java.util.ArrayList<>(tags);
    }

    // Status effect entry for serialization
    public record StatusEffectEntry(String effect, int duration, int amplifier) {
        @SuppressWarnings("null")
        public static final Codec<StatusEffectEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.STRING.fieldOf("effect").forGetter(StatusEffectEntry::effect),
                                Codec.INT.fieldOf("duration")
                                        .forGetter(StatusEffectEntry::duration),
                                Codec.INT.optionalFieldOf("amplifier", 0)
                                        .forGetter(StatusEffectEntry::amplifier))
                        .apply(instance, StatusEffectEntry::new));

        public Optional<RegistryEntry<StatusEffect>> getStatusEffect() {
            Identifier id = Identifier.tryParse(effect);
            if (id == null) {
                return Optional.empty();
            }
            return Registries.STATUS_EFFECT.getEntry(id)
                    .map(entry -> (net.minecraft.registry.entry.RegistryEntry<StatusEffect>) entry);
        }
    }

    // VFX data for spell visual effects
    public record VfxData(String particleType, int particleCount, String color) {
        @SuppressWarnings("null")
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
