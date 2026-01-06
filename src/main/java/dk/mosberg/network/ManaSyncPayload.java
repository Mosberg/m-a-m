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
 * Server-to-Client packet for synchronizing mana data.
 *
 * TODO: Add mana regeneration rate sync (client-side prediction) TODO: Include active effects/buffs
 * mana modification state TODO: Add mana pool status flags (burning, frozen, corrupted) TODO:
 * Include burst/overdrive state information TODO: Add timestamp for latency compensation TODO:
 * Implement delta encoding for bandwidth optimization TODO: Add status effects info (what's
 * modifying mana currently) TODO: Include prediction delta (server estimated mana next tick)
 */
public record ManaSyncPayload(float personalMana, float personalMax, float auraMana, float auraMax,
        float reserveMana, float reserveMax, String activePriority) implements CustomPayload {

    public static final CustomPayload.Id<ManaSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "mana_sync"));

    @SuppressWarnings("null")
    public static final PacketCodec<RegistryByteBuf, ManaSyncPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.FLOAT, ManaSyncPayload::personalMana, PacketCodecs.FLOAT,
                    ManaSyncPayload::personalMax, PacketCodecs.FLOAT, ManaSyncPayload::auraMana,
                    PacketCodecs.FLOAT, ManaSyncPayload::auraMax, PacketCodecs.FLOAT,
                    ManaSyncPayload::reserveMana, PacketCodecs.FLOAT, ManaSyncPayload::reserveMax,
                    PacketCodecs.STRING, ManaSyncPayload::activePriority, ManaSyncPayload::new);

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }

    @SuppressWarnings("null")
    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        MAM.LOGGER.info("Registered ManaSyncPayload");
    }
}
