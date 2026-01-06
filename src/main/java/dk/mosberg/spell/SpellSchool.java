package dk.mosberg.spell;

/**
 * The four spell schools.
 */
public enum SpellSchool {
    AIR("Air", 0x87CEEB), EARTH("Earth", 0x8B4513), FIRE("Fire", 0xFF4500), WATER("Water",
            0x1E90FF);

    private final String displayName;
    private final int color;

    SpellSchool(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "school.mam." + name().toLowerCase();
    }
}
