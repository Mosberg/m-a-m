package dk.mosberg.client.network;

import dk.mosberg.MAM;
import dk.mosberg.client.gui.SpellScreenHelper;
import dk.mosberg.network.ManaSyncPayload;
import dk.mosberg.network.OpenSpellBookPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Handles client-side network packet reception.
 *
 * TODO: Add disconnect handling (clear cached data on disconnect) TODO: Implement packet validation
 * (checksums, version checks) TODO: Add packet re-ordering for out-of-order delivery TODO:
 * Implement bandwidth optimization (compression, delta encoding) TODO: Add packet history for
 * debugging/logs TODO: Implement custom packet handlers registration system TODO: Add telemetry for
 * packet latency tracking TODO: Implement graceful degradation for missing spell data
 */
public class ClientNetworkHandler {

    @SuppressWarnings("null")
    public static void register() {
        // Handle mana sync from server
        ClientPlayNetworking.registerGlobalReceiver(ManaSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientManaData.updateFromServer(payload.personalMana(), payload.personalMax(),
                        payload.auraMana(), payload.auraMax(), payload.reserveMana(),
                        payload.reserveMax(), payload.activePriority());
            });
        });

        // Handle spellbook opening from server
        ClientPlayNetworking.registerGlobalReceiver(OpenSpellBookPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                SpellScreenHelper.openSpellSelection(payload.tier());
            });
        });

        MAM.LOGGER.info("Registered client network handlers");
    }
}
