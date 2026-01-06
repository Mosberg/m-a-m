package dk.mosberg.network;

import org.jetbrains.annotations.NotNull;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server-to-Client packet for synchronizing the selected spell's cooldown.
 */
public record SelectedCooldownPayload(Identifier spellId, float remainingSeconds)
        implements CustomPayload {

    public static final CustomPayload.Id<SelectedCooldownPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "selected_cooldown"));

    public static final PacketCodec<RegistryByteBuf, SelectedCooldownPayload> CODEC = PacketCodec
            .tuple(Identifier.PACKET_CODEC, SelectedCooldownPayload::spellId, PacketCodecs.FLOAT,
                    SelectedCooldownPayload::remainingSeconds, SelectedCooldownPayload::new);

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        MAM.LOGGER.info("Registered SelectedCooldownPayload");
    }
}
