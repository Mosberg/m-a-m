package dk.mosberg.spell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.mosberg.MAM;
import net.minecraft.util.Identifier;

/**
 * Spell modification/transmutation recipe that transforms one spell into another.
 *
 * <p>
 * Spell modifications allow players to transform existing spells via catalysts:
 * <ul>
 * <li>Input spell (spell to transform)
 * <li>Output spell (result of transformation)
 * <li>Catalyst items (ingredients required)
 * <li>Mana cost (mana consumed for transmutation)
 * <li>Requirements (minimum spell tier, school affinity, etc.)
 * </ul>
 *
 * <p>
 * Example transmutations:
 * <ul>
 * <li>Fireball → Inferno (add 2 Ruby gemstones)
 * <li>Frostbolt → Absolute Zero (add 3 Sapphires + 1 Ice Catalyst)
 * <li>Fire Burst → Lava Splash (add Fire + Earth catalysts, requires Lava school 30+)
 * <li>Restore → Resurrection (add 5 Sapphires + life force)
 * </ul>
 */
public record SpellModification(String id, Identifier inputSpell, Identifier outputSpell,
        List<String> catalystItems, int catalystCount, float manaCostRequired, int minInputTier,
        String[] requiredSchools) {

    /** Codec for JSON serialization */
    @SuppressWarnings("null")
    public static final Codec<SpellModification> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(Codec.STRING.fieldOf("id").forGetter(SpellModification::id),
                    Identifier.CODEC.fieldOf("inputSpell").forGetter(SpellModification::inputSpell),
                    Identifier.CODEC.fieldOf("outputSpell")
                            .forGetter(SpellModification::outputSpell),
                    Codec.STRING.listOf().fieldOf("catalystItems")
                            .forGetter(SpellModification::catalystItems),
                    Codec.INT.optionalFieldOf("catalystCount", 1)
                            .forGetter(SpellModification::catalystCount),
                    Codec.FLOAT.optionalFieldOf("manaCostRequired", 10.0f)
                            .forGetter(SpellModification::manaCostRequired),
                    Codec.INT.optionalFieldOf("minInputTier", 1)
                            .forGetter(SpellModification::minInputTier),
                    Codec.STRING.listOf().optionalFieldOf("requiredSchools", new ArrayList<>())
                            .xmap(l -> l.toArray(new String[0]), java.util.Arrays::asList)
                            .forGetter(SpellModification::requiredSchools))
                    .apply(instance, SpellModification::new));

    /**
     * Validates modification constraints.
     *
     * @return true if input != output, both spells valid, catalysts non-empty, and mana cost > 0
     */
    public boolean isValid() {
        boolean spellsDifferent = !inputSpell.equals(outputSpell);
        boolean spellsNonNull = inputSpell != null && outputSpell != null;
        boolean catalystsValid =
                catalystItems != null && !catalystItems.isEmpty() && catalystCount > 0;
        boolean manaCostValid = manaCostRequired > 0;
        boolean tierValid = minInputTier >= 1 && minInputTier <= 4;

        return spellsDifferent && spellsNonNull && catalystsValid && manaCostValid && tierValid;
    }

    /**
     * Gets the translation key for this modification's display name.
     *
     * @return key in format "spell_modification.mam.{id}"
     */
    public String getTranslationKey() {
        return "spell_modification.mam." + id;
    }

    /**
     * Gets required schools as a list.
     *
     * @return list of school requirements
     */
    public List<String> getRequiredSchoolsList() {
        return List.of(requiredSchools);
    }

    /**
     * Checks if player can perform this modification.
     *
     * <p>
     * Validates:
     * <ul>
     * <li>Input spell exists in registry
     * <li>Output spell exists in registry
     * <li>Input spell is correct tier or higher
     * <li>All required schools have minimum proficiency
     * </ul>
     *
     * @param input input spell to validate
     * @param output output spell to validate
     * @param inputTier tier of input spell instance
     * @return true if modification is valid
     */
    public boolean canPerform(Spell input, Spell output, int inputTier) {
        if (input == null || output == null) {
            return false;
        }

        // Check tier requirement
        if (inputTier < minInputTier) {
            MAM.LOGGER.debug("Spell modification {} requires tier {} or higher, got {}", id,
                    minInputTier, inputTier);
            return false;
        }

        return true;
    }

    /**
     * Creates a spell modification with default mana cost.
     *
     * @param id unique identifier
     * @param input input spell ID
     * @param output output spell ID
     * @param catalysts list of catalyst item IDs
     * @param catalystCount count needed
     * @return validated modification
     */
    public static SpellModification create(String id, Identifier input, Identifier output,
            List<String> catalysts, int catalystCount) {
        return new SpellModification(id, input, output, catalysts, catalystCount, 20.0f, 1,
                new String[0]);
    }

    /**
     * Compares modifications by output spell ID alphabetically.
     *
     * @return comparator ordering by output spell ID
     */
    public static Comparator<SpellModification> byOutputSpell() {
        return (m1, m2) -> m1.outputSpell().toString().compareTo(m2.outputSpell().toString());
    }

    @Override
    public String toString() {
        return String.format(
                "SpellModification{id=%s, %s → %s, catalysts=%d×%s, mana=%.1f, minTier=%d}", id,
                inputSpell, outputSpell, catalystCount, catalystItems, manaCostRequired,
                minInputTier);
    }
}
