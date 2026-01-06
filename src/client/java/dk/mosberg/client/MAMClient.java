package dk.mosberg.client;

import dk.mosberg.client.hud.ManaHudOverlay;
import dk.mosberg.config.ClientConfig;
import net.fabricmc.api.ClientModInitializer;

public class MAMClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Load client config
		ClientConfig.getInstance();

		// Register HUD overlay
		ManaHudOverlay.register();

		// Register client network handlers
		ClientNetworkHandler.register();
	}
}
