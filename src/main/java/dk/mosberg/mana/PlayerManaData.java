package dk.mosberg.mana;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import dk.mosberg.config.ServerConfig;
import net.minecraft.nbt.NbtCompound;

/**
 * Holds the three-pool mana system data for a player with advanced mechanics: - Overflow
 * redirection between pools - Drain effects that temporarily reduce regen - Mana shield absorbing
 * damage - Restoration from potions/items - Player-to-player mana transfer - Pool burnout penalties
 * - Burst mode for high-risk/high-reward casting - Mana debt for borrowing future regen -
 * Efficiency modifiers from equipment/buffs
 */
public class PlayerManaData {
    @SuppressWarnings("null")
    private final Map<ManaPoolType, ManaPool> pools = new EnumMap<>(ManaPoolType.class);
    private ManaPoolType activePriority = ManaPoolType.PERSONAL;

    // Advanced mechanics state
    private final Map<ManaPoolType, Float> drainEffects = new EnumMap<>(ManaPoolType.class);
    private final Map<ManaPoolType, Integer> burnoutCounters = new EnumMap<>(ManaPoolType.class);
    private float manaShieldCapacity = 0f;
    private boolean burstModeActive = false;
    private int burstModeTicks = 0;
    private float manaDebt = 0f;
    private float efficiencyModifier = 1.0f;
    private boolean overflowEnabled = true;

    // Synchronization tracking
    private boolean needsSync = false;

    public PlayerManaData() {
        this(loadConfigValues());
    }

    private PlayerManaData(ServerConfig config) {
        // Initialize all three pools with configured defaults
        pools.put(ManaPoolType.PERSONAL,
                new ManaPool(config.personalManaCapacity, config.personalManaRegen));
        pools.put(ManaPoolType.AURA, new ManaPool(config.auraManaCapacity, config.auraManaRegen));
        pools.put(ManaPoolType.RESERVE,
                new ManaPool(config.reserveManaCapacity, config.reserveManaRegen));

        // Initialize advanced mechanics state
        for (ManaPoolType type : ManaPoolType.values()) {
            drainEffects.put(type, 0f);
            burnoutCounters.put(type, 0);
        }
    }

    private static ServerConfig loadConfigValues() {
        // ServerConfig is available on both sides; clients will just read defaults if file absent
        return ServerConfig.getInstance();
    }

    /**
     * Attempts to consume mana, using the priority pool first. Falls back to other pools if needed.
     * Also handles mana debt if enabled.
     *
     * @param amount Amount of mana to consume
     * @return true if mana was successfully consumed
     */
    public boolean consumeMana(float amount) {
        float actualAmount = amount / efficiencyModifier;

        // Apply burst mode multiplier
        if (burstModeActive) {
            actualAmount *= 1.5f; // 50% more consumption in burst mode
        }

        // Try active priority pool first
        if (getPool(activePriority).consume(actualAmount)) {
            incrementBurnoutCounter(activePriority);
            markNeedsSync();
            return true;
        }

        // Try other pools in order: Personal -> Aura -> Reserve
        for (ManaPoolType type : ManaPoolType.values()) {
            if (type != activePriority && getPool(type).consume(actualAmount)) {
                incrementBurnoutCounter(type);
                markNeedsSync();
                return true;
            }
        }

        // If all pools exhausted, try borrowing from mana debt (max 50 mana)
        float debtLimit = 50f;
        if (manaDebt < debtLimit) {
            float canBorrow = Math.min(actualAmount, debtLimit - manaDebt);
            manaDebt += canBorrow;
            markNeedsSync();
            return canBorrow >= actualAmount;
        }

        return false;
    }

    /**
     * Restores mana to a specific pool, with overflow redirection enabled. Used by potions, items,
     * and passive abilities.
     *
     * @param type Pool type to restore
     * @param amount Amount to restore
     */
    public void restoreMana(ManaPoolType type, float amount) {
        ManaPool pool = getPool(type);
        float excess = pool.restore(amount);

        // Redirect overflow to other pools if enabled
        if (overflowEnabled && excess > 0) {
            for (ManaPoolType otherType : ManaPoolType.values()) {
                if (otherType != type) {
                    excess = getPool(otherType).restore(excess);
                    if (excess <= 0)
                        break;
                }
            }
        }

        markNeedsSync();
    }

    /**
     * Transfers mana from this player to another player.
     *
     * @param target Target player data
     * @param amount Amount to transfer
     * @param sourcePool Pool to take from
     * @param targetPool Pool to give to
     * @return Actual amount transferred
     */
    public float transferMana(PlayerManaData target, float amount, ManaPoolType sourcePool,
            ManaPoolType targetPool) {
        ManaPool source = getPool(sourcePool);
        if (source.getCurrentMana() < amount) {
            amount = source.getCurrentMana();
        }

        if (source.consume(amount)) {
            target.restoreMana(targetPool, amount);
            markNeedsSync();
            target.markNeedsSync();
            return amount;
        }

        return 0f;
    }

