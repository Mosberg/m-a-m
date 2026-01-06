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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) {
                return;
            }

            if (openSpellKey != null) {
                while (openSpellKey.wasPressed()) {
                    openSpellSelectionScreen(client);
                }
            }

            if (toggleHudKey != null) {
                while (toggleHudKey.wasPressed()) {
                    toggleHUD();
                }
            }
        });

        MAM.LOGGER.info("Registered client keybindings");
    }

    /**
     * Open the spell selection screen.
     */
    public static void openSpellSelectionScreen(MinecraftClient client) {
        if (client == null || client.player == null || client.currentScreen != null) {
            return;
        }

        if (client.player.isSpectator() || client.player.isDead()) {
            return;
        }

        // Check if player has spellbook in either hand
        ItemStack mainHand = client.player.getMainHandStack();
        ItemStack offHand = client.player.getOffHandStack();
        ItemStack spellbook = ItemStack.EMPTY;
        int spellbookTier = 0;

        // Check main hand
        if (mainHand.getItem() instanceof SpellbookItem spellbookItem) {
            spellbook = mainHand;
            spellbookTier = spellbookItem.getTier();
        }

        // Check off hand if not found in main hand
        if (spellbook.isEmpty() && offHand.getItem() instanceof SpellbookItem spellbookItem) {
            spellbook = offHand;
            spellbookTier = spellbookItem.getTier();
        }

        // Validate we have spellbook
        if (spellbook.isEmpty()) {
            client.player.sendMessage(Text.translatable("message.mam.no_spellbook"), true);
            return;
        }

        // Get all spells up to spellbook tier (all schools)
        List<Spell> spells = SpellRegistry.getSpellsByMaxTier(spellbookTier);
        if (spells.isEmpty()) {
            client.player.sendMessage(Text.translatable("message.mam.no_spells"), true);
            return;
        }

        MAM.LOGGER.debug("Opening spell selection screen with {} spells", spells.size());
        client.setScreen(new SpellSelectionScreen(spells));
    }

    /**
     * Toggle HUD visibility.
     */
    public static void toggleHUD() {
        // TODO: Implement HUD visibility toggle
        MAM.LOGGER.info("HUD toggle pressed (feature coming soon)");
    }
}
