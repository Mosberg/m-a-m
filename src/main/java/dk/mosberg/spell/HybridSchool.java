package dk.mosberg.spell;

import java.util.Comparator;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.mosberg.MAM;

/**
 * Hybrid school combining two parent schools to unlock mixed-element spells.
 *
 * <p>
 * Each hybrid school is defined by:
 * <ul>
 * <li>Two parent schools (e.g., Fire + Water = Vapor)
 * <li>Unique identifier (e.g., "vapor")
 * <li>Combined name (e.g., "Vapor School")
 * <li>Minimum proficiency in EACH parent school to unlock (default: 50)
 * <li>Hybrid stat multiplier (blends parent schools' modifiers)
 * </ul>
 *
 * <p>
 * Example combinations:
 * <ul>
 * <li>Fire + Water → Vapor (explosive steam)
 * <li>Fire + Air → Magma (lava/heat waves)
 * <li>Water + Earth → Mud (slow, heavy)
 * <li>Air + Earth → Sand (dust storms, abrasion)
 * <li>Fire + Earth → Lava (molten, destructive)
 * <li>Water + Air → Ice (frozen, slowing)
 * </ul>
 *
 * <p>
 * Stat combination follows: Hybrid value = (parent1 + parent2) / 2
 */
public record HybridSchool(String id, String displayName, SpellSchool parent1, SpellSchool parent2,
        int minProficiencyPerParent, float damageMultiplier, float manaCostMultiplier,
        float cooldownMultiplier) {

    /** Default minimum proficiency required in each parent school */
    public static final int DEFAULT_MIN_PROFICIENCY = 50;

    /** Codec for JSON serialization */
    @SuppressWarnings("null")
    public static final Codec<HybridSchool> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.STRING.fieldOf("id").forGetter(HybridSchool::id),
                    Codec.STRING.fieldOf("displayName").forGetter(HybridSchool::displayName),
                    Codec.STRING.xmap(str -> {
                        try {
                            return SpellSchool.valueOf(str.toUpperCase());
                        } catch (IllegalArgumentException ex) {
                            MAM.LOGGER.warn("Invalid school '{}' in hybrid, defaulting to AIR",
                                    str);
                            return SpellSchool.AIR;
                        }
                    }, school -> school.name().toLowerCase()).fieldOf("parent1")
                            .forGetter(HybridSchool::parent1),
                    Codec.STRING.xmap(str -> {
                        try {
                            return SpellSchool.valueOf(str.toUpperCase());
                        } catch (IllegalArgumentException ex) {
                            MAM.LOGGER.warn("Invalid school '{}' in hybrid, defaulting to AIR",
                                    str);
                            return SpellSchool.AIR;
                        }
                    }, school -> school.name().toLowerCase()).fieldOf("parent2")
                            .forGetter(HybridSchool::parent2),
                    Codec.INT.optionalFieldOf("minProficiencyPerParent", DEFAULT_MIN_PROFICIENCY)
                            .forGetter(HybridSchool::minProficiencyPerParent),
                    Codec.FLOAT.optionalFieldOf("damageMultiplier", 1.0f)
                            .forGetter(HybridSchool::damageMultiplier),
                    Codec.FLOAT.optionalFieldOf("manaCostMultiplier", 1.0f)
                            .forGetter(HybridSchool::manaCostMultiplier),
                    Codec.FLOAT.optionalFieldOf("cooldownMultiplier", 1.0f)
                            .forGetter(HybridSchool::cooldownMultiplier))
                    .apply(instance, HybridSchool::new));

    /**
     * Validates hybrid school constraints.
     *
     * @return true if both parent schools are present and different, id non-empty, display name
     *         non-empty, and modifiers in reasonable ranges
     */
    public boolean isValid() {
        boolean parentsDifferent = !parent1.equals(parent2);
        boolean idsNonEmpty =
                id != null && !id.isEmpty() && displayName != null && !displayName.isEmpty();
        boolean proficiencyValid = minProficiencyPerParent >= 0 && minProficiencyPerParent <= 100;
        boolean multipliersValid = damageMultiplier >= 0.5f && damageMultiplier <= 2.0f
                && manaCostMultiplier >= 0.5f && manaCostMultiplier <= 1.5f
                && cooldownMultiplier >= 0.5f && cooldownMultiplier <= 1.5f;

        return parentsDifferent && idsNonEmpty && proficiencyValid && multipliersValid;
    }

    /**
     * Gets the translation key for this hybrid school's display name.
     *
     * @return key in format "school.mam.{id}"
     */
    public String getTranslationKey() {
        return "school.mam." + id;
    }

    /**
     * Gets all parent schools as a list.
     *
     * @return list containing parent1 and parent2
     */
    public List<SpellSchool> getParentSchools() {
        return List.of(parent1, parent2);
    }

    /**
     * Checks if player has minimum proficiency in both parent schools.
     *
     * <p>
     * Used to determine if player can unlock this hybrid school.
     *
     * @param proficiency1 proficiency in parent1 school (0-100)
     * @param proficiency2 proficiency in parent2 school (0-100)
     * @return true if both proficiencies >= minProficiencyPerParent
     */
    public boolean meetsRequirements(int proficiency1, int proficiency2) {
        return proficiency1 >= minProficiencyPerParent && proficiency2 >= minProficiencyPerParent;
    }

    /**
     * Creates a hybrid school with auto-validated modifiers.
     *
     * <p>
     * Combines parent school modifiers:
     * <ul>
     * <li>Damage: average of parent1 and parent2 damage multipliers
     * <li>Mana: average of parent1 and parent2 mana multipliers
     * <li>Cooldown: average of parent1 and parent2 cooldown multipliers
     * </ul>
     *
     * @param id unique identifier
     * @param displayName display name
     * @param parent1 first parent school
     * @param parent2 second parent school
     * @param minProficiency minimum proficiency in each parent
     * @return validated hybrid school with averaged modifiers
     */
    public static HybridSchool create(String id, String displayName, SpellSchool parent1,
            SpellSchool parent2, int minProficiency) {

        // Clamp proficiency to valid range
        int clampedProf = Math.max(0, Math.min(100, minProficiency));

        // Blend parent modifiers
        float avgDamage = (parent1.getDamageMultiplier() + parent2.getDamageMultiplier()) / 2.0f;
        float avgMana = (parent1.getManaCostMultiplier() + parent2.getManaCostMultiplier()) / 2.0f;
        float avgCooldown =
                (parent1.getCooldownMultiplier() + parent2.getCooldownMultiplier()) / 2.0f;

        return new HybridSchool(id, displayName, parent1, parent2, clampedProf, avgDamage, avgMana,
                avgCooldown);
    }

    /**
     * Compares two hybrid schools by display name alphabetically.
     *
     * @return comparator ordering by display name A-Z
     */
    public static Comparator<HybridSchool> byDisplayName() {
        return Comparator.comparing(HybridSchool::displayName);
    }

    @Override
    public String toString() {
        return String.format(
                "HybridSchool{id=%s, name=%s, parents=%s+%s, minProf=%d, dmg=%.2fx, mana=%.2fx, cd=%.2fx}",
                id, displayName, parent1, parent2, minProficiencyPerParent, damageMultiplier,
                manaCostMultiplier, cooldownMultiplier);
    }
}
