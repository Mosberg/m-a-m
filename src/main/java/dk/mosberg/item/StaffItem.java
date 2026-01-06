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

    public StaffItem(Settings settings, int tier) {
        super(settings);
        this.tier = tier;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        // Staff is a passive buff item, doesn't cast spells
        if (world.isClient()) {
            player.sendMessage(Text.translatable("item.mam.staff.passive_item"), true);
        }
        return ActionResult.PASS;
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
        Integer tierValue = stack.get(MAMDataComponents.TIER);
        SpellSchool school = stack.get(MAMDataComponents.SPELL_SCHOOL);

        if (tierValue == null || school == null) {
            return List.of();
        }

        return SpellRegistry.getSpellsBySchoolAndMaxTier(school, tierValue);
    }
}
