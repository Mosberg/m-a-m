package dk.mosberg.spell;

/**
 * The five spell cast types supported by the magic system.
 */
public enum SpellCastType {
    PROJECTILE("Projectile", "Fires a projectile towards the target"), AOE("Area of Effect",
            "Affects all entities in an area"), UTILITY("Utility",
                    "Provides buffs, teleportation, or other utility"), RITUAL("Ritual",
                            "Requires channeling time and consumes mana over time"), SYNERGY(
                                    "Synergy", "Combines effects from multiple schools");

    private final String displayName;
    private final String description;

    SpellCastType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getTranslationKey() {
        return "spell.mam.cast_type." + name().toLowerCase();
    }
}
