# Mana And Magic - AI Coding Agent Instructions

## Project Overview

Minecraft 1.21.11 Fabric mod implementing a data-driven magic system with 4 spell schools (Air, Earth, Fire, Water), gemstones, spellbooks, and staffs. Built with Java 21, Yarn mappings, and split source sets for strict client/server separation.

## Minecraft 1.21.11 Remote Indexing & API References

**GitHub Copilot Remote Indexing:** This project uses remote repository indexing for enhanced context awareness. The following repositories are indexed for API guidance:

### Core Dependencies (Remote Indexed)

- **Fabric API** - `https://github.com/FabricMC/fabric` - Fabric API hooks, networking, rendering
- **Minecraft Yarn Mappings** - `https://github.com/FabricMC/yarn` - Readable class/method names for 1.21.11
- **Fabric Loader** - `https://github.com/FabricMC/fabric-loader` - Mod loading system

## Minecraft 1.21.11 Key Documentation Sources (Remote Indexed)

### Minecraft 1.21.11 Yarn Mappings Documentation by FabricMC (Remote Indexed)

- **Yarn 1.21.11+build.3 - Overview** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/index.html - Minecraft 1.21.11 yarn-mappings overview list by FabricMC
- **Yarn 1.21.11+build.3 - Hierarchy For All Packages** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/overview-tree.html - Minecraft 1.21.11 yarn-mappings Hierarchy For All Packages by FabricMC
- **Yarn 1.21.11+build.3 - Deprecated** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/deprecated-list.html - Minecraft 1.21.11 yarn-mappings deprecated list by FabricMC
- **Yarn 1.21.11+build.3 - Index** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/index-files/index-1.html - Minecraft 1.21.11 yarn-mappings index list by FabricMC
- **Yarn 1.21.11+build.3 - JavaDoc Help** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/help-doc.html#index - Minecraft 1.21.11 yarn-mappings JavaDoc Help by FabricMC
- **Yarn 1.21.11+build.3 - All Packages** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/allpackages-index.html - Minecraft 1.21.11 yarn-mappings All Packages List by FabricMC
- **Yarn 1.21.11+build.3 - All Classes and Interfaces** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/allclasses-index.html - Minecraft 1.21.11 yarn-mappings All Classes and Interfaces List by FabricMC
- **Yarn 1.21.11+build.3 - Constant Field Values** - https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/constant-values.html - Minecraft 1.21.11 yarn-mappings Constant Field Values List by FabricMC

### Minecraft 1.21.11 Fabric API Documentation (Remote Indexed)

- **Fabric API 0.141.1+1.21.11 - Overview** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/index.html - Fabric API 0.140.2+1.21.11 overview list by FabricMC
- **Fabric API 0.141.1+1.21.11 - Hierarchy For All Packages** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/overview-tree.html - Fabric API 0.141.1+1.21.11 Hierarchy For All Packages by FabricMC
- **Fabric API 0.141.1+1.21.11 - Deprecated** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/deprecated-list.html - Fabric API 0.141.1+1.21.11 deprecated list by FabricMC
- **Fabric API 0.141.1+1.21.11 - Index** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/index-all.html - Fabric API 0.141.1+1.21.11 index list by FabricMC
- **Fabric API 0.141.1+1.21.11 - JavaDoc Help** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/help-doc.html#index - Fabric API 0.141.1+1.21.11 JavaDoc Help by FabricMC
- **Fabric API 0.141.1+1.21.11 - All Packages** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/allpackages-index.html - Fabric API 0.141.1+1.21.11 All Packages List by FabricMC
- **Fabric API 0.141.1+1.21.11 - All Classes and Interfaces** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/allclasses-index.html - Fabric API 0.141.1+1.21.11 All Classes and Interfaces List by FabricMC
- **Fabric API 0.141.1+1.21.11 - Constant Field Values** - https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/constant-values.html - Fabric API 0.141.1+1.21.11 Constant Field Values List by FabricMC

### Java 21 Documentation (Remote Indexed)

- **Java SE 21 - Oracle Docs** - https://docs.oracle.com/en/java/javase/21/docs/api/index.html - Official Java SE 21 API documentation by Oracle
- **Java SE 21 - Javadoc Search Spec** - https://docs.oracle.com/en/java/javase/21/docs/specs/javadoc/javadoc-search-spec.html - Java SE 21 Javadoc search specification by Oracle

