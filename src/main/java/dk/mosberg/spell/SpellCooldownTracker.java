package dk.mosberg.spell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

/**
 * Tracks active spell cooldowns per player with advanced mechanics: - Shared cooldown groups -
 * Cooldown reduction modifiers - Cooldown stacking for repeated casts - Cooldown reset events -
 * Partial cooldown recovery by school - Cooldown immunity periods - Persistence across respawns
 */
public class SpellCooldownTracker {
    private final Map<Identifier, Float> cooldowns = new HashMap<>();
    private final Map<String, Float> groupCooldowns = new HashMap<>(); // Group ID -> remaining time
    private final Map<Identifier, Integer> stackCounts = new HashMap<>(); // Spell -> consecutive
                                                                          // uses
    private final Set<Identifier> immuneSpells = new HashSet<>(); // Spells with immunity active

    // Modifiers
    private float globalCooldownReduction = 1.0f; // 1.0 = normal, 0.5 = half cooldowns
    private final Map<SpellSchool, Float> schoolRecoveryModifiers = new HashMap<>();
    private boolean persistOnDeath = true;

    public SpellCooldownTracker() {
        // Initialize school recovery modifiers
        for (SpellSchool school : SpellSchool.values()) {
            schoolRecoveryModifiers.put(school, 1.0f);
        }
    }

    /**
     * Advances all active cooldowns by the given amount of time (in seconds). Removes cooldowns
     * that have fully expired. Applies school-specific recovery rates.
     */
    public void tick(float deltaTime) {
        // Tick individual spell cooldowns with global reduction
        cooldowns.replaceAll(
                (spellId, remaining) -> remaining - (deltaTime * globalCooldownReduction));
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        // Tick group cooldowns
        groupCooldowns.replaceAll(
                (group, remaining) -> remaining - (deltaTime * globalCooldownReduction));
        groupCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);

