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
            .apply(instance, (id, name, school, desc, castType, manaCost, castTime, cooldown, tier,
                    requiredLevel, damage, range, projectileSpeed, aoeRadius, knockback,
                    sound) -> new Spell(id, name, school, desc, castType, manaCost, castTime,
                            cooldown, tier, requiredLevel, damage, range, projectileSpeed,
                            aoeRadius, knockback, List.of(), java.util.Map.of(), sound,
                            Optional.empty(), List.of(), Optional.empty())));

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
    private final SpellRarity rarity;
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
                customData, sound, vfx, List.of(), Optional.empty());
    }

    public Spell(Identifier id, String name, String school, String description, String castType,
            float manaCost, float castTime, float cooldown, int tier, int requiredLevel,
            float damage, float range, float projectileSpeed, float aoeRadius, float knockback,
            List<StatusEffectEntry> statusEffects, java.util.Map<String, Float> customData,
            String sound, Optional<VfxData> vfx, List<String> tags) {
        this(id, name, school, description, castType, manaCost, castTime, cooldown, tier,
                requiredLevel, damage, range, projectileSpeed, aoeRadius, knockback, statusEffects,
                customData, sound, vfx, tags, Optional.empty());
    }

    public Spell(Identifier id, String name, String school, String description, String castType,
            float manaCost, float castTime, float cooldown, int tier, int requiredLevel,
            float damage, float range, float projectileSpeed, float aoeRadius, float knockback,
            List<StatusEffectEntry> statusEffects, java.util.Map<String, Float> customData,
            String sound, Optional<VfxData> vfx, List<String> tags, Optional<String> rarity) {
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
        this.rarity = rarity.map(r -> SpellRarity.valueOf(r.toUpperCase()))
                .orElse(SpellRarity.fromTier(tier));
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

    public SpellRarity getRarity() {
        return rarity;
    }

    /**
     * Calculate effective damage with all modifiers applied. Includes: school modifiers, rarity
     * multiplier, environmental effects, pool modifiers.
     *
     * @param isRaining Whether it's raining
     * @param isUnderwater Whether caster is underwater
     * @param isInNether Whether caster is in the Nether
     * @param isOnGround Whether caster is on solid ground
     * @param poolModifier Damage modifier from the mana pool used
     * @return Effective damage value
     */
    public float getEffectiveDamage(boolean isRaining, boolean isUnderwater, boolean isInNether,
            boolean isOnGround, float poolModifier) {
        float baseDamage = this.damage;

        // Apply school stat multiplier
        baseDamage *= school.getDamageMultiplier();

        // Apply rarity power multiplier
        baseDamage *= rarity.getPowerModifier();

        // Apply environmental modifier
        baseDamage *=
                school.getEnvironmentalModifier(isRaining, isUnderwater, isInNether, isOnGround);

        // Apply pool modifier
        baseDamage *= poolModifier;

        return baseDamage;
    }

    /**
     * Calculate effective mana cost with school modifiers.
     *
     * @return Effective mana cost
     */
    public float getEffectiveManaCost() {
        return this.manaCost * school.getManaCostMultiplier();
    }

    /**
     * Calculate effective cooldown with school, cast type, and pool modifiers.
     *
     * @param poolModifier Cooldown modifier from the mana pool used
     * @return Effective cooldown in seconds
     */
    public float getEffectiveCooldown(float poolModifier) {
        float baseCooldown = this.cooldown;

        // Apply school multiplier
        baseCooldown *= school.getCooldownMultiplier();

        // Apply cast type multiplier
        baseCooldown *= castType.getCooldownMultiplier();

        // Apply pool modifier
        baseCooldown *= poolModifier;

        return baseCooldown;
    }

    /**
     * Calculate effective range with pool modifiers.
     *
     * @param poolModifier Range modifier from the mana pool used
     * @return Effective range
     */
    public float getEffectiveRange(float poolModifier) {
        return this.range * poolModifier;
    }

    /**
     * Check if this spell should have bonus effects in the current environment. Returns
     * environmental interaction messages for UI.
     *
     * @param isRaining Whether it's raining
     * @param isUnderwater Whether caster is underwater
     * @param isInNether Whether caster is in the Nether
     * @param isOnGround Whether caster is on solid ground
     * @return Environmental bonus message, or empty if no bonus
     */
    public String getEnvironmentalBonus(boolean isRaining, boolean isUnderwater, boolean isInNether,
            boolean isOnGround) {
        float modifier =
                school.getEnvironmentalModifier(isRaining, isUnderwater, isInNether, isOnGround);

        if (modifier > 1.0f) {
            int bonusPercent = (int) ((modifier - 1.0f) * 100);
            return "+" + bonusPercent + "% " + school.getDisplayName() + " power!";
        } else if (modifier < 1.0f) {
            int penaltyPercent = (int) ((1.0f - modifier) * 100);
            return "-" + penaltyPercent + "% " + school.getDisplayName() + " power";
        }

        return "";
    }

    /**
     * Check if this spell has any environmental interactions active.
     */
    public boolean hasEnvironmentalEffect(boolean isRaining, boolean isUnderwater,
            boolean isInNether, boolean isOnGround) {
        float modifier =
                school.getEnvironmentalModifier(isRaining, isUnderwater, isInNether, isOnGround);
        return modifier != 1.0f;
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

    // TODO: Add spell upgrade/scaling system
    // TODO: Add spell combination/fusion recipes
    // TODO: Add spell modification system (transmutation)
    // TODO: Add spell presets/loadouts
    // TODO: Add spell tutorial/guidance system
    // TODO: Add spell rarity/quality tiers
    // TODO: Add spell unlock requirements/progression
    // TODO: Add conditional spell effects based on environment
    // TODO: Add spell animation configuration
    // TODO: Add spell sound effect customization

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
