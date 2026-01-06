package dk.mosberg.mana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import dk.mosberg.spell.SpellCooldownTracker;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

/**
 * Combines mana and cooldown tracking into a single player attachment with advanced casting
 * mechanics: - Casting state tracking (idle, channeling, casting, cooldown) - Interrupt mechanics
 * (damage/movement breaks channeling) - Concentration system (focus affects spell accuracy/power) -
 * Casting speed modifiers (haste/slowness) - Backfire/critical failure on low concentration - Combo
 * system (consecutive similar spells) - Rhythm-based casting (timed button presses for bonuses) -
 * Spell memory (memorized spells for quick access) - Fatigue system (too much casting reduces
 * effectiveness) - Synergy tracking (combining elements with other players)
 */
public class PlayerCastingData {
    private final PlayerManaData manaData;
    private final SpellCooldownTracker cooldownTracker;

    // Casting state tracking
    private CastingState castingState = CastingState.IDLE;
    private Identifier currentSpellId = null;
    private int castingTicks = 0;

    // Concentration system
    private float concentration = 100f; // 0-100, affects spell accuracy/power
    private float concentrationDecayRate = 0.5f; // Decay per tick when not maintained

    // Casting speed modifiers
    private float castingSpeedModifier = 1.0f; // 1.0 = normal, 2.0 = double speed, 0.5 = half speed

    // Combo system
    private final List<ComboEntry> comboHistory = new ArrayList<>();
    private int currentComboCount = 0;
    private SpellSchool lastSchool = null;

    // Rhythm system
    private final Queue<Long> rhythmTimings = new LinkedList<>(); // Timestamps of recent casts
    private float rhythmBonus = 0f; // 0-1, bonus from good timing

    // Spell memory
    private final List<Identifier> memorizedSpells = new ArrayList<>(10); // Max 10 memorized

    // Fatigue system
    private float fatigueLevel = 0f; // 0-100, higher = more tired
    private int recentCastCount = 0; // Casts in last 100 ticks

    // Synergy tracking
    private final Map<UUID, SynergyEntry> synergyPartners = new HashMap<>();

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

    // === Casting State Methods ===

    /**
     * Starts casting a spell, entering CHANNELING state.
     */
    public boolean startCasting(Identifier spellId) {
        if (!castingState.canStartCast()) {
            return false;
        }

        this.castingState = CastingState.CHANNELING;
        this.currentSpellId = spellId;
        this.castingTicks = 0;
        return true;
    }

    /**
     * Transitions from CHANNELING to CASTING state.
     */
    public void beginExecution() {
        if (castingState == CastingState.CHANNELING) {
            castingState = CastingState.CASTING;
            castingTicks = 0;
        }
    }

    /**
     * Finishes casting, entering COOLDOWN state.
     */
    public void finishCasting() {
        if (currentSpellId != null) {
            // Update combo history
            recordCombo(currentSpellId);

            // Update rhythm timing
            recordRhythm();

            // Increase fatigue
            increaseFatigue();
        }

        castingState = CastingState.COOLDOWN;
        currentSpellId = null;
        castingTicks = 0;
    }

    /**
     * Cancels current casting, returning to IDLE.
     */
    public void cancelCasting() {
        castingState = CastingState.IDLE;
        currentSpellId = null;
        castingTicks = 0;
    }

    /**
     * Attempts to interrupt casting (from damage or movement).
     * 
     * @return true if casting was interrupted
     */
    public boolean interruptCasting() {
        if (castingState.isInterruptible()) {
            // Lose concentration
            concentration = Math.max(0, concentration - 20f);

            cancelCasting();
            return true;
        }
        return false;
    }

    public CastingState getCastingState() {
        return castingState;
    }

    public Identifier getCurrentSpellId() {
        return currentSpellId;
    }

    // === Concentration System ===

    /**
     * Gets current concentration level (0-100).
     */
    public float getConcentration() {
        return concentration;
    }

    /**
     * Sets concentration level (clamped 0-100).
     */
    public void setConcentration(float value) {
        this.concentration = Math.max(0, Math.min(100, value));
    }

    /**
     * Maintains concentration (called during channeling).
     */
    public void maintainConcentration(float amount) {
        concentration = Math.min(100, concentration + amount);
    }

    /**
     * Breaks concentration (called on damage/interrupt).
     */
    public void breakConcentration(float amount) {
        concentration = Math.max(0, concentration - amount);
    }

    /**
     * Gets spell power multiplier based on concentration (0.5 - 1.5).
     */
    public float getConcentrationPowerMultiplier() {
        return 0.5f + (concentration / 100f); // 0% = 0.5x, 100% = 1.5x
    }

    /**
     * Checks if backfire should occur (low concentration).
     * 
     * @return true if backfire should happen
     */
    public boolean shouldBackfire() {
        return concentration < 30f && Math.random() < (0.3f - concentration / 100f);
    }

    // === Casting Speed Modifiers ===

    public float getCastingSpeedModifier() {
        return castingSpeedModifier;
    }

