package dk.mosberg.mana;

/**
 * Three mana pool types in the three-pool system.
 */
public enum ManaPoolType {
    PERSONAL(250, 0.5f, "Personal", 0xFF4A90E2), // Blue
    AURA(500, 0.25f, "Aura", 0xFF9B59B6), // Purple
    RESERVE(1000, 0.1f, "Reserve", 0xFFE74C3C); // Red

    private final int defaultCapacity;
    private final float defaultRegenRate;
    private final String displayName;
    private final int color;

    ManaPoolType(int defaultCapacity, float defaultRegenRate, String displayName, int color) {
        this.defaultCapacity = defaultCapacity;
        this.defaultRegenRate = defaultRegenRate;
        this.displayName = displayName;
        this.color = color;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public float getDefaultRegenRate() {
        return defaultRegenRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "mana.mam.pool." + name().toLowerCase();
    }
}
