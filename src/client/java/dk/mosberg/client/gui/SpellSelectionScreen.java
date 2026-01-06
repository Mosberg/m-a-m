package dk.mosberg.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import dk.mosberg.network.SelectSpellPayload;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Advanced GUI screen for selecting spells from a spellbook with filtering, sorting, and favorite
 * spell tracking.
 */
@Environment(EnvType.CLIENT)
public class SpellSelectionScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 4;
    private static final int FILTER_BUTTON_WIDTH = 40;

    private final List<Spell> allSpells;
    private List<Spell> displayedSpells;
    private final List<ButtonWidget> spellButtons = new ArrayList<>();
    private final Set<String> favoriteSpells = new HashSet<>();

    private int scrollOffset = 0;
    private SortMethod sortMethod = SortMethod.ALPHABETICAL;
    private SpellSchool filterSchool = null;
    private int filterTier = 0;
    private boolean showFavoritesOnly = false;
    private String searchText = "";

    public enum SortMethod {
        ALPHABETICAL("Name"), DAMAGE("Damage"), MANA_COST("Mana"), COOLDOWN("Cooldown"), RANGE(
                "Range"), TIER("Tier");

        public final String displayName;

        SortMethod(String displayName) {
            this.displayName = displayName;
        }
    }

    public SpellSelectionScreen(List<Spell> spells) {
        super(Text.literal("Spell Selection"));
        this.allSpells = new ArrayList<>(spells);
        this.displayedSpells = new ArrayList<>(spells);
        loadFavorites();
        applyFiltersAndSort();
    }

    @Override
    protected void init() {
        super.init();
        spellButtons.clear();

        // School filter buttons (top row)
        int filterButtonY = 40;
        int filterButtonStartX = 10;

        addFilterButton(filterButtonStartX, filterButtonY, "All", null);
        addFilterButton(filterButtonStartX + 50, filterButtonY, "🔥", SpellSchool.FIRE);
        addFilterButton(filterButtonStartX + 90, filterButtonY, "💧", SpellSchool.WATER);
        addFilterButton(filterButtonStartX + 130, filterButtonY, "💨", SpellSchool.AIR);
        addFilterButton(filterButtonStartX + 170, filterButtonY, "⛰", SpellSchool.EARTH);

        // Sort button and settings
        this.addDrawableChild(ButtonWidget
                .builder(Text.literal("Sort: " + sortMethod.displayName), btn -> cycleSortMethod())
                .dimensions(this.width - 150, filterButtonY, 140, BUTTON_HEIGHT).build());

        // Favorites toggle button
        String favText = showFavoritesOnly ? "★ Favorites Only" : "☆ All Spells";
        this.addDrawableChild(ButtonWidget
                .builder(Text.literal(favText), btn -> toggleFavoritesOnly())
                .dimensions(this.width - 150, filterButtonY + 25, 140, BUTTON_HEIGHT).build());

        // Spell buttons
        int startY = 80;
        int maxVisibleButtons = (this.height - startY - 50) / (BUTTON_HEIGHT + BUTTON_SPACING);

        for (int i = 0; i < Math.min(displayedSpells.size(), maxVisibleButtons); i++) {
            int index = i + scrollOffset;
            if (index >= displayedSpells.size())
                break;

            Spell spell = displayedSpells.get(index);
            int buttonX = 10;
            int buttonY = startY + i * (BUTTON_HEIGHT + BUTTON_SPACING);

            boolean isFavorite = favoriteSpells.contains(spell.getId().toString());
            String favoriteMarker = isFavorite ? "★ " : "☆ ";
            String buttonText = favoriteMarker + spell.getName() + " (T" + spell.getTier() + ") ["
                    + (int) spell.getManaCost() + "m]";

            ButtonWidget button =
                    ButtonWidget.builder(Text.literal(buttonText), btn -> selectSpell(spell))
                            .dimensions(buttonX, buttonY, this.width - 20, BUTTON_HEIGHT).build();

            spellButtons.add(button);
            this.addDrawableChild(button);
        }

        // Close button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("✕ Close"), btn -> this.close())
                .dimensions((this.width - BUTTON_WIDTH) / 2, this.height - 30, BUTTON_WIDTH,
                        BUTTON_HEIGHT)
                .build());
    }

    private void addFilterButton(int x, int y, String text, SpellSchool school) {
        boolean isActive = (school == null && filterSchool == null)
                || (school != null && school == filterSchool);
        int color = isActive ? 0xFF55FF : 0xAAAAAA;
        String displayText = isActive ? "» " + text + " «" : text;

        this.addDrawableChild(ButtonWidget
                .builder(Text.literal(displayText).withColor(color), btn -> setFilterSchool(school))
                .dimensions(x, y, FILTER_BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private void setFilterSchool(SpellSchool school) {
        this.filterSchool = school;
        applyFiltersAndSort();
        scrollOffset = 0;
        this.clearChildren();
        this.init();
    }

    private void cycleSortMethod() {
        SortMethod[] methods = SortMethod.values();
        int nextIndex = (sortMethod.ordinal() + 1) % methods.length;
        this.sortMethod = methods[nextIndex];
        applyFiltersAndSort();
        scrollOffset = 0;
        this.clearChildren();
        this.init();
    }

    private void toggleFavoritesOnly() {
        this.showFavoritesOnly = !this.showFavoritesOnly;
        applyFiltersAndSort();
        scrollOffset = 0;
        this.clearChildren();
        this.init();
    }

    private void applyFiltersAndSort() {
        // Start with all spells
        displayedSpells = new ArrayList<>(allSpells);

        // Apply school filter
        if (filterSchool != null) {
            displayedSpells =
                    displayedSpells.stream().filter(spell -> spell.getSchool() == filterSchool)
                            .collect(Collectors.toList());
        }

        // Apply tier filter
        if (filterTier > 0) {
            displayedSpells = displayedSpells.stream()
                    .filter(spell -> spell.getTier() <= filterTier).collect(Collectors.toList());
        }

        // Apply search filter
        if (!searchText.isEmpty()) {
            String searchLower = searchText.toLowerCase();
            displayedSpells = displayedSpells.stream()
                    .filter(spell -> spell.getName().toLowerCase().contains(searchLower)
                            || spell.getDescription().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        // Apply favorites filter
        if (showFavoritesOnly) {
            displayedSpells = displayedSpells.stream()
                    .filter(spell -> favoriteSpells.contains(spell.getId().toString()))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        sortSpells();
    }

    private void sortSpells() {
        Comparator<Spell> comparator = switch (sortMethod) {
            case ALPHABETICAL -> Comparator.comparing(Spell::getName);
            case DAMAGE -> Comparator.comparingDouble(Spell::getDamage).reversed();
            case MANA_COST -> Comparator.comparingDouble(Spell::getManaCost);
            case COOLDOWN -> Comparator.comparingDouble(Spell::getCooldown);
            case RANGE -> Comparator.comparingDouble(Spell::getRange).reversed();
            case TIER -> Comparator.comparingInt(Spell::getTier).reversed();
        };

        displayedSpells.sort(comparator);
    }

    private void selectSpell(Spell spell) {
        ClientPlayNetworking.send(new SelectSpellPayload(spell.getId()));

        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(Text.literal("✓ Selected: " + spell.getName()), true);
        }

        this.close();
    }

    private void toggleFavorite(Spell spell) {
        String spellId = spell.getId().toString();
        if (favoriteSpells.contains(spellId)) {
            favoriteSpells.remove(spellId);
        } else {
            favoriteSpells.add(spellId);
        }
        saveFavorites();
        this.clearChildren();
        this.init();
    }

    private void loadFavorites() {
        // TODO: Load from client-side config file if needed
        // For now, favorites are session-based
    }

    private void saveFavorites() {
        // TODO: Save to client-side config file if needed
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render semi-transparent background
        context.fill(0, 0, this.width, this.height, 0x80000000);

        // Render title with school info
        String titleText = "Spell Grimoire";
        if (filterSchool != null) {
            titleText += " - " + filterSchool.name();
        }
        context.drawCenteredTextWithShadow(this.textRenderer, titleText, this.width / 2, 10,
                0xFFFFFF);

        // Render spell count and filters info
        String countText = displayedSpells.size() + " spells";
        if (showFavoritesOnly) {
            countText += " (Favorites)";
        }
        if (filterSchool != null) {
            countText += " | School: " + filterSchool.name();
        }
        context.drawTextWithShadow(this.textRenderer, countText, 10, 28, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);

        // Render tooltips for hovered spell buttons
        for (int i = 0; i < spellButtons.size(); i++) {
            ButtonWidget button = spellButtons.get(i);
            if (button.isHovered()) {
                int spellIndex = i + scrollOffset;
                if (spellIndex < displayedSpells.size()) {
                    Spell spell = displayedSpells.get(spellIndex);
                    renderSpellTooltip(context, spell, mouseX, mouseY);
                }
                break;
            }
        }
    }

    private void renderSpellTooltip(DrawContext context, Spell spell, int x, int y) {
        List<Text> tooltip = new ArrayList<>();

        // Spell name with school color
        int schoolColor = getSchoolColor(spell.getSchool());
        String favoriteMarker = favoriteSpells.contains(spell.getId().toString()) ? "★ " : "";
        tooltip.add(Text.literal(favoriteMarker + spell.getName()).withColor(schoolColor));

        // School and tier
        tooltip.add(
                Text.literal("School: " + spell.getSchool().name() + " | Tier: " + spell.getTier())
                        .withColor(0xFFFF55));

        // Description if available
        if (!spell.getDescription().isEmpty()) {
            tooltip.add(Text.literal("").withColor(0x666666)); // Spacer
            tooltip.add(Text.literal(spell.getDescription()).withColor(0xCCCCCC));
        }

        // Spell statistics
        tooltip.add(Text.literal("").withColor(0x666666)); // Spacer
        tooltip.add(Text.literal("Damage: " + String.format("%.1f", spell.getDamage()))
                .withColor(0xFF5555));
        tooltip.add(Text.literal("Mana Cost: " + String.format("%.1f", spell.getManaCost()))
                .withColor(0x5555FF));
        tooltip.add(Text.literal("Cooldown: " + String.format("%.1f", spell.getCooldown()) + "s")
                .withColor(0xFF55FF));
        tooltip.add(Text.literal("Range: " + String.format("%.1f", spell.getRange()) + "m")
                .withColor(0x55FF55));

        if (spell.getAoeRadius() > 0) {
            tooltip.add(
                    Text.literal("AOE Radius: " + String.format("%.1f", spell.getAoeRadius()) + "m")
                            .withColor(0xFFAA00));
        }

        if (spell.getKnockback() > 0) {
            tooltip.add(Text.literal("Knockback: " + String.format("%.1f", spell.getKnockback()))
                    .withColor(0xFF5500));
        }

        // Tags if available
        tooltip.add(Text.literal("").withColor(0x666666)); // Spacer
        String tagsText = "Tags: "
                + String.join(", ", spell.getTags().isEmpty() ? List.of("none") : spell.getTags());
        tooltip.add(Text.literal(tagsText).withColor(0x888888));

        context.drawTooltip(this.textRenderer, tooltip, x, y);
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
        int maxScroll = Math.max(0, displayedSpells.size() - 10);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
        this.clearChildren();
        this.init();
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Handle right-click to toggle favorite on spell buttons
        for (int i = 0; i < spellButtons.size(); i++) {
            ButtonWidget spellButton = spellButtons.get(i);
            if (spellButton.isMouseOver(mouseX, mouseY) && button == 1) { // Right-click
                int spellIndex = i + scrollOffset;
                if (spellIndex < displayedSpells.size()) {
                    toggleFavorite(displayedSpells.get(spellIndex));
                    return true;
                }
            }
        }

        // Delegate to parent for normal button clicks
        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
