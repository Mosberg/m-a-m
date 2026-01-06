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
 * Staff item for casting spells.
 */
public class StaffItem extends Item {
    private final int tier;

    public StaffItem(Settings settings, int tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Get selected spell from NBT
        String spellIdStr = stack.get(MAMDataComponents.SELECTED_SPELL);

        if (spellIdStr == null || spellIdStr.isEmpty()) {
            if (world.isClient()) {
                player.sendMessage(Text.translatable("item.mam.staff.no_spell"), true);
            }
            return ActionResult.FAIL;
        }

        Identifier spellId = Identifier.tryParse(spellIdStr);
        if (spellId == null) {
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            player.getItemCooldownManager().set(stack, 20); // 1 second cooldown
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

    /**
     * Sets the selected spell on this staff.
     */
    public static void setSelectedSpell(ItemStack stack, Identifier spellId) {
        stack.set(MAMDataComponents.SELECTED_SPELL, spellId.toString());
    }

    /**
     * Gets available spells for this staff based on bound gemstone.
     */
    public static List<Spell> getAvailableSpells(ItemStack stack) {
        int tier = stack.get(MAMDataComponents.TIER);
        SpellSchool school = stack.get(MAMDataComponents.SPELL_SCHOOL);

        if (school == null) {
            return List.of();
        }

        return SpellRegistry.getSpellsBySchoolAndMaxTier(school, tier);
    }
}
