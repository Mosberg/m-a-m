package dk.mosberg.network;

import dk.mosberg.MAM;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.ManaPoolType;
import dk.mosberg.mana.PlayerManaData;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellCaster;
import dk.mosberg.spell.SpellRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers server-side packet handlers.
 */
public class ServerNetworkHandler {

    public static void register() {
        // Handle spell casting from client
        ServerPlayNetworking.registerGlobalReceiver(CastSpellPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var spell = SpellRegistry.getSpell(payload.spellId());

                if (spell.isPresent()) {
                    SpellCaster.castSpell(player, spell.get());
                } else {
                    MAM.LOGGER.warn("Player {} tried to cast unknown spell: {}",
                            player.getName().getString(), payload.spellId());
                }
            });
        });

        MAM.LOGGER.info("Registered server network handlers");
    }

    /**
     * Sends mana data to a client.
     */
    public static void syncManaToClient(net.minecraft.server.network.ServerPlayerEntity player) {
        PlayerManaData manaData = player.getAttachedOrCreate(ManaAttachments.PLAYER_MANA);

        ManaSyncPayload payload =
                new ManaSyncPayload(manaData.getPool(ManaPoolType.PERSONAL).getCurrentMana(),
                        manaData.getPool(ManaPoolType.PERSONAL).getMaxCapacity(),
                        manaData.getPool(ManaPoolType.AURA).getCurrentMana(),
                        manaData.getPool(ManaPoolType.AURA).getMaxCapacity(),
                        manaData.getPool(ManaPoolType.RESERVE).getCurrentMana(),
                        manaData.getPool(ManaPoolType.RESERVE).getMaxCapacity(),
                        manaData.getActivePriority().name());

        ServerPlayNetworking.send(player, payload);
    }
}