    /**
     * Applies a drain effect to a pool, reducing its regen rate temporarily.
     *
     * @param type Pool type to drain
     * @param drainAmount Percentage reduction (0.0 to 1.0)
     * @param durationTicks Duration in ticks
     */
    public void applyDrainEffect(ManaPoolType type, float drainAmount, int durationTicks) {
        drainEffects.put(type, Math.max(drainEffects.get(type), drainAmount));
        markNeedsSync();
    }

    /**
     * Activates mana shield with specified capacity. Shield absorbs damage instead of health until
     * depleted.
     *
     * @param capacity Shield capacity
     */
    public void activateManaShield(float capacity) {
        this.manaShieldCapacity = capacity;
        markNeedsSync();
    }

    /**
     * Absorbs damage with mana shield, returning remaining damage.
     *
     * @param damage Incoming damage
     * @return Damage that wasn't absorbed
     */
    public float absorbDamage(float damage) {
        if (manaShieldCapacity <= 0)
            return damage;

        float absorbed = Math.min(damage, manaShieldCapacity);
        manaShieldCapacity -= absorbed;
        markNeedsSync();
        return damage - absorbed;
    }

    /**
     * Activates burst mode for high-risk/high-reward casting. Increases mana consumption but boosts
     * spell power.
     *
     * @param durationTicks Duration in ticks
     */
    public void activateBurstMode(int durationTicks) {
        this.burstModeActive = true;
        this.burstModeTicks = durationTicks;
        markNeedsSync();
    }

    /**
     * Gets burnout penalty for a pool (0.0 to 1.0). Higher values = worse stats from overuse.
     */
    public float getBurnoutPenalty(ManaPoolType type) {
        int counter = burnoutCounters.get(type);
        return Math.min(counter / 100f, 0.5f); // Max 50% penalty at 100 uses
    }

    /**
     * Increments burnout counter for a pool.
     */
    private void incrementBurnoutCounter(ManaPoolType type) {
        burnoutCounters.put(type, burnoutCounters.get(type) + 1);
    }

    /**
     * Decays burnout counters each tick (slow recovery).
     */
    private void decayBurnout() {
        for (ManaPoolType type : ManaPoolType.values()) {
            int current = burnoutCounters.get(type);
            if (current > 0) {
                burnoutCounters.put(type, Math.max(0, current - 1)); // Decay 1 per tick
            }
        }
    }

    /**
     * Regenerates all mana pools. Called each tick on the server. Also handles drain effects, debt
     * repayment, and burst mode.
     */
    public void tickRegeneration() {
        // Decay burst mode
        if (burstModeActive) {
            burstModeTicks--;
            if (burstModeTicks <= 0) {
                burstModeActive = false;
                markNeedsSync();
            }
        }

        // Decay drain effects (reduce by 1% per tick)
        for (ManaPoolType type : ManaPoolType.values()) {
            float drain = drainEffects.get(type);
            if (drain > 0) {
                drainEffects.put(type, Math.max(0, drain - 0.01f));
            }
        }

        // Decay burnout counters
        decayBurnout();

        // Regenerate pools with drain penalties
        for (ManaPoolType type : ManaPoolType.values()) {
            ManaPool pool = getPool(type);
            float drainPenalty = drainEffects.get(type);
            float effectiveRegen = pool.getRegenRate() * (1f - drainPenalty);

            // Apply efficiency modifier
            effectiveRegen *= efficiencyModifier;

            // Custom regen with modified rate
            float newMana = Math.min(pool.getCurrentMana() + effectiveRegen, pool.getMaxCapacity());
            pool.set(newMana);
        }

        // Repay mana debt gradually (1 mana per tick)
        if (manaDebt > 0) {
            float repayment = Math.min(1f, manaDebt);

            // Try to take from personal pool first
            if (getPool(ManaPoolType.PERSONAL).consume(repayment)) {
                manaDebt -= repayment;
            } else {
                // Debt persists if can't repay
            }
        }
    }

    public ManaPool getPool(ManaPoolType type) {
        return Objects.requireNonNull(pools.get(type), "Missing mana pool for type " + type);
    }

    public ManaPoolType getActivePriority() {
        return activePriority;
    }

    public void setActivePriority(ManaPoolType priority) {
        this.activePriority = Objects.requireNonNull(priority, "Priority cannot be null");
        markNeedsSync();
    }

    public void updatePool(ManaPoolType type, int maxCapacity, float current, float regen) {
        pools.put(type, new ManaPool(maxCapacity, current, regen));
        markNeedsSync();
    }

    @SuppressWarnings("null")
    public float getTotalMana() {
        return pools.values().stream().map(ManaPool::getCurrentMana).reduce(0f, Float::sum);
    }

    @SuppressWarnings("null")
    public float getTotalCapacity() {
        return pools.values().stream().map(ManaPool::getMaxCapacity).reduce(0, Integer::sum);
    }

    // Advanced mechanics getters/setters

    public float getManaShieldCapacity() {
        return manaShieldCapacity;
    }

    public boolean isBurstModeActive() {
        return burstModeActive;
    }

    public float getManaDebt() {
        return manaDebt;
    }

    public void setManaDebt(float debt) {
        this.manaDebt = Math.max(0, debt);
        markNeedsSync();
    }

