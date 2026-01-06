package dk.mosberg.mana;

/**
 * Represents a single mana pool with capacity, current amount, and regeneration rate.
 *
 * TODO: Add temporary capacity modifiers (e.g., buffs increase max capacity) TODO: Implement
 * conditional regeneration (only regen in specific biomes/dimensions) TODO: Add drain effects
 * (spells/mobs drain mana over time) TODO: Implement overfill mechanics (temporary capacity
 * overflow with penalty) TODO: Add efficiency tracking (waste vs. actual usage ratios) TODO:
 * Implement resonance effects (repeated use from same pool increases efficiency) TODO: Add critical
 * points (specific thresholds that unlock abilities) TODO: Implement pool corruption effects (stale
 * mana becomes less useful)
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
        ticksSinceLastUse++;

        int effectiveMaxCapacity = getMaxCapacity();
        if (currentMana < effectiveMaxCapacity) {
            float netRegen = baseRegenRate - drainRate;

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
