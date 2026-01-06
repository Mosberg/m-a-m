package dk.mosberg.client.tooltip;

import java.util.List;
import dk.mosberg.item.MAMDataComponents;
import dk.mosberg.item.SpellbookItem;
import dk.mosberg.item.StaffItem;
import dk.mosberg.spell.SpellSchool;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

/**
 * Registers rich item tooltips for client-side display.
 */
public final class ItemTooltips {
    private ItemTooltips() {}

    public static void register() {
        ItemTooltipCallback.EVENT.register(ItemTooltips::handleTooltip);
    }

    private static void handleTooltip(ItemStack stack, Item.TooltipContext context,
            TooltipType type, List<Text> lines) {
        if (stack.getItem() instanceof SpellbookItem book) {
            addSpellbookTooltip(stack, book, lines);
        } else if (stack.getItem() instanceof StaffItem staff) {
            addStaffTooltip(stack, staff, lines);
        }
    }

    private static void addSpellbookTooltip(ItemStack stack, SpellbookItem book, List<Text> lines) {
        Integer tier = stack.get(MAMDataComponents.TIER);
        if (tier != null) {
            lines.add(Text.translatable("tooltip.mam.spellbook.tier", tier));
        }

        var selected = SpellbookItem.getSelectedSpell(stack);
        if (selected != null) {
            lines.add(Text.translatable("message.mam.spell.selected", selected));
        } else {
            lines.add(Text.translatable("item.mam.spellbook.no_spell"));
        }
    }

    private static void addStaffTooltip(ItemStack stack, StaffItem staff, List<Text> lines) {
        Integer tier = stack.get(MAMDataComponents.TIER);
        if (tier != null) {
            lines.add(Text.translatable("tooltip.mam.staff.tier", tier));
        }

        SpellSchool school = stack.get(MAMDataComponents.SPELL_SCHOOL);
        if (school != null) {
            lines.add(Text.translatable("tooltip.mam.staff.school",
                    Text.translatable("school.mam." + school.name().toLowerCase())));
        }

        var selected = StaffItem.getSelectedSpell(stack);
        if (selected != null) {
            lines.add(Text.translatable("message.mam.spell.selected", selected));
        }
    }
}
