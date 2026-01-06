package dk.mosberg.mana;

import java.util.Objects;
import dk.mosberg.MAM;
import dk.mosberg.config.ServerConfig;
import dk.mosberg.network.ServerNetworkHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Handles server-side mana regeneration for all players.
 */
public class ManaRegenerationHandler {

    @SuppressWarnings("null")
    public static void register() {
        ServerConfig config = ServerConfig.getInstance();

        // Initial sync on join
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerNetworkHandler.syncManaToClient(handler.getPlayer());
        });

        final int syncInterval = Math.max(0, config.manaSyncIntervalTicks);
        final boolean syncEnabled = config.enableManaSyncPackets && syncInterval > 0;

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int currentTick = (int) (server.getOverworld().getTime() % Integer.MAX_VALUE);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerManaData manaData =
                        Objects.requireNonNull(
                                player.getAttachedOrCreate(ManaAttachments.PLAYER_MANA,
                                        PlayerManaData::new),
                                "Player mana attachment should always exist");
                manaData.tickRegeneration();

                if (syncEnabled && currentTick % syncInterval == 0) {
                    ServerNetworkHandler.syncManaToClient(player);
                }
            }
        });

        MAM.LOGGER.info("Registered mana regeneration handler");
    }
}
