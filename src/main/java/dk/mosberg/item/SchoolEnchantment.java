package dk.mosberg.item;

import java.util.Comparator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.mosberg.MAM;
import dk.mosberg.spell.SpellSchool;

/**
 * School-based enchantment for staffs/spellbooks that specialize equipment in specific spell
 * schools.
 *
 * <p>
 * Each enchantment binds a staff/spellbook to a school, providing modifiers for spells of that
 * school:
 * <ul>
 * <li>Damage multiplier (base: 1.0x, range: 0.5-2.0x)
 * <li>Mana cost reduction (base: 1.0x, range: 0.5-1.5x)
 * <li>Cooldown reduction (base: 1.0x, range: 0.5-1.5x)
 * <li>Range/projectile speed bonus (base: 1.0x, range: 0.75-1.5x)
 * </ul>
 *
 * <p>
 * Enchantment level (1-3) determines strength of modifiers. Higher level = stronger bonuses.
 *
 * <p>
 * Example: Staff of Inferno (Fire enchantment, level 2):
 * <ul>
 * <li>Fire spells deal 1.3x damage
 * <li>Fire spells cost 10% less mana
 * <li>Fire spell cooldowns 15% shorter
 * <li>Fire projectiles 1.2x faster
 * </ul>
 */
public record SchoolEnchantment(SpellSchool school, int level) {

    /** Enchantment level range */
    public static final int MIN_LEVEL = 1;

    public static final int MAX_LEVEL = 3;

    /** Codec for JSON serialization/deserialization */
    @SuppressWarnings("null")
    public static final Codec<SchoolEnchantment> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.xmap(str -> {
                try {
                    return SpellSchool.valueOf(str.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    MAM.LOGGER.warn("Invalid spell school '{}' in enchantment, defaulting to AIR",
                            str);
                    return SpellSchool.AIR;
                }
            }, school -> school.name().toLowerCase()).fieldOf("school")
                    .forGetter(SchoolEnchantment::school),
                    Codec.INT.fieldOf("level").forGetter(SchoolEnchantment::level))
                    .apply(instance, SchoolEnchantment::new));

    /**
     * Validates enchantment constraints.
     *
     * @return true if school is non-null and level is 1-3
     */
    public boolean isValid() {
        return school != null && level >= MIN_LEVEL && level <= MAX_LEVEL;
    }

    /**
     * Gets the damage multiplier bonus for this enchantment level.
     *
     * <p>
     * Level 1: +0.15 (1.15x damage)
     * <p>
     * Level 2: +0.30 (1.30x damage)
     * <p>
     * Level 3: +0.50 (1.50x damage)
     *
     * @return damage multiplier bonus (0.15 - 0.50)
     */
    public float getDamageBonus() {
        return switch (level) {
            case 1 -> 0.15f;
            case 2 -> 0.30f;
            case 3 -> 0.50f;
            default -> 0f;
        };
    }

    /**
     * Gets the mana cost reduction for this enchantment level.
     *
     * <p>
     * Level 1: -0.05 (0.95x mana cost)
     * <p>
     * Level 2: -0.10 (0.90x mana cost)
     * <p>
     * Level 3: -0.15 (0.85x mana cost)
     *
     * @return mana cost multiplier reduction (0.05 - 0.15), subtract from 1.0
     */
    public float getManaCostReduction() {
        return switch (level) {
            case 1 -> 0.05f;
            case 2 -> 0.10f;
            case 3 -> 0.15f;
            default -> 0f;
        };
    }

    /**
     * Gets the cooldown reduction for this enchantment level.
     *
     * <p>
     * Level 1: -0.08 (0.92x cooldown, 8% faster)
     * <p>
     * Level 2: -0.15 (0.85x cooldown, 15% faster)
     * <p>
     * Level 3: -0.25 (0.75x cooldown, 25% faster)
     *
     * @return cooldown multiplier reduction (0.08 - 0.25), subtract from 1.0
     */
    public float getCooldownReduction() {
        return switch (level) {
            case 1 -> 0.08f;
            case 2 -> 0.15f;
            case 3 -> 0.25f;
            default -> 0f;
        };
    }

    /**
     * Gets the range/projectile speed bonus for this enchantment level.
     *
     * <p>
     * Level 1: +0.10 (1.10x range/speed)
     * <p>
     * Level 2: +0.20 (1.20x range/speed)
     * <p>
     * Level 3: +0.35 (1.35x range/speed)
     *
     * @return range/speed bonus multiplier (0.10 - 0.35)
     */
    public float getRangeBonus() {
        return switch (level) {
            case 1 -> 0.10f;
            case 2 -> 0.20f;
            case 3 -> 0.35f;
            default -> 0f;
        };
    }

    /**
     * Gets the translation key for this enchantment display name.
     *
     * @return key in format "enchantment.mam.school_{school}_lvl_{level}"
     */
    public String getTranslationKey() {
        return String.format("enchantment.mam.%s_lvl_%d", school.name().toLowerCase(), level);
    }

    /**
     * Creates a new enchantment with validated level.
     *
     * @param school spell school
     * @param level enchantment level (auto-clamped to 1-3)
     * @return validated enchantment
     */
    public static SchoolEnchantment create(SpellSchool school, int level) {
        int clampedLevel = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        return new SchoolEnchantment(school, clampedLevel);
    }

    /**
     * Compares two enchantments by level (descending).
     *
     * @return comparator ordering by level high-to-low
     */
    public static Comparator<SchoolEnchantment> byLevelDescending() {
        return (e1, e2) -> Integer.compare(e2.level(), e1.level());
    }

    @Override
    public String toString() {
        return String.format(
                "SchoolEnchantment{school=%s, level=%d, damage=+%.0f%%, mana=-%.0f%%, cooldown=-%.0f%%, range=+%.0f%%}",
                school, level, getDamageBonus() * 100, getManaCostReduction() * 100,
                getCooldownReduction() * 100, getRangeBonus() * 100);
    }
}
