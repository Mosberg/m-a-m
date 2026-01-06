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
 */
public class MAMDataComponents {

    // Component for spell school binding on gemstones
    @SuppressWarnings("null")
    public static final ComponentType<SpellSchool> SPELL_SCHOOL = Registry.register(
            Registries.DATA_COMPONENT_TYPE, Identifier.of(MAM.MOD_ID, "spell_school"),
            ComponentType.<SpellSchool>builder()
                    .codec(Codec.STRING.xmap(str -> SpellSchool.valueOf(str.toUpperCase()),
                            school -> school.name().toLowerCase()))
                    .packetCodec(
                            PacketCodecs.STRING.xmap(str -> SpellSchool.valueOf(str.toUpperCase()),
                                    school -> school.name().toLowerCase()))
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
}
