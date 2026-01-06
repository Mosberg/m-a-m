package dk.mosberg.mana;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a linked mana pool shared between multiple players in a team or guild.
 *
 * Features: - Shared capacity: all players contribute to same pool, share from same pool -
 * Distributed regen: regen distributed based on num members or balanced equally - Linked members:
 * UUIDs of all players in the team
 *
 * Example usage: Guild members cast spells that all draw from guild mana pool instead of personal
 * pools
 */
public class PoolLink {

    private final String linkId; // Team/guild identifier
    private final ManaPoolType poolType;
    private final Set<UUID> linkedMembers; // UUIDs of linked players

    private float sharedCapacity;
    private float sharedMana;

    private float sharedRegenRate;

    public PoolLink(String linkId, ManaPoolType poolType, List<UUID> initialMembers) {
        this.linkId = linkId;
        this.poolType = poolType;
        this.linkedMembers = new HashSet<>(initialMembers);

        // Calculate shared capacity as sum of member capacities
        this.sharedCapacity = poolType.getDefaultCapacity() * linkedMembers.size();
        this.sharedMana = 0.0f; // Start empty

        // Shared regen = base rate * pool capacity for balance
        this.sharedRegenRate = poolType.getDefaultRegenRate();
    }

    /**
     * Add a player to the linked pool. Increases capacity based on their individual allocation.
     */
    public void addMember(UUID memberId) {
        if (linkedMembers.add(memberId)) {
            // Increase shared capacity when new member joins
            sharedCapacity += poolType.getDefaultCapacity();
        }
    }

    /**
     * Remove a player from the linked pool. Decreases capacity and redistributes remaining mana.
     */
    public void removeMember(UUID memberId) {
        if (linkedMembers.remove(memberId)) {
            // Decrease capacity and cap current mana
            sharedCapacity -= poolType.getDefaultCapacity();
            if (sharedMana > sharedCapacity) {
                sharedMana = sharedCapacity;
            }
        }
    }

    /**
     * Regenerate shared mana. Called each tick. Distributes regen equally to pool.
     *
     * Formula: regenAmount = baseRegenRate * poolCapacity / numMembers This ensures consistent
     * regen scaling regardless of group size.
     */
    public void updateRegen() {
        if (linkedMembers.isEmpty())
            return;

        // Regen is distributed: each member contributes equal regen
        float regenThisTick = sharedRegenRate * (poolType.getDefaultCapacity() / 20.0f); // 20
                                                                                         // ticks/sec

        sharedMana = Math.min(sharedMana + regenThisTick, sharedCapacity);
    }

    /**
     * Consume mana from the shared pool. Returns amount actually consumed.
     *
     * @param amount Amount to consume
     * @return Actual amount consumed (capped by available mana)
     */
    public float consumeMana(float amount) {
        float consumed = Math.min(amount, sharedMana);
        sharedMana -= consumed;
        return consumed;
    }

    /**
     * Add mana to the shared pool (e.g., from regeneration or restoration items).
     */
    public void addMana(float amount) {
        sharedMana = Math.min(sharedMana + amount, sharedCapacity);
    }

    /**
     * Get the percentage of the shared pool filled (0-100).
     */
    public float getFilledPercentage() {
        if (sharedCapacity <= 0)
            return 0.0f;
        return (sharedMana / sharedCapacity) * 100.0f;
    }

    /**
     * Get effective damage modifier based on pool type and fill level. Damage scales with fill
     * percentage for high-risk/high-reward gameplay.
     */
    public float getDamageModifier() {
        float baseModifier = poolType.getDamageModifier();
        float fillPercent = getFilledPercentage();

        // At 0% filled: 0.5x damage. At 100% filled: 1.0x damage.
        float scaleBonus = (fillPercent / 100.0f) * 0.5f;
        return baseModifier * (0.5f + scaleBonus);
    }

    /**
     * Get number of members in the linked pool.
     */
    public int getMemberCount() {
        return linkedMembers.size();
    }

    /**
     * Save linked pool data to NBT for persistent storage.
     *
     * Note: NBT serialization will be implemented when integrating with PlayerManaData sync.
     */
    public void toNbt() {
        // TODO: Implement NBT serialization for persistent pool links
    }

    /**
     * Load linked pool data from NBT.
     *
     * Note: NBT deserialization will be implemented when integrating with PlayerManaData sync.
     */
    public static PoolLink fromNbt() {
        // TODO: Implement NBT deserialization for persistent pool links
        throw new UnsupportedOperationException("NBT deserialization not yet implemented");
    }

    // Getters
    public String getLinkId() {
        return linkId;
    }

    public ManaPoolType getPoolType() {
        return poolType;
    }

    public Set<UUID> getLinkedMembers() {
        return new HashSet<>(linkedMembers);
    }

    public float getSharedCapacity() {
        return sharedCapacity;
    }

    public float getSharedMana() {
        return sharedMana;
    }

    public float getSharedRegenRate() {
        return sharedRegenRate;
    }
}
