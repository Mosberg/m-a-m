package dk.mosberg.spell;

/**
 * Spell rarity tiers affecting drop rates, power, and visual effects.
 */
public enum SpellRarity {
    COMMON("Common", 0xFFFFFF, 1.0f), UNCOMMON("Uncommon", 0x55FF55, 1.1f), RARE("Rare", 0x5555FF,
            1.25f), EPIC("Epic", 0xAA00AA, 1.5f), LEGENDARY("Legendary", 0xFFAA00, 2.0f);

    private final String displayName;
    private final int color;
    private final float powerModifier;

    SpellRarity(String displayName, int color, float powerModifier) {
        this.displayName = displayName;
        this.color = color;
        this.powerModifier = powerModifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    /**
     * Power modifier applied to spell effects (damage, heal, range, etc.). Common: 1.0x, Uncommon:
     * 1.1x, Rare: 1.25x, Epic: 1.5x, Legendary: 2.0x
     */
    public float getPowerModifier() {
        return powerModifier;
    }

    public String getTranslationKey() {
        return "rarity.mam." + name().toLowerCase();
    }

    /**
     * Get rarity from tier. Higher tiers have better chance of higher rarity.
     */
    public static SpellRarity fromTier(int tier) {
        return switch (tier) {
            case 1 -> COMMON;
            case 2 -> UNCOMMON;
            case 3 -> RARE;
            case 4 -> EPIC;
            default -> COMMON;
        };
    }
}
