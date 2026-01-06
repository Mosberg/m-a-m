package dk.mosberg.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
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

    public int getTier() {
        return tier;
    }

    /**
     * Set the selected spell on a spellbook
     * 
     * @param spellbook The spellbook item stack
     * @param spellId The spell identifier to set
     */
    public static void setSelectedSpell(ItemStack spellbook, Identifier spellId) {
        spellbook.set(MAMDataComponents.SELECTED_SPELL, spellId.toString());
    }

    /**
     * Get the selected spell from a spellbook
     * 
     * @param spellbook The spellbook item stack
     * @return The selected spell identifier, or null if none selected
     */
    public static Identifier getSelectedSpell(ItemStack spellbook) {
        String spellIdStr = spellbook.get(MAMDataComponents.SELECTED_SPELL);
        return spellIdStr != null ? Identifier.of(spellIdStr) : null;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Get selected spell from spellbook
        Identifier spellId = getSelectedSpell(stack);

        if (spellId == null) {
            if (world.isClient()) {
                player.sendMessage(Text.translatable("item.mam.spellbook.no_spell"), true);
            }
            return ActionResult.FAIL;
        }

        // Server handles spell casting via network handler
        // Client sends packet in StaffCastingHandler
        return ActionResult.SUCCESS;
    }

    @Override
    public Text getName(ItemStack stack) {
        return super.getName(stack);
    }
}
