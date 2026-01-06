package dk.mosberg.spell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dk.mosberg.MAM;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

/**
 * Registry for spells loaded from data packs.
 */
public class SpellRegistry {
    private static final Map<Identifier, Spell> SPELLS = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.of(MAM.MOD_ID, "spells");
                    }

                    @Override
                    public void reload(ResourceManager manager) {
                        loadSpells(manager);
                    }
                });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MAM.LOGGER.info("Loaded {} spells", SPELLS.size());
        });

        MAM.LOGGER.info("Registered spell registry");
    }

    private static void loadSpells(ResourceManager manager) {
        SPELLS.clear();
        int loaded = 0;
        int failed = 0;

        // Find all spell JSON files in data/{namespace}/spells/**/*.json
        Map<Identifier, Resource> resources =
                manager.findResources("spells", id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();

            // Convert file path to spell ID: data/mam/spells/air/air_strike.json -> mam:air_strike
            String path = fileId.getPath();
            // Remove "spells/" prefix and ".json" suffix
            String spellPath = path.substring("spells/".length(), path.length() - ".json".length());
            // Get just the filename (last part after /)
            String spellName = spellPath.substring(spellPath.lastIndexOf('/') + 1);
            Identifier spellId = Identifier.of(fileId.getNamespace(), spellName);

            try (BufferedReader reader =
                    new BufferedReader(new InputStreamReader(entry.getValue().getInputStream(),
                            StandardCharsets.UTF_8))) {

                JsonElement json = JsonParser.parseReader(reader);
                Spell spell = Spell.CODEC.parse(JsonOps.INSTANCE, json).resultOrPartial(
                        error -> MAM.LOGGER.error("Failed to parse spell {}: {}", spellId, error))
                        .orElse(null);

                if (spell != null) {
                    SPELLS.put(spell.getId(), spell);
                    loaded++;
                    MAM.LOGGER.debug("Loaded spell: {}", spell.getId());
                } else {
                    failed++;
                }
            } catch (Exception e) {
                MAM.LOGGER.error("Failed to load spell from {}: {}", fileId, e.getMessage());
                failed++;
            }
        }

        MAM.LOGGER.info("Spell loading complete: {} loaded, {} failed", loaded, failed);
    }

    public static Optional<Spell> getSpell(Identifier id) {
        return Optional.ofNullable(SPELLS.get(id));
    }

    public static Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(SPELLS.values());
    }

    public static List<Spell> getSpellsBySchool(SpellSchool school) {
        return SPELLS.values().stream().filter(spell -> spell.getSchool() == school)
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }

    public static List<Spell> getSpellsByTier(int tier) {
        return SPELLS.values().stream().filter(spell -> spell.getTier() == tier).toList();
    }

    public static List<Spell> getSpellsBySchoolAndMaxTier(SpellSchool school, int maxTier) {
        return SPELLS.values().stream()
                .filter(spell -> spell.getSchool() == school && spell.getTier() <= maxTier)
                .sorted(Comparator.comparingInt(Spell::getTier)).toList();
    }
}
