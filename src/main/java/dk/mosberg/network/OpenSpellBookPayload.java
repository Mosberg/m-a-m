package dk.mosberg.network;

import dk.mosberg.MAM;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server-to-Client packet for opening the spell selection GUI.
 */
public record OpenSpellBookPayload(int tier) implements CustomPayload {

    public static final CustomPayload.Id<OpenSpellBookPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "open_spellbook"));

    public static final PacketCodec<RegistryByteBuf, OpenSpellBookPayload> CODEC = PacketCodec
            .tuple(PacketCodecs.INTEGER, OpenSpellBookPayload::tier, OpenSpellBookPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        MAM.LOGGER.info("Registered OpenSpellBookPayload");
    }
}
