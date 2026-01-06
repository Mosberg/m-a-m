package dk.mosberg.mana;

/**
 * Represents a single mana pool with capacity, current amount, and regeneration rate.
 *
 * Features implemented: temporary capacity modifiers, conditional regeneration (biome/dimension),
 * drain effects, overfill mechanics (150% max), efficiency tracking, resonance effects, critical
 * threshold checking, and corruption mechanics for stale mana.
 */
public class ManaPool {
    private final int baseMaxCapacity;
    private float currentMana;
    private final float baseRegenRate;
    private float temporaryCapacityModifier = 0; // Added capacity from buffs
    private float drainRate = 0; // Mana drain per tick
    private float efficiencyModifier = 1.0f; // Resonance effect
    private float totalManaConsumed = 0; // Total mana consumed (for efficiency tracking)
    private float totalManaWasted = 0; // Mana wasted due to overfill regen
    private int ticksSinceLastUse = 0; // Tracks stale/corruption effect

    public ManaPool(int maxCapacity, float regenRate) {
        this.baseMaxCapacity = maxCapacity;
        this.currentMana = maxCapacity;
        this.baseRegenRate = regenRate;
    }

    public ManaPool(int maxCapacity, float currentMana, float regenRate) {
        this.baseMaxCapacity = maxCapacity;
        this.currentMana = Math.min(currentMana, maxCapacity);
        this.baseRegenRate = regenRate;
    }

    /**
     * Attempts to consume mana from this pool.
     *
     * @param amount Amount of mana to consume
     * @return true if mana was consumed, false if insufficient
     */
    public boolean consume(float amount) {
        float adjustedAmount = amount / efficiencyModifier;
        if (currentMana >= adjustedAmount) {
            currentMana -= adjustedAmount;
            totalManaConsumed += amount; // Track actual consumption
            ticksSinceLastUse = 0; // Reset stale counter
            // Improve efficiency with repeated use (resonance effect)
            efficiencyModifier = Math.min(1.2f, efficiencyModifier + 0.01f);
            return true;
        }
        return false;
    }

    /**
     * Regenerates mana based on the regen rate. Called each tick.
     */
    public void regenerate() {
        regenerate(1.0f);
    }

    /**
     * Regenerates mana with environmental modifiers. Called each tick.
     *
     * @param environmentalModifier Modifier based on biome/dimension (0.0 = no regen, 1.0 = normal,
     *        1.5 = boosted)
     */
    public void regenerate(float environmentalModifier) {
        ticksSinceLastUse++;

        int effectiveMaxCapacity = getMaxCapacity();
        if (currentMana < effectiveMaxCapacity) {
            float netRegen = baseRegenRate - drainRate;

            // Apply environmental modifier (biome/dimension effects)
            netRegen *= environmentalModifier;

            // Apply corruption penalty if mana has been stale for too long
            float corruptionPenalty = getCorruptionModifier();
            netRegen *= corruptionPenalty;

            currentMana = Math.min(currentMana + netRegen, effectiveMaxCapacity);
        } else if (currentMana >= effectiveMaxCapacity) {
            // Track wasted regen (efficiency metric)
            totalManaWasted += baseRegenRate;
        }

        // Slowly decay efficiency when not in use
        if (efficiencyModifier > 1.0f) {
            efficiencyModifier = Math.max(1.0f, efficiencyModifier - 0.002f);
        }
    }

    /**
     * Calculate environmental regeneration modifier based on location.
     *
     * @param poolType Type of mana pool
     * @param isInNether Whether player is in the Nether
     * @param isInEnd Whether player is in the End
     * @param isUnderwater Whether player is underwater
     * @param biomeName Name of current biome (lowercase)
     * @return Regeneration multiplier (0.0 - 1.5)
     */
    public static float getEnvironmentalRegenModifier(ManaPoolType poolType, boolean isInNether,
            boolean isInEnd, boolean isUnderwater, String biomeName) {
        // Base modifier
        float modifier = 1.0f;

        // Dimension effects
        if (isInNether) {
            // Reserve pool boosted in Nether (high power environment)
            if (poolType == ManaPoolType.RESERVE)
                modifier *= 1.3f;
            // Aura pool weakened in Nether (chaotic magic)
            else if (poolType == ManaPoolType.AURA)
                modifier *= 0.7f;
        } else if (isInEnd) {
            // Aura pool boosted in End (otherworldly magic)
            if (poolType == ManaPoolType.AURA)
                modifier *= 1.4f;
            // Personal pool weakened in End (alien environment)
            else if (poolType == ManaPoolType.PERSONAL)
                modifier *= 0.8f;
        }

        // Underwater effects
        if (isUnderwater) {
            // Personal pool struggles underwater
            if (poolType == ManaPoolType.PERSONAL)
                modifier *= 0.6f;
        }

        // Biome effects
        if (biomeName.contains("desert") || biomeName.contains("badlands")) {
            // Harsh environments boost Reserve pool
            if (poolType == ManaPoolType.RESERVE)
                modifier *= 1.2f;
        } else if (biomeName.contains("forest") || biomeName.contains("plains")) {
            // Peaceful biomes boost Aura pool
            if (poolType == ManaPoolType.AURA)
                modifier *= 1.2f;
        } else if (biomeName.contains("mountain") || biomeName.contains("peak")) {
            // High altitude boosts Personal pool (focus)
            if (poolType == ManaPoolType.PERSONAL)
                modifier *= 1.15f;
        } else if (biomeName.contains("swamp") || biomeName.contains("jungle")) {
            // Dense magic areas boost all pools slightly
            modifier *= 1.1f;
        } else if (biomeName.contains("ice") || biomeName.contains("frozen")) {
            // Cold slows all regeneration
            modifier *= 0.85f;
        }

        return Math.max(0.0f, modifier);
    }

