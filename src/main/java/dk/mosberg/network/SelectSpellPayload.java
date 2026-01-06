package dk.mosberg.network;

import org.jetbrains.annotations.NotNull;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client-to-Server packet for selecting a spell in the spellbook.
 *
 * TODO: Add spell variant/path selection (alternate effects) TODO: Include spell power level
 * (empowered, normal, weak) TODO: Add selected targets (entity UUIDs or coordinates) TODO: Include
 * combo information (previous spells for chain attacks) TODO: Add casting mode (quick-cast,
 * charged, etc.) TODO: Implement spell slot selection (quick-bar slot) TODO: Add customization
 * flags (spell modifiers)
 */
public record SelectSpellPayload(Identifier spellId) implements CustomPayload {

    public static final CustomPayload.Id<SelectSpellPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "select_spell"));

    public static final PacketCodec<RegistryByteBuf, SelectSpellPayload> CODEC = PacketCodec
            .tuple(Identifier.PACKET_CODEC, SelectSpellPayload::spellId, SelectSpellPayload::new);

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }

    @SuppressWarnings("null")
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        MAM.LOGGER.info("Registered SelectSpellPayload");
    }
}
