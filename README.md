# Minecraft 1.21.11 - Mana And Magic (Fabric)

## Minecraft 1.21.11 Fabric Project Overview and Setup

**Mana And Magic** is a data-driven, extensible magic mod for Minecraft 1.21.11 using Fabric Loader. Built with Java 21, Yarn mappings, and split source sets for strict client/server separation.

**Tech Stack:**

- Minecraft 1.21.11 + Fabric Loader 0.18.4
- Yarn mappings `1.21.11+build.3` (intermediary → named)
- Fabric Loom 1.14.10 with split source sets
- Java 21 toolchain with G1GC optimization

## Features

- **Spell Schools & Spells:** Air, Earth, Fire, Water schools with data-driven spells (JSON under `data/mam/spells/**`), currently including the four strike projectiles.
- **Items & Progression:** Four-tier spellbooks (novice → master) for casting; staffs act as optional passive buffs; four gemstones (ruby, sapphire, moonstone, peridot) bound to schools; assets under `assets/mam` with models/textures.
- **Casting Flow:** Hold a spellbook, press `R` to open the spell selection GUI, right-click to cast the selected spell. Spell availability respects the spellbook tier.
- **Mana System:** Three-pool mana (personal, aura, reserve) with regeneration, attachments via Fabric Data Attachments (`PlayerManaData`) and HUD overlay (toggle key placeholder implemented).
- **Entities & Rendering:** Custom `SpellProjectileEntity` with school-based textures and renderer; keybindings and HUD registered in the client entrypoint.
- **Networking:** Custom payloads for casting, selection, opening spellbook UI, and mana sync (`CastSpellPayload`, `SelectSpellPayload`, `OpenSpellBookPayload`, `ManaSyncPayload`); server handler routes selections to the held spellbook.
- **Architecture:** Strict split source sets (`src/main` common/server, `src/client` client-only). Client code may depend on common; server never imports client. Data-driven content and registry helpers live in `dk.mosberg` packages.
- **Build & Datagen:** Gradle tasks for client/server runs, build, and data generation; metadata sourced from `gradle.properties` (do not edit `fabric.mod.json` directly).

### Gradle Properties

**gradle.properties**

```properties
# ═══════════════════════════════════════════════════════════════════════════════
# Gradle JVM Configuration - Optimized for Fabric Development
# ═══════════════════════════════════════════════════════════════════════════════

# Allocate 4GB heap for larger Minecraft builds; G1GC recommended for heap >2GB
# MaxGCPauseMillis: Target pause time (lower = more frequent, shorter pauses)
org.gradle.jvmargs=-Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200

# Build Performance Optimizations
# configuration-cache: Speeds up subsequent builds by ~20% (stores task graphs)
# parallel: Tasks execute in parallel when possible
# caching: Remote/local build cache support
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

# ═══════════════════════════════════════════════════════════════════════════════
# Mod Metadata - Uniquely Identifies Your Mod (exported to fabric.mod.json)
# ═══════════════════════════════════════════════════════════════════════════════
# Note: Changes here auto-populate fabric.mod.json during build.processResources
# DO NOT edit fabric.mod.json directly - it's a template file.

maven_group=dk.mosberg
archives_base_name=mam

# mod_id: Used in code as MAM.MOD_ID (keep lowercase, no underscores)
mod_id=mam
mod_version=1.0.0
mod_name=Mana And Magic
mod_description=Mana and Magic is a data-driven, extensible Minecraft magic mod for Fabric.
mod_author=Mosberg
mod_homepage=https://mosberg.github.io/mam
mod_sources=https://github.com/mosberg/mam
mod_issues=https://github.com/mosberg/mam/issues
mod_license=MIT

# ═══════════════════════════════════════════════════════════════════════════════
# Minecraft & Fabric Versions - Keep Updated via https://fabricmc.net/develop
# ═══════════════════════════════════════════════════════════════════════════════
# Version Strategy: Always use latest patch in minor version (e.g., 1.21.11)
# Check https://fabricmc.net/use/installer/ for latest stable versions

minecraft_version=1.21.11
loader_version=0.18.4
yarn_mappings=1.21.11+build.3
loom_version=1.14.10
fabric_version=0.141.1+1.21.11
java_version=21

# ═══════════════════════════════════════════════════════════════════════════════
# Library Versions - Standard Dependencies
# ═══════════════════════════════════════════════════════════════════════════════

gson_version=2.13.2
slf4j_version=2.0.17
annotations_version=26.0.2

# ═══════════════════════════════════════════════════════════════════════════════
# Testing Framework
# ═══════════════════════════════════════════════════════════════════════════════

junit_version=5.11.0
```

### Gradle Build Script

**build.gradle**

