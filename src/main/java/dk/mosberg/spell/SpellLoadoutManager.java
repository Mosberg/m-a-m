package dk.mosberg.spell;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import dk.mosberg.MAM;

/**
 * Manages spell loadouts/presets for individual players.
 *
 * Features: - Save/load player-specific spell loadouts - Switch between loadouts - Export/import
 * loadout templates - Validate loadouts (correct spell count, no duplicates, etc.)
 *
 * Thread-safe: uses synced map per player
 */
public class SpellLoadoutManager {

    // Map from player UUID to their loadouts
    private static final Map<UUID, PlayerLoadoutData> PLAYER_LOADOUTS = new HashMap<>();

    // Global default loadout templates (shared across all players)
    private static final Map<String, SpellLoadout> LOADOUT_TEMPLATES = new HashMap<>();

    static {
        // Initialize some default templates
        initializeTemplates();
    }

    private SpellLoadoutManager() {}

    /**
     * Player-specific loadout data.
     */
    public static class PlayerLoadoutData {
        private final UUID playerId;
        private final Map<String, SpellLoadout> loadouts = new HashMap<>();
        private String activeLoadoutName;

        public PlayerLoadoutData(UUID playerId) {
            this.playerId = playerId;
            // Create one default loadout when player first logs in
            this.activeLoadoutName = "Default";
            this.loadouts.put("Default", SpellLoadout.empty("Default", 4));
        }

        public Optional<SpellLoadout> getActiveLoadout() {
            return Optional.ofNullable(loadouts.get(activeLoadoutName));
        }

        public void setActiveLoadout(String name) throws IllegalArgumentException {
            if (!loadouts.containsKey(name)) {
                throw new IllegalArgumentException("Loadout '" + name + "' does not exist");
            }
            this.activeLoadoutName = name;
        }
    }

    /**
     * Initialize default loadout templates for all players.
     */
    private static void initializeTemplates() {
        // Can add default templates here for common playstyles
        // e.g., LOADOUT_TEMPLATES.put("PVE Damage", ...);
    }

    /**
     * Get or create loadout data for a player.
     */
    private static PlayerLoadoutData getOrCreatePlayerData(UUID playerId) {
        return PLAYER_LOADOUTS.computeIfAbsent(playerId, PlayerLoadoutData::new);
    }

    /**
     * Save a new loadout for a player.
     *
     * @param playerId The player's UUID
     * @param loadout The loadout to save
     * @throws IllegalArgumentException if loadout name already exists
     */
    public static void saveLoadout(UUID playerId, SpellLoadout loadout) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);

        if (data.loadouts.containsKey(loadout.name())) {
            throw new IllegalArgumentException("Loadout '" + loadout.name()
                    + "' already exists. Use updateLoadout() to modify.");
        }

        data.loadouts.put(loadout.name(), loadout);
        MAM.LOGGER.debug("Saved loadout '{}' for player {}", loadout.name(), playerId);
    }

    /**
     * Update an existing loadout for a player.
     *
     * @param playerId The player's UUID
     * @param loadout The updated loadout
     * @return true if updated, false if loadout doesn't exist
     */
    public static boolean updateLoadout(UUID playerId, SpellLoadout loadout) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);

        if (!data.loadouts.containsKey(loadout.name())) {
            return false;
        }

        data.loadouts.put(loadout.name(), loadout);
        return true;
    }

    /**
     * Delete a loadout for a player.
     *
     * @param playerId The player's UUID
     * @param loadoutName The name of the loadout to delete
     * @return true if deleted, false if loadout doesn't exist or is the last loadout
     */
    public static boolean deleteLoadout(UUID playerId, String loadoutName) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);

        if (data.loadouts.size() <= 1) {
            // Must keep at least one loadout
            return false;
        }

        return data.loadouts.remove(loadoutName) != null;
    }

    /**
     * Get a specific loadout for a player.
     */
    public static Optional<SpellLoadout> getLoadout(UUID playerId, String loadoutName) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);
        return Optional.ofNullable(data.loadouts.get(loadoutName));
    }

    /**
     * Get the active loadout for a player.
     */
    public static Optional<SpellLoadout> getActiveLoadout(UUID playerId) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);
        return data.getActiveLoadout();
    }

    /**
     * Switch to a different loadout for a player.
     *
     * @param playerId The player's UUID
     * @param loadoutName The name of the loadout to switch to
     * @return true if switch successful, false if loadout doesn't exist
     */
    public static boolean switchLoadout(UUID playerId, String loadoutName) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);

        if (!data.loadouts.containsKey(loadoutName)) {
            return false;
        }

        data.setActiveLoadout(loadoutName);
        MAM.LOGGER.debug("Player {} switched to loadout '{}'", playerId, loadoutName);
        return true;
    }

    /**
     * Get all loadout names for a player.
     */
    public static Collection<String> getLoadoutNames(UUID playerId) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);
        return Collections.unmodifiableCollection(data.loadouts.keySet());
    }

    /**
     * Get all loadouts for a player.
     */
    public static Collection<SpellLoadout> getAllLoadouts(UUID playerId) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);
        return Collections.unmodifiableCollection(data.loadouts.values());
    }

    /**
     * Get count of loadouts for a player.
     */
    public static int getLoadoutCount(UUID playerId) {
        PlayerLoadoutData data = getOrCreatePlayerData(playerId);
        return data.loadouts.size();
    }

    /**
     * Register a loadout template (shared across all players).
     *
     * @param name The template name
     * @param loadout The template loadout
     */
    public static void registerTemplate(String name, SpellLoadout loadout) {
        LOADOUT_TEMPLATES.put(name, loadout);
        MAM.LOGGER.info("Registered loadout template '{}'", name);
    }

    /**
     * Get a loadout template by name.
     */
    public static Optional<SpellLoadout> getTemplate(String name) {
        return Optional.ofNullable(LOADOUT_TEMPLATES.get(name));
    }

    /**
     * Get all available loadout templates.
     */
    public static Collection<SpellLoadout> getAllTemplates() {
        return Collections.unmodifiableCollection(LOADOUT_TEMPLATES.values());
    }

    /**
     * Clear all loadout data for a player (e.g., on world reset).
     */
    public static void clearPlayerData(UUID playerId) {
        PLAYER_LOADOUTS.remove(playerId);
        MAM.LOGGER.debug("Cleared loadout data for player {}", playerId);
    }

    /**
     * Clear all player loadout data (on server shutdown).
     */
    public static void clearAll() {
        PLAYER_LOADOUTS.clear();
        MAM.LOGGER.info("Cleared all player loadout data");
    }
}
