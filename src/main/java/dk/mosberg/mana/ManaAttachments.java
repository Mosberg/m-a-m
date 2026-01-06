package dk.mosberg.mana;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

/**
 * Fabric Data Attachment for storing player casting data (mana + cooldowns).
 */
public class ManaAttachments {

    // Codec for serializing PlayerCastingData
    public static final Codec<PlayerCastingData> PLAYER_CASTING_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(Codec
                    .unboundedMap(Codec.STRING, PoolData.CODEC).fieldOf("pools").forGetter(data -> {
                        java.util.Map<String, PoolData> map = new java.util.HashMap<>();
                        for (ManaPoolType type : ManaPoolType.values()) {
                            ManaPool pool = data.getManaData().getPool(type);
                            map.put(type.name(), new PoolData(pool.getCurrentMana(),
                                    pool.getMaxCapacity(), pool.getRegenRate()));
                        }
                        return map;
                    }),
                    Codec.STRING.fieldOf("activePriority")
                            .forGetter(data -> data.getManaData().getActivePriority().name()))
                    .apply(instance, (pools, priority) -> {
                        PlayerCastingData data = new PlayerCastingData();
                        pools.forEach((key, poolData) -> {
                            try {
                                ManaPoolType type = ManaPoolType.valueOf(key);
                                data.getManaData().getPool(type).set(poolData.current);
                            } catch (IllegalArgumentException e) {
                                MAM.LOGGER.warn("Invalid mana pool type: {}", key);
                            }
                        });
                        try {
                            data.getManaData().setActivePriority(ManaPoolType.valueOf(priority));
                        } catch (IllegalArgumentException e) {
                            data.getManaData().setActivePriority(ManaPoolType.PERSONAL);
                        }
                        return data;
                    }));

    @SuppressWarnings({"deprecation", "unchecked", "null"})
    public static final AttachmentType<PlayerCastingData> PLAYER_CASTING = AttachmentRegistry
            .<PlayerCastingData>builder().persistent(new Codec<PlayerCastingData>() {
                @Override
                public <T> com.mojang.serialization.DataResult<com.mojang.datafixers.util.Pair<PlayerCastingData, T>> decode(
                        com.mojang.serialization.DynamicOps<T> ops, T input) {
                    if (ops instanceof NbtOps && input instanceof NbtCompound nbt) {
                        PlayerCastingData data = new PlayerCastingData();
                        data.readNbt(nbt);
                        return com.mojang.serialization.DataResult
                                .success(com.mojang.datafixers.util.Pair.of(data, ops.empty()));
                    }
                    return PLAYER_CASTING_CODEC.decode(ops, input);
                }

                @Override
                public <T> com.mojang.serialization.DataResult<T> encode(PlayerCastingData input,
                        com.mojang.serialization.DynamicOps<T> ops, T prefix) {
                    if (ops instanceof NbtOps) {
                        NbtCompound nbt = new NbtCompound();
                        input.writeNbt(nbt);
                        T result = (T) nbt;
                        return com.mojang.serialization.DataResult.success(result);
                    }
                    return PLAYER_CASTING_CODEC.encode(input, ops, prefix);
                }
            }).copyOnDeath().buildAndRegister(Identifier.of(MAM.MOD_ID, "player_casting"));

    // Keep old PLAYER_MANA for backward compatibility
    @Deprecated(forRemoval = true)
    public static final AttachmentType<PlayerCastingData> PLAYER_MANA = PLAYER_CASTING;

    public static void register() {
        MAM.LOGGER.info("Registered player casting data attachments");
    }

    // Helper record for codec serialization
    private record PoolData(float current, int max, float regen) {
        @SuppressWarnings("null")
        public static final Codec<PoolData> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(Codec.FLOAT.fieldOf("current").forGetter(PoolData::current),
                        Codec.INT.fieldOf("max").forGetter(PoolData::max),
                        Codec.FLOAT.fieldOf("regen").forGetter(PoolData::regen))
                .apply(instance, PoolData::new));
    }
}
