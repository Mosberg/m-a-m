package dk.mosberg.mana;

import java.util.Objects;
import dk.mosberg.spell.SpellCooldownTracker;
import net.minecraft.nbt.NbtCompound;

/**
 * Combines mana and cooldown tracking into a single player attachment. This is the main data holder
 * for all casting-related mechanics.
 *
 * TODO: Add casting state tracking (idle, channeling, casting, cooldown phases) TODO: Implement
 * interrupt mechanics (damage/movement breaks channeling) TODO: Add concentration system (tracking
 * focus level for spell accuracy/power) TODO: Implement casting speed modifiers (haste/slowness
 * effects) TODO: Add backfire/critical failure mechanics for low concentration TODO: Implement
 * combo system tracking (consecutive similar spells) TODO: Add rhythm-based casting (timed button
 * presses for bonuses) TODO: Implement spell memory system (memorized spells for quick access)
 * TODO: Add fatigue system (too much casting reduces effectiveness) TODO: Implement synergy
 * tracking (combining elements with other players)
 */
public class PlayerCastingData {
    private final PlayerManaData manaData;
    private final SpellCooldownTracker cooldownTracker;

    public PlayerCastingData() {
        this.manaData = new PlayerManaData();
        this.cooldownTracker = new SpellCooldownTracker();
    }

    public PlayerManaData getManaData() {
        return Objects.requireNonNull(manaData, "Mana data should never be null");
    }

    public SpellCooldownTracker getCooldownTracker() {
        return Objects.requireNonNull(cooldownTracker, "Cooldown tracker should never be null");
    }

    /**
     * Advances both mana regen and cooldown timers by the given delta time (in seconds). Called
     * once per server tick (20 ticks per second = 0.05 seconds per call).
     */
    public void tick() {
        manaData.tickRegeneration();
        cooldownTracker.tick(0.05f); // 1 tick = 0.05 seconds (20 ticks per second)
    }

    /**
     * Serializes all casting data to NBT.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        manaData.writeNbt(nbt);
        cooldownTracker.writeNbt(nbt);
        return nbt;
    }

    /**
     * Deserializes all casting data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        manaData.readNbt(nbt);
        cooldownTracker.readNbt(nbt);
    }
}
