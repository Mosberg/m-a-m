package dk.mosberg.item;

import com.mojang.serialization.Codec;
import dk.mosberg.MAM;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Custom data components for mod items.
 *
 * TODO: Add MANA_EFFICIENCY component for staff/spellbook enchantments TODO: Add SPELL_POWER
 * component for damage/range modifiers TODO: Add COOLDOWN_REDUCTION component for faster spell
 * casting TODO: Add FAVORITE_SPELLS component for multiple favorites storage TODO: Add CAST_COUNT
 * component for tracking usage stats TODO: Add EXPERIENCE component for spell progression/leveling
 * TODO: Add CUSTOM_NAME component for weapon naming customization TODO: Add ATTUNEMENT component
 * for school affinity tracking TODO: Add ENCHANTMENT_LEVEL component for tiered enchantments
 */
public class MAMDataComponents {

    // Component for spell school binding on gemstones
    public static final ComponentType<SpellSchool> SPELL_SCHOOL = Registry.register(
            Registries.DATA_COMPONENT_TYPE, Identifier.of(MAM.MOD_ID, "spell_school"),
            ComponentType.<SpellSchool>builder()
                    .codec(Codec.STRING.xmap(MAMDataComponents::decodeSpellSchool,
                            MAMDataComponents::encodeSpellSchool))
                    .packetCodec(PacketCodecs.STRING.xmap(MAMDataComponents::decodeSpellSchool,
                            MAMDataComponents::encodeSpellSchool))
                    .build());

    // Component for tier level on staffs and spellbooks
    public static final ComponentType<Integer> TIER = Registry.register(
            Registries.DATA_COMPONENT_TYPE, Identifier.of(MAM.MOD_ID, "tier"), ComponentType
                    .<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());

    // Component for selected spell on staffs
    public static final ComponentType<String> SELECTED_SPELL =
            Registry.register(Registries.DATA_COMPONENT_TYPE,
                    Identifier.of(MAM.MOD_ID, "selected_spell"), ComponentType.<String>builder()
                            .codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build());

    public static void register() {
        MAM.LOGGER.info("Registered data components");
    }

    private static SpellSchool decodeSpellSchool(String value) {
        try {
            return SpellSchool.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            MAM.LOGGER.warn("Invalid spell school '{}', defaulting to AIR", value);
            return SpellSchool.AIR;
        }
    }

    private static String encodeSpellSchool(SpellSchool school) {
        return school.name().toLowerCase();
    }
}
