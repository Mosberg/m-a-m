package dk.mosberg.client.network;

import dk.mosberg.mana.ManaPoolType;
import dk.mosberg.mana.PlayerManaData;

/**
 * Client-side storage for synced mana data.
 */
public class ClientManaData {
    private static final PlayerManaData clientMana = new PlayerManaData();

    public static void updateFromServer(float personalMana, float personalMax, float auraMana,
            float auraMax, float reserveMana, float reserveMax, String activePriority) {
        clientMana.getPool(ManaPoolType.PERSONAL).set(personalMana);
        clientMana.getPool(ManaPoolType.AURA).set(auraMana);
        clientMana.getPool(ManaPoolType.RESERVE).set(reserveMana);

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