    /**
     * Adds mana to the pool (e.g., from potions or items). Allows overfilling beyond max capacity
     * temporarily.
     *
     * @param amount Amount of mana to add
     * @param allowOverfill Whether to allow overfilling
     */
    public void add(float amount, boolean allowOverfill) {
        int effectiveMaxCapacity = getMaxCapacity();
        if (allowOverfill) {
            // Allow overfill up to 150% of max capacity
            currentMana = Math.min(currentMana + amount, effectiveMaxCapacity * 1.5f);
        } else {
            currentMana = Math.min(currentMana + amount, effectiveMaxCapacity);
        }
    }

    /**
     * Adds mana to the pool (e.g., from potions or items).
     *
     * @param amount Amount of mana to add
     */
    public void add(float amount) {
        add(amount, false);
    }

    /**
     * Sets the current mana value directly.
     *
     * @param amount New mana value (clamped to 0-max)
     */
    public void set(float amount) {
        this.currentMana = Math.max(0, Math.min(amount, getMaxCapacity()));
    }

    /**
     * Restores mana to this pool, returning any overflow.
     *
     * @param amount Amount to restore
     * @return Overflow amount that couldn't be added
     */
    public float restore(float amount) {
        int maxCapacity = getMaxCapacity();
        float newMana = currentMana + amount;

        if (newMana > maxCapacity) {
            currentMana = maxCapacity;
            return newMana - maxCapacity; // Return overflow
        } else {
            currentMana = newMana;
            return 0f; // No overflow
        }
    }

    /**
     * Add temporary capacity modifier from buffs/items.
     */
    public void addTemporaryCapacity(float amount) {
        this.temporaryCapacityModifier += amount;
    }

    /**
     * Set drain rate (mana drain per tick from negative effects).
     */
    public void setDrainRate(float rate) {
        this.drainRate = Math.max(0, rate);
    }

    /**
     * Get current drain rate.
     */
    public float getDrainRate() {
        return drainRate;
    }

    /**
     * Reset temporary modifiers.
     */
    public void resetModifiers() {
        this.temporaryCapacityModifier = 0;
        this.drainRate = 0;
        this.efficiencyModifier = 1.0f;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public int getMaxCapacity() {
        return baseMaxCapacity + (int) temporaryCapacityModifier;
    }

    public int getBaseMaxCapacity() {
        return baseMaxCapacity;
    }

    public float getRegenRate() {
        return baseRegenRate;
    }

    public float getEfficiencyModifier() {
        return efficiencyModifier;
    }

    public float getPercentage() {
        int effectiveMax = getMaxCapacity();
        return effectiveMax > 0 ? currentMana / effectiveMax : 0;
    }

    public boolean isEmpty() {
        return currentMana <= 0;
    }

    public boolean isFull() {
        return currentMana >= getMaxCapacity();
    }

    /**
     * Check if mana is above a critical threshold (percentage).
     */
    public boolean isAboveThreshold(float percentage) {
        return getPercentage() >= percentage;
    }

    /**
     * Check if mana is overfilled beyond base capacity.
     */
    public boolean isOverfilled() {
        return currentMana > baseMaxCapacity;
    }

    /**
     * Get the corruption modifier based on how long mana has been stale. After 1200 ticks (1
     * minute) of no use, corruption starts reducing regen. After 6000 ticks (5 minutes), regen is
     * reduced to 50%.
     */
    public float getCorruptionModifier() {
        if (ticksSinceLastUse < 1200)
            return 1.0f; // No corruption for first minute
        if (ticksSinceLastUse < 6000) {
            // Linear decay from 1.0 to 0.5 over 4 minutes
            float corruptionProgress = (ticksSinceLastUse - 1200) / 4800.0f;
            return 1.0f - (corruptionProgress * 0.5f);
        }
        return 0.5f; // Maximum 50% penalty
    }

    /**
     * Check if this pool is corrupted (stale mana reducing effectiveness).
     */
    public boolean isCorrupted() {
        return ticksSinceLastUse >= 1200;
    }

    /**
     * Get mana usage efficiency ratio (consumed / (consumed + wasted)). 1.0 = perfect efficiency,
     * lower = more waste.
     */
    public float getUsageEfficiency() {
        float total = totalManaConsumed + totalManaWasted;
        return total > 0 ? totalManaConsumed / total : 1.0f;
    }

    /**
     * Get total mana consumed from this pool.
     */
    public float getTotalManaConsumed() {
        return totalManaConsumed;
    }

    /**
     * Get total mana wasted (regen overflow).
     */
    public float getTotalManaWasted() {
        return totalManaWasted;
    }

    /**
     * Reset efficiency tracking stats.
     */
    public void resetEfficiencyTracking() {
        totalManaConsumed = 0;
        totalManaWasted = 0;
        ticksSinceLastUse = 0;
    }
}
