package dk.mosberg.item;

import java.util.List;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellRegistry;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Staff item that provides buffs to spells when equipped.
 */
public class StaffItem extends Item {
    private final int tier;
    private static final int MIN_TIER = 1;
    private static final int MAX_TIER = 4;

    public StaffItem(Settings settings, int tier) {
        super(settings);
        this.tier = clampTier(tier);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        // Staff is a passive buff item, doesn't cast spells
        if (world.isClient()) {
            player.sendMessage(Text.translatable("item.mam.staff.passive_item"), true);
        }
        return ActionResult.PASS;
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
     * Sets the selected spell on this staff.
     */
    public static void setSelectedSpell(ItemStack stack, Identifier spellId) {
        stack.set(MAMDataComponents.SELECTED_SPELL, spellId.toString());
    }

    public static Identifier getSelectedSpell(ItemStack stack) {
        String spellIdStr = stack.get(MAMDataComponents.SELECTED_SPELL);
        return spellIdStr != null ? Identifier.of(spellIdStr) : null;
    }

    /**
     * Gets available spells for this staff based on bound gemstone.
     */
    public static List<Spell> getAvailableSpells(ItemStack stack) {
        Integer tierValue = stack.get(MAMDataComponents.TIER);
        if (tierValue == null) {
            tierValue = null; // explicit for readability
        }
        SpellSchool school = stack.get(MAMDataComponents.SPELL_SCHOOL);

        if (school == null) {
            return List.of();
        }

        int clampedTier = tierValue != null ? clampTier(tierValue) : MIN_TIER;
        if (tierValue == null || tierValue != clampedTier) {
            stack.set(MAMDataComponents.TIER, clampedTier);
        }

        return SpellRegistry.getSpellsBySchoolAndMaxTier(school, clampedTier);
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();
        stack.set(MAMDataComponents.TIER, tier);
        return stack;
    }

}
