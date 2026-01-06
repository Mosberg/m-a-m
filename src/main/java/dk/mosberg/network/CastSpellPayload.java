package dk.mosberg.network;

import dk.mosberg.MAM;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client-to-Server packet for casting a spell.
 */
public record CastSpellPayload(Identifier spellId) implements CustomPayload {

    public static final CustomPayload.Id<CastSpellPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "cast_spell"));

    public static final PacketCodec<RegistryByteBuf, CastSpellPayload> CODEC = PacketCodec
            .tuple(Identifier.PACKET_CODEC, CastSpellPayload::spellId, CastSpellPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        MAM.LOGGER.info("Registered CastSpellPayload");
    }
}