### Fabricmc 1.21.11 Developer Documentation (Remote Indexed)

- **Docs Fabricmc Develop** - https://docs.fabricmc.net/develop/ - Developer Guides for Fabric projects
- **Docs Fabricmc Develop Items Creating Your First Item** - https://docs.fabricmc.net/develop/items/first-item - Step-by-step item creation guide
- **Docs Fabricmc Develop Items Food Items** - https://docs.fabricmc.net/develop/items/food - Guide on creating food items
- **Docs Fabricmc Develop Items Potions** - https://docs.fabricmc.net/develop/items/potions - Guide on creating potion items
- **Docs Fabricmc Develop Items Spawn Eggs** - https://docs.fabricmc.net/develop/items/spawn-eggs - Guide on creating spawn egg items
- **Docs Fabricmc Develop Items Tools and Weapons** - https://docs.fabricmc.net/develop/items/custom-tools - Guide on creating custom tools and weapons
- **Docs Fabricmc Develop Items Custom Armor** - https://docs.fabricmc.net/develop/items/custom-armor - Guide on creating custom armor items
- **Docs Fabricmc Develop Items Item Models** - https://docs.fabricmc.net/develop/items/item-models - Guide on defining item models
- **Docs Fabricmc Develop Items Item Appearance** - https://docs.fabricmc.net/develop/items/item-appearance - Guide on customizing item appearance
- **Docs Fabricmc Develop Items Custom Creative Tabs** - https://docs.fabricmc.net/develop/items/custom-item-groups - Guide on creating custom creative tabs
- **Docs Fabricmc Develop Items Custom Item Interactions** - https://docs.fabricmc.net/develop/items/custom-item-interactions - Guide on implementing custom item interactions
- **Docs Fabricmc Develop Items Custom Enchantment Effects** - https://docs.fabricmc.net/develop/items/custom-enchantment-effects - Guide on creating custom enchantment effects
- **Docs Fabricmc Develop Items Custom Data Components** - https://docs.fabricmc.net/develop/items/custom-data-components - Guide on adding custom data components to items
- **Docs Fabricmc Develop Blocks Creating Your First Block** - https://docs.fabricmc.net/develop/blocks/first-block - Step-by-step block creation guide
- **Docs Fabricmc Develop Blocks Block Models** - https://docs.fabricmc.net/develop/blocks/block-models - Guide on defining block models
- **Docs Fabricmc Develop Blocks Block States** - https://docs.fabricmc.net/develop/blocks/blockstates - Guide on defining block states
- **Docs Fabricmc Develop Blocks Block Entities** - https://docs.fabricmc.net/develop/blocks/block-entities - Guide on creating block entities
- **Docs Fabricmc Develop Blocks Block Entity Renderers** - https://docs.fabricmc.net/develop/blocks/block-entity-renderers - Guide on rendering block entities
- **Docs Fabricmc Develop Blocks Transparency and Tinting** - https://docs.fabricmc.net/develop/blocks/transparency-and-tinting - Guide on block transparency and tinting
- **Docs Fabricmc Develop Entities Entity Attributes** - https://docs.fabricmc.net/develop/entities/attributes - Guide on defining entity attributes
- **Docs Fabricmc Develop Entities Mob Effects** - https://docs.fabricmc.net/develop/entities/effects - Guide on creating custom mob effects
- **Docs Fabricmc Develop Entities Damage Types** - https://docs.fabricmc.net/develop/entities/damage-types - Guide on defining custom damage types
- **Docs Fabricmc Develop Sounds Playing Sounds** - https://docs.fabricmc.net/develop/sounds/using-sounds - Guide on playing sounds in-game
- **Docs Fabricmc Develop Sounds Creating Custom Sounds** - https://docs.fabricmc.net/develop/sounds/custom-sounds - Guide on creating and adding custom sounds
- **Docs Fabricmc Develop Sounds Dynamic and Interactive Sounds** - https://docs.fabricmc.net/develop/sounds/dynamic-sounds - Guide on implementing dynamic and interactive sounds
- **Docs Fabricmc Develop Commands Creating Commands** - https://docs.fabricmc.net/develop/commands/basics - Guide on creating commands
- **Docs Fabricmc Develop Commands Command Arguments** - https://docs.fabricmc.net/develop/commands/arguments - Guide on using command arguments
- **Docs Fabricmc Develop Commands Command Suggestions** - https://docs.fabricmc.net/develop/commands/suggestions - Guide on adding suggestions to commands
- **Docs Fabricmc Develop Rendering Basic Rendering Concepts** - https://docs.fabricmc.net/develop/rendering/basic-concepts - Overview of rendering concepts in Fabric
- **Docs Fabricmc Develop Rendering Drawing to the GUI** - https://docs.fabricmc.net/develop/rendering/draw-context - Guide on drawing to the GUI
- **Docs Fabricmc Develop Rendering Rendering in the HUD** - https://docs.fabricmc.net/develop/rendering/hud - Guide on rendering elements in the HUD
- **Docs Fabricmc Develop Rendering Rendering in the World** - https://docs.fabricmc.net/develop/rendering/world - Guide on rendering custom elements in the world
- **Docs Fabricmc Develop Rendering GUI Custom Screens** - https://docs.fabricmc.net/develop/rendering/gui/custom-screens - Guide on creating custom GUI screens
- **Docs Fabricmc Develop Rendering GUI Custom Widgets** - https://docs.fabricmc.net/develop/rendering/gui/custom-widgets - Guide on creating custom GUI widgets
- **Docs Fabricmc Develop Rendering Particles Creating Custom Particles** - https://docs.fabricmc.net/develop/rendering/particles/creating-particles - Guide on creating custom particles
- **Docs Fabricmc Develop Codecs** - https://docs.fabricmc.net/develop/codecs/ - Guide on using codecs for data serialization
- **Docs Fabricmc Develop Data Attachments** - https://docs.fabricmc.net/develop/data-attachments/ - Guide on attaching custom data to game objects
- **Docs Fabricmc Develop Saved Data** - https://docs.fabricmc.net/develop/saved-data/ - Guide on saving and loading custom data
- **Docs Fabricmc Develop Events** - https://docs.fabricmc.net/develop/events/ - Overview of event handling in Fabric
- **Docs Fabricmc Develop Text and Translations** - https://docs.fabricmc.net/develop/text-and-translations/ - Guide on handling text and translations
- **Docs Fabricmc Develop Networking** - https://docs.fabricmc.net/develop/networking/ - Guide on implementing networking in Fabric mods
- **Docs Fabricmc Develop Key Mappings** - https://docs.fabricmc.net/develop/key-mappings/ - Guide on creating and handling key mappings
- **Docs Fabricmc Develop Debugging Mods** - https://docs.fabricmc.net/develop/debugging/ - Guide on debugging Fabric mods
- **Docs Fabricmc Develop Automated Testing** - https://docs.fabricmc.net/develop/automatic-testing/ - Guide on setting up automated tests for Fabric mods
- **Docs Fabricmc Develop Data Generation Data Generation Setup** - https://docs.fabricmc.net/develop/data-generation/setup/ - Guide on setting up data generation
- **Docs Fabricmc Develop Data Generation Translation Generation** - https://docs.fabricmc.net/develop/data-generation/translations/ - Guide on generating translations
- **Docs Fabricmc Develop Data Generation Block Model Generation** - https://docs.fabricmc.net/develop/data-generation/block-models/ - Guide on generating block models
- **Docs Fabricmc Develop Data Generation Item Model Generation** - https://docs.fabricmc.net/develop/data-generation/item-models/ - Guide on generating item models
- **Docs Fabricmc Develop Data Generation Tag Generation** - https://docs.fabricmc.net/develop/data-generation/tags/ - Guide on generating tags
- **Docs Fabricmc Develop Data Generation Advancement Generation** - https://docs.fabricmc.net/develop/data-generation/advancements/ - Guide on generating advancements
- **Docs Fabricmc Develop Data Generation Recipe Generation** - https://docs.fabricmc.net/develop/data-generation/recipes/ - Guide on generating recipes
- **Docs Fabricmc Develop Data Generation Loot Table Generation** - https://docs.fabricmc.net/develop/data-generation/loot-tables/ - Guide on generating loot tables

