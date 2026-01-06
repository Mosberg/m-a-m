package dk.mosberg.spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dk.mosberg.MAM;
import net.minecraft.util.Identifier;

/**
 * Registry for spell variants: data-driven modifiers applied to spells after loading.
 *
 * Variants enable: - Difficulty tuning without JSON duplication - Event-specific spell
 * modifications (seasonal, balance patches) - Server-side custom spell tweaks
 *
 * Usage: - SpellRegistry applies all variants to their target spells post-inheritance - Variants
 * can be registered programmatically or loaded from data packs
 *
 * Thread-safe: variants are immutable and can be applied concurrently
 */
public class SpellVariantRegistry {

    // Map from spell ID to list of variants affecting it
    private static final Map<Identifier, List<SpellVariant>> VARIANTS_BY_SPELL = new HashMap<>();

    private SpellVariantRegistry() {}

    /**
     * Get all variants affecting a specific spell.
     *
     * @param spellId The spell's identifier
     * @return List of variants (empty if none)
     */
    public static List<SpellVariant> getVariants(Identifier spellId) {
        return VARIANTS_BY_SPELL.getOrDefault(spellId, List.of());
    }

    /**
     * Check if a spell has any variants.
     */
    public static boolean hasVariants(Identifier spellId) {
        return VARIANTS_BY_SPELL.containsKey(spellId);
    }

    /**
     * Apply all variants for a spell, returning a modified copy.
     *
     * Variants are applied in order; later variants override earlier numeric multipliers.
     *
     * @param spell The spell to modify
     * @return Modified spell if variants exist, original spell otherwise
     */
    public static Spell applyVariants(Spell spell) {
        List<SpellVariant> variants = getVariants(spell.getId());
        if (variants.isEmpty()) {
            return spell;
        }

        Spell result = spell;
        for (SpellVariant variant : variants) {
            result = variant.apply(result);
        }

        // Log applied variants for debugging
        MAM.LOGGER.debug("Applied {} variant(s) to spell {}", variants.size(), spell.getId());

        return result;
    }

    /**
     * Register a variant programmatically (for testing or runtime modifications).
     *
     * @param variant The variant to register
     */
    public static void registerVariant(SpellVariant variant) {
        VARIANTS_BY_SPELL.computeIfAbsent(variant.spellId(), id -> new java.util.ArrayList<>())
                .add(variant);
    }

    /**
     * Clear all registered variants (for testing or resource reloads).
     */
    public static void clear() {
        VARIANTS_BY_SPELL.clear();
    }
}
