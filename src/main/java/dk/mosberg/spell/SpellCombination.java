package dk.mosberg.spell;

import java.util.List;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

/**
 * Represents a spell combination/fusion recipe: combining multiple spells creates a new enhanced
 * spell.
 *
 * Example: Combining Fire Strike + Air Strike creates Plasma Strike (hybrid spell with both
 * schools' benefits).
 *
 * Features: - Multiple input spells (typically 2-4) - Single output spell (fused result) - Optional
 * catalysts (items/conditions required) - Cost scaling (mana/resources to perform fusion)
 *
 * Usage: - Players learn combination recipes from progression/unlocks - Casting input spells in
 * sequence automatically triggers fusion if conditions met - Fused spells are more powerful but
 * have longer cooldowns
 */
public record SpellCombination(Identifier id, String name, List<Identifier> inputSpells,
        Identifier outputSpell, int catalystCount, float fusionCooldownBonus,
        float fusionDamageBonus) {

    /**
     * Validate the combination recipe.
     */
    public boolean isValid() {
        // Must have 2-4 input spells
        if (inputSpells.size() < 2 || inputSpells.size() > 4) {
            return false;
        }

        // Output spell must be defined
        if (outputSpell == null) {
            return false;
        }

        // Bonuses must be reasonable
        if (fusionCooldownBonus < 0.5f || fusionCooldownBonus > 3.0f) {
            return false;
        }

        if (fusionDamageBonus < 0.5f || fusionDamageBonus > 3.0f) {
            return false;
        }

        return true;
    }

    /**
     * Get descriptive name for the combination (e.g., "Fire + Water Fusion").
     */
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : id.getPath();
    }

    /**
     * Check if a sequence of cast spells matches this combination recipe.
     *
     * @param recentCasts List of recently cast spell IDs (in order)
     * @return true if the recent casts match this combination's input spells in order
     */
    public boolean matches(List<Identifier> recentCasts) {
        if (recentCasts.size() < inputSpells.size()) {
            return false;
        }

        // Check if the last N casts match input spells (in order, allowing other casts between)
        int inputIdx = 0;
        for (int i = recentCasts.size() - 1; i >= 0 && inputIdx < inputSpells.size(); i--) {
            if (recentCasts.get(i).equals(inputSpells.get(inputSpells.size() - 1 - inputIdx))) {
                inputIdx++;
            }
        }

        return inputIdx == inputSpells.size();
    }

    /**
     * Codec for JSON serialization/deserialization.
     */
    @SuppressWarnings("null")
    public static final Codec<SpellCombination> CODEC =
            RecordCodecBuilder.create(instance -> instance
                    .group(Identifier.CODEC.fieldOf("id").forGetter(SpellCombination::id),
                            Codec.STRING.optionalFieldOf("name", "")
                                    .forGetter(SpellCombination::name),
                            Identifier.CODEC.listOf().fieldOf("input_spells")
                                    .forGetter(SpellCombination::inputSpells),
                            Identifier.CODEC.fieldOf("output_spell")
                                    .forGetter(SpellCombination::outputSpell),
                            Codec.INT.optionalFieldOf("catalyst_count", 0)
                                    .forGetter(SpellCombination::catalystCount),
                            Codec.FLOAT.optionalFieldOf("fusion_cooldown_bonus", 1.5f)
                                    .forGetter(SpellCombination::fusionCooldownBonus),
                            Codec.FLOAT.optionalFieldOf("fusion_damage_bonus", 1.3f)
                                    .forGetter(SpellCombination::fusionDamageBonus))
                    .apply(instance, SpellCombination::new));
}
