package dk.mosberg.spell;

import java.util.List;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

/**
 * Represents a spell variant: data-driven modifiers applied to spells post-load.
 *
 * Variants enable: - Difficulty tuning without JSON duplication - Event-specific spell
 * modifications (seasonal, balance patches) - Server-side custom spell tweaks
 *
 * Example variant: "variants/difficulty_hard/fire_strike.json" applies 1.3x damage multiplier to
 * Fire Strike
 */
public record SpellVariant(Identifier spellId, float damageMultiplier, float manaCostMultiplier,
        float cooldownMultiplier, List<String> tagsAdded, List<String> tagsRemoved,
        List<Spell.StatusEffectEntry> statusEffectsAdded, int tierOffset) {

    public SpellVariant {
        // Validate ranges
        if (damageMultiplier < 0.0f) {
            throw new IllegalArgumentException("Damage multiplier cannot be negative");
        }
        if (manaCostMultiplier < 0.0f) {
            throw new IllegalArgumentException("Mana cost multiplier cannot be negative");
        }
        if (cooldownMultiplier < 0.0f) {
            throw new IllegalArgumentException("Cooldown multiplier cannot be negative");
        }
        if (tierOffset < -3 || tierOffset > 3) {
            throw new IllegalArgumentException("Tier offset must be between -3 and +3");
        }
    }

    /**
     * Factory method for creating a variant with default values (identity transformation).
     */
    public static SpellVariant identity(Identifier spellId) {
        return new SpellVariant(spellId, 1.0f, 1.0f, 1.0f, List.of(), List.of(), List.of(), 0);
    }

    /**
     * Apply this variant's modifiers to a spell, returning a modified copy.
     *
     * @param spell The spell to modify
     * @return A new spell with variant modifiers applied
     */
    public Spell apply(Spell spell) {
        // Validate spell matches variant target
        if (!spell.getId().equals(spellId)) {
            throw new IllegalArgumentException(
                    "Variant targets spell " + spellId + " but got " + spell.getId());
        }

        // Apply numeric modifiers
        float modifiedDamage = spell.getDamage() * damageMultiplier;
        float modifiedManaCost = spell.getManaCost() * manaCostMultiplier;
        float modifiedCooldown = spell.getCooldown() * cooldownMultiplier;
        int modifiedTier = Math.max(1, Math.min(4, spell.getTier() + tierOffset));

        // Merge tags: add new, remove specified
        var tags = new java.util.ArrayList<>(spell.getTags());
        tags.addAll(tagsAdded);
        tags.removeAll(tagsRemoved);

        // Merge status effects: add variant's, remove none (only additive)
        var effects = new java.util.ArrayList<>(spell.getStatusEffects());
        effects.addAll(statusEffectsAdded);

        // Create variant spell with modified values
        // Note: Spell constructor copies lists, so we can reuse them
        return new Spell(spell.getId(), spell.getName(), spell.getSchool().name(),
                spell.getDescription(), spell.getCastType().name(), modifiedManaCost,
                spell.getCastTime(), modifiedCooldown, modifiedTier, spell.getRequiredLevel(),
                modifiedDamage, spell.getRange(), spell.getProjectileSpeed(), spell.getAoeRadius(),
                spell.getKnockback(), effects, spell.getCustomData(), spell.getSoundIdentifier(),
                spell.getVfxOptional(), tags,
                Optional.ofNullable(spell.getRarity()).map(Enum::name), spell.getParent(),
                spell.getAnimationOptional(), spell.getFormatVersion());
    }

    // Codec for JSON serialization
    @SuppressWarnings("null")
    public static final Codec<SpellVariant> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.fieldOf("spell_id").forGetter(SpellVariant::spellId),
                    Codec.FLOAT.optionalFieldOf("damage_multiplier", 1.0f)
                            .forGetter(SpellVariant::damageMultiplier),
                    Codec.FLOAT.optionalFieldOf("mana_cost_multiplier", 1.0f)
                            .forGetter(SpellVariant::manaCostMultiplier),
                    Codec.FLOAT.optionalFieldOf("cooldown_multiplier", 1.0f)
                            .forGetter(SpellVariant::cooldownMultiplier),
                    Codec.STRING.listOf().optionalFieldOf("tags_added", List.of())
                            .forGetter(SpellVariant::tagsAdded),
                    Codec.STRING.listOf().optionalFieldOf("tags_removed", List.of())
                            .forGetter(SpellVariant::tagsRemoved),
                    Spell.StatusEffectEntry.CODEC.listOf()
                            .optionalFieldOf("status_effects_added", List.of())
                            .forGetter(SpellVariant::statusEffectsAdded),
                    Codec.INT.optionalFieldOf("tier_offset", 0).forGetter(SpellVariant::tierOffset))
            .apply(instance, SpellVariant::new));
}
