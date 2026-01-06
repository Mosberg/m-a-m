package dk.mosberg.network;

import org.jetbrains.annotations.NotNull;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Client-to-Server packet for casting a spell.
 *
 * TODO: Add spell targeting data (target coordinates, entity UUID) TODO: Implement spell variant
 * selection (alternate effects/paths) TODO: Add spell modification flags (empowered, hastened,
 * etc.) TODO: Support spell chaining (cast in sequence on same payload) TODO: Add spell prediction
 * data for server validation TODO: Implement conditional spell casting (if mana > X then cast)
 * TODO: Add combo tracking (previous spells in sequence)
 */
public record CastSpellPayload(Identifier spellId) implements CustomPayload {

    public static final CustomPayload.Id<CastSpellPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "cast_spell"));

    public static final PacketCodec<RegistryByteBuf, CastSpellPayload> CODEC = PacketCodec
            .tuple(Identifier.PACKET_CODEC, CastSpellPayload::spellId, CastSpellPayload::new);

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }

    @SuppressWarnings("null")
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);
        MAM.LOGGER.info("Registered CastSpellPayload");
    }
}