    public void setCastingSpeedModifier(float modifier) {
        this.castingSpeedModifier = Math.max(0.1f, Math.min(3.0f, modifier));
    }

    /**
     * Applies a temporary haste effect.
     */
    public void applyHaste(float multiplier, int durationTicks) {
        // TODO: Implement timed effects when needed
        this.castingSpeedModifier = multiplier;
    }

    // === Combo System ===

    /**
     * Records a spell cast for combo tracking.
     */
    private void recordCombo(Identifier spellId) {
        // Add to history
        comboHistory.add(new ComboEntry(spellId, System.currentTimeMillis()));

        // Clean old entries (older than 5 seconds)
        long cutoff = System.currentTimeMillis() - 5000;
        comboHistory.removeIf(entry -> entry.timestamp < cutoff);

        // Calculate combo count for same spell
        currentComboCount =
                (int) comboHistory.stream().filter(entry -> entry.spellId.equals(spellId)).count();
    }

    /**
     * Gets current combo count.
     */
    public int getComboCount() {
        return currentComboCount;
    }

    /**
     * Gets combo bonus multiplier (1.0 - 2.0 based on combo count).
     */
    public float getComboMultiplier() {
        return Math.min(2.0f, 1.0f + (currentComboCount * 0.1f));
    }

    // === Rhythm System ===