**Usage in Development:**

- Copilot can reference Fabric API patterns from indexed repositories
- Minecraft 1.21.11-specific APIs are resolved via Yarn mappings and Fabric API mappings
- Client/server split patterns follow Fabric Loader conventions
- Network packet handling uses Fabric Networking API v1

## Architecture & Source Organization

### Core Content Design

**4 Spell Schools:** Air, Earth, Fire, Water
**4 Gemstone Types:** Ruby (Fire), Sapphire (Water), Moonstone (Air), Peridot (Earth)
**4 Spellbook Tiers:** Novice (Tier 1), Apprentice (Tier 2), Adept (Tier 3), Master (Tier 4)
**4 Staff Tiers:** Novice (Tier 1), Apprentice (Tier 2), Adept (Tier 3), Master (Tier 4)

**Design principle:** Each gemstone binds to specific spell schools.

### Split Source Sets (Loom Feature)

**Critical:** This project uses `splitEnvironmentSourceSets()` which physically separates client and server code at compile time.

```
📁mam
└── 📁src
    ├── 📁client # Client-only code (NEVER imported by server)
    │   ├── 📁java
    │   │   └── 📁dk
    │   │       └── 📁mosberg
    │   │           └── 📁client
    │   │               └── MAMClient.java # Client entrypoint (ClientModInitializer)
    │   └── 📁resources # Client-specific assets
    └── 📁main # Server-side & common logic (runs on both sides)
        ├── 📁java
        │   └── 📁dk
        │       └── 📁mosberg
        │           ├── 📁datagen
        │           │   └── MAMDataGenerator.java # Data Generator
        │           └── MAM.java # Main entrypoint (ModInitializer)
        └── 📁resources
            ├── 📁assets
            │   └── 📁mam # Shared resources
            │       ├── 📁blockstates
            │       ├── 📁items # Important for loading textures in-game
            │       │   ├── moonstone.json
            │       │   ├── peridot.json
            │       │   ├── projectile_air.json
            │       │   ├── projectile_earth.json
            │       │   ├── projectile_fire.json
            │       │   ├── projectile_water.json
            │       │   ├── ruby.json
            │       │   ├── sapphire.json
            │       │   ├── spellbook_adept.json
            │       │   ├── spellbook_apprentice.json
            │       │   ├── spellbook_master.json
            │       │   ├── spellbook_novice.json
            │       │   ├── staff_adept.json
            │       │   ├── staff_apprentice.json
            │       │   ├── staff_master.json
            │       │   └── staff_novice.json
            │       ├── 📁lang
            │       │   └── en_us.json
            │       ├── 📁models
            │       │   ├── 📁block
            │       │   ├── 📁entity
            │       │   └── 📁item
            │       │       ├── moonstone.json
            │       │       ├── peridot.json
            │       │       ├── projectile_air.json
            │       │       ├── projectile_earth.json
            │       │       ├── projectile_fire.json
            │       │       ├── projectile_water.json
            │       │       ├── ruby.json
            │       │       ├── sapphire.json
            │       │       ├── spellbook_adept.json
            │       │       ├── spellbook_apprentice.json
            │       │       ├── spellbook_master.json
            │       │       ├── spellbook_novice.json
            │       │       ├── staff_adept.json
            │       │       ├── staff_apprentice.json
            │       │       ├── staff_master.json
            │       │       └── staff_novice.json
            │       └── 📁textures
            │           ├── 📁block
            │           ├── 📁entity
            │           ├── 📁gui
            │           └── 📁item
            │               ├── 📁gemstone
            │               │   ├── moonstone.png
            │               │   ├── peridot.png
            │               │   ├── ruby.png
            │               │   └── sapphire.png
            │               ├── 📁projectile
            │               │   ├── projectile_air.png
            │               │   ├── projectile_earth.png
            │               │   ├── projectile_fire.png
            │               │   └── projectile_water.png
            │               ├── 📁spellbook
            │               │   ├── spellbook_adept.png
            │               │   ├── spellbook_apprentice.png
            │               │   ├── spellbook_master.png
            │               │   └── spellbook_novice.png
            │               └── 📁staff
            │                   ├── staff_adept.png
            │                   ├── staff_apprentice.png
            │                   ├── staff_master.png
            │                   └── staff_novice.png
            ├── 📁data
            │   └── 📁mam # Shared data resources
            │       └── 📁spells
            │           ├── 📁air
            │           │   └── air_strike.json
            │           ├── 📁earth
            │           │   └── earth_strike.json
            │           ├── 📁fire
            │           │   └── fire_strike.json
            │           └── 📁water
            │               └── water_strike.json
            ├── fabric.mod.json # Mod manifest (template - auto-expanded)
            ├── icon.png
            └── mam.properties
```

