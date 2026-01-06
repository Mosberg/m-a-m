# Mana And Magic - Implementation Roadmap

This document contains all TODO items from the codebase organized by implementation priority and dependencies.

---

## Phase 1: Foundation & Core Systems (Essential)

### Spell School System

- [x] **SpellSchool.java** - Add school-specific stat modifiers (damage multiplier, mana cost, cooldown)
- [ ] **SpellSchool.java** - Implement school affinity system (players stronger in certain schools)
- [x] **SpellSchool.java** - Add school weakness/resistance interactions (rock-paper-scissors style)
- [ ] **SpellSchool.java** - Implement school-based enchantments (spellbooks/staffs specialize in school)
- [x] **SpellSchool.java** - Add environmental interactions per school (water floats, fire spreads)
- [ ] **SpellSchool.java** - Implement school evolution (higher tiers unlock new mechanics)
- [ ] **SpellSchool.java** - Add school combinations (hybrid schools for mixed-element spells)
- [ ] **SpellSchool.java** - Implement school prestige/mastery system (unlock bonuses)

### Spell Cast Types

- [x] **SpellCastType.java** - Add BEAM cast type (continuous laser attack until released)
- [x] **SpellCastType.java** - Add SELF-CAST type (buffs/debuffs on caster)
- [x] **SpellCastType.java** - Add TRAP type (placed on ground, triggered by entities)
- [x] **SpellCastType.java** - Add SUMMON type (spawns entities to fight for player)
- [x] **SpellCastType.java** - Add TRANSFORM type (temporarily changes player form/abilities)
- [x] **SpellCastType.java** - Implement cast type-specific cooldown multipliers
- [x] **SpellCastType.java** - Add cast type animations and VFX customization per type
- [x] **SpellCastType.java** - Implement cast type restrictions by tier/equipment

### Mana Pool System

- [ ] **ManaPoolType.java** - Add fourth pool type for special mechanics (overflow, burnout, or skill-based)
- [ ] **ManaPoolType.java** - Implement pool affinity system (players born with pool preferences)
- [ ] **ManaPoolType.java** - Add pool conversion mechanics (convert between pools with penalties)
- [ ] **ManaPoolType.java** - Implement pool linking (shared pools for teams/guilds)
- [ ] **ManaPoolType.java** - Add regional pool type variations (biome-based mana types)
- [x] **ManaPoolType.java** - Implement pool thresholds for special abilities (unlock at % full)
- [x] **ManaPoolType.java** - Add pool attribute modifiers (affects spell damage, range, cooldown per pool)
- [x] **ManaPoolType.java** - Implement pool combo bonuses (using multiple pools in sequence)

### Mana Pool Implementation

- [x] **ManaPool.java** - Add temporary capacity modifiers (e.g., buffs increase max capacity)
- [ ] **ManaPool.java** - Implement conditional regeneration (only regen in specific biomes/dimensions)
- [x] **ManaPool.java** - Add drain effects (spells/mobs drain mana over time)
- [x] **ManaPool.java** - Implement overfill mechanics (temporary capacity overflow with penalty)
- [x] **ManaPool.java** - Add efficiency tracking (waste vs. actual usage ratios)
- [x] **ManaPool.java** - Implement resonance effects (repeated use from same pool increases efficiency)
- [x] **ManaPool.java** - Add critical points (specific thresholds that unlock abilities)
- [x] **ManaPool.java** - Implement pool corruption effects (stale mana becomes less useful)

### Spell Registry & Data

- [x] **SpellRegistry.java** - Implement spell validation on load (check required fields)
- [ ] **SpellRegistry.java** - Add spell compatibility checking (version, dependencies)
- [ ] **SpellRegistry.java** - Implement hot-reload for spells during development
- [ ] **SpellRegistry.java** - Add spell inheritance/templates system
- [ ] **SpellRegistry.java** - Implement spell balancing presets (easy, normal, hard)
- [x] **SpellRegistry.java** - Add spell tag system for categorization and filtering
- [ ] **SpellRegistry.java** - Implement spell compression for network transfer
- [x] **SpellRegistry.java** - Add spell dependency resolution (spells requiring other spells)
- [ ] **SpellRegistry.java** - Implement spell variant/modification system
- [ ] **SpellRegistry.java** - Add spell versioning and migration system

