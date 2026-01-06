package dk.mosberg.spell;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

/**
 * Tutorial/guidance data for spells, providing help to new players.
 *
 * <p>
 * Each spell can have associated tutorial information:
 * <ul>
 * <li>Tutorial description (multiline tooltip)
 * <li>Unlock requirements (level, proficiency, items, etc.)
 * <li>Tips and strategies
 * <li>Related spells (progression chain, alternatives)
 * <li>Difficulty rating (1-5 stars)
 * <li>Common mistakes
 * </ul>
 *
 * <p>
 * Example tutorial for Fireball:
 *
 * <pre>
 * {
 *   "spellId": "mam:fireball",
 *   "description": "A classic fire spell that launches a burning projectile.",
 *   "difficultyRating": 2,
 *   "unlockRequirements": {
 *     "minLevel": 5,
 *     "minFireProficiency": 10,
 *     "requiredItem": "mam:spellbook_apprentice"
 *   },
 *   "tips": [
 *     "Best used at medium range",
 *     "Burns targets briefly for additional damage",
 *     "Can ignite flammable blocks"
 *   ],
 *   "relatedSpells": ["mam:fire_burst", "mam:inferno", "mam:flame_wave"],
 *   "commonMistakes": [
 *     "Using at close range (take splash damage)",
 *     "Not waiting for cooldown between casts"
 *   ]
 * }
 * </pre>
 */
public record SpellTutorial(Identifier spellId, String description, int difficultyRating,
        UnlockRequirements unlockRequirements, List<String> tips, List<Identifier> relatedSpells,
        List<String> commonMistakes) {

    /**
     * Unlock requirements for a spell tutorial.
     *
     * @param minLevel minimum player level
     * @param minSchoolProficiency minimum proficiency in spell school (0-100)
     * @param requiredItem optional required item ID
     */
    public record UnlockRequirements(int minLevel, int minSchoolProficiency,
            Optional<String> requiredItem) {

        /** Codec for UnlockRequirements */
        @SuppressWarnings("null")
        public static final Codec<UnlockRequirements> CODEC =
                RecordCodecBuilder.create(instance -> instance
                        .group(Codec.INT.optionalFieldOf("minLevel", 0)
                                .forGetter(UnlockRequirements::minLevel),
                                Codec.INT.optionalFieldOf("minSchoolProficiency", 0)
                                        .forGetter(UnlockRequirements::minSchoolProficiency),
                                Codec.STRING.optionalFieldOf("requiredItem")
                                        .forGetter(UnlockRequirements::requiredItem))
                        .apply(instance, UnlockRequirements::new));
    }

    /** Codec for SpellTutorial */
    @SuppressWarnings("null")
    public static final Codec<SpellTutorial> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.fieldOf("spellId").forGetter(SpellTutorial::spellId),
                    Codec.STRING.fieldOf("description").forGetter(SpellTutorial::description),
                    Codec.INT.optionalFieldOf("difficultyRating", 3)
                            .forGetter(SpellTutorial::difficultyRating),
                    UnlockRequirements.CODEC
                            .optionalFieldOf("unlockRequirements",
                                    new UnlockRequirements(0, 0, Optional.empty()))
                            .forGetter(SpellTutorial::unlockRequirements),
                    Codec.STRING.listOf().optionalFieldOf("tips", List.of())
                            .forGetter(SpellTutorial::tips),
                    Identifier.CODEC.listOf().optionalFieldOf("relatedSpells", List.of())
                            .forGetter(SpellTutorial::relatedSpells),
                    Codec.STRING.listOf().optionalFieldOf("commonMistakes", List.of())
                            .forGetter(SpellTutorial::commonMistakes))
            .apply(instance, SpellTutorial::new));

    /**
     * Validates tutorial constraints.
     *
     * @return true if spell ID non-null, description non-empty, difficulty 1-5
     */
    public boolean isValid() {
        boolean spellValid = spellId != null;
        boolean descriptionValid = description != null && !description.isEmpty();
        boolean difficultyValid = difficultyRating >= 1 && difficultyRating <= 5;

        return spellValid && descriptionValid && difficultyValid;
    }

    /**
     * Gets difficulty as star representation.
     *
     * @return string of ★ characters (e.g., "★★★" for difficulty 3)
     */
    public String getDifficultyStars() {
        return "★".repeat(difficultyRating);
    }

    /**
     * Gets the translation key for this tutorial's description.
     *
     * @return key in format "tutorial.mam.{spell_id}.description"
     */
    public String getDescriptionKey() {
        return "tutorial.mam." + spellId.getPath() + ".description";
    }

    /**
     * Checks if player meets unlock requirements.
     *
     * <p>
     * Note: Actual level/proficiency checking happens in calling code.
     *
     * @param playerLevel player level
     * @param playerSchoolProficiency player proficiency in spell school
     * @param hasRequiredItem true if player has required item
     * @return true if all requirements met
     */
    public boolean meetsUnlockRequirements(int playerLevel, int playerSchoolProficiency,
            boolean hasRequiredItem) {
        // Check level requirement
        if (playerLevel < unlockRequirements.minLevel) {
            return false;
        }

        // Check proficiency requirement
        if (playerSchoolProficiency < unlockRequirements.minSchoolProficiency) {
            return false;
        }

        // Check item requirement (only if specified)
        if (unlockRequirements.requiredItem.isPresent() && !hasRequiredItem) {
            return false;
        }

        return true;
    }

    /**
     * Creates a basic spell tutorial with minimal setup.
     *
     * @param spellId spell identifier
     * @param description spell description
     * @param difficulty difficulty rating (1-5)
     * @return tutorial with empty tips/related/mistakes
     */
    public static SpellTutorial create(Identifier spellId, String description, int difficulty) {
        int clampedDifficulty = Math.max(1, Math.min(5, difficulty));
        return new SpellTutorial(spellId, description, clampedDifficulty,
                new UnlockRequirements(0, 0, Optional.empty()), List.of(), List.of(), List.of());
    }

    @Override
    public String toString() {
        return String.format(
                "SpellTutorial{spell=%s, difficulty=%s, tips=%d, related=%d, mistakes=%d}", spellId,
                getDifficultyStars(), tips.size(), relatedSpells.size(), commonMistakes.size());
    }
}