### Environment Rules

1. **Server code cannot import client packages** - Causes `ClassNotFoundException` on dedicated servers
2. **Client code can import server code** - Safe, clients have all classes
3. **Annotate client-only classes with `@Environment(EnvType.CLIENT)`** (from `net.fabricmc.api.EnvType`)
4. **Use Yarn mappings** - All Minecraft classes use intermediary names that remap to readable names

### Entry Points

- `dk.mosberg.MAM` (main) - Runs on both client and server, register items/blocks here
- `dk.mosberg.client.MAMClient` (client) - Runs only on client, register renderers/keybinds here
- `dk.mosberg.datagen.MAMDataGenerator` (fabric-datagen) - Data generation entry point

## Critical Architecture Rules

### Source Set Separation (Fabric Loom Feature)

This project uses `splitEnvironmentSourceSets()` - client and server code are physically separated at compile time:

- **`src/main/java/`** - Server-side and common code (runs on both sides). Register items, blocks, entities here.
- **`src/client/java/`** - Client-only code (renderers, keybinds, GUI). NEVER imported by server.
- **Entry Points:**
  - `dk.mosberg.MAM` (main) - Common initialization, implements `ModInitializer`
  - `dk.mosberg.client.MAMClient` (client) - Client initialization, implements `ClientModInitializer`
  - `dk.mosberg.datagen.MAMDataGenerator` (fabric-datagen) - Data generation, implements `DataGeneratorEntrypoint`

