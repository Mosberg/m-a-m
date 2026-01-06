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
 *
 * TODO: Add charge-up mechanics (hold to build power) TODO: Implement spell aimed mode (crosshair
 * targeting) TODO: Add continuous casting support (hold for duration) TODO: Implement combo
 * detection (rapid spell succession) TODO: Add casting cancellation (ESC key) TODO: Implement staff
 * attachment animations TODO: Add client-side prediction for instant feedback TODO: Implement spell
 * prep animations/effects
 */
@Environment(EnvType.CLIENT)
public class StaffCastingHandler {

    @SuppressWarnings("null")
    private static final UseItemCallback SPELL_CASTING_CALLBACK = (player, world, hand) -> {
        if (!world.isClient()) {
            return ActionResult.PASS;
        }

        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof SpellbookItem)) {
            return ActionResult.PASS;
        }

        Identifier spellId = SpellbookItem.getSelectedSpell(stack);
        if (spellId != null) {
            ClientPlayNetworking.send(new CastSpellPayload(spellId));
        }
        return ActionResult.PASS;
    };

    /**
     * Register the spellbook casting handler.
     */
    public static void register() {
        UseItemCallback.EVENT.register(SPELL_CASTING_CALLBACK);
    }
}
