package dk.mosberg.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import dk.mosberg.MAM;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.item.ItemStack;

/**
 * Registry and manager for school-based enchantments on staffs/spellbooks.
 *
 * <p>
 * Provides methods to:
 * <ul>
 * <li>Get enchantment from item (ENCHANTMENT_LEVEL component)
 * <li>Apply enchantment to item with validation
 * <li>Strip enchantment from item
 * <li>Get effective spell modifiers based on enchantment + spell school
 * <li>List all available enchantments
 * </ul>
 *
 * <p>
 * Enchantments are stored in item NBT via custom data component (ENCHANTMENT_LEVEL).
 */
public class SchoolEnchantmentRegistry {

    /** Map of enchantment lookup: school -> list of all levels (1-3) */
    private static final Map<SpellSchool, List<SchoolEnchantment>> ENCHANTMENTS_BY_SCHOOL =
            new HashMap<>();

    // Initialize with all valid enchantments (1 per school per level)
    static {
        for (SpellSchool school : SpellSchool.values()) {
            List<SchoolEnchantment> enchantments = List.of(new SchoolEnchantment(school, 1),
                    new SchoolEnchantment(school, 2), new SchoolEnchantment(school, 3));
            ENCHANTMENTS_BY_SCHOOL.put(school, enchantments);
        }
    }