**Rule:** Server code CANNOT import `dk.mosberg.client.*` packages (causes `ClassNotFoundException` on dedicated servers). Client code CAN import main code.

### Configuration Management

**DO NOT edit `fabric.mod.json` directly** - it's a template file. All mod metadata is defined in `gradle.properties` and auto-populated during `processResources` build task:

- `mod_id`, `mod_version`, `mod_name`, `mod_description`, `mod_author`, etc.
- Version numbers: `minecraft_version`, `loader_version`, `fabric_version`, `java_version`
- Changes to metadata require running Gradle's `processResources` task to take effect

### Naming Conventions

- Package structure: `dk.mosberg` (main), `dk.mosberg.client` (client-only)
- Mod ID constant: `MAM.MOD_ID` (value: "mam") - use for all registry keys
- Logger pattern: `MAM.LOGGER` (SLF4J) - use for console/log output
- Keep mod ID lowercase, no underscores

## Development Workflows

### Build and Run

- **Client:** Run `./gradlew runClient` or use IDE run configuration "Minecraft Client" (runs in `run/`)
- **Server:** Run `./gradlew runServer` or use IDE run configuration "Minecraft Server" (runs in `run-server/`)
- **Build JAR:** `./gradlew build` (output: `build/libs/mam-1.0.0.jar`)
- **Data Generation:** `./gradlew runDatagen` (outputs to `src/main/generated/resources/`)

### Key Gradle Tasks

- `./gradlew clean` - Clean build artifacts
- `./gradlew build` - Full build with tests and JAR packaging
- `./gradlew javadoc` - Generate JavaDocs (output: `build/docs/javadoc/`)
- `./gradlew projectInfo` - Display build configuration and version info

### Testing

- Framework: JUnit 5 (Jupiter) with parameterized tests support
- Run tests: `./gradlew test`
- Test reports: `build/reports/tests/test/index.html`

## Resource Management

### Assets Structure

Assets live in `src/main/resources/assets/mam/`:

