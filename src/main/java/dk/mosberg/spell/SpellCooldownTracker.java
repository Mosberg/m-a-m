package dk.mosberg.spell;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Tracks active spell cooldowns per player. Used to prevent spam casting. Stores spell ID ->
 * remaining cooldown time in seconds.
 *
 * TODO: Implement shared cooldown groups (spells on same cooldown) TODO: Add cooldown reduction
 * modifiers (from equipment/buffs) TODO: Implement cooldown stacking for repeated casts (increasing
 * penalties) TODO: Add cooldown reset mechanics (triggered by specific events) TODO: Implement
 * cooldown visualization (progress bar, particle effects) TODO: Add partial cooldown recovery
 * (faster recovery for certain schools) TODO: Implement cooldown immunity periods (for overpowered
 * spells) TODO: Add cooldown persistence across respawns (with reset option)
 */
public class SpellCooldownTracker {
    private final Map<Identifier, Float> cooldowns = new HashMap<>();

    /**
     * Advances all active cooldowns by the given amount of time (in seconds). Removes cooldowns
     * that have fully expired.
     */
    public void tick(float deltaTime) {
        cooldowns.replaceAll((spellId, remaining) -> remaining - deltaTime);
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    /**
     * Checks if a spell is currently on cooldown.
     */
    public boolean isOnCooldown(Identifier spellId) {
        return cooldowns.containsKey(spellId) && cooldowns.get(spellId) > 0;
    }

    /**
     * Gets the remaining cooldown time for a spell in seconds. Returns 0 if spell is not on
     * cooldown.
     */
    public float getRemainingCooldown(Identifier spellId) {
        return cooldowns.getOrDefault(spellId, 0f);
    }

    /**
     * Starts or resets a cooldown for a spell.
     */
    public void startCooldown(Identifier spellId, float cooldownDuration) {
        if (cooldownDuration > 0) {
            cooldowns.put(spellId, cooldownDuration);
        }
    }

    /**
     * Clears a spell's cooldown immediately.
     */
    public void clearCooldown(Identifier spellId) {
        cooldowns.remove(spellId);
    }

    /**
     * Clears all active cooldowns.
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Serializes cooldown data to NBT for persistence.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound cooldownsNbt = new NbtCompound();
        for (Map.Entry<Identifier, Float> entry : cooldowns.entrySet()) {
            cooldownsNbt.putFloat(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("cooldowns", cooldownsNbt);
        return nbt;
    }

    /**
     * Deserializes cooldown data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("cooldowns")) {
            var optionalCompound = nbt.getCompound("cooldowns");
            if (optionalCompound.isPresent()) {
                NbtCompound cooldownsNbt = optionalCompound.get();
                for (String key : cooldownsNbt.getKeys()) {
                    try {
                        Identifier spellId = Identifier.tryParse(key);
                        if (spellId != null) {
                            var optionalFloat = cooldownsNbt.getFloat(key);
                            if (optionalFloat.isPresent()) {
                                cooldowns.put(spellId, optionalFloat.get());
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid identifiers
                    }
                }
            }
        }
    }
}
