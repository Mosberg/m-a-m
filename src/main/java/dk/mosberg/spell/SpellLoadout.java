package dk.mosberg.spell;

import java.util.List;
import net.minecraft.util.Identifier;

/**
 * Represents a spell loadout/preset: a saved configuration of equipped spells.
 *
 * Allows players to: - Save favorite spell combinations as loadouts - Quickly switch between
 * loadouts for different playstyles - Share loadout templates with other players
 *
 * Example loadouts: - "PVE Offense": Fire Strike, Flame Wave, Inferno, Heal - "PVP Defense": Earth
 * Wall, Water Shield, Gust, Refresh - "Support": Heal, Regeneration, Fortify, Buff Aura
 */
public record SpellLoadout(String name, String description, List<Identifier> equippedSpells,
        int slotCount) {

    public SpellLoadout {
        // Validate loadout
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Loadout name cannot be empty");
        }
        if (equippedSpells == null) {
            throw new IllegalArgumentException("Equipped spells list cannot be null");
        }
        if (slotCount < 1 || slotCount > 12) {
            throw new IllegalArgumentException("Slot count must be between 1 and 12");
        }
        if (equippedSpells.size() > slotCount) {
            throw new IllegalArgumentException("Cannot equip more spells than available slots");
        }
    }

    /**
     * Create a new empty loadout with the given name and slot count.
     */
    public static SpellLoadout empty(String name, int slotCount) {
        return new SpellLoadout(name, "", List.of(), slotCount);
    }

    /**
     * Check if a loadout is complete (all slots filled).
     */
    public boolean isFull() {
        return equippedSpells.size() == slotCount;
    }

    /**
     * Get the number of empty slots in this loadout.
     */
    public int getEmptySlots() {
        return slotCount - equippedSpells.size();
    }

    /**
     * Create a copy of this loadout with different spells.
     */
    public SpellLoadout withSpells(List<Identifier> spells) {
        return new SpellLoadout(name, description, spells, slotCount);
    }

    /**
     * Create a copy of this loadout with a new name.
     */
    public SpellLoadout withName(String newName) {
        return new SpellLoadout(newName, description, equippedSpells, slotCount);
    }

    /**
     * Get the "weight" of the loadout based on spell rarity/power.
     *
     * Used for balance checking: higher weight = more powerful spells. Can be used to limit loadout
     * power for competitive play.
     */
    public float getWeight() {
        // This is a placeholder; in practice, you'd look up spell stats
        return equippedSpells.size() * 1.0f;
    }

    /**
     * Check if two loadouts are compatible (have the same slot count).
     */
    public boolean isCompatibleWith(SpellLoadout other) {
        return this.slotCount == other.slotCount();
    }

    /**
     * Check if this loadout contains a specific spell.
     */
    public boolean containsSpell(Identifier spellId) {
        return equippedSpells.contains(spellId);
    }
}