```gradle
plugins {
    id 'fabric-loom' version "${loom_version}"
    id 'maven-publish'
    id 'java'
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

// ═════════════════════════════════════════════════════════════════════════════════
// Maven Repositories for Dependencies
// ═════════════════════════════════════════════════════════════════════════════════

repositories {
    mavenCentral()

    mavenLocal()

    maven {
        name = "Fabric"
        url = "https://maven.fabricmc.net/"
    }

    maven {
        name = "Terraformers"
        url = "https://maven.terraformersmc.com/releases/"
    }

    maven {
        name = "Shedaniel"
        url = "https://maven.shedaniel.me/"
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Fabric Loom Configuration - IDE Integration and Run Configurations
// ═════════════════════════════════════════════════════════════════════════════════

loom {
    splitEnvironmentSourceSets()

	mods {
		"mam" {
			sourceSet sourceSets.main
			sourceSet sourceSets.client
		}
	}

    runs {
        // Client run configuration (F5 in IDE)
        client {
            client()
            configName = "Minecraft Client"
            ideConfigGenerated = true
            runDir = "run"
        }

        // Server run configuration (F5 in IDE)
        server {
            server()
            configName = "Minecraft Server"
            ideConfigGenerated = true
            runDir = "run-server"
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Fabric API Data Generation
// ═════════════════════════════════════════════════════════════════════════════════

fabricApi {
	configureDataGeneration {
		client = true
	}
}

// ═════════════════════════════════════════════════════════════════════════════════
// Dependencies - All Required Libraries and Frameworks
// ═════════════════════════════════════════════════════════════════════════════════

dependencies {
    // Minecraft & Fabric Core
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"

    // Libraries - Bundled with JAR
    include implementation("com.google.code.gson:gson:${project.gson_version}")

    // Compile-Only - Annotations for Better IDE Support
    compileOnly "org.jetbrains:annotations:${project.annotations_version}"

    // Testing Framework - JUnit 6 with BOM for Dependency Management
    testImplementation platform("org.junit:junit-bom:${project.junit_version}")
    testImplementation "org.junit.jupiter:junit-jupiter"
    testImplementation "org.junit.jupiter:junit-jupiter-params"
    testRuntimeOnly "org.junit.platform:junit-platform-launcher"
}

// ═════════════════════════════════════════════════════════════════════════════════
// Source Sets Configuration
// ═════════════════════════════════════════════════════════════════════════════════

sourceSets {
    main {
        resources {
            srcDirs += [
                "src/main/generated/resources"
            ]
            exclude ".cache"
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Resource Processing
// ═════════════════════════════════════════════════════════════════════════════════

processResources {
    // Capture properties as local variables to avoid Groovy closure issues
    def modId = project.mod_id
    def modVersion = project.mod_version
    def modName = project.mod_name
    def modDescription = project.mod_description
    def modAuthor = project.mod_author
    def modHomepage = project.mod_homepage
    def modSources = project.mod_sources
    def modIssues = project.mod_issues
    def modLicense = project.mod_license
    def fabricLoaderVer = project.loader_version
    def minecraftVer = project.minecraft_version
    def javaVer = project.java_version

    // Define inputs for build cache invalidation
    inputs.property "mod_id", modId
    inputs.property "mod_version", modVersion
    inputs.property "mod_name", modName
    inputs.property "mod_description", modDescription
    inputs.property "mod_author", modAuthor
    inputs.property "mod_homepage", modHomepage
    inputs.property "mod_sources", modSources
    inputs.property "mod_issues", modIssues
    inputs.property "mod_license", modLicense
    inputs.property "loader_version", fabricLoaderVer
    inputs.property "minecraft_version", minecraftVer
    inputs.property "java_version", javaVer

    // Process fabric.mod.json template with property values
    filesMatching("fabric.mod.json") {
        expand(
            "mod_id": modId,
            "mod_version": modVersion,
            "mod_name": modName,
            "mod_description": modDescription,
            "mod_author": modAuthor,
            "mod_homepage": modHomepage,
            "mod_sources": modSources,
            "mod_issues": modIssues,
            "mod_license": modLicense,
            "loader_version": fabricLoaderVer,
            "minecraft_version": minecraftVer,
            "java_version": javaVer
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Java Compilation Configuration
// ═════════════════════════════════════════════════════════════════════════════════

tasks.withType(JavaCompile).configureEach {
    it.options.encoding = "UTF-8"
    it.options.release = project.java_version.toInteger()
    it.options.compilerArgs += ["-Xlint:deprecation", "-Xlint:unchecked"]
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(project.java_version.toInteger())
    }
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// ═════════════════════════════════════════════════════════════════════════════════
// JAR Manifest Configuration - Build Metadata
// ═════════════════════════════════════════════════════════════════════════════════

jar {
    // Capture properties for manifest
    def archivesBaseName = project.archives_base_name
    def modName = project.mod_name
    def modAuthor = project.mod_author
    def modVersion = project.version

    preserveFileTimestamps = false
    reproducibleFileOrder = true

    from("LICENSE") {
        rename { "${it}_${archivesBaseName}" }
    }

    manifest {
        attributes(
            "Specification-Title": modName,
            "Specification-Vendor": modAuthor,
            "Specification-Version": "1",
            "Implementation-Title": modName,
            "Implementation-Version": modVersion,
            "Implementation-Vendor": modAuthor,
            "Implementation-Timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Javadoc Configuration - Documentation Generation
// ═════════════════════════════════════════════════════════════════════════════════

javadoc {
    options.encoding = 'UTF-8'
    options.charSet = 'UTF-8'
    options.addStringOption('Xdoclint:none', '-quiet')
}

// ═════════════════════════════════════════════════════════════════════════════════
// Test Configuration - JUnit 5 with Comprehensive Logging
// ═════════════════════════════════════════════════════════════════════════════════

test {
    useJUnitPlatform()

    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat = "full"
        showStandardStreams = false
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Maven Publication Configuration - JAR Distribution
// ═════════════════════════════════════════════════════════════════════════════════

publishing {
    publications {
        mavenJava(MavenPublication) {
            artifactId = project.archives_base_name
            from components.java

            pom {
                name = project.mod_name
                description = project.mod_description
                url = project.mod_homepage

                licenses {
                    license {
                        name = project.mod_license
                    }
                }

                developers {
                    developer {
                        name = project.mod_author
                    }
                }

                scm {
                    url = project.mod_sources
                    connection = "scm:git:${project.mod_sources}.git"
                }
            }
        }
    }

    repositories {
        // Uncomment to publish to local Maven repository
        // mavenLocal()

        // Template for custom Maven repository
        // maven {
        //     name = "MyMaven"
        //     url = "https://maven.example.com/releases"
        //     credentials {
        //         username = project.findProperty("maven.username") ?: System.getenv("MAVEN_USERNAME")
        //         password = project.findProperty("maven.password") ?: System.getenv("MAVEN_PASSWORD")
        //     }
        // }
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// Custom Tasks - Utility Commands
// ═════════════════════════════════════════════════════════════════════════════════

// Display build information - Configuration cache compliant
tasks.register("projectInfo") {
    // Capture properties at configuration time for configuration cache compatibility
    def modName = project.mod_name
    def modVersion = project.mod_version
    def minecraftVersion = project.minecraft_version
    def loaderVersion = project.loader_version
    def fabricVersion = project.fabric_version
    def javaVersion = project.java_version
    def gradleVersion = gradle.gradleVersion
    doLast {
        println """
            ======================================
            Mana And Magic Build Information
            ======================================
            Mod Name:       ${modName}
            Version:        ${modVersion}
            Minecraft:      ${minecraftVersion}
            Fabric Loader:  ${loaderVersion}
            Fabric API:     ${fabricVersion}
            Java Version:   ${javaVersion}
            Gradle Version: ${gradleVersion}
            ======================================
        """.stripIndent()
    }
}

// Clean build artifacts
clean {
}
```

