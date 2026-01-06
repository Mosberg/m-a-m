package dk.mosberg.mana;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.nbt.NbtCompound;

/**
 * Holds the three-pool mana system data for a player.
 */
public class PlayerManaData {
    @SuppressWarnings("null")
    private final Map<ManaPoolType, ManaPool> pools = new EnumMap<>(ManaPoolType.class);
    private ManaPoolType activePriority = ManaPoolType.PERSONAL;

    public PlayerManaData() {
        // Initialize all three pools with default values
        for (ManaPoolType type : ManaPoolType.values()) {
            pools.put(type, new ManaPool(type.getDefaultCapacity(), type.getDefaultRegenRate()));
        }
    }

    /**
     * Attempts to consume mana, using the priority pool first. Falls back to other pools if needed.
     *
     * @param amount Amount of mana to consume
     * @return true if mana was successfully consumed
     */
    public boolean consumeMana(float amount) {
        // Try active priority pool first
        if (getPool(activePriority).consume(amount)) {
            return true;
        }

        // Try other pools in order: Personal -> Aura -> Reserve
        for (ManaPoolType type : ManaPoolType.values()) {
            if (type != activePriority && getPool(type).consume(amount)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Regenerates all mana pools. Called each tick on the server.
     */
    public void tickRegeneration() {
        pools.values().forEach(ManaPool::regenerate);
    }

    public ManaPool getPool(ManaPoolType type) {
        return Objects.requireNonNull(pools.get(type), "Missing mana pool for type " + type);
    }

    public ManaPoolType getActivePriority() {
        return activePriority;
    }

    public void setActivePriority(ManaPoolType priority) {
        this.activePriority = Objects.requireNonNull(priority, "Priority cannot be null");
    }

    @SuppressWarnings("null")
    public float getTotalMana() {
        return pools.values().stream().map(ManaPool::getCurrentMana).reduce(0f, Float::sum);
    }

    @SuppressWarnings("null")
    public float getTotalCapacity() {
        return pools.values().stream().map(ManaPool::getMaxCapacity).reduce(0, Integer::sum);
    }

    /**
     * Serializes mana data to NBT for persistence.
     */
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound poolsNbt = new NbtCompound();
        for (Map.Entry<ManaPoolType, ManaPool> entry : pools.entrySet()) {
            NbtCompound poolNbt = new NbtCompound();
            poolNbt.putFloat("current", entry.getValue().getCurrentMana());
            poolNbt.putInt("max", entry.getValue().getMaxCapacity());
            poolNbt.putFloat("regen", entry.getValue().getRegenRate());
            poolsNbt.put(entry.getKey().name(), poolNbt);
        }
        nbt.put("pools", poolsNbt);
        nbt.putString("activePriority", activePriority.name());
        return nbt;
    }

    /**
     * Deserializes mana data from NBT.
     */
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("pools")) {
            NbtCompound poolsNbt = nbt.getCompound("pools").get();
            for (ManaPoolType type : ManaPoolType.values()) {
                if (poolsNbt.contains(type.name())) {
                    NbtCompound poolNbt = poolsNbt.getCompound(type.name()).get();
                    float current = poolNbt.getFloat("current").get();
                    int max = poolNbt.getInt("max").get();
                    float regen = poolNbt.getFloat("regen").get();
                    pools.put(type, new ManaPool(max, current, regen));
                }
            }
        }
        if (nbt.contains("activePriority")) {
            try {
                String priorityStr = nbt.getString("activePriority").get();
                activePriority = ManaPoolType.valueOf(priorityStr);
            } catch (IllegalArgumentException e) {
                activePriority = ManaPoolType.PERSONAL;
            }
        }
    }
}
