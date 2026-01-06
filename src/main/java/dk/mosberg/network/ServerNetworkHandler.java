package dk.mosberg.network;

import java.util.Objects;
import dk.mosberg.MAM;
import dk.mosberg.item.SpellbookItem;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.ManaPoolType;
import dk.mosberg.mana.PlayerManaData;
import dk.mosberg.spell.SpellCaster;
import dk.mosberg.spell.SpellRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Registers server-side packet handlers.
 */
public class ServerNetworkHandler {

    @SuppressWarnings("null")
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

        // Handle spell selection from spell GUI
        ServerPlayNetworking.registerGlobalReceiver(SelectSpellPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var spell = SpellRegistry.getSpell(payload.spellId());

                if (spell.isPresent()) {
                    // Find spellbook in player's hands or inventory
                    ItemStack spellbook = ItemStack.EMPTY;
                    if (player.getMainHandStack().getItem() instanceof SpellbookItem) {
                        spellbook = player.getMainHandStack();
                    } else if (player.getOffHandStack().getItem() instanceof SpellbookItem) {
                        spellbook = player.getOffHandStack();
                    } else {
                        // Search inventory for spellbook
                        for (int i = 0; i < player.getInventory().size(); i++) {
                            ItemStack stack = player.getInventory().getStack(i);
                            if (stack.getItem() instanceof SpellbookItem) {
                                spellbook = stack;
                                break;
                            }
                        }
                    }

                    if (!spellbook.isEmpty()) {
                        Identifier spellId = payload.spellId();
                        SpellbookItem.setSelectedSpell(spellbook, spellId);
                        MAM.LOGGER.info("Spell {} bound to spellbook for player {}",
                                spell.get().getName(), player.getName().getString());
                    } else {
                        MAM.LOGGER.warn("Player {} selected spell but has no spellbook",
                                player.getName().getString());
                    }
                } else {
                    MAM.LOGGER.warn("Player {} tried to select unknown spell: {}",
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
        @SuppressWarnings("null")
        dk.mosberg.mana.PlayerCastingData castingData = Objects.requireNonNull(
                player.getAttachedOrCreate(ManaAttachments.PLAYER_CASTING,
                        dk.mosberg.mana.PlayerCastingData::new),
                "Player casting data attachment should always exist");

        PlayerManaData manaData = castingData.getManaData();

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