### Spell Core Features

- [ ] **Spell.java** - Add spell upgrade/scaling system
- [ ] **Spell.java** - Add spell combination/fusion recipes
- [ ] **Spell.java** - Add spell modification system (transmutation)
- [ ] **Spell.java** - Add spell presets/loadouts
- [ ] **Spell.java** - Add spell tutorial/guidance system
- [x] **Spell.java** - Add spell rarity/quality tiers
- [ ] **Spell.java** - Add spell unlock requirements/progression
- [x] **Spell.java** - Add conditional spell effects based on environment
- [ ] **Spell.java** - Add spell animation configuration
- [ ] **Spell.java** - Add spell sound effect customization

---

## Phase 2: Mana Management & Player Mechanics

### Player Mana Data

- [ ] **PlayerManaData.java** - Implement mana pool overflow mechanic (cap excess, redirect to other pools)
- [ ] **PlayerManaData.java** - Add mana drain effects (applied by spells/items, reduces regen temporarily)
- [ ] **PlayerManaData.java** - Implement mana shield system (absorbs damage instead of health)
- [ ] **PlayerManaData.java** - Add restoration mechanics (potions, items, passive abilities)
- [ ] **PlayerManaData.java** - Implement mana sharing/transfer between players (coop mechanics)
- [ ] **PlayerManaData.java** - Add pool burnout penalty (too much casting from one pool reduces stats)
- [ ] **PlayerManaData.java** - Implement mana burst mode (temporary high consumption, high output)
- [ ] **PlayerManaData.java** - Add synchronization events to notify clients of mana changes
- [ ] **PlayerManaData.java** - Implement mana debt system (temporarily borrow from future regen)
- [ ] **PlayerManaData.java** - Add mana efficiency modifiers per player (from equipment/buffs)

### Casting Data & State

- [ ] **PlayerCastingData.java** - Add casting state tracking (idle, channeling, casting, cooldown phases)
- [ ] **PlayerCastingData.java** - Implement interrupt mechanics (damage/movement breaks channeling)
- [ ] **PlayerCastingData.java** - Add concentration system (tracking focus level for spell accuracy/power)
- [ ] **PlayerCastingData.java** - Implement casting speed modifiers (haste/slowness effects)
- [ ] **PlayerCastingData.java** - Add backfire/critical failure mechanics for low concentration
- [ ] **PlayerCastingData.java** - Implement combo system tracking (consecutive similar spells)
- [ ] **PlayerCastingData.java** - Add rhythm-based casting (timed button presses for bonuses)
- [ ] **PlayerCastingData.java** - Implement spell memory system (memorized spells for quick access)
- [ ] **PlayerCastingData.java** - Add fatigue system (too much casting reduces effectiveness)
- [ ] **PlayerCastingData.java** - Implement synergy tracking (combining elements with other players)

### Mana Regeneration

- [ ] **ManaRegenerationHandler.java** - Add conditional regen (only in safe zones, combat, etc.)
- [ ] **ManaRegenerationHandler.java** - Implement mana drain/steal effects
- [ ] **ManaRegenerationHandler.java** - Add mana shield mechanics
- [ ] **ManaRegenerationHandler.java** - Implement depletion penalties
- [ ] **ManaRegenerationHandler.java** - Add mana burst/overdrive mechanics
- [ ] **ManaRegenerationHandler.java** - Implement restoration items/abilities
- [ ] **ManaRegenerationHandler.java** - Add mana pool sharing between players
- [ ] **ManaRegenerationHandler.java** - Implement pool visibility settings
- [ ] **ManaRegenerationHandler.java** - Add regen boost abilities/items
- [ ] **ManaRegenerationHandler.java** - Implement pooled mana transfer logic

### Cooldown System

- [ ] **SpellCooldownTracker.java** - Implement shared cooldown groups (spells on same cooldown)
- [ ] **SpellCooldownTracker.java** - Add cooldown reduction modifiers (from equipment/buffs)
- [ ] **SpellCooldownTracker.java** - Implement cooldown stacking for repeated casts (increasing penalties)
- [ ] **SpellCooldownTracker.java** - Add cooldown reset mechanics (triggered by specific events)
- [ ] **SpellCooldownTracker.java** - Implement cooldown visualization (progress bar, particle effects)
- [ ] **SpellCooldownTracker.java** - Add partial cooldown recovery (faster recovery for certain schools)
- [ ] **SpellCooldownTracker.java** - Implement cooldown immunity periods (for overpowered spells)
- [ ] **SpellCooldownTracker.java** - Add cooldown persistence across respawns (with reset option)

