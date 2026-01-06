package dk.mosberg.client.gui;

import java.util.ArrayList;
import java.util.List;
import dk.mosberg.network.SelectSpellPayload;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * GUI screen for selecting spells from a spellbook.
 */
@Environment(EnvType.CLIENT)
public class SpellSelectionScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;

    private final List<Spell> availableSpells;
    private final List<ButtonWidget> spellButtons = new ArrayList<>();
    private int scrollOffset = 0;

    public SpellSelectionScreen(List<Spell> spells) {
        super(Text.translatable("gui.mam.spell_selection"));
        this.availableSpells = new ArrayList<>(spells);
    }

    @Override
    protected void init() {
        super.init();

        spellButtons.clear();

        int startY = 60;
        int maxVisibleButtons = (this.height - startY - 40) / (BUTTON_HEIGHT + BUTTON_SPACING);

        // Create buttons for visible spells
        for (int i = 0; i < Math.min(availableSpells.size(), maxVisibleButtons); i++) {
            int index = i + scrollOffset;
            if (index >= availableSpells.size())
                break;

            Spell spell = availableSpells.get(index);
            int buttonX = (this.width - BUTTON_WIDTH) / 2;
            int buttonY = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);

            ButtonWidget button =
                    ButtonWidget
                            .builder(
                                    Text.literal(spell.getName() + " (T" + spell.getTier() + ") - "
                                            + spell.getManaCost() + " mana"),
                                    btn -> selectSpell(spell))
                            .dimensions(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();

            spellButtons.add(button);
            this.addDrawableChild(button);
        }

        // Add close button
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("gui.done"), btn -> this.close())
                        .dimensions((this.width - BUTTON_WIDTH) / 2, this.height - 30, BUTTON_WIDTH,
                                BUTTON_HEIGHT)
                        .build());
    }

    private void selectSpell(Spell spell) {
        // Send spell selection to server
        ClientPlayNetworking.send(new SelectSpellPayload(spell.getId()));

        // Show confirmation message
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.translatable("spell.mam.selected", spell.getName()),
                    true);
        }

        this.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20,
                0xFFFFFF);

        // Render spell count
        Text spellCount = Text.literal(availableSpells.size() + " spells available");
        context.drawCenteredTextWithShadow(this.textRenderer, spellCount, this.width / 2, 40,
                0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);

        // Render tooltip for hovered spell button
        for (int i = 0; i < spellButtons.size(); i++) {
            ButtonWidget button = spellButtons.get(i);
            if (button.isHovered()) {
                int spellIndex = i + scrollOffset;
                if (spellIndex < availableSpells.size()) {
                    Spell spell = availableSpells.get(spellIndex);
                    List<Text> tooltip = new ArrayList<>();
                    tooltip.add(Text.literal(spell.getName())
                            .withColor(getSchoolColor(spell.getSchool())));
                    if (!spell.getDescription().isEmpty()) {
                        tooltip.add(Text.literal(spell.getDescription()).withColor(0xAAAAAA));
                    }
                    tooltip.add(Text.literal("School: " + spell.getSchool().name())
                            .withColor(0xFFFF55));
                    tooltip.add(Text.literal("Tier: " + spell.getTier()).withColor(0x55FFFF));
                    tooltip.add(Text.literal("Damage: " + spell.getDamage()).withColor(0xFF5555));
                    tooltip.add(Text.literal("Range: " + spell.getRange()).withColor(0x55FF55));

                    context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
                }
                break;
            }
        }
    }

    private int getSchoolColor(SpellSchool school) {
        return switch (school) {
            case FIRE -> 0xFF5555; // Red
            case WATER -> 0x5555FF; // Blue
            case AIR -> 0x55FFFF; // Cyan
            case EARTH -> 0x55FF55; // Green
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount,
            double verticalAmount) {
        int maxScroll = Math.max(0, availableSpells.size() - 10);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        this.clearChildren();
        this.init();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
