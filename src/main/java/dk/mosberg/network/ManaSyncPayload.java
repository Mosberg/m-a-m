package dk.mosberg.network;

import dk.mosberg.MAM;
import dk.mosberg.mana.ManaPoolType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Server-to-Client packet for synchronizing mana data.
 */
public record ManaSyncPayload(float personalMana, float personalMax, float auraMana, float auraMax,
        float reserveMana, float reserveMax, String activePriority) implements CustomPayload {

    public static final CustomPayload.Id<ManaSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MAM.MOD_ID, "mana_sync"));

    public static final PacketCodec<RegistryByteBuf, ManaSyncPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.FLOAT, ManaSyncPayload::personalMana, PacketCodecs.FLOAT,
                    ManaSyncPayload::personalMax, PacketCodecs.FLOAT, ManaSyncPayload::auraMana,
                    PacketCodecs.FLOAT, ManaSyncPayload::auraMax, PacketCodecs.FLOAT,
                    ManaSyncPayload::reserveMana, PacketCodecs.FLOAT, ManaSyncPayload::reserveMax,
                    PacketCodecs.STRING, ManaSyncPayload::activePriority, ManaSyncPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
        MAM.LOGGER.info("Registered ManaSyncPayload");
    }
}
