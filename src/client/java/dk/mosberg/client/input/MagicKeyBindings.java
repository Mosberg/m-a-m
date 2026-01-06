package dk.mosberg.client.input;

import java.util.List;
import dk.mosberg.MAM;
import dk.mosberg.client.gui.SpellSelectionScreen;
import dk.mosberg.item.SpellbookItem;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Client-side keybinding handler for Mana and Magic mod. Registers and handles key presses.
 *
 * Note: Full keybinding registration deferred - infrastructure is in place for future enhancement.
 */
@Environment(EnvType.CLIENT)
public class MagicKeyBindings {
    private static net.minecraft.client.option.KeyBinding openSpellKey;
    private static net.minecraft.client.option.KeyBinding toggleHudKey;
    private static net.minecraft.client.option.KeyBinding toggleHudModeKey;

    /**
     * Register client event handlers.
     */
    public static void register() {
        openSpellKey =
                net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(
                        new net.minecraft.client.option.KeyBinding("key.mam.open_spell_selection",
                                net.minecraft.client.util.InputUtil.Type.KEYSYM,
                                org.lwjgl.glfw.GLFW.GLFW_KEY_R,
                                net.minecraft.client.option.KeyBinding.Category.MISC));

        toggleHudKey = net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
                .registerKeyBinding(new net.minecraft.client.option.KeyBinding("key.mam.toggle_hud",
                        net.minecraft.client.util.InputUtil.Type.KEYSYM,
                        org.lwjgl.glfw.GLFW.GLFW_KEY_H,
                        net.minecraft.client.option.KeyBinding.Category.MISC));

        toggleHudModeKey = net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
                .registerKeyBinding(new net.minecraft.client.option.KeyBinding(
                        "key.mam.toggle_hud_mode", net.minecraft.client.util.InputUtil.Type.KEYSYM,
                        org.lwjgl.glfw.GLFW.GLFW_KEY_J,
                        net.minecraft.client.option.KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSpellKey.wasPressed()) {
                openSpellSelectionScreen(client);
            }

            while (toggleHudKey.wasPressed()) {
                toggleHUD();
            }

            while (toggleHudModeKey.wasPressed()) {
                dk.mosberg.client.hud.ManaHudOverlay.cycleMode();
            }
        });

        MAM.LOGGER.info("Registered client keybindings");

        // TODO: Add custom keybinding configuration file
        // TODO: Add spell quick-cast keybindings (1-9)
        // TODO: Add stance/form switching keybindings
        // TODO: Add spell hotbar keybindings
        // TODO: Add ability to modify keybindings in-game
        // TODO: Add keybinding conflict detection
        // TODO: Add mouse button support for keybindings
        // TODO: Add gamepad/controller support
        // TODO: Add keybinding profiles for different scenarios
    }

    /**
     * Open the spell selection screen.
     */
    public static void openSpellSelectionScreen(MinecraftClient client) {
        if (client.currentScreen != null)
            return;

        var player = client.player;
        if (player == null || player.isSpectator() || player.isDead())
            return;

        // Check main hand first (more common case)
        ItemStack mainHand = player.getMainHandStack();
        int spellbookTier;

        if (mainHand.getItem() instanceof SpellbookItem spellbookItem) {
            spellbookTier = spellbookItem.getTier();
        } else {
            // Check off hand
            ItemStack offHand = player.getOffHandStack();
            if (offHand.getItem() instanceof SpellbookItem spellbookItem) {
                spellbookTier = spellbookItem.getTier();
            } else {
                player.sendMessage(Text.translatable("message.mam.no_spellbook"), true);
                return;
            }
        }

        // Get spells with cached lookup
        List<Spell> spells = SpellRegistry.getSpellsByMaxTier(spellbookTier);
        if (spells.isEmpty()) {
            player.sendMessage(Text.translatable("message.mam.no_spells"), true);
            return;
        }

        MAM.LOGGER.debug("Opening spell selection screen with {} spells", spells.size());
        client.setScreen(new SpellSelectionScreen(spells));
    }

    /**
     * Toggle HUD visibility.
     */
    public static void toggleHUD() {
        dk.mosberg.client.hud.ManaHudOverlay.toggle();
    }
}
