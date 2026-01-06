package dk.mosberg.mana;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import dk.mosberg.MAM;

/**
 * Registry for managing pool links: shared mana pools for teams, guilds, or cooperative groups.
 *
 * Maintains a central registry of all active pool links and provides methods to create, remove, and
 * query linked pools.
 *
 * Thread-safe operations for concurrent access from game threads and network handlers.
 */
public class PoolLinkRegistry {

    // Map from link ID (team/guild ID) to the shared pool
    private static final Map<String, PoolLink> POOL_LINKS = new HashMap<>();

    private PoolLinkRegistry() {}

    /**
     * Create a new pool link for a team/guild.
     *
     * @param linkId Unique identifier for the pool link (e.g., guild ID)
     * @param poolType The type of mana pool shared by all members
     * @param initialMembers Initial list of member UUIDs
     * @return The created pool link
     * @throws IllegalArgumentException if link ID already exists
     */
    public static PoolLink createLink(String linkId, ManaPoolType poolType,
            java.util.List<UUID> initialMembers) {
        if (POOL_LINKS.containsKey(linkId)) {
            throw new IllegalArgumentException("Pool link with ID '" + linkId + "' already exists");
        }

        PoolLink link = new PoolLink(linkId, poolType, initialMembers);
        POOL_LINKS.put(linkId, link);
        MAM.LOGGER.info("Created pool link '{}' for {} member(s)", linkId, initialMembers.size());

        return link;
    }

    /**
     * Remove a pool link from the registry. Called when a team/guild is dissolved.
     *
     * @param linkId The link ID to remove
     * @return true if removed, false if link didn't exist
     */
    public static boolean removeLink(String linkId) {
        boolean removed = POOL_LINKS.remove(linkId) != null;
        if (removed) {
            MAM.LOGGER.info("Removed pool link '{}'", linkId);
        }
        return removed;
    }

    /**
     * Get a pool link by ID.
     *
     * @param linkId The link ID
     * @return Optional containing the pool link if found
     */
    public static Optional<PoolLink> getLink(String linkId) {
        return Optional.ofNullable(POOL_LINKS.get(linkId));
    }

    /**
     * Get all pool links a player is a member of.
     *
     * @param playerId The player's UUID
     * @return Collection of all pool links the player is in
     */
    public static java.util.List<PoolLink> getPlayerLinks(UUID playerId) {
        return POOL_LINKS.values().stream()
                .filter(link -> link.getLinkedMembers().contains(playerId)).toList();
    }

    /**
     * Add a player to a pool link (e.g., when they join a team).
     *
     * @param linkId The link ID
     * @param playerId The player's UUID
     * @return true if added, false if link doesn't exist
     */
    public static boolean addPlayerToLink(String linkId, UUID playerId) {
        PoolLink link = POOL_LINKS.get(linkId);
        if (link == null) {
            return false;
        }

        link.addMember(playerId);
        MAM.LOGGER.debug("Added player {} to pool link '{}'", playerId, linkId);
        return true;
    }

    /**
     * Remove a player from a pool link (e.g., when they leave a team).
     *
     * @param linkId The link ID
     * @param playerId The player's UUID
     * @return true if removed, false if link doesn't exist
     */
    public static boolean removePlayerFromLink(String linkId, UUID playerId) {
        PoolLink link = POOL_LINKS.get(linkId);
        if (link == null) {
            return false;
        }

        link.removeMember(playerId);
        MAM.LOGGER.debug("Removed player {} from pool link '{}'", playerId, linkId);
        return true;
    }

    /**
     * Update regen for all pool links. Called on each game tick.
     */
    public static void tickAll() {
        for (PoolLink link : POOL_LINKS.values()) {
            link.updateRegen();
        }
    }

    /**
     * Get all active pool links.
     */
    public static Collection<PoolLink> getAllLinks() {
        return java.util.Collections.unmodifiableCollection(POOL_LINKS.values());
    }

    /**
     * Clear all pool links. Used during server shutdown or world reset.
     */
    public static void clear() {
        POOL_LINKS.clear();
        MAM.LOGGER.info("Cleared all pool links");
    }

    /**
     * Get count of active pool links.
     */
    public static int getLinkCount() {
        return POOL_LINKS.size();
    }
}