---

## Phase 3: Spell Casting & Advanced Mechanics

### Spell Casting Logic

- [ ] **SpellCaster.java** - Implement ritual channeling system
- [ ] **SpellCaster.java** - Add synergy multi-player mechanics
- [ ] **SpellCaster.java** - Implement spell combo detection
- [ ] **SpellCaster.java** - Add beam/ray casting mechanics
- [ ] **SpellCaster.java** - Implement teleportation spells
- [ ] **SpellCaster.java** - Add summon mechanics
- [ ] **SpellCaster.java** - Implement self-buff/debuff system
- [ ] **SpellCaster.java** - Add trap placement mechanics
- [ ] **SpellCaster.java** - Implement transformation system

### Projectiles & Entities

- [ ] **SpellProjectileEntity.java** - Implement homing projectile behavior (seeking target with radius)
- [ ] **SpellProjectileEntity.java** - Add projectile bounce/ricochet mechanics (configurable bounce count/angle)
- [ ] **SpellProjectileEntity.java** - Implement piercing projectiles that ignore entity collision up to max hits
- [ ] **SpellProjectileEntity.java** - Add trajectory prediction/curves (parabolic, sine wave, spiral patterns)
- [ ] **SpellProjectileEntity.java** - Implement chaining projectiles (jump to nearby targets on hit)
- [ ] **SpellProjectileEntity.java** - Add detonation mechanics (delayed explosion, proximity trigger, on-water)
- [ ] **SpellProjectileEntity.java** - Implement frost trail effects (slowness aura, block freezing)
- [ ] **SpellProjectileEntity.java** - Add fire trail effects (ignite blocks, spreading fire)
- [ ] **SpellProjectileEntity.java** - Implement particle trail customization per tier/school
- [ ] **SpellProjectileEntity.java** - Add sound effects on spawn, trail, impact (school-specific)

### Entity System

- [ ] **MAMEntities.java** - Add SPELL_AURA entity type (visual effect area, damage over time)
- [ ] **MAMEntities.java** - Add SUMMONED_FAMILIAR entity type (AI-controlled helper creature)
- [ ] **MAMEntities.java** - Add SPELL_TRAP entity type (triggered trap mechanism)
- [ ] **MAMEntities.java** - Add BUFF_ORB entity type (floating pickup for mana/buffs)
- [ ] **MAMEntities.java** - Add EXPLOSION_EFFECT entity type (custom explosion with school effects)
- [ ] **MAMEntities.java** - Implement entity collision settings per entity type
- [ ] **MAMEntities.java** - Add pathfinding for summoned creatures
- [ ] **MAMEntities.java** - Implement AI behavior trees for spell-summoned entities
- [ ] **MAMEntities.java** - Add entity data serialization for persistence

---

## Phase 4: Items & Equipment

### Data Components

- [ ] **MAMDataComponents.java** - Add MANA_EFFICIENCY component for staff/spellbook enchantments
- [ ] **MAMDataComponents.java** - Add SPELL_POWER component for damage/range modifiers
- [ ] **MAMDataComponents.java** - Add COOLDOWN_REDUCTION component for faster spell casting
- [ ] **MAMDataComponents.java** - Add FAVORITE_SPELLS component for multiple favorites storage
- [ ] **MAMDataComponents.java** - Add CAST_COUNT component for tracking usage stats
- [ ] **MAMDataComponents.java** - Add EXPERIENCE component for spell progression/leveling
- [ ] **MAMDataComponents.java** - Add CUSTOM_NAME component for weapon naming customization
- [ ] **MAMDataComponents.java** - Add ATTUNEMENT component for school affinity tracking
- [ ] **MAMDataComponents.java** - Add ENCHANTMENT_LEVEL component for tiered enchantments

### Staff Items

