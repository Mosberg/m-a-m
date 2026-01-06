package dk.mosberg.mana;

import java.util.Objects;
import dk.mosberg.MAM;
import dk.mosberg.config.ServerConfig;
import dk.mosberg.network.ServerNetworkHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

/**
 * Handles server-side mana regeneration and cooldown ticking for all players. Applies conditional
 * regen modifiers based on environment, player state, and combat status.
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
                PlayerCastingData castingData = Objects.requireNonNull(
                        player.getAttachedOrCreate(ManaAttachments.PLAYER_CASTING,
                                PlayerCastingData::new),
                        "Player casting data attachment should always exist");

                // Apply conditional regen modifiers
                applyConditionalModifiers(player, castingData);

                // Tick mana regen and cooldown timers
                castingData.tick();

                if (syncEnabled && currentTick % syncInterval == 0) {
                    ServerNetworkHandler.syncManaToClient(player);
                }
            }
        });

        MAM.LOGGER.info("Registered mana regeneration and cooldown handler");
    }

    /**
     * Applies conditional mana regeneration modifiers based on player state.
     */
    private static void applyConditionalModifiers(ServerPlayerEntity player,
            PlayerCastingData castingData) {
        PlayerManaData manaData = castingData.getManaData();
        float efficiencyModifier = 1.0f;

        // Check safe zone vs combat (no recent damage)
        boolean inCombat = player.age - player.getLastAttackTime() < 100; // 5 seconds
        if (!inCombat && player.isSneaking()) {
            // Meditation mode: +50% regen when sneaking and not in combat
            efficiencyModifier *= 1.5f;
        } else if (inCombat) {
            // Combat: -25% regen
            efficiencyModifier *= 0.75f;
        }

        // Check environment
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        BlockPos pos = player.getBlockPos();

        // Biome-based modifiers
        Biome biome = world.getBiome(pos).value();
        if (biome.getTemperature() > 1.5f) {
            // Hot biomes: -10% regen
            efficiencyModifier *= 0.9f;
        } else if (biome.getTemperature() < 0.2f) {
            // Cold biomes: +10% regen
            efficiencyModifier *= 1.1f;
        }

        // Underwater: +20% regen (water conducts mana)
        if (player.isSubmergedInWater()) {
            efficiencyModifier *= 1.2f;
        }

        // Low health: -50% regen (body prioritizes healing)
        if (player.getHealth() < player.getMaxHealth() * 0.3f) {
            efficiencyModifier *= 0.5f;
        }

        // Apply the calculated modifier
        manaData.setEfficiencyModifier(efficiencyModifier);
    }
}
