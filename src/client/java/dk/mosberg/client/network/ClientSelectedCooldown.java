package dk.mosberg.client.network;

import net.minecraft.util.Identifier;

/**
 * Client-side storage for currently selected spell cooldown.
 */
public class ClientSelectedCooldown {
    private static Identifier currentSpellId = null;
    private static float remainingSeconds = 0f;

    public static void update(Identifier spellId, float remaining) {
        currentSpellId = spellId;
        remainingSeconds = Math.max(remaining, 0f);
    }

    public static Identifier getCurrentSpellId() {
        return currentSpellId;
    }

    public static float getRemainingSeconds() {
        return remainingSeconds;
    }
}