### Gradle Settings Script

**settings.gradle**

```gradle
// ═════════════════════════════════════════════════════════════════════════════════
// Plugin Management Configuration
// ═════════════════════════════════════════════════════════════════════════════════
// Gradle queries these repositories IN ORDER for plugins (fabric-loom, maven-publish)

pluginManagement {
	repositories {
		// Fabric Maven: Contains fabric-loom and all Minecraft/Fabric dependencies
		maven {
			name = 'Fabric'
			url = 'https://maven.fabricmc.net/'
		}
		// Maven Central: Standard Java libraries and build plugins
		mavenCentral()
		// Gradle Plugin Portal: Gradle-native plugins (maven-publish, java, etc.)
		gradlePluginPortal()
	}
}

// Project Configuration
rootProject.name = "mam"
```

### Fabric Mod Metadata

**fabric.mod.json**

```json
{
  "schemaVersion": 1,
  "id": "${mod_id}",
  "version": "${mod_version}",
  "name": "${mod_name}",
  "description": "${mod_description}",
  "authors": ["${mod_author}"],
  "contact": {
    "homepage": "${mod_homepage}",
    "sources": "${mod_sources}",
    "issues": "${mod_issues}"
  },
  "license": "${mod_license}",
  "icon": "icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["dk.mosberg.MAM"],
    "client": ["dk.mosberg.client.MAMClient"],
    "fabric-datagen": ["dk.mosberg.datagen.MAMDataGenerator"]
  },
  "mixins": [],
  "depends": {
    "fabricloader": ">=${loader_version}",
    "minecraft": "~${minecraft_version}",
    "java": ">=${java_version}",
    "fabric-api": "*"
  }
}
```

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