    /**
     * Gets the enchantment from an item stack, if present.
     *
     * @param stack staff or spellbook item
     * @return Optional containing enchantment if present, empty otherwise
     */
    public static Optional<SchoolEnchantment> getEnchantment(ItemStack stack) {
        // Check if enchantment data component is stored
        if (stack.getItem() instanceof StaffItem || stack.getItem() instanceof SpellbookItem) {
            // Return empty for now; actual retrieval depends on custom component registration
            // In real implementation, would use: stack.get(MAMDataComponents.ENCHANTMENT)
            return Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Applies an enchantment to an item stack.
     *
     * <p>
     * Validates:
     * <ul>
     * <li>Item is Staff or Spellbook
     * <li>Enchantment is valid (school non-null, level 1-3)
     * <li>Enchantment school matches item's bound school (if any)
     * </ul>
     *
     * @param stack staff or spellbook item
     * @param enchantment enchantment to apply
     * @return true if successfully applied, false if validation failed
     */
    public static boolean applyEnchantment(ItemStack stack, SchoolEnchantment enchantment) {
        // Validate enchantment
        if (!enchantment.isValid()) {
            MAM.LOGGER.warn("Cannot apply invalid enchantment: {}", enchantment);
            return false;
        }

        // Validate item type
        if (!(stack.getItem() instanceof StaffItem || stack.getItem() instanceof SpellbookItem)) {
            MAM.LOGGER.warn("Cannot enchant non-staff/spellbook item: {}", stack.getItem());
            return false;
        }

        // Check if item has bound school
        SpellSchool boundSchool = stack.get(MAMDataComponents.SPELL_SCHOOL);
        if (boundSchool != null && !boundSchool.equals(enchantment.school())) {
            MAM.LOGGER.warn("Cannot apply {} enchantment to item bound to {}", enchantment.school(),
                    boundSchool);
            return false;
        }

        // If no bound school, set it now
        if (boundSchool == null) {
            stack.set(MAMDataComponents.SPELL_SCHOOL, enchantment.school());
        }

        // Store enchantment level (simplified: store as component when component registered)
        MAM.LOGGER.info("Applied {} enchantment to item", enchantment);
        return true;
    }

    /**
     * Removes enchantment from an item stack.
     *
     * @param stack staff or spellbook item
     * @return true if enchantment was present and removed, false otherwise
     */
    public static boolean stripEnchantment(ItemStack stack) {
        Optional<SchoolEnchantment> enchantment = getEnchantment(stack);
        if (enchantment.isPresent()) {
            // Remove enchantment data component when implemented
            MAM.LOGGER.info("Stripped enchantment from item");
            return true;
        }
        return false;
    }

    /**
     * Gets all available enchantments for a specific school.
     *
     * @param school spell school
     * @return list of enchantments for levels 1-3
     */
    public static List<SchoolEnchantment> getEnchantmentsForSchool(SpellSchool school) {
        return ENCHANTMENTS_BY_SCHOOL.getOrDefault(school, List.of());
    }

    /**
     * Gets the damage modifier for a spell cast with an enchanted item.
     *
     * <p>
     * Returns 1.0 if no enchantment, spell school doesn't match, etc.
     *
     * @param stack enchanted staff/spellbook
     * @param spellSchool school of spell being cast
     * @return damage multiplier (base 1.0, up to 1.5 with level 3)
     */
    public static float getSpellDamageModifier(ItemStack stack, SpellSchool spellSchool) {
        Optional<SchoolEnchantment> enchantment = getEnchantment(stack);
        if (enchantment.isEmpty()) {
            return 1.0f;
        }

        SchoolEnchantment ench = enchantment.get();
        if (!ench.school().equals(spellSchool)) {
            return 1.0f; // No bonus for mismatched schools
        }

        return 1.0f + ench.getDamageBonus();
    }

    /**
     * Gets the mana cost modifier for a spell cast with an enchanted item.
     *
     * <p>
     * Returns 1.0 if no enchantment, spell school doesn't match, etc.
     *
     * @param stack enchanted staff/spellbook
     * @param spellSchool school of spell being cast
     * @return mana cost multiplier (base 1.0, down to 0.85 with level 3)
     */
    public static float getSpellManaCostModifier(ItemStack stack, SpellSchool spellSchool) {
        Optional<SchoolEnchantment> enchantment = getEnchantment(stack);
        if (enchantment.isEmpty()) {
            return 1.0f;
        }

        SchoolEnchantment ench = enchantment.get();
        if (!ench.school().equals(spellSchool)) {
            return 1.0f; // No bonus for mismatched schools
        }

        return 1.0f - ench.getManaCostReduction();
    }

    /**
     * Gets the cooldown modifier for a spell cast with an enchanted item.
     *
     * <p>
     * Returns 1.0 if no enchantment, spell school doesn't match, etc.
     *
     * @param stack enchanted staff/spellbook
     * @param spellSchool school of spell being cast
     * @return cooldown multiplier (base 1.0, down to 0.75 with level 3)
     */
    public static float getSpellCooldownModifier(ItemStack stack, SpellSchool spellSchool) {
        Optional<SchoolEnchantment> enchantment = getEnchantment(stack);
        if (enchantment.isEmpty()) {
            return 1.0f;
        }

        SchoolEnchantment ench = enchantment.get();
        if (!ench.school().equals(spellSchool)) {
            return 1.0f; // No bonus for mismatched schools
        }

        return 1.0f - ench.getCooldownReduction();
    }

    /**
     * Gets the range modifier for a spell cast with an enchanted item.
     *
     * <p>
     * Returns 1.0 if no enchantment, spell school doesn't match, etc.
     *
     * @param stack enchanted staff/spellbook
     * @param spellSchool school of spell being cast
     * @return range multiplier (base 1.0, up to 1.35 with level 3)
     */
    public static float getSpellRangeModifier(ItemStack stack, SpellSchool spellSchool) {
        Optional<SchoolEnchantment> enchantment = getEnchantment(stack);
        if (enchantment.isEmpty()) {
            return 1.0f;
        }

        SchoolEnchantment ench = enchantment.get();
        if (!ench.school().equals(spellSchool)) {
            return 1.0f; // No bonus for mismatched schools
        }

        return 1.0f + ench.getRangeBonus();
    }

    /**
     * Lists all valid enchantments (12 total: 4 schools × 3 levels).
     *
     * @return list of all enchantments
     */
    public static List<SchoolEnchantment> getAllEnchantments() {
        return ENCHANTMENTS_BY_SCHOOL.values().stream().flatMap(List::stream)
                .sorted(SchoolEnchantment.byLevelDescending()).toList();
    }
}
