package dk.mosberg.client.gui;

import dk.mosberg.config.ClientConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class HudConfigScreen extends Screen {
    private final ClientConfig cfg;

    private ButtonWidget toggleHudBtn;
    private ButtonWidget modeBtn;
    private ButtonWidget offsetXMinusBtn;
    private ButtonWidget offsetXPlusBtn;
    private ButtonWidget offsetYMinusBtn;
    private ButtonWidget offsetYPlusBtn;
    private ButtonWidget scaleMinusBtn;
    private ButtonWidget scalePlusBtn;
    private ButtonWidget closeBtn;

    public HudConfigScreen() {
        super(Text.literal("Mana HUD Settings"));
        this.cfg = ClientConfig.getInstance();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 60;

        // Toggle HUD visibility
        toggleHudBtn = ButtonWidget
                .builder(Text.literal(cfg.showManaHud ? "HUD: ON" : "HUD: OFF"), btn -> {
                    cfg.showManaHud = !cfg.showManaHud;
                    cfg.save();
                    btn.setMessage(Text.literal(cfg.showManaHud ? "HUD: ON" : "HUD: OFF"));
                }).dimensions(centerX - 100, y, 200, 20).build();
        this.addDrawableChild(toggleHudBtn);

        y += 30;
        // Cycle HUD mode
        modeBtn = ButtonWidget.builder(Text.literal("Mode: " + cfg.hudMode), btn -> {
            cfg.hudMode = cfg.hudMode.equalsIgnoreCase("DETAILED") ? "COMPACT" : "DETAILED";
            cfg.save();
            btn.setMessage(Text.literal("Mode: " + cfg.hudMode));
        }).dimensions(centerX - 100, y, 200, 20).build();
        this.addDrawableChild(modeBtn);

        y += 30;
        // Offset X controls
        offsetXMinusBtn = ButtonWidget.builder(Text.literal("X -"), btn -> {
            cfg.hudOffsetX = Math.max(-1000, cfg.hudOffsetX - 5);
            cfg.save();
            updateLabels();
        }).dimensions(centerX - 150, y, 60, 20).build();
        this.addDrawableChild(offsetXMinusBtn);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("X: " + cfg.hudOffsetX), b -> {
        }).dimensions(centerX - 85, y, 170, 20).build());

        offsetXPlusBtn = ButtonWidget.builder(Text.literal("X +"), btn -> {
            cfg.hudOffsetX = Math.min(1000, cfg.hudOffsetX + 5);
            cfg.save();
            updateLabels();
        }).dimensions(centerX + 95, y, 60, 20).build();
        this.addDrawableChild(offsetXPlusBtn);

        y += 30;
        // Offset Y controls
        offsetYMinusBtn = ButtonWidget.builder(Text.literal("Y -"), btn -> {
            cfg.hudOffsetY = Math.max(-1000, cfg.hudOffsetY - 5);
            cfg.save();
            updateLabels();
        }).dimensions(centerX - 150, y, 60, 20).build();
        this.addDrawableChild(offsetYMinusBtn);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Y: " + cfg.hudOffsetY), b -> {
        }).dimensions(centerX - 85, y, 170, 20).build());

        offsetYPlusBtn = ButtonWidget.builder(Text.literal("Y +"), btn -> {
            cfg.hudOffsetY = Math.min(1000, cfg.hudOffsetY + 5);
            cfg.save();
            updateLabels();
        }).dimensions(centerX + 95, y, 60, 20).build();
        this.addDrawableChild(offsetYPlusBtn);

        y += 30;
        // Scale controls
        scaleMinusBtn = ButtonWidget.builder(Text.literal("Scale -"), btn -> {
            cfg.hudScale = Math.max(0.5f, cfg.hudScale - 0.1f);
            cfg.save();
            updateLabels();
        }).dimensions(centerX - 150, y, 90, 20).build();
        this.addDrawableChild(scaleMinusBtn);

        this.addDrawableChild(ButtonWidget
                .builder(Text.literal("Scale: " + String.format("%.1f", cfg.hudScale)), b -> {
                }).dimensions(centerX - 55, y, 110, 20).build());

        scalePlusBtn = ButtonWidget.builder(Text.literal("Scale +"), btn -> {
            cfg.hudScale = Math.min(3.0f, cfg.hudScale + 0.1f);
            cfg.save();
            updateLabels();
        }).dimensions(centerX + 60, y, 90, 20).build();
        this.addDrawableChild(scalePlusBtn);

        y += 40;
        closeBtn = ButtonWidget.builder(Text.literal("Close"), btn -> this.close())
                .dimensions(centerX - 60, y, 120, 20).build();
        this.addDrawableChild(closeBtn);
    }

    private void updateLabels() {
        // Reinitialize to refresh middle labels displaying current values
        this.clearChildren();
        this.init();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, delta);
        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal("Mana HUD Settings"), this.width / 2, 30, 0xFFFFFF);
    }
}
