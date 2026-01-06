package dk.mosberg.client.network;

import dk.mosberg.mana.ManaPoolType;
import dk.mosberg.mana.PlayerManaData;

/**
 * Client-side storage for synced mana data.
 *
 * TODO: Implement client-side mana prediction (estimate next tick value) TODO: Add mana change
 * history tracking for smooth animations TODO: Implement mana spike detection (large changes
 * warrant warnings) TODO: Add status effects tracking on client TODO: Implement visual feedback
 * queuing system TODO: Add mana pool priority changing via UI TODO: Implement threshold callbacks
 * (low mana warnings) TODO: Add statistics tracking (total cast, total damage, etc.)
 */
public class ClientManaData {
    private static final PlayerManaData clientMana = new PlayerManaData();

    public static void updateFromServer(float personalMana, float personalMax, float auraMana,
            float auraMax, float reserveMana, float reserveMax, String activePriority,
            float personalRegen, float auraRegen, float reserveRegen) {
        clientMana.updatePool(ManaPoolType.PERSONAL, Math.round(personalMax), personalMana,
                personalRegen);
        clientMana.updatePool(ManaPoolType.AURA, Math.round(auraMax), auraMana, auraRegen);
        clientMana.updatePool(ManaPoolType.RESERVE, Math.round(reserveMax), reserveMana,
                reserveRegen);

        try {
            clientMana.setActivePriority(ManaPoolType.valueOf(activePriority));
        } catch (IllegalArgumentException e) {
            clientMana.setActivePriority(ManaPoolType.PERSONAL);
        }
    }

    public static PlayerManaData get() {
        return clientMana;
    }
}
