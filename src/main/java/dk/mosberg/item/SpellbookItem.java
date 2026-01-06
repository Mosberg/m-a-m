package dk.mosberg.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (world.isClient) {
            // TODO: Open spell selection GUI
            player.sendMessage(Text.translatable("item.mam.spellbook.open"), true);
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.translatable(this.getTranslationKey(stack));
    }

    public int getTier() {
        return tier;
    }
}
