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
 * Server-to-Client packet for opening the spell selection GUI.
 *
 * TODO: Add available spells list (avoid resending data unnecessarily) TODO: Include player's
 * current mana state in payload TODO: Add cooldown information for active spells TODO: Include GUI
 * customization flags (layout, theme) TODO: Add contextual info (PvP mode, area effects, etc.)
 * TODO: Include spell school affinity information TODO: Add recommendations for current situation
 * TODO: Implement quick-close timeout
 */
public record OpenSpellBookPayload(int tier) implements CustomPayload {

    public static final CustomPayload.Id<OpenSpellBookPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "open_spellbook"));

    @SuppressWarnings("null")
    public static final PacketCodec<RegistryByteBuf, OpenSpellBookPayload> CODEC = PacketCodec
            .tuple(PacketCodecs.INTEGER, OpenSpellBookPayload::tier, OpenSpellBookPayload::new);

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }

    @SuppressWarnings("null")
    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        MAM.LOGGER.info("Registered OpenSpellBookPayload");
    }
}
