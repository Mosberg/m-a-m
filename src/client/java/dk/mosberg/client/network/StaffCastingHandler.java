package dk.mosberg.client.network;

import dk.mosberg.item.SpellbookItem;
import dk.mosberg.network.CastSpellPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

/**
 * Client-side handler for spellbook spell casting. Intercepts spellbook use events and sends cast
 * spell packets to server.
 */
@Environment(EnvType.CLIENT)
public class StaffCastingHandler {

    /**
     * Register the spellbook casting handler.
     */
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);

            // Check if player is using a spellbook
            if (!(stack.getItem() instanceof SpellbookItem)) {
                return ActionResult.PASS;
            }

            // Get selected spell from spellbook
            Identifier spellId = SpellbookItem.getSelectedSpell(stack);
            if (spellId == null) {
                return ActionResult.PASS;
            }

            // Send cast spell packet to server
            ClientPlayNetworking.send(new CastSpellPayload(spellId));

            return ActionResult.PASS;
        });
    }
}
