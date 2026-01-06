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
        clientMana.updatePool(ManaPoolType.PERSONAL, Math.round(personalMax), personalMana,
                clientMana.getPool(ManaPoolType.PERSONAL).getRegenRate());
        clientMana.updatePool(ManaPoolType.AURA, Math.round(auraMax), auraMana,
                clientMana.getPool(ManaPoolType.AURA).getRegenRate());
        clientMana.updatePool(ManaPoolType.RESERVE, Math.round(reserveMax), reserveMana,
                clientMana.getPool(ManaPoolType.RESERVE).getRegenRate());

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