    /**
     * Records timing for rhythm-based casting.
     */
    private void recordRhythm() {
        long now = System.currentTimeMillis();
        rhythmTimings.offer(now);

        // Keep only last 5 casts
        while (rhythmTimings.size() > 5) {
            rhythmTimings.poll();
        }

        // Calculate rhythm bonus if we have at least 3 casts
        if (rhythmTimings.size() >= 3) {
            List<Long> timings = new ArrayList<>(rhythmTimings);

            // Calculate intervals
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < timings.size(); i++) {
                intervals.add(timings.get(i) - timings.get(i - 1));
            }

            // Check consistency (all intervals within 20% of average)
            long avgInterval =
                    intervals.stream().mapToLong(Long::longValue).sum() / intervals.size();
            boolean consistent = intervals.stream()
                    .allMatch(interval -> Math.abs(interval - avgInterval) < avgInterval * 0.2);

            rhythmBonus = consistent ? 0.25f : 0f; // 25% bonus for good rhythm
        }
    }

    public float getRhythmBonus() {
        return rhythmBonus;
    }

    // === Spell Memory ===

    /**
     * Memorizes a spell for quick access.
     */
    public boolean memorizeSpell(Identifier spellId) {
        if (memorizedSpells.size() >= 10) {
            return false; // Memory full
        }

        if (!memorizedSpells.contains(spellId)) {
            memorizedSpells.add(spellId);
            return true;
        }
        return false;
    }

    /**
     * Forgets a memorized spell.
     */
    public void forgetSpell(Identifier spellId) {
        memorizedSpells.remove(spellId);
    }

    /**
     * Checks if a spell is memorized.
     */
    public boolean isSpellMemorized(Identifier spellId) {
        return memorizedSpells.contains(spellId);
    }

    public List<Identifier> getMemorizedSpells() {
        return new ArrayList<>(memorizedSpells);
    }

    // === Fatigue System ===

    /**
     * Increases fatigue from casting.
     */
    private void increaseFatigue() {
        fatigueLevel = Math.min(100, fatigueLevel + 2f); // +2 per cast
        recentCastCount++;
    }

    /**
     * Gets fatigue penalty (0-1, higher = worse).
     */
    public float getFatiguePenalty() {
        return fatigueLevel / 100f;
    }

    /**
     * Gets effectiveness multiplier accounting for fatigue (0.5-1.0).
     */
    public float getEffectivenessMultiplier() {
        return 1.0f - (getFatiguePenalty() * 0.5f); // Max 50% reduction
    }

    // === Synergy Tracking ===

    /**
     * Records synergy with another player.
     */
    public void recordSynergy(UUID partnerId, SpellSchool school) {
        long now = System.currentTimeMillis();

        SynergyEntry entry = synergyPartners.get(partnerId);
        if (entry == null) {
            entry = new SynergyEntry(partnerId, school, now);
            synergyPartners.put(partnerId, entry);
        } else {
            entry.update(school, now);
        }
    }

    /**
     * Gets synergy bonus with a partner (0-0.5).
     */
    public float getSynergyBonus(UUID partnerId) {
        SynergyEntry entry = synergyPartners.get(partnerId);
        if (entry == null) {
            return 0f;
        }

        // Synergy decays over time
        long age = System.currentTimeMillis() - entry.lastCastTime;
        if (age > 10000) { // 10 seconds
            return 0f;
        }

        // Bonus scales with combo count
        return Math.min(0.5f, entry.comboCount * 0.1f);
    }

    /**
     * Advances both mana regen and cooldown timers by the given delta time (in seconds). Called
     * once per server tick (20 ticks per second = 0.05 seconds per call).
     */
    public void tick() {
        manaData.tickRegeneration();
        cooldownTracker.tick(0.05f); // 1 tick = 0.05 seconds (20 ticks per second)

        // Decay concentration when not channeling
        if (castingState != CastingState.CHANNELING) {
            concentration = Math.max(0, concentration - concentrationDecayRate);
        }

        // Recover from fatigue slowly
        if (fatigueLevel > 0) {
            fatigueLevel = Math.max(0, fatigueLevel - 0.1f); // Recover 0.1 per tick
        }

        // Advance casting ticks
        if (castingState.isActivelyCasting()) {
            castingTicks++;
        }

        // Track recent cast count
        if (castingTicks % 100 == 0) { // Every 5 seconds
            recentCastCount = 0;
        }
    }

    /**
     * Serializes all casting data to NBT.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        manaData.writeNbt(nbt);
        cooldownTracker.writeNbt(nbt);

        // Casting state
        nbt.putString("castingState", castingState.name());
        if (currentSpellId != null) {
            nbt.putString("currentSpellId", currentSpellId.toString());
        }
        nbt.putInt("castingTicks", castingTicks);

        // Concentration
        nbt.putFloat("concentration", concentration);
        nbt.putFloat("concentrationDecayRate", concentrationDecayRate);

        // Speed
        nbt.putFloat("castingSpeedModifier", castingSpeedModifier);

        // Combo
        nbt.putInt("currentComboCount", currentComboCount);
        if (lastSchool != null) {
            nbt.putString("lastSchool", lastSchool.name());
        }

        // Rhythm
        nbt.putFloat("rhythmBonus", rhythmBonus);

        // Memorized spells
        NbtList memorizedList = new NbtList();
        for (Identifier spellId : memorizedSpells) {
            memorizedList.add(NbtString.of(spellId.toString()));
        }
        nbt.put("memorizedSpells", memorizedList);

        // Fatigue
        nbt.putFloat("fatigueLevel", fatigueLevel);
        nbt.putInt("recentCastCount", recentCastCount);

        return nbt;
    }

    /**
     * Deserializes all casting data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        manaData.readNbt(nbt);
        cooldownTracker.readNbt(nbt);

        // Casting state
        if (nbt.contains("castingState")) {
            try {
                castingState = CastingState.valueOf(nbt.getString("castingState").get());
            } catch (IllegalArgumentException e) {
                castingState = CastingState.IDLE;
            }
        }
        if (nbt.contains("currentSpellId")) {
            currentSpellId = Identifier.of(nbt.getString("currentSpellId").get());
        }
        if (nbt.contains("castingTicks")) {
            castingTicks = nbt.getInt("castingTicks").get();
        }

        // Concentration
        if (nbt.contains("concentration")) {
            concentration = nbt.getFloat("concentration").get();
        }
        if (nbt.contains("concentrationDecayRate")) {
            concentrationDecayRate = nbt.getFloat("concentrationDecayRate").get();
        }

        // Speed
        if (nbt.contains("castingSpeedModifier")) {
            castingSpeedModifier = nbt.getFloat("castingSpeedModifier").get();
        }

        // Combo
        if (nbt.contains("currentComboCount")) {
            currentComboCount = nbt.getInt("currentComboCount").get();
        }
        if (nbt.contains("lastSchool")) {
            try {
                lastSchool = SpellSchool.valueOf(nbt.getString("lastSchool").get());
            } catch (IllegalArgumentException e) {
                lastSchool = null;
            }
        }

        // Rhythm
        if (nbt.contains("rhythmBonus")) {
            rhythmBonus = nbt.getFloat("rhythmBonus").get();
        }

        // Memorized spells
        memorizedSpells.clear();
        if (nbt.contains("memorizedSpells")) {
            NbtList memorizedList = nbt.getList("memorizedSpells").get();
            for (int i = 0; i < memorizedList.size(); i++) {
                memorizedSpells.add(Identifier.of(memorizedList.getString(i).get()));
            }
        }

        // Fatigue
        if (nbt.contains("fatigueLevel")) {
            fatigueLevel = nbt.getFloat("fatigueLevel").get();
        }
        if (nbt.contains("recentCastCount")) {
            recentCastCount = nbt.getInt("recentCastCount").get();
        }
    }

    // === Helper Classes ===

    /**
     * Represents a combo entry for spell tracking.
     */
    private static class ComboEntry {
        final Identifier spellId;
        final long timestamp;

        ComboEntry(Identifier spellId, long timestamp) {
            this.spellId = spellId;
            this.timestamp = timestamp;
        }
    }

    /**
     * Represents synergy with another player.
     */
    private static class SynergyEntry {
        final UUID partnerId;
        SpellSchool lastSchool;
        long lastCastTime;
        int comboCount;

        SynergyEntry(UUID partnerId, SpellSchool school, long timestamp) {
            this.partnerId = partnerId;
            this.lastSchool = school;
            this.lastCastTime = timestamp;
            this.comboCount = 1;
        }

        void update(SpellSchool school, long timestamp) {
            if (school == lastSchool && (timestamp - lastCastTime) < 3000) {
                comboCount++;
            } else {
                comboCount = 1;
            }
            lastSchool = school;
            lastCastTime = timestamp;
        }
    }
}
