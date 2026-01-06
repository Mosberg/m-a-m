package dk.mosberg.item;

import dk.mosberg.network.OpenSpellBookPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * Spellbook item for managing and selecting spells.
 */
public class SpellbookItem extends Item {
    private final int tier;

    public SpellbookItem(Settings settings, int tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient()) {
            // On server, send packet to client to open GUI
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new OpenSpellBookPayload(tier));
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack);
    }

    public int getTier() {
        return tier;
    }
}