    public float getEfficiencyModifier() {
        return efficiencyModifier;
    }

    public void setEfficiencyModifier(float modifier) {
        this.efficiencyModifier = Math.max(0.1f, Math.min(2.0f, modifier)); // Clamp 0.1 to 2.0
        markNeedsSync();
    }

    public boolean isOverflowEnabled() {
        return overflowEnabled;
    }

    public void setOverflowEnabled(boolean enabled) {
        this.overflowEnabled = enabled;
    }

    public float getDrainEffect(ManaPoolType type) {
        return drainEffects.get(type);
    }

    /**
     * Marks data as needing synchronization to client.
     */
    private void markNeedsSync() {
        this.needsSync = true;
    }

    /**
     * Checks if data needs sync and resets flag.
     */
    public boolean consumeSyncFlag() {
        boolean result = needsSync;
        needsSync = false;
        return result;
    }

    /**
     * Serializes mana data to NBT for persistence.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        // Serialize pools
        NbtCompound poolsNbt = new NbtCompound();
        for (Map.Entry<ManaPoolType, ManaPool> entry : pools.entrySet()) {
            NbtCompound poolNbt = new NbtCompound();
            poolNbt.putFloat("current", entry.getValue().getCurrentMana());
            poolNbt.putInt("max", entry.getValue().getMaxCapacity());
            poolNbt.putFloat("regen", entry.getValue().getRegenRate());
            poolsNbt.put(entry.getKey().name(), poolNbt);
        }
        nbt.put("pools", poolsNbt);
        nbt.putString("activePriority", activePriority.name());

        // Serialize advanced mechanics
        NbtCompound drainsNbt = new NbtCompound();
        for (Map.Entry<ManaPoolType, Float> entry : drainEffects.entrySet()) {
            drainsNbt.putFloat(entry.getKey().name(), entry.getValue());
        }
        nbt.put("drainEffects", drainsNbt);

        NbtCompound burnoutNbt = new NbtCompound();
        for (Map.Entry<ManaPoolType, Integer> entry : burnoutCounters.entrySet()) {
            burnoutNbt.putInt(entry.getKey().name(), entry.getValue());
        }
        nbt.put("burnoutCounters", burnoutNbt);

        nbt.putFloat("manaShieldCapacity", manaShieldCapacity);
        nbt.putBoolean("burstModeActive", burstModeActive);
        nbt.putInt("burstModeTicks", burstModeTicks);
        nbt.putFloat("manaDebt", manaDebt);
        nbt.putFloat("efficiencyModifier", efficiencyModifier);
        nbt.putBoolean("overflowEnabled", overflowEnabled);

        return nbt;
    }

    /**
     * Deserializes mana data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        // Deserialize pools
        if (nbt.contains("pools")) {
            NbtCompound poolsNbt = nbt.getCompound("pools").get();
            for (ManaPoolType type : ManaPoolType.values()) {
                if (poolsNbt.contains(type.name())) {
                    NbtCompound poolNbt = poolsNbt.getCompound(type.name()).get();
                    float current = poolNbt.getFloat("current").get();
                    int max = poolNbt.getInt("max").get();
                    float regen = poolNbt.getFloat("regen").get();
                    pools.put(type, new ManaPool(max, current, regen));
                }
            }
        }
        if (nbt.contains("activePriority")) {
            try {
                String priorityStr = nbt.getString("activePriority").get();
                activePriority = ManaPoolType.valueOf(priorityStr);
            } catch (IllegalArgumentException e) {
                activePriority = ManaPoolType.PERSONAL;
            }
        }

        // Deserialize advanced mechanics
        if (nbt.contains("drainEffects")) {
            NbtCompound drainsNbt = nbt.getCompound("drainEffects").get();
            for (ManaPoolType type : ManaPoolType.values()) {
                if (drainsNbt.contains(type.name())) {
                    drainEffects.put(type, drainsNbt.getFloat(type.name()).get());
                }
            }
        }

        if (nbt.contains("burnoutCounters")) {
            NbtCompound burnoutNbt = nbt.getCompound("burnoutCounters").get();
            for (ManaPoolType type : ManaPoolType.values()) {
                if (burnoutNbt.contains(type.name())) {
                    burnoutCounters.put(type, burnoutNbt.getInt(type.name()).get());
                }
            }
        }

        if (nbt.contains("manaShieldCapacity")) {
            manaShieldCapacity = nbt.getFloat("manaShieldCapacity").get();
        }
        if (nbt.contains("burstModeActive")) {
            burstModeActive = nbt.getBoolean("burstModeActive").get();
        }
        if (nbt.contains("burstModeTicks")) {
            burstModeTicks = nbt.getInt("burstModeTicks").get();
        }
        if (nbt.contains("manaDebt")) {
            manaDebt = nbt.getFloat("manaDebt").get();
        }
        if (nbt.contains("efficiencyModifier")) {
            efficiencyModifier = nbt.getFloat("efficiencyModifier").get();
        }
        if (nbt.contains("overflowEnabled")) {
            overflowEnabled = nbt.getBoolean("overflowEnabled").get();
        }
    }
}
