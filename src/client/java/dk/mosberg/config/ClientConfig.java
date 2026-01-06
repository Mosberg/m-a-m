package dk.mosberg.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dk.mosberg.MAM;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Client-side configuration stored in JSON format.
 */
public class ClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("mam-client.json");

    private static ClientConfig INSTANCE;

    // Configuration fields
    public boolean showManaHud = true;
    public boolean showHealthInHud = true;
    public int hudOffsetX = 0;
    public int hudOffsetY = 0;
    public float hudScale = 1.0f;

    public static ClientConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static ClientConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ClientConfig config = GSON.fromJson(json, ClientConfig.class);
                MAM.LOGGER.info("Loaded client config from {}", CONFIG_PATH);
                return config != null ? config : new ClientConfig();
            } catch (IOException e) {
                MAM.LOGGER.error("Failed to load client config", e);
            }
        }

        // Create default config
        ClientConfig config = new ClientConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            MAM.LOGGER.info("Saved client config to {}", CONFIG_PATH);
        } catch (IOException e) {
            MAM.LOGGER.error("Failed to save client config", e);
        }
    }

    /**
     * Reloads the config from disk (hot-reload support).
     */
    public static void reload() {
        INSTANCE = load();
        MAM.LOGGER.info("Reloaded client config");
    }
}