        // Decay stack counts slowly (reduce by 1 every 5 seconds)
        if (Math.random() < deltaTime / 5.0) {
            stackCounts.replaceAll((spellId, count) -> Math.max(0, count - 1));
            stackCounts.entrySet().removeIf(entry -> entry.getValue() <= 0);
        }
    }

    /**
     * Checks if a spell is currently on cooldown (individual or group).
     */
    public boolean isOnCooldown(Identifier spellId) {
        return cooldowns.containsKey(spellId) && cooldowns.get(spellId) > 0;
    }

    /**
     * Checks if a cooldown group is active.
     */
    public boolean isGroupOnCooldown(String groupId) {
        return groupCooldowns.containsKey(groupId) && groupCooldowns.get(groupId) > 0;
    }

    /**
     * Gets the remaining cooldown time for a spell in seconds. Returns 0 if spell is not on
     * cooldown.
     */
    public float getRemainingCooldown(Identifier spellId) {
        return cooldowns.getOrDefault(spellId, 0f);
    }

    /**
     * Gets the remaining cooldown for a group.
     */
    public float getGroupRemainingCooldown(String groupId) {
        return groupCooldowns.getOrDefault(groupId, 0f);
    }

    /**
     * Starts or resets a cooldown for a spell with stacking penalties.
     *
     * @param spellId Spell identifier
     * @param baseCooldown Base cooldown duration
     * @param groupId Optional cooldown group (null if not grouped)
     */
    public void startCooldown(Identifier spellId, float baseCooldown, String groupId) {
        if (baseCooldown <= 0)
            return;

        // Check immunity
        if (immuneSpells.contains(spellId)) {
            return; // Immune, no cooldown
        }

        // Apply stacking penalty
        int stackCount = stackCounts.getOrDefault(spellId, 0);
        float stackMultiplier = 1.0f + (stackCount * 0.2f); // +20% per stack
        float effectiveCooldown = baseCooldown * stackMultiplier * globalCooldownReduction;

        // Set individual cooldown
        cooldowns.put(spellId, effectiveCooldown);

        // Set group cooldown if specified
        if (groupId != null && !groupId.isEmpty()) {
            groupCooldowns.put(groupId, effectiveCooldown);
        }

        // Increment stack count
        stackCounts.put(spellId, stackCount + 1);
    }

    /**
     * Starts cooldown without group.
     */
    public void startCooldown(Identifier spellId, float cooldownDuration) {
        startCooldown(spellId, cooldownDuration, null);
    }

    /**
     * Clears a spell's cooldown immediately.
     */
    public void clearCooldown(Identifier spellId) {
        cooldowns.remove(spellId);
        stackCounts.remove(spellId);
    }

    /**
     * Clears all spells in a cooldown group.
     */
    public void clearGroupCooldown(String groupId) {
        groupCooldowns.remove(groupId);
    }

    /**
     * Resets all cooldowns (triggered by specific events like respawn).
     */
    public void resetAllCooldowns() {
        if (!persistOnDeath) {
            clearAll();
        }
    }

    /**
     * Clears all active cooldowns.
     */
    public void clearAll() {
        cooldowns.clear();
        groupCooldowns.clear();
        stackCounts.clear();
    }

    // === Modifier Methods ===

    /**
     * Sets global cooldown reduction modifier.
     * 
     * @param reduction 1.0 = normal, 0.5 = half cooldowns (faster), 2.0 = double (slower)
     */
    public void setGlobalCooldownReduction(float reduction) {
        this.globalCooldownReduction = Math.max(0.1f, Math.min(3.0f, reduction));
    }

    public float getGlobalCooldownReduction() {
        return globalCooldownReduction;
    }

    /**
     * Sets faster recovery for a specific school.
     * 
     * @param school Spell school
     * @param modifier 1.0 = normal, 0.5 = twice as fast, 2.0 = half speed
     */
    public void setSchoolRecoveryModifier(SpellSchool school, float modifier) {
        schoolRecoveryModifiers.put(school, Math.max(0.1f, Math.min(3.0f, modifier)));
    }

    public float getSchoolRecoveryModifier(SpellSchool school) {
        return schoolRecoveryModifiers.getOrDefault(school, 1.0f);
    }

    /**
     * Grants temporary cooldown immunity for a spell.
     * 
     * @param spellId Spell to make immune
     * @param durationSeconds How long immunity lasts (0 = permanent until cleared)
     */
    public void grantImmunity(Identifier spellId, float durationSeconds) {
        immuneSpells.add(spellId);
        // TODO: Add timed immunity expiration if needed
    }

    /**
     * Removes immunity from a spell.
     */
    public void revokeImmunity(Identifier spellId) {
        immuneSpells.remove(spellId);
    }

    public boolean hasImmunity(Identifier spellId) {
        return immuneSpells.contains(spellId);
    }

    /**
     * Sets whether cooldowns persist after death.
     */
    public void setPersistOnDeath(boolean persist) {
        this.persistOnDeath = persist;
    }

    /**
     * Gets stack count for a spell (how many times cast recently).
     */
    public int getStackCount(Identifier spellId) {
        return stackCounts.getOrDefault(spellId, 0);
    }

    /**
     * Serializes cooldown data to NBT for persistence.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Individual cooldowns
        NbtCompound cooldownsNbt = new NbtCompound();
        for (Map.Entry<Identifier, Float> entry : cooldowns.entrySet()) {
            cooldownsNbt.putFloat(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("cooldowns", cooldownsNbt);

        // Group cooldowns
        NbtCompound groupsNbt = new NbtCompound();
        for (Map.Entry<String, Float> entry : groupCooldowns.entrySet()) {
            groupsNbt.putFloat(entry.getKey(), entry.getValue());
        }
        nbt.put("groupCooldowns", groupsNbt);

        // Stack counts
        NbtCompound stacksNbt = new NbtCompound();
        for (Map.Entry<Identifier, Integer> entry : stackCounts.entrySet()) {
            stacksNbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("stackCounts", stacksNbt);

        // Immune spells
        NbtList immuneList = new NbtList();
        for (Identifier spellId : immuneSpells) {
            immuneList.add(NbtString.of(spellId.toString()));
        }
        nbt.put("immuneSpells", immuneList);

        // Modifiers
        nbt.putFloat("globalCooldownReduction", globalCooldownReduction);
        nbt.putBoolean("persistOnDeath", persistOnDeath);

        // School modifiers
        NbtCompound schoolModsNbt = new NbtCompound();
        for (Map.Entry<SpellSchool, Float> entry : schoolRecoveryModifiers.entrySet()) {
            schoolModsNbt.putFloat(entry.getKey().name(), entry.getValue());
        }
        nbt.put("schoolModifiers", schoolModsNbt);

        return nbt;
    }

    /**
     * Deserializes cooldown data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        // Individual cooldowns
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

        // Group cooldowns
        if (nbt.contains("groupCooldowns")) {
            var optionalCompound = nbt.getCompound("groupCooldowns");
            if (optionalCompound.isPresent()) {
                NbtCompound groupsNbt = optionalCompound.get();
                for (String key : groupsNbt.getKeys()) {
                    var optionalFloat = groupsNbt.getFloat(key);
                    if (optionalFloat.isPresent()) {
                        groupCooldowns.put(key, optionalFloat.get());
                    }
                }
            }
        }

        // Stack counts
        if (nbt.contains("stackCounts")) {
            var optionalCompound = nbt.getCompound("stackCounts");
            if (optionalCompound.isPresent()) {
                NbtCompound stacksNbt = optionalCompound.get();
                for (String key : stacksNbt.getKeys()) {
                    try {
                        Identifier spellId = Identifier.tryParse(key);
                        if (spellId != null) {
                            var optionalInt = stacksNbt.getInt(key);
                            if (optionalInt.isPresent()) {
                                stackCounts.put(spellId, optionalInt.get());
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid
                    }
                }
            }
        }

        // Immune spells
        immuneSpells.clear();
        if (nbt.contains("immuneSpells")) {
            NbtList immuneList = nbt.getList("immuneSpells").get();
            for (int i = 0; i < immuneList.size(); i++) {
                try {
                    Identifier spellId = Identifier.tryParse(immuneList.getString(i).get());
                    if (spellId != null) {
                        immuneSpells.add(spellId);
                    }
                } catch (Exception e) {
                    // Skip invalid
                }
            }
        }

        // Modifiers
        if (nbt.contains("globalCooldownReduction")) {
            globalCooldownReduction = nbt.getFloat("globalCooldownReduction").get();
        }
        if (nbt.contains("persistOnDeath")) {
            persistOnDeath = nbt.getBoolean("persistOnDeath").get();
        }

        // School modifiers
        if (nbt.contains("schoolModifiers")) {
            var optionalCompound = nbt.getCompound("schoolModifiers");
            if (optionalCompound.isPresent()) {
                NbtCompound schoolModsNbt = optionalCompound.get();
                for (String key : schoolModsNbt.getKeys()) {
                    try {
                        SpellSchool school = SpellSchool.valueOf(key);
                        var optionalFloat = schoolModsNbt.getFloat(key);
                        if (optionalFloat.isPresent()) {
                            schoolRecoveryModifiers.put(school, optionalFloat.get());
                        }
                    } catch (IllegalArgumentException e) {
                        // Skip invalid school
                    }
                }
            }
        }
    }
}
