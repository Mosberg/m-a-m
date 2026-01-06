package dk.mosberg.client;

import dk.mosberg.client.hud.ManaHudOverlay;
import dk.mosberg.client.input.MagicKeyBindings;
import dk.mosberg.client.network.ClientNetworkHandler;
import dk.mosberg.client.network.StaffCastingHandler;
import dk.mosberg.client.render.SpellProjectileEntityRenderer;
import dk.mosberg.config.ClientConfig;
import dk.mosberg.entity.MAMEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class MAMClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Load client config
		ClientConfig.getInstance();

		// Register entity renderers
		EntityRendererRegistry.register(MAMEntities.SPELL_PROJECTILE,
				SpellProjectileEntityRenderer::new);

		// Register HUD overlay
		ManaHudOverlay.register();

		// Register client network handlers
		ClientNetworkHandler.register();

		// Register key bindings
		MagicKeyBindings.register();

		// Register spellbook casting handler
		StaffCastingHandler.register();
	}
}
