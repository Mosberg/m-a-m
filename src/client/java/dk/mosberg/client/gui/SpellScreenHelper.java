package dk.mosberg.client.gui;

import java.util.List;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellRegistry;
import dk.mosberg.spell.SpellSchool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

/**
 * Client-side helper for opening spell selection screen.
 */
@Environment(EnvType.CLIENT)
public class SpellScreenHelper {

    /**
     * Opens the spell selection screen for a given school and max tier.
     */
    public static void openSpellSelection(SpellSchool school, int maxTier) {
        List<Spell> spells = SpellRegistry.getSpellsBySchoolAndMaxTier(school, maxTier);
        MinecraftClient.getInstance().setScreen(new SpellSelectionScreen(spells));
    }

    /**
     * Opens the spell selection screen with all spells up to a max tier.
     */
    public static void openSpellSelection(int maxTier) {
        List<Spell> spells = SpellRegistry.getSpellsByTier(maxTier);
        MinecraftClient.getInstance().setScreen(new SpellSelectionScreen(spells));
    }

    /**
     * Opens the spell selection screen with all available spells.
     */
    public static void openSpellSelection() {
        List<Spell> spells = SpellRegistry.getAllSpells().stream().toList();
        MinecraftClient.getInstance().setScreen(new SpellSelectionScreen(spells));
    }
}