- [ ] **StaffItem.java** - Add staff enchantments system
- [ ] **StaffItem.java** - Implement staff durability mechanics
- [ ] **StaffItem.java** - Add staff leveling system
- [ ] **StaffItem.java** - Implement staff ultimate abilities
- [ ] **StaffItem.java** - Add mana infusion mechanics
- [ ] **StaffItem.java** - Implement staff customization options
- [ ] **StaffItem.java** - Add staff stat display UI
- [ ] **StaffItem.java** - Implement stat modifiers system
- [ ] **StaffItem.java** - Add special attack mechanics
- [ ] **StaffItem.java** - Implement staff leveling unlocks

### Spellbook Items

- [ ] **SpellbookItem.java** - Add spellbook enchantments
- [ ] **SpellbookItem.java** - Implement experience/leveling system
- [ ] **SpellbookItem.java** - Add quick-cast feature
- [ ] **SpellbookItem.java** - Implement spell organization system
- [ ] **SpellbookItem.java** - Add spell descriptions storage
- [ ] **SpellbookItem.java** - Implement rarity system
- [ ] **SpellbookItem.java** - Add passive abilities per tier
- [ ] **SpellbookItem.java** - Implement custom notes/annotations
- [ ] **SpellbookItem.java** - Add thematic styling per school
- [ ] **SpellbookItem.java** - Implement crafting progression

---

## Phase 5: Networking & Server Configuration

### Network Payloads

- [ ] **CastSpellPayload.java** - Add spell targeting data (target coordinates, entity UUID)
- [ ] **CastSpellPayload.java** - Implement spell variant selection (alternate effects/paths)
- [ ] **CastSpellPayload.java** - Add spell modification flags (empowered, hastened, etc.)
- [ ] **CastSpellPayload.java** - Support spell chaining (cast in sequence on same payload)
- [ ] **CastSpellPayload.java** - Add spell prediction data for server validation
- [ ] **CastSpellPayload.java** - Implement conditional spell casting (if mana > X then cast)
- [ ] **CastSpellPayload.java** - Add combo tracking (previous spells in sequence)

### Mana Sync

