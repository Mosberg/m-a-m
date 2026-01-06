package dk.mosberg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.mosberg.config.ServerConfig;
import dk.mosberg.entity.MAMEntities;
import dk.mosberg.item.MAMDataComponents;
import dk.mosberg.item.SpellbookItem;
import dk.mosberg.item.StaffItem;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.ManaRegenerationHandler;
import dk.mosberg.network.CastSpellPayload;
import dk.mosberg.network.ManaSyncPayload;
import dk.mosberg.network.OpenSpellBookPayload;
import dk.mosberg.network.SelectSpellPayload;
import dk.mosberg.network.ServerNetworkHandler;
import dk.mosberg.spell.SpellRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class MAM implements ModInitializer {
	public static final String MOD_ID = "mam";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// ═════════════════════════════════════════════════════════════════════════════
	// Gemstone Items
	// ═════════════════════════════════════════════════════════════════════════════
	public static final Item RUBY = registerItem("ruby", new Item(new Item.Settings()
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "ruby")))));
	public static final Item SAPPHIRE = registerItem("sapphire", new Item(new Item.Settings()
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "sapphire")))));
	public static final Item MOONSTONE = registerItem("moonstone", new Item(new Item.Settings()
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "moonstone")))));
	public static final Item PERIDOT = registerItem("peridot", new Item(new Item.Settings()
			.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "peridot")))));

	// ═════════════════════════════════════════════════════════════════════════════
	// Staff Items (4 tiers)
	// ═════════════════════════════════════════════════════════════════════════════
	public static final Item STAFF_NOVICE =
			registerItem("staff_novice",
					new StaffItem(
							new Item.Settings().maxCount(1).registryKey(RegistryKey
									.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "staff_novice"))),
							1));
	public static final Item STAFF_APPRENTICE = registerItem("staff_apprentice",
			new StaffItem(new Item.Settings().maxCount(1).registryKey(
					RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "staff_apprentice"))),
					2));
	public static final Item STAFF_ADEPT =
			registerItem("staff_adept",
					new StaffItem(
							new Item.Settings().maxCount(1).registryKey(RegistryKey
									.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "staff_adept"))),
							3));
	public static final Item STAFF_MASTER =
			registerItem("staff_master",
					new StaffItem(
							new Item.Settings().maxCount(1).registryKey(RegistryKey
									.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "staff_master"))),
							4));

	// ═════════════════════════════════════════════════════════════════════════════
	// Spellbook Items (4 tiers)
	// ═════════════════════════════════════════════════════════════════════════════
	public static final Item SPELLBOOK_NOVICE = registerItem("spellbook_novice",
			new SpellbookItem(new Item.Settings().maxCount(1).registryKey(
					RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "spellbook_novice"))),
					1));
	public static final Item SPELLBOOK_APPRENTICE =
			registerItem("spellbook_apprentice",
					new SpellbookItem(new Item.Settings().maxCount(1).registryKey(RegistryKey
							.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "spellbook_apprentice"))),
							2));
	public static final Item SPELLBOOK_ADEPT = registerItem("spellbook_adept",
			new SpellbookItem(new Item.Settings().maxCount(1).registryKey(
					RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "spellbook_adept"))),
					3));
	public static final Item SPELLBOOK_MASTER = registerItem("spellbook_master",
			new SpellbookItem(new Item.Settings().maxCount(1).registryKey(
					RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "spellbook_master"))),
					4));

	@SuppressWarnings("null")
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Initializing Mana And Magic...");

		// Register data components
		MAMDataComponents.register();

		// Register entities
		MAMEntities.register();

		// Register mana system
		ManaAttachments.register();
		ManaRegenerationHandler.register();

		// Register spell system
		SpellRegistry.register();

		// Register networking
		CastSpellPayload.register();
		ManaSyncPayload.register();
		SelectSpellPayload.register();
		OpenSpellBookPayload.register();
		ServerNetworkHandler.register();

		// Load server config
		ServerConfig.getInstance();

		// Add items to creative tabs
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.add(STAFF_NOVICE);
			entries.add(STAFF_APPRENTICE);
			entries.add(STAFF_ADEPT);
			entries.add(STAFF_MASTER);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.add(SPELLBOOK_NOVICE);
			entries.add(SPELLBOOK_APPRENTICE);
			entries.add(SPELLBOOK_ADEPT);
			entries.add(SPELLBOOK_MASTER);
		});

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.add(RUBY);
			entries.add(SAPPHIRE);
			entries.add(MOONSTONE);
			entries.add(PERIDOT);
		});

		LOGGER.info("Mana And Magic initialized successfully!");
	}

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
	}
}
