package dk.mosberg.mana;

import java.util.Objects;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles server-side mana regeneration for all players.
 */
public class ManaRegenerationHandler {

    @SuppressWarnings("null")
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerManaData manaData =
                        Objects.requireNonNull(
                                player.getAttachedOrCreate(ManaAttachments.PLAYER_MANA,
                                        PlayerManaData::new),
                                "Player mana attachment should always exist");
                manaData.tickRegeneration();
            }
        });

        MAM.LOGGER.info("Registered mana regeneration handler");
    }
}
