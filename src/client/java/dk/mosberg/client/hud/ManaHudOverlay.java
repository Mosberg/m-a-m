package dk.mosberg.client.hud;

import org.jetbrains.annotations.NotNull;
import dk.mosberg.client.network.ClientManaData;
import dk.mosberg.client.network.ClientSelectedCooldown;
import dk.mosberg.config.ClientConfig;
import dk.mosberg.mana.ManaPool;
import dk.mosberg.mana.ManaPoolType;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Renders the three-tier mana bars in the HUD.
 *
 * TODO: Add HUD customization settings (position, size, transparency, color themes) TODO: Implement
 * mana bar animations (drain pulse, regen glow, critical warning blink) TODO: Add spell cooldown
 * indicators overlay on HUD TODO: Implement status effect icons display on mana bars TODO: Add mana
 * regeneration rate display (per tick/second) TODO: Create mana threshold warnings (critical low,
 * etc.) with visual/audio cues TODO: Add alternative HUD layout modes
 * (vertical/horizontal/compact/detailed) TODO: Implement screen shake effect when mana depleted or
 * critical TODO: Add floating combat text for mana drain/regen events TODO: Implement HUD scaling
 * based on mana pool tier (visual emphasis)
 */
@SuppressWarnings("deprecation")
public class ManaHudOverlay implements HudRenderCallback {

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 9;
    private static final int BAR_SPACING = 2;
    private static volatile boolean enabled = true;
    private static volatile HudMode mode = HudMode.DETAILED;

    public enum HudMode {
        COMPACT, DETAILED
    }

    public static void register() {
        // Initialize from client config
        ClientConfig cfg = ClientConfig.getInstance();
        enabled = cfg.showManaHud;
        mode = "COMPACT".equalsIgnoreCase(cfg.hudMode) ? HudMode.COMPACT : HudMode.DETAILED;

        HudRenderCallback.EVENT.register(new ManaHudOverlay());
    }

    public static void toggle() {
        enabled = !enabled;
        ClientConfig cfg = ClientConfig.getInstance();
        cfg.showManaHud = enabled;
        cfg.save();
    }

    public static void cycleMode() {
        mode = (mode == HudMode.DETAILED) ? HudMode.COMPACT : HudMode.DETAILED;
        ClientConfig cfg = ClientConfig.getInstance();
        cfg.hudMode = (mode == HudMode.DETAILED) ? "DETAILED" : "COMPACT";
        cfg.save();
    }

    // onHudRender implemented below with cooldown overlay

    private void renderManaBar(DrawContext drawContext, int x, int y, ManaPoolType poolType,
            int barWidth, int barHeight) {
        ManaPool pool = ClientManaData.get().getPool(poolType);
        float percentage = pool.getPercentage();
        int color = poolType.getColor();

        // Background (dark gray)
        drawContext.fill(x, y, x + barWidth, y + barHeight, 0xFF000000);

        // Border (lighter gray)
        drawContext.fill(x, y, x + barWidth, y + 1, 0xFF555555); // Top
        drawContext.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, 0xFF555555); // Bottom
        drawContext.fill(x, y, x + 1, y + barHeight, 0xFF555555); // Left
        drawContext.fill(x + barWidth - 1, y, x + barWidth, y + barHeight, 0xFF555555); // Right

        // Mana fill
        int fillWidth = (int) ((barWidth - 2) * percentage);
        if (fillWidth > 0) {
            drawContext.fill(x + 1, y + 1, x + 1 + fillWidth, y + barHeight - 1, color);
        }

        // Text label (pool name and values)
        if (mode == HudMode.DETAILED) {
            String text = String.format("%s: %.0f/%.0f (+%.2f/t)",
                    poolType.getDisplayName().substring(0, 1), pool.getCurrentMana(),
                    (float) pool.getMaxCapacity(), pool.getRegenRate());
            drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, x + 2,
                    y + 1, 0xFFFFFF);
        }
    }

    private void renderCooldownOverlay(DrawContext drawContext, int x, int startY, int barHeight,
            int barSpacing) {
        var spellId = ClientSelectedCooldown.getCurrentSpellId();
        float remaining = ClientSelectedCooldown.getRemainingSeconds();
        if (spellId == null || remaining <= 0)
            return;

        String cdText = String.format("CD %s: %.1fs", spellId.getPath(), remaining);
        drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, cdText, x,
                startY - 2 * (barHeight + barSpacing) - 12, 0xFFAAAA);
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

        // Read client HUD config for offsets and scale
        ClientConfig cfg = ClientConfig.getInstance();
        // Keep local static state in sync with config (in case settings changed via UI)
        enabled = cfg.showManaHud;
        mode = "COMPACT".equalsIgnoreCase(cfg.hudMode) ? HudMode.COMPACT : HudMode.DETAILED;
        float scale = Math.max(0.5f, Math.min(3.0f, cfg.hudScale));
        int offsetX = cfg.hudOffsetX;
        int offsetY = cfg.hudOffsetY;

        // Position: Above hotbar, right side
        int x = screenWidth / 2 + 10 + offsetX;
        int startY = screenHeight - 49 + offsetY;

        int barWidth = Math.max(20, Math.round(BAR_WIDTH * scale));
        int barHeight = Math.max(6, Math.round(BAR_HEIGHT * scale));
        int barSpacing = Math.max(1, Math.round(BAR_SPACING * scale));

        // Render three mana bars stacked
        renderManaBar(drawContext, x, startY, ManaPoolType.PERSONAL, barWidth, barHeight);
        renderManaBar(drawContext, x, startY - (barHeight + barSpacing), ManaPoolType.AURA,
                barWidth, barHeight);
        renderManaBar(drawContext, x, startY - 2 * (barHeight + barSpacing), ManaPoolType.RESERVE,
                barWidth, barHeight);

        // Render cooldown overlay in detailed mode only
        if (mode == HudMode.DETAILED) {
            renderCooldownOverlay(drawContext, x, startY, barHeight, barSpacing);
        }
    }
}
