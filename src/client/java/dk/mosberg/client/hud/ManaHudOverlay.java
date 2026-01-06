package dk.mosberg.client.hud;

import org.jetbrains.annotations.NotNull;
import dk.mosberg.client.network.ClientManaData;
import dk.mosberg.mana.ManaPool;
import dk.mosberg.mana.ManaPoolType;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders the three-tier mana bars in the HUD.
 */
@SuppressWarnings("deprecation")
public class ManaHudOverlay implements HudRenderCallback {

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 9;
    private static final int BAR_SPACING = 2;
    private static volatile boolean enabled = true;

    public static void register() {
        HudRenderCallback.EVENT.register(new ManaHudOverlay());
    }

    public static void toggle() {
        enabled = !enabled;
    }

    @Override
    public void onHudRender(@SuppressWarnings("null") @NotNull DrawContext drawContext,
            @SuppressWarnings("null") @NotNull RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (!enabled || client.player == null || client.options.hudHidden) {
            return;
        }

        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();

        // Position: Above hotbar, right side
        int x = screenWidth / 2 + 10;
        int startY = screenHeight - 49;

        // Render three mana bars stacked
        renderManaBar(drawContext, x, startY, ManaPoolType.PERSONAL);
        renderManaBar(drawContext, x, startY - (BAR_HEIGHT + BAR_SPACING), ManaPoolType.AURA);
        renderManaBar(drawContext, x, startY - 2 * (BAR_HEIGHT + BAR_SPACING),
                ManaPoolType.RESERVE);
    }

    private void renderManaBar(DrawContext drawContext, int x, int y, ManaPoolType poolType) {
        ManaPool pool = ClientManaData.get().getPool(poolType);
        float percentage = pool.getPercentage();
        int color = poolType.getColor();

        // Background (dark gray)
        drawContext.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF000000);

        // Border (lighter gray)
        drawContext.fill(x, y, x + BAR_WIDTH, y + 1, 0xFF555555); // Top
        drawContext.fill(x, y + BAR_HEIGHT - 1, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF555555); // Bottom
        drawContext.fill(x, y, x + 1, y + BAR_HEIGHT, 0xFF555555); // Left
        drawContext.fill(x + BAR_WIDTH - 1, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xFF555555); // Right

        // Mana fill
        int fillWidth = (int) ((BAR_WIDTH - 2) * percentage);
        if (fillWidth > 0) {
            drawContext.fill(x + 1, y + 1, x + 1 + fillWidth, y + BAR_HEIGHT - 1, color);
        }

        // Text label (pool name and values)
        String text = String.format("%s: %.0f/%.0f", poolType.getDisplayName().substring(0, 1), // First
                                                                                                // letter
                pool.getCurrentMana(), (float) pool.getMaxCapacity());
        drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + 2,
                y + 1, 0xFFFFFF);
    }
}
