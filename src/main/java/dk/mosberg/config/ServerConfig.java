package dk.mosberg.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import dk.mosberg.MAM;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Server-side configuration stored in server.properties format.
 */
public class ServerConfig {
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mam-server.properties");

    private static ServerConfig INSTANCE;

    // Configuration fields
    public int personalManaCapacity = 250;
    public float personalManaRegen = 0.5f;
    public int auraManaCapacity = 500;
    public float auraManaRegen = 0.25f;
    public int reserveManaCapacity = 1000;
    public float reserveManaRegen = 0.1f;

    public boolean enableManaSyncPackets = true;
    public int manaSyncIntervalTicks = 20; // Sync every second

    public static ServerConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ServerConfig load() {
        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
                MAM.LOGGER.info("Loaded server config from {}", CONFIG_PATH);
            } catch (IOException e) {
                MAM.LOGGER.error("Failed to load server config", e);
            }
        }

        // Create config from properties (with defaults)
        ServerConfig config = new ServerConfig();
        config.personalManaCapacity =
                Integer.parseInt(props.getProperty("personalManaCapacity", "250"));
        config.personalManaRegen = Float.parseFloat(props.getProperty("personalManaRegen", "0.5"));
        config.auraManaCapacity = Integer.parseInt(props.getProperty("auraManaCapacity", "500"));
        config.auraManaRegen = Float.parseFloat(props.getProperty("auraManaRegen", "0.25"));
        config.reserveManaCapacity =
                Integer.parseInt(props.getProperty("reserveManaCapacity", "1000"));
        config.reserveManaRegen = Float.parseFloat(props.getProperty("reserveManaRegen", "0.1"));
        config.enableManaSyncPackets =
                Boolean.parseBoolean(props.getProperty("enableManaSyncPackets", "true"));
        config.manaSyncIntervalTicks =
                Integer.parseInt(props.getProperty("manaSyncIntervalTicks", "20"));

        // Save if file didn't exist
        if (!Files.exists(CONFIG_PATH)) {
            config.save();
        }

        return config;
    }

    public void save() {
        Properties props = new Properties();
        props.setProperty("personalManaCapacity", String.valueOf(personalManaCapacity));
        props.setProperty("personalManaRegen", String.valueOf(personalManaRegen));
        props.setProperty("auraManaCapacity", String.valueOf(auraManaCapacity));
        props.setProperty("auraManaRegen", String.valueOf(auraManaRegen));
        props.setProperty("reserveManaCapacity", String.valueOf(reserveManaCapacity));
        props.setProperty("reserveManaRegen", String.valueOf(reserveManaRegen));
        props.setProperty("enableManaSyncPackets", String.valueOf(enableManaSyncPackets));
        props.setProperty("manaSyncIntervalTicks", String.valueOf(manaSyncIntervalTicks));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "Mana And Magic Server Configuration");
            }
            MAM.LOGGER.info("Saved server config to {}", CONFIG_PATH);
        } catch (IOException e) {
            MAM.LOGGER.error("Failed to save server config", e);
        }
    }

    /**
     * Reloads the config from disk (hot-reload support).
     */
    public static void reload() {
        INSTANCE = load();
        MAM.LOGGER.info("Reloaded server config");
    }

    // TODO: Add spell damage scaling configuration
    // TODO: Add spell cooldown global multiplier
    // TODO: Add mana cost scaling configuration
    // TODO: Add spell availability restrictions by tier/level
    // TODO: Add PvP/PvE damage multiplier configuration
    // TODO: Add spell casting speed multiplier
    // TODO: Add projectile speed configuration
    // TODO: Add AOE radius scaling configuration
    // TODO: Add spell effect duration scaling
    // TODO: Add difficulty-based spell modifications
    // TODO: Add spell disable/whitelist configuration
    // TODO: Add world region spell restrictions
    // TODO: Add permission-based spell access
}
