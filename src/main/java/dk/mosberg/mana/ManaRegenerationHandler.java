package dk.mosberg.mana;

import dk.mosberg.MAM;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles server-side mana regeneration for all players.
 */
public class ManaRegenerationHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerManaData manaData = player.getAttachedOrCreate(ManaAttachments.PLAYER_MANA,
                        PlayerManaData::new);
                manaData.tickRegeneration();
            }
        });

        MAM.LOGGER.info("Registered mana regeneration handler");
    }
}
