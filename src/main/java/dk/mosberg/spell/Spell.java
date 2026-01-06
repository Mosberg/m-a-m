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

    /**
     * Check if a player meets the unlock requirements for this spell. Requirements: player level >=
     * requiredLevel, tier access, prerequisite spells learned.
     *
     * @param playerLevel Current player level
     * @param maxTierUnlocked Maximum spell tier the player has unlocked
     * @param knownSpells List of spell IDs the player has already learned
     * @return true if player can unlock/learn this spell
     */
    public boolean meetsUnlockRequirements(int playerLevel, int maxTierUnlocked,
            List<Identifier> knownSpells) {
        // Check level requirement
        if (playerLevel < requiredLevel) {
            return false;
        }

        // Check tier access
        if (tier > maxTierUnlocked) {
            return false;
        }

        // Check prerequisite spells (from tags starting with "requires:")
        for (String tag : tags) {
            if (tag.startsWith("requires:")) {
                String requiredSpellName = tag.substring("requires:".length());
                Identifier requiredSpellId = Identifier.of(id.getNamespace(), requiredSpellName);
                if (!knownSpells.contains(requiredSpellId)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get the list of prerequisite spell IDs required to unlock this spell.
     */
    public List<Identifier> getPrerequisiteSpells() {
        List<Identifier> prerequisites = new java.util.ArrayList<>();
        for (String tag : tags) {
            if (tag.startsWith("requires:")) {
                String requiredSpellName = tag.substring("requires:".length());
                prerequisites.add(Identifier.of(id.getNamespace(), requiredSpellName));
            }
        }
        return prerequisites;
    }

    /**
     * Get the unlock cost for this spell (in experience levels). Higher tier and rarity spells cost
     * more.
     */
    public int getUnlockCost() {
        int baseCost = tier * 5; // Tier 1 = 5 levels, Tier 4 = 20 levels
        float rarityMultiplier = switch (rarity) {
            case COMMON -> 1.0f;
            case UNCOMMON -> 1.2f;
            case RARE -> 1.5f;
            case EPIC -> 2.0f;
            case LEGENDARY -> 3.0f;
        };
        return (int) (baseCost * rarityMultiplier);
    }

    /**
     * Get a description of why the spell is locked (for UI display).
     */
    public String getUnlockRequirementDescription(int playerLevel, int maxTierUnlocked,
            List<Identifier> knownSpells) {
        if (playerLevel < requiredLevel) {
            return "Requires level " + requiredLevel + " (current: " + playerLevel + ")";
        }
        if (tier > maxTierUnlocked) {
            return "Requires tier " + tier + " access (current max: " + maxTierUnlocked + ")";
        }

        List<Identifier> prerequisites = getPrerequisiteSpells();
        for (Identifier prereq : prerequisites) {
            if (!knownSpells.contains(prereq)) {
                return "Requires spell: " + prereq.getPath();
            }
        }

        return "Ready to unlock for " + getUnlockCost() + " levels";
    }

    /**
     * Get the custom sound identifier for this spell. Returns the sound field or generates a
     * default based on school and cast type.
     */
    public String getSoundIdentifier() {
        if (sound != null && !sound.isEmpty()) {
            return sound;
        }

        // Generate default sound based on school and cast type
        String schoolSound = school.name().toLowerCase();
        String typeSound = switch (castType) {
            case PROJECTILE -> "projectile";
            case AOE -> "explosion";
            case BEAM -> "beam";
            case SELF_CAST -> "buff";
            case UTILITY -> "utility";
            case RITUAL -> "ritual";
            case SYNERGY -> "synergy";
            case TRAP -> "trap";
            case SUMMON -> "summon";
            case TRANSFORM -> "transform";
        };

        return "mam:spell." + schoolSound + "." + typeSound;
    }

    /**
     * Get the volume modifier for this spell's sound based on tier and rarity.
     */
    public float getSoundVolume() {
        float baseVolume = 1.0f;

        // Tier affects volume (higher tier = louder)
        baseVolume *= (1.0f + (tier - 1) * 0.1f); // Tier 1 = 1.0x, Tier 4 = 1.3x

        // Rarity affects volume
        baseVolume *= switch (rarity) {
            case COMMON -> 0.9f;
            case UNCOMMON -> 1.0f;
            case RARE -> 1.1f;
            case EPIC -> 1.2f;
            case LEGENDARY -> 1.4f;
        };

        return Math.min(2.0f, baseVolume);
    }

    /**
     * Get the pitch modifier for this spell's sound.
     */
    public float getSoundPitch() {
        // School affects pitch
        float pitch = switch (school) {
            case AIR -> 1.2f; // Higher pitch
            case FIRE -> 1.0f; // Normal
            case WATER -> 0.9f; // Lower pitch
            case EARTH -> 0.8f; // Lowest pitch
        };

        // Cast type fine-tuning
        if (castType == SpellCastType.BEAM || castType == SpellCastType.PROJECTILE) {
            pitch += 0.1f;
        } else if (castType == SpellCastType.RITUAL || castType == SpellCastType.TRANSFORM) {
            pitch -= 0.1f;
        }

        return Math.max(0.5f, Math.min(2.0f, pitch));
    }


    // TODO: Add spell combination/fusion recipes
    // TODO: Add spell modification system (transmutation)
    // TODO: Add spell presets/loadouts
    // TODO: Add spell tutorial/guidance system
    // TODO: Add spell animation configuration


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

    /**
     * Spell upgrade system - represents spell progression/scaling. Spells can be upgraded with XP
     * or resources to improve effectiveness.
     */
    public static class SpellUpgrade {
        private final int upgradeLevel; // 0 = base, 1-5 = upgrade levels
        private final int totalXpInvested; // Total XP spent on upgrades

        public SpellUpgrade(int upgradeLevel, int totalXpInvested) {
            this.upgradeLevel = Math.max(0, Math.min(5, upgradeLevel)); // Cap at level 5
            this.totalXpInvested = Math.max(0, totalXpInvested);
        }

        /**
         * Get damage scaling multiplier from upgrade level. Level 0: 1.0x, Level 1: 1.1x, Level 5:
         * 1.5x
         */
        public float getDamageMultiplier() {
            return 1.0f + (upgradeLevel * 0.1f);
        }

        /**
         * Get mana cost reduction from upgrade level. Level 0: 1.0x, Level 5: 0.85x (15% reduction)
         */
        public float getManaCostMultiplier() {
            return 1.0f - (upgradeLevel * 0.03f);
        }

        /**
         * Get cooldown reduction from upgrade level. Level 0: 1.0x, Level 5: 0.75x (25% reduction)
         */
        public float getCooldownMultiplier() {
            return 1.0f - (upgradeLevel * 0.05f);
        }

        /**
         * Get range bonus from upgrade level. Level 0: 1.0x, Level 5: 1.25x (25% increase)
         */
        public float getRangeMultiplier() {
            return 1.0f + (upgradeLevel * 0.05f);
        }

        /**
         * Calculate XP required for next upgrade level.
         */
        public int getXpForNextLevel(int spellTier, SpellRarity spellRarity) {
            if (upgradeLevel >= 5)
                return Integer.MAX_VALUE; // Max level

            // Base XP scales with tier: Tier 1 = 100 XP/level, Tier 4 = 400 XP/level
            int baseXp = spellTier * 100;

            // Scale with upgrade level (exponential): 1x, 1.5x, 2x, 2.5x, 3x
            float levelMultiplier = 1.0f + (upgradeLevel * 0.5f);

            // Rarity affects cost
            float rarityMultiplier = switch (spellRarity) {
                case COMMON -> 1.0f;
                case UNCOMMON -> 1.2f;
                case RARE -> 1.5f;
                case EPIC -> 2.0f;
                case LEGENDARY -> 3.0f;
            };

            return (int) (baseXp * levelMultiplier * rarityMultiplier);
        }

        /**
         * Check if spell can be upgraded to next level.
         */
        public boolean canUpgrade(int availableXp, int spellTier, SpellRarity spellRarity) {
            return upgradeLevel < 5 && availableXp >= getXpForNextLevel(spellTier, spellRarity);
        }

        /**
         * Create upgraded version of this upgrade.
         */
        public SpellUpgrade upgrade(int spellTier, SpellRarity spellRarity) {
            if (upgradeLevel >= 5)
                return this;
            int xpCost = getXpForNextLevel(spellTier, spellRarity);
            return new SpellUpgrade(upgradeLevel + 1, totalXpInvested + xpCost);
        }

        public int getUpgradeLevel() {
            return upgradeLevel;
        }

        public int getTotalXpInvested() {
            return totalXpInvested;
        }

        /**
         * Create base spell upgrade (level 0, no XP).
         */
        public static SpellUpgrade createBase() {
            return new SpellUpgrade(0, 0);
        }
    }

    /**
     * Get the scaled damage value for this spell at a specific upgrade level.
     */
    public float getUpgradedDamage(SpellUpgrade upgrade) {
        return damage * upgrade.getDamageMultiplier();
    }

    /**
     * Get the scaled mana cost for this spell at a specific upgrade level.
     */
    public float getUpgradedManaCost(SpellUpgrade upgrade) {
        return manaCost * upgrade.getManaCostMultiplier();
    }

    /**
     * Get the scaled cooldown for this spell at a specific upgrade level.
     */
    public float getUpgradedCooldown(SpellUpgrade upgrade) {
        return cooldown * upgrade.getCooldownMultiplier();
    }

    /**
     * Get the scaled range for this spell at a specific upgrade level.
     */
    public float getUpgradedRange(SpellUpgrade upgrade) {
        return range * upgrade.getRangeMultiplier();
    }

    /**
     * Get upgrade progress description for UI display.
     */
    public String getUpgradeDescription(SpellUpgrade upgrade) {
        if (upgrade.getUpgradeLevel() == 0) {
            return "Not upgraded (XP: " + upgrade.getXpForNextLevel(tier, rarity) + " for Level 1)";
        }
        if (upgrade.getUpgradeLevel() >= 5) {
            return "Max Level (" + upgrade.getTotalXpInvested() + " XP invested)";
        }
        return "Level " + upgrade.getUpgradeLevel() + " (Next: "
                + upgrade.getXpForNextLevel(tier, rarity) + " XP)";
    }
}
