package dk.mosberg.mana;

/**
 * Represents a single mana pool with capacity, current amount, and regeneration rate.
 */
public class ManaPool {
    private final int maxCapacity;
    private float currentMana;
    private final float regenRate;

    public ManaPool(int maxCapacity, float regenRate) {
        this.maxCapacity = maxCapacity;
        this.currentMana = maxCapacity;
        this.regenRate = regenRate;
    }

    public ManaPool(int maxCapacity, float currentMana, float regenRate) {
        this.maxCapacity = maxCapacity;
        this.currentMana = Math.min(currentMana, maxCapacity);
        this.regenRate = regenRate;
    }

    /**
     * Attempts to consume mana from this pool.
     * 
     * @param amount Amount of mana to consume
     * @return true if mana was consumed, false if insufficient
     */
    public boolean consume(float amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    /**
     * Regenerates mana based on the regen rate. Called each tick.
     */
    public void regenerate() {
        if (currentMana < maxCapacity) {
            currentMana = Math.min(currentMana + regenRate, maxCapacity);
        }
    }

    /**
     * Adds mana to the pool (e.g., from potions or items).
     * 
     * @param amount Amount of mana to add
     */
    public void add(float amount) {
        currentMana = Math.min(currentMana + amount, maxCapacity);
    }

    /**
     * Sets the current mana value directly.
     * 
     * @param amount New mana value (clamped to 0-max)
     */
    public void set(float amount) {
        this.currentMana = Math.max(0, Math.min(amount, maxCapacity));
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public float getRegenRate() {
        return regenRate;
    }

    public float getPercentage() {
        return maxCapacity > 0 ? currentMana / maxCapacity : 0;
    }

    public boolean isEmpty() {
        return currentMana <= 0;
    }

    public boolean isFull() {
        return currentMana >= maxCapacity;
    }
}