- [ ] **ManaSyncPayload.java** - Add mana regeneration rate sync (client-side prediction)
- [ ] **ManaSyncPayload.java** - Include active effects/buffs mana modification state
- [ ] **ManaSyncPayload.java** - Add mana pool status flags (burning, frozen, corrupted)
- [ ] **ManaSyncPayload.java** - Include burst/overdrive state information
- [ ] **ManaSyncPayload.java** - Add timestamp for latency compensation
- [ ] **ManaSyncPayload.java** - Implement delta encoding for bandwidth optimization
- [ ] **ManaSyncPayload.java** - Add status effects info (what's modifying mana currently)
- [ ] **ManaSyncPayload.java** - Include prediction delta (server estimated mana next tick)

### Server Network Handler

- [ ] **ServerNetworkHandler.java** - Implement server-side spell validation (check player permissions/tier)
- [ ] **ServerNetworkHandler.java** - Add anti-cheat verification for spell parameters
- [ ] **ServerNetworkHandler.java** - Implement lag compensation for spell targeting
- [ ] **ServerNetworkHandler.java** - Add batch packet handling for multi-spell sequences
- [ ] **ServerNetworkHandler.java** - Implement connection rate limiting for rapid spell casts
- [ ] **ServerNetworkHandler.java** - Add player state tracking (casting, channeling, cooldown)
- [ ] **ServerNetworkHandler.java** - Implement spell log recording for replay/moderation
- [ ] **ServerNetworkHandler.java** - Add cross-server spell sync for multiplayer worlds
- [ ] **ServerNetworkHandler.java** - Implement client-side prediction rollback on failure

### Server Configuration

- [ ] **ServerConfig.java** - Add spell damage scaling multipliers
- [ ] **ServerConfig.java** - Implement cooldown multipliers for difficulty modes
- [ ] **ServerConfig.java** - Add mana cost scaling per tier
- [ ] **ServerConfig.java** - Implement tier-based ability restrictions
- [ ] **ServerConfig.java** - Add PvP vs PvE damage multipliers
- [ ] **ServerConfig.java** - Implement projectile speed scaling
- [ ] **ServerConfig.java** - Add AOE radius scaling
- [ ] **ServerConfig.java** - Implement difficulty mode presets
- [ ] **ServerConfig.java** - Add spell whitelist/blacklist system
- [ ] **ServerConfig.java** - Implement regional spell restrictions
- [ ] **ServerConfig.java** - Add permission system for advanced spells
- [ ] **ServerConfig.java** - Implement balanced PvP settings
- [ ] **ServerConfig.java** - Add economic/currency multipliers

---

## Phase 6: Client UI & Rendering

### Projectile Rendering

- [ ] **SpellProjectileEntityRenderer.java** - Implement school-specific particle trails during flight
- [ ] **SpellProjectileEntityRenderer.java** - Add glow effect (enchantment glow) for higher-tier spells
- [ ] **SpellProjectileEntityRenderer.java** - Implement rotation animation based on velocity
- [ ] **SpellProjectileEntityRenderer.java** - Add size scaling per tier (larger for stronger spells)
- [ ] **SpellProjectileEntityRenderer.java** - Implement shadow rendering underneath projectile
- [ ] **SpellProjectileEntityRenderer.java** - Add impact effect rendering on hit
- [ ] **SpellProjectileEntityRenderer.java** - Implement trail particle system behind projectile
- [ ] **SpellProjectileEntityRenderer.java** - Add custom texture overlay system
- [ ] **SpellProjectileEntityRenderer.java** - Implement school-specific color tinting

### HUD Overlay

- [ ] **ManaHudOverlay.java** - Add HUD customization settings (position, size, transparency, color themes)
- [ ] **ManaHudOverlay.java** - Implement mana bar animations (drain pulse, regen glow, critical warning blink)
- [ ] **ManaHudOverlay.java** - Add spell cooldown indicators overlay on HUD
- [ ] **ManaHudOverlay.java** - Implement status effect icons display on mana bars
- [ ] **ManaHudOverlay.java** - Add mana regeneration rate display (per tick/second)
- [ ] **ManaHudOverlay.java** - Create mana threshold warnings (critical low, etc.) with visual/audio cues
- [ ] **ManaHudOverlay.java** - Add alternative HUD layout modes (vertical/horizontal/compact/detailed)
- [ ] **ManaHudOverlay.java** - Implement screen shake effect when mana depleted or critical
- [ ] **ManaHudOverlay.java** - Add floating combat text for mana drain/regen events
- [ ] **ManaHudOverlay.java** - Implement HUD scaling based on mana pool tier (visual emphasis)

### Spell Selection Screen

- [ ] **SpellSelectionScreen.java** - Add search bar functionality
- [ ] **SpellSelectionScreen.java** - Implement drag-and-drop spell ordering
- [ ] **SpellSelectionScreen.java** - Add spell comparison mode
- [ ] **SpellSelectionScreen.java** - Implement requirement validation display
- [ ] **SpellSelectionScreen.java** - Add preview animations for spells
- [ ] **SpellSelectionScreen.java** - Implement spell deck management
- [ ] **SpellSelectionScreen.java** - Add synergy recommendations
- [ ] **SpellSelectionScreen.java** - Show economy/cost display
- [ ] **SpellSelectionScreen.java** - Add learning/unlock indicators
- [ ] **SpellSelectionScreen.java** - Implement spell favorites organization
- [ ] **SpellSelectionScreen.java** - Add filtering by school and type

### Item Tooltips

- [ ] **ItemTooltips.java** - Add damage/range/cooldown tooltip details
- [ ] **ItemTooltips.java** - Implement comparison mode (showing differences from held item)
- [ ] **ItemTooltips.java** - Add enchantment information display
- [ ] **ItemTooltips.java** - Implement stat breakdown (base + modifiers)
- [ ] **ItemTooltips.java** - Add requirement indicators (level, stats, etc.)
- [ ] **ItemTooltips.java** - Implement animated tooltip content (for dynamic values)
- [ ] **ItemTooltips.java** - Add rarity coloring and border effects
- [ ] **ItemTooltips.java** - Implement tooltip sound effects
- [ ] **ItemTooltips.java** - Add custom formatting per school

### Spell Screen Helper

- [ ] **SpellScreenHelper.java** - Add screen transition animations
- [ ] **SpellScreenHelper.java** - Implement spell history tracking (recently cast spells)
- [ ] **SpellScreenHelper.java** - Add quick-access spell shortcuts
- [ ] **SpellScreenHelper.java** - Implement nested screen support (backup actions)
- [ ] **SpellScreenHelper.java** - Add loading state indication
- [ ] **SpellScreenHelper.java** - Implement error handling for network delays
- [ ] **SpellScreenHelper.java** - Add screen memory (restore last state when reopened)
- [ ] **SpellScreenHelper.java** - Implement accessibility options (large text, high contrast)

### Client Network Handler

- [ ] **ClientNetworkHandler.java** - Add disconnect handling (clear cached data on disconnect)
- [ ] **ClientNetworkHandler.java** - Implement packet validation (checksums, version checks)
- [ ] **ClientNetworkHandler.java** - Add packet re-ordering for out-of-order delivery
- [ ] **ClientNetworkHandler.java** - Implement bandwidth optimization (compression, delta encoding)
- [ ] **ClientNetworkHandler.java** - Add packet history for debugging/logs
- [ ] **ClientNetworkHandler.java** - Implement custom packet handlers registration system
- [ ] **ClientNetworkHandler.java** - Add telemetry for packet latency tracking
- [ ] **ClientNetworkHandler.java** - Implement graceful degradation for missing spell data

### Client Mana Data

- [ ] **ClientManaData.java** - Implement client-side mana prediction (estimate next tick value)
- [ ] **ClientManaData.java** - Add mana change history tracking for smooth animations
- [ ] **ClientManaData.java** - Implement mana spike detection (large changes warrant warnings)
- [ ] **ClientManaData.java** - Add status effects tracking on client
- [ ] **ClientManaData.java** - Implement visual feedback queuing system
- [ ] **ClientManaData.java** - Add mana pool priority changing via UI
- [ ] **ClientManaData.java** - Implement threshold callbacks (low mana warnings)
- [ ] **ClientManaData.java** - Add statistics tracking (total cast, total damage, etc.)

### Client Initialization

- [ ] **MAMClient.java** - Register block entity renderers for magic-infused blocks
- [ ] **MAMClient.java** - Implement particle system initialization
- [ ] **MAMClient.java** - Register custom screen handlers
- [ ] **MAMClient.java** - Add input method customization (gamepad support)
- [ ] **MAMClient.java** - Implement shader registration for spell effects
- [ ] **MAMClient.java** - Register sound event pre-loading
- [ ] **MAMClient.java** - Add debug screen overlay registration
- [ ] **MAMClient.java** - Implement video settings for spell visuals
- [ ] **MAMClient.java** - Register texture atlases for animated textures

---

## Phase 7: Keybindings & Input

### Magic Keybindings

- [ ] **MagicKeyBindings.java** - Add in-game keybind customization UI
- [ ] **MagicKeyBindings.java** - Implement quick-cast slots (keys 1-9)
- [ ] **MagicKeyBindings.java** - Add stance switching keybinds
- [ ] **MagicKeyBindings.java** - Implement hotbar modifications
- [ ] **MagicKeyBindings.java** - Add conflict detection and resolution
- [ ] **MagicKeyBindings.java** - Implement mouse button support
- [ ] **MagicKeyBindings.java** - Add gamepad/controller support
- [ ] **MagicKeyBindings.java** - Implement keybind profiles/presets
- [ ] **MagicKeyBindings.java** - Add accessibility keybind alternatives

### Staff Casting Handler

- [ ] **StaffCastingHandler.java** - Add charge-up mechanics (hold to build power)
- [ ] **StaffCastingHandler.java** - Implement spell aimed mode (crosshair targeting)
- [ ] **StaffCastingHandler.java** - Add continuous casting support (hold for duration)
- [ ] **StaffCastingHandler.java** - Implement combo detection (rapid spell succession)
- [ ] **StaffCastingHandler.java** - Add casting cancellation (ESC key)
- [ ] **StaffCastingHandler.java** - Implement staff attachment animations
- [ ] **StaffCastingHandler.java** - Add client-side prediction for instant feedback
- [ ] **StaffCastingHandler.java** - Implement spell prep animations/effects

---

## Phase 8: Client Configuration & Customization

### Client Configuration

- [ ] **ClientConfig.java** - Add HUD layout presets (compact, minimal, detailed, classic)
- [ ] **ClientConfig.java** - Implement color theme customization (light/dark/custom)
- [ ] **ClientConfig.java** - Add particle intensity slider (low/normal/high/ultra)
- [ ] **ClientConfig.java** - Implement sound volume per spell school
- [ ] **ClientConfig.java** - Add tooltip verbosity settings
- [ ] **ClientConfig.java** - Implement screen shake intensity slider
- [ ] **ClientConfig.java** - Add accessibility options (colorblind modes, text sizes)
- [ ] **ClientConfig.java** - Implement keybind customization UI
- [ ] **ClientConfig.java** - Add performance/quality settings (render distance, shadows)
- [ ] **ClientConfig.java** - Implement language selection override

### Spell Variants & Selection Payload

- [ ] **SelectSpellPayload.java** - Add spell variant/path selection (alternate effects)
- [ ] **SelectSpellPayload.java** - Include spell power level (empowered, normal, weak)
- [ ] **SelectSpellPayload.java** - Add selected targets (entity UUIDs or coordinates)
- [ ] **SelectSpellPayload.java** - Include combo information (previous spells for chain attacks)
- [ ] **SelectSpellPayload.java** - Add casting mode (quick-cast, charged, etc.)
- [ ] **SelectSpellPayload.java** - Implement spell slot selection (quick-bar slot)
- [ ] **SelectSpellPayload.java** - Add customization flags (spell modifiers)

### Spellbook Opening Payload

- [ ] **OpenSpellBookPayload.java** - Add available spells list (avoid resending data unnecessarily)
- [ ] **OpenSpellBookPayload.java** - Include player's current mana state in payload
- [ ] **OpenSpellBookPayload.java** - Add cooldown information for active spells
- [ ] **OpenSpellBookPayload.java** - Include GUI customization flags (layout, theme)
- [ ] **OpenSpellBookPayload.java** - Add contextual info (PvP mode, area effects, etc.)
- [ ] **OpenSpellBookPayload.java** - Include spell school affinity information
- [ ] **OpenSpellBookPayload.java** - Add recommendations for current situation
- [ ] **OpenSpellBookPayload.java** - Implement quick-close timeout

---

## Phase 9: Main Mod Registry & Content

### Core Mod Initialization

- [ ] **MAM.java** - Implement crafting recipes for spellbooks/staffs
- [ ] **MAM.java** - Add enchantment recipes system
- [ ] **MAM.java** - Implement gemstone binding mechanics
- [ ] **MAM.java** - Add spell fusion crafting recipes
- [ ] **MAM.java** - Implement transmutation system
- [ ] **MAM.java** - Add block-based magic workbench
- [ ] **MAM.java** - Implement mob-based spell dropping
- [ ] **MAM.java** - Add dimension-specific spells
- [ ] **MAM.java** - Implement PvP arena system
- [ ] **MAM.java** - Add tournament/competitive system
- [ ] **MAM.java** - Implement quest system for spell learning
- [ ] **MAM.java** - Add achievement/milestone system
- [ ] **MAM.java** - Implement leaderboard system
- [ ] **MAM.java** - Add mastery/certification system
- [ ] **MAM.java** - Implement cosmetic customization options

---

## Implementation Priority Legend

### Critical Path (Must Complete First)

- Phase 1: Foundation systems establish the core mechanics
- Phase 2: Mana management enables basic gameplay
- Phase 3: Casting mechanics create player interactions
- Phase 4: Items provide player progression

### High Priority (Enables Content)

- Phase 5: Networking ensures multiplayer functionality
- Phase 6: Client UI creates player experience
- Phase 7: Keybindings allow player control

### Medium Priority (Quality of Life)

- Phase 8: Configuration customization
- Phase 9: Mod content and progression

### Lower Priority (Polish & Expansion)

- Advanced mechanics requiring all systems
- Cosmetics and convenience features
- Balancing and optimization

---

## Dependency Notes

- All client features depend on Phase 1-3 foundation
- Networking (Phase 5) depends on items and casting (Phase 3-4)
- UI (Phase 6) depends on network sync and client data
- Configuration (Phase 8) depends on all core systems
- Mod content (Phase 9) depends on items, crafting, and configuration

---

## Legend

- [ ] = Not started
- [x] = Completed
- Comment = Implementation notes

Last Updated: 2026-01-06
Total TODOs: 150+