- **items/** - Item model definitions (JSON) linking items to model files
- **models/item/** - 3D model definitions (JSON) referencing textures
- **textures/item/** - PNG textures organized by category:
  - `gemstone/` - Ruby, Sapphire, Moonstone, Peridot
  - `spellbook/` - Novice, Apprentice, Adept, Master tiers
  - `staff/` - Novice, Apprentice, Adept, Master tiers
  - `projectile/` - Air, Earth, Fire, Water spell projectiles
- **lang/** - Translations (e.g., `en_us.json`)

**Asset Linkage Pattern:** Each item requires 3 files:

1. Item registration in Java code (e.g., `Registry.register(...)`)
2. Model pointer: `assets/mam/items/moonstone.json` → `"model": "mam:item/moonstone"`
3. Model definition: `assets/mam/models/item/moonstone.json` → `"textures": {"layer0": "mam:item/gemstone/moonstone"}`
4. Texture file: `assets/mam/textures/item/gemstone/moonstone.png`

### Data-Driven Content

Data packs live in `src/main/resources/data/mam/`:

- **spells/** - Spell definitions organized by school (`air/`, `earth/`, `fire/`, `water/`)
- Each school has base spells like `air_strike.json`, `earth_strike.json`, etc.

**Spell JSON Schema Example** (`data/mam/spells/air/air_strike.json`):

```json
{
  "id": "mam:air_strike",
  "school": "air",
  "tier": 1,
  "manaCost": 12.0,
  "damage": 2.0,
  "range": 40.0,
  "projectileSpeed": 1.5,
  "vfx": {
    "particleType": "wind_particle",
    "color": "87CEEB"
  }
}
```

All spells follow this structure with required fields: `id`, `school`, `tier`, `manaCost`

## Content Design System

### Magic System Structure

- **4 Spell Schools:** Air, Earth, Fire, Water
- **4 Gemstones:** Ruby (Fire), Sapphire (Water), Moonstone (Air), Peridot (Earth)
- **4 Progression Tiers:** Novice (T1), Apprentice (T2), Adept (T3), Master (T4)
- **Design Principle:** Each gemstone binds to a specific spell school

When creating new content, follow the tier-based naming pattern and maintain school-to-gemstone associations.

### Gemstone-School Binding System

**Core Binding Relationships:**

| Gemstone  | Spell School | Theme              | Visual Color   |
| --------- | ------------ | ------------------ | -------------- |
| Ruby      | Fire         | Destruction, Heat  | Red/Orange     |
| Sapphire  | Water        | Healing, Flow      | Blue           |
| Moonstone | Air          | Mobility, Wind     | Sky Blue/White |
| Peridot   | Earth        | Defense, Stability | Green          |

**Implementation Pattern:**

When a player uses a spellbook or staff:

1. System checks if the item has a bound gemstone (via NBT/custom data component)
2. Gemstone determines which spell school's spells are accessible
3. Staff/spellbook tier determines maximum spell tier available (Novice staff → tier 1 spells only)

**Example Binding Flow:**

- Player combines `staff_apprentice` + `moonstone` → Can cast Air school spells up to tier 2
- Player combines `spellbook_master` + `ruby` → Can cast Fire school spells up to tier 4

### Extending the Magic System

#### Adding a New Spell School

**Example: Adding "Lightning" school with Topaz gemstone**

**Step 1:** Create gemstone item assets

```
assets/mam/textures/item/gemstone/topaz.png
assets/mam/models/item/topaz.json:
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "mam:item/gemstone/topaz"
  }
}
```

**Step 2:** Create spell data structure

```
data/mam/spells/lightning/lightning_strike.json:
{
  "id": "mam:lightning_strike",
  "school": "lightning",
  "tier": 1,
  "manaCost": 15.0,
  "damage": 4.0,
  "range": 50.0,
  "vfx": {
    "particleType": "electric_spark",
    "color": "FFD700"
  }
}
```

**Step 3:** Register items in `MAM.java`

```java
public static final Item TOPAZ = Registry.register(
    Registries.ITEM,
    Identifier.of(MOD_ID, "topaz"),
    new Item(new Item.Settings())
);
```

**Step 4:** Add translations in `assets/mam/lang/en_us.json`

```json
{
  "item.mam.topaz": "Topaz",
  "spell.mam.lightning_strike": "Lightning Strike",
  "school.mam.lightning": "Lightning"
}
```

#### Adding a New Progression Tier

**Example: Adding "Grandmaster" (Tier 5)**

**Step 1:** Create staff/spellbook variants

```
assets/mam/textures/item/staff/staff_grandmaster.png
assets/mam/textures/item/spellbook/spellbook_grandmaster.png
```

**Step 2:** Create item models following existing pattern

```
assets/mam/models/item/staff_grandmaster.json
assets/mam/items/staff_grandmaster.json
```

**Step 3:** Create tier 5 spells for each school

```
data/mam/spells/fire/inferno.json:
{
  "id": "mam:inferno",
  "school": "fire",
  "tier": 5,
  "manaCost": 50.0,
  "damage": 15.0,
  "aoeRadius": 10.0
}
```

**Step 4:** Update tier validation logic to recognize tier 5

- Modify spell casting system to allow tier 5 access for grandmaster items
- Ensure progression gates respect the new tier (e.g., level requirements)

#### Naming Conventions for Extensions

- **Gemstones:** Single-word lowercase: `topaz`, `emerald`, `amethyst`
- **Schools:** Single-word lowercase: `lightning`, `ice`, `nature`
- **Tiers:** Follow progression: `novice`, `apprentice`, `adept`, `master`, `grandmaster`, `archmage`
- **Spells:** `{school}_{action}`: `lightning_bolt`, `ice_shard`, `nature_growth`

#### Required Consistency Checks

When extending the system, ensure:

- Each gemstone has exactly ONE associated school
- Texture resolution is 16x16 pixels (Minecraft standard)
- Model files reference correct texture paths using `mam:` namespace
- Spell JSON includes all required fields: `id`, `school`, `tier`, `manaCost`
- Translation keys follow pattern: `item.mam.{item_name}`, `spell.mam.{spell_id}`

## Dependency Information

### Core Dependencies

- Minecraft 1.21.11 with Fabric Loader 0.18.4
- Fabric API 0.141.1+1.21.11
- Yarn mappings 1.21.11+build.3 (use for all Minecraft class references)
- Java 21 toolchain (sourceCompatibility and targetCompatibility)

### Bundled Libraries

- GSON 2.13.2 (included in JAR via `include implementation`)
- JetBrains Annotations 26.0.2 (compile-only)

### Documentation References

- Fabric API docs: https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/
- Yarn mappings: https://maven.fabricmc.net/docs/yarn-1.21.11+build.3/
- Fabric Wiki: https://docs.fabricmc.net/develop/

## Code Patterns

### Registry Pattern

All game objects (items, blocks, entities) are registered in `MAM.onInitialize()` using Fabric Registry API. Use `MAM.MOD_ID` for namespacing registry keys.

### Logging Pattern

Use `MAM.LOGGER` (SLF4J) for all logging:

```java
MAM.LOGGER.info("Message");
MAM.LOGGER.warn("Warning");
MAM.LOGGER.error("Error");
```

### Environment Annotations

When code must exist only on client (e.g., rendering), annotate classes/methods:

```java
@Environment(EnvType.CLIENT)
public class MyRenderer { }
```

## TODO Management & Progress Tracking

### Automatic TODO Verification

**IMPORTANT:** When implementing features or making significant progress, ALWAYS verify and update the TODO tracking system:

1. **Check TODO.md Status**: Read `TODO.md` to see which features are marked as completed `[x]`
2. **Verify Implementation**: For each marked feature, check if the corresponding class actually has the feature implemented
3. **Update TODO.md**: Mark features as complete `[x]` when verified in code
4. **Clean Class TODOs**: Remove completed TODO comments from class JavaDoc headers when features are confirmed working

### TODO Verification Process

When asked to "check TODOs", "verify progress", or after implementing multiple features, follow this process:

```
1. Read TODO.md Phase 1 section (lines 1-80)
2. For each [x] marked item, verify implementation:
   - SpellSchool.java: Check for stat modifiers, weakness/resistance, environmental methods
   - SpellCastType.java: Verify all cast types exist, cooldown multipliers, tier restrictions
   - ManaPoolType.java: Verify SKILL pool exists, combo bonuses, threshold methods
   - ManaPool.java: Check for efficiency tracking, corruption, overfill mechanics
   - SpellRegistry.java: Verify validation, hot-reload, difficulty presets, dependency resolution
   - Spell.java: Check for rarity field, environmental effect methods
3. Update TODO.md: Change [ ] to [x] for verified features
4. Clean classes: Remove TODO comments that match completed items
```

### TODO Comment Removal Pattern

When removing completed TODOs from class headers:

**Before:**

```java
/**
 * Class description.
 *
 * TODO: Feature A (implemented ✓)
 * TODO: Feature B (not implemented)
 * TODO: Feature C (implemented ✓)
 */
```

**After:**

```java
/**
 * Class description.
 *
 * Features implemented: Feature A, Feature C
 *
 * TODO: Feature B (not implemented)
 */
```

### Progress Reporting

After verification, provide a brief summary:

- Features verified: X/Y in Phase 1
- TODOs removed: Z comments cleaned
- Build status: Compiled successfully / Has errors

**Do NOT create a separate markdown file to document changes unless explicitly requested.**

## Performance Optimizations

### Gradle Configuration

- G1GC with 4GB heap allocation for faster builds
- Configuration cache enabled (~20% build speedup)
- Parallel task execution enabled
- Build cache enabled for incremental builds

### Run Configurations

Both client and server use separate run directories to avoid conflicts (`run/` and `run-server/`).
