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
    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 4;

    public SpellbookItem(Settings settings, int tier) {
        super(settings);
        this.tier = clampTier(tier);
    }

    public int getTier() {
        return tier;
    }

    private static int clampTier(int value) {
        return Math.max(MIN_TIER, Math.min(MAX_TIER, value));
    }

    private int resolveTier(ItemStack stack) {
        Integer stored = stack.get(MAMDataComponents.TIER);
        int resolved = stored != null ? clampTier(stored) : tier;
        if (stored == null || stored != resolved) {
            stack.set(MAMDataComponents.TIER, resolved);
        }
        return resolved;
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

    public static boolean hasSelectedSpell(ItemStack spellbook) {
        return spellbook.get(MAMDataComponents.SELECTED_SPELL) != null;
    }

    public static void clearSelectedSpell(ItemStack spellbook) {
        spellbook.remove(MAMDataComponents.SELECTED_SPELL);
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
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.set(MAMDataComponents.TIER, tier);
        return stack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Get selected spell from spellbook
        Identifier spellId = getSelectedSpell(stack);

        // Ensure tier is clamped/present on use
        resolveTier(stack);

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

    // TODO: Add spellbook enchantment support
    // TODO: Add spellbook experience/leveling system
    // TODO: Add spellbook quick-cast shortcuts
    // TODO: Add spellbook spell organization/sorting
    // TODO: Add spellbook custom spell descriptions
    // TODO: Add spellbook rarity/quality tiers
    // TODO: Add spellbook passive bonuses
    // TODO: Add spellbook spell requirement checking
    // TODO: Add spellbook craft-ability metrics
    // TODO: Add spellbook sharing/trading mechanics
}
