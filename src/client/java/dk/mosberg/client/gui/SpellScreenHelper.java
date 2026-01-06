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
 *
 * TODO: Add screen transition animations TODO: Implement spell history tracking (recently cast
 * spells) TODO: Add quick-access spell shortcuts TODO: Implement nested screen support (backup
 * actions) TODO: Add loading state indication TODO: Implement error handling for network delays
 * TODO: Add screen memory (restore last state when reopened) TODO: Implement accessibility options
 * (large text, high contrast)
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
        List<Spell> spells = SpellRegistry.getSpellsByMaxTier(maxTier);
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
