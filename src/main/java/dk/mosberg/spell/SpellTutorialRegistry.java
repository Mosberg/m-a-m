package dk.mosberg.spell;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;
import net.minecraft.util.Identifier;

/**
 * Registry for spell tutorials/guidance information.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Register spell tutorials
 * <li>Lookup tutorials by spell ID
 * <li>Find tutorials by difficulty range
 * <li>List all tutorials or unlocked tutorials
 * <li>Find related spells from tutorial data
 * </ul>
 *
 * <p>
 * Tutorials are optional - spells work fine without them, but provide helpful guidance.
 */
public class SpellTutorialRegistry {

    /** Map of spell ID -> SpellTutorial */
    private static final Map<Identifier, SpellTutorial> TUTORIALS = new HashMap<>();

    /**
     * Registers a spell tutorial.
     *
     * <p>
     * Validates the tutorial and rejects invalid entries:
     * <ul>
     * <li>Spell ID must be non-null
     * <li>Description must be non-empty
     * <li>Difficulty must be 1-5
     * </ul>
     *
     * @param tutorial tutorial to register
     * @return true if registered successfully, false if validation failed
     */
    public static boolean register(SpellTutorial tutorial) {
        // Validate tutorial
        if (!tutorial.isValid()) {
            MAM.LOGGER.warn("Cannot register invalid spell tutorial: {}", tutorial);
            return false;
        }

        // Check for duplicate spell
        if (TUTORIALS.containsKey(tutorial.spellId())) {
            MAM.LOGGER.warn("Tutorial for spell '{}' already registered", tutorial.spellId());
            return false;
        }

        // Store tutorial
        TUTORIALS.put(tutorial.spellId(), tutorial);

        MAM.LOGGER.info("Registered tutorial for spell '{}'", tutorial.spellId());
        return true;
    }

    /**
     * Gets the tutorial for a specific spell.
     *
     * @param spellId spell identifier
     * @return Optional containing tutorial if registered
     */
    public static Optional<SpellTutorial> getTutorial(Identifier spellId) {
        return Optional.ofNullable(TUTORIALS.get(spellId));
    }

    /**
     * Gets all tutorials in a difficulty range (inclusive).
     *
     * <p>
     * Example: getTutorialsByDifficulty(1, 2) → Beginner-friendly tutorials
     *
     * @param minDifficulty minimum difficulty (1-5)
     * @param maxDifficulty maximum difficulty (1-5)
     * @return list of tutorials sorted by difficulty
     */
    public static List<SpellTutorial> getTutorialsByDifficulty(int minDifficulty,
            int maxDifficulty) {
        return TUTORIALS.values().stream().filter(
                t -> t.difficultyRating() >= minDifficulty && t.difficultyRating() <= maxDifficulty)
                .sorted(Comparator.comparingInt(SpellTutorial::difficultyRating)).toList();
    }

    /**
     * Gets tutorials that must be unlocked first before a given spell.
     *
     * <p>
     * Returns tutorials for spells related to the given spell (from related spell list).
     *
     * @param spellId spell to find prerequisites for
     * @return list of tutorials for related/prerequisite spells
     */
    public static List<SpellTutorial> getRelatedTutorials(Identifier spellId) {
        Optional<SpellTutorial> tutorial = getTutorial(spellId);
        if (tutorial.isEmpty()) {
            return List.of();
        }

        return tutorial.get().relatedSpells().stream()
                .flatMap(related -> getTutorial(related).stream()).toList();
    }

    /**
     * Gets all tutorials available to a player based on unlock requirements.
     *
     * <p>
     * Filters tutorials where player meets all unlock requirements.
     *
     * @param playerLevel player level
     * @param playerSchoolProficiency proficiency in relevant school (0-100)
     * @param playerItems set of item IDs player has (simplified)
     * @return list of unlocked tutorials
     */
    public static List<SpellTutorial> getUnlockedTutorials(int playerLevel,
            int playerSchoolProficiency, java.util.Set<String> playerItems) {
        return TUTORIALS.values().stream()
                .filter(t -> t.meetsUnlockRequirements(playerLevel, playerSchoolProficiency,
                        t.unlockRequirements().requiredItem().map(playerItems::contains)
                                .orElse(true)))
                .sorted(Comparator.comparingInt(SpellTutorial::difficultyRating)).toList();
    }

    /**
     * Gets tips for casting a specific spell.
     *
     * <p>
     * Returns empty list if no tutorial registered.
     *
     * @param spellId spell identifier
     * @return list of tips, or empty list if none available
     */
    public static List<String> getTips(Identifier spellId) {
        return getTutorial(spellId).map(SpellTutorial::tips).orElse(List.of());
    }

    /**
     * Gets common mistakes for a spell.
     *
     * <p>
     * Returns empty list if no tutorial registered.
     *
     * @param spellId spell identifier
     * @return list of common mistakes, or empty list if none available
     */
    public static List<String> getCommonMistakes(Identifier spellId) {
        return getTutorial(spellId).map(SpellTutorial::commonMistakes).orElse(List.of());
    }

    /**
     * Lists all registered tutorials.
     *
     * @return list of all tutorials, sorted by difficulty
     */
    public static List<SpellTutorial> getAllTutorials() {
        return TUTORIALS.values().stream()
                .sorted(Comparator.comparingInt(SpellTutorial::difficultyRating)).toList();
    }

    /**
     * Gets count of registered tutorials.
     *
     * @return number of registered tutorials
     */
    public static int getTutorialCount() {
        return TUTORIALS.size();
    }

    /**
     * Checks if a spell has a tutorial registered.
     *
     * @param spellId spell identifier
     * @return true if tutorial exists
     */
    public static boolean hasTutorial(Identifier spellId) {
        return TUTORIALS.containsKey(spellId);
    }

    /**
     * Clears all registered tutorials (useful for reloading).
     */
    public static void clear() {
        TUTORIALS.clear();
        MAM.LOGGER.info("Cleared spell tutorial registry");
    }
}
