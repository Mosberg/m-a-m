package dk.mosberg.spell;

import java.util.List;
import java.util.Objects;
import dk.mosberg.MAM;
import dk.mosberg.entity.SpellProjectileEntity;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.PlayerCastingData;
import dk.mosberg.mana.PlayerManaData;
import dk.mosberg.network.ServerNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Handles spell casting logic on the server with advanced mechanics: - Ritual channeling system -
 * Multi-player synergy - Spell combo detection - Beam/ray casting - Teleportation - Summon
 * mechanics - Self-buff/debuff system - Trap placement - Transformation system
 */
public class SpellCaster {

    public static void castSpell(ServerPlayerEntity player, Spell spell) {
        @SuppressWarnings("null")
        PlayerCastingData castingData = Objects.requireNonNull(
                player.getAttachedOrCreate(ManaAttachments.PLAYER_CASTING, PlayerCastingData::new),
                "Player casting data attachment should always exist");

        // Check if already casting
        if (castingData.getCastingState().isActivelyCasting()) {
            player.sendMessage(Text.literal("Already casting a spell"), true);
            return;
        }

        // Check cooldown FIRST before consuming mana
        if (castingData.getCooldownTracker().isOnCooldown(spell.getId())) {
            float remaining = castingData.getCooldownTracker().getRemainingCooldown(spell.getId());
            player.sendMessage(
                    Text.literal(String.format("Spell on cooldown: %.1fs remaining", remaining)),
                    true);
            return;
        }

        if (!hasRequiredSpellbook(player, spell)) {
            return;
        }

        // Check mana cost
        PlayerManaData manaData = castingData.getManaData();
        float manaCost = spell.getManaCost();

        // Apply combo multiplier to reduce mana cost
        float comboDiscount = castingData.getComboCount() > 0 ? 0.9f : 1.0f;
        manaCost *= comboDiscount;

        if (!manaData.consumeMana(manaCost)) {
            player.sendMessage(Text.translatable("mana.mam.insufficient"), true);
            return;
        }

        // Check for backfire
        if (castingData.shouldBackfire()) {
            player.sendMessage(Text.literal("§cSpell backfired!"), true);
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            player.damage(world, world.getDamageSources().magic(), 2.0f);
            castingData.setConcentration(0);
            return;
        }

        // Sync mana to client after consumption
        ServerNetworkHandler.syncManaToClient(player);

        // Start cooldown for this spell
        castingData.getCooldownTracker().startCooldown(spell.getId(), spell.getCooldown(), null);

        // Start casting state
        if (!castingData.startCasting(spell.getId())) {
            player.sendMessage(Text.literal("Cannot start casting"), true);
            return;
        }

        // Cast spell based on type
        switch (spell.getCastType()) {
            case PROJECTILE -> castProjectile(player, spell, castingData);
            case AOE -> castAoE(player, spell, castingData);
            case UTILITY -> castUtility(player, spell, castingData);
            case RITUAL -> castRitual(player, spell, castingData);
            case SYNERGY -> castSynergy(player, spell, castingData);
            case SELF_CAST -> castSelfBuff(player, spell, castingData);
            case SUMMON -> castSummon(player, spell, castingData);
            case TRANSFORM -> castTransform(player, spell, castingData);
            case TRAP -> castTrap(player, spell, castingData);
            case BEAM -> castBeam(player, spell, castingData);
        }

        // Record synergy with nearby players
        recordSynergyWithNearbyPlayers(player, spell, castingData);

        // Finish casting
        castingData.finishCasting();

        // Play sound effect
        if (!spell.getSound().isEmpty()) {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            MAM.LOGGER.debug("Spell {} has sound effect: {}", spell.getId(), spell.getSound());
        }

        MAM.LOGGER.debug("Player {} cast spell {}", player.getName().getString(), spell.getId());
    }

    private static void castProjectile(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Apply concentration and combo multipliers
        float powerMultiplier =
                castingData.getConcentrationPowerMultiplier() * castingData.getComboMultiplier();

        // Create and spawn projectile entity
        SpellProjectileEntity projectile = new SpellProjectileEntity(world, player, spell);
        // TODO: Apply power multiplier to projectile when damage scaling is added
        world.spawnEntity(projectile);

        MAM.LOGGER.debug("Casting projectile spell: {} with {}x power", spell.getId(),
                powerMultiplier);
    }

    private static void castAoE(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Apply multipliers
        float powerMultiplier = castingData.getConcentrationPowerMultiplier()
                * castingData.getComboMultiplier() * castingData.getEffectivenessMultiplier();

        float damage = spell.getDamage() * powerMultiplier;

        // Get entities in AoE radius
        double radius = spell.getAoeRadius();
        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity != player && entity.squaredDistanceTo(player) <= radius * radius)
                .forEach(entity -> {
                    // Apply damage
                    entity.damage(world, player.getDamageSources().playerAttack(player), damage);

                    // Apply knockback
                    if (spell.getKnockback() > 0) {
                        Vec3d knockbackVec = new Vec3d(entity.getX(), entity.getY(), entity.getZ())
                                .subtract(new Vec3d(player.getX(), player.getY(), player.getZ()))
                                .normalize().multiply(spell.getKnockback());
                        entity.addVelocity(knockbackVec.x, 0.2, knockbackVec.z);
                        entity.velocityDirty = true;
                    }
                });

        // Spawn particles
        for (int i = 0; i < 20; i++) {
            double angle = (2 * Math.PI * i) / 20;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            world.spawnParticles(ParticleTypes.FLAME, x, player.getY() + 0.5, z, 1, 0, 0, 0, 0);
        }

        MAM.LOGGER.debug("Casting AoE spell: {} with radius {} and damage {}", spell.getId(),
                radius, damage);
    }

    private static void castUtility(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        // Check if this is a teleportation spell
        if (spell.getId().getPath().contains("teleport")
                || spell.getId().getPath().contains("blink")) {
            castTeleportation(player, spell, castingData);
            return;
        }

        // Apply status effects to caster
        for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
            effectEntry.getStatusEffect().ifPresent(effect -> {
                player.addStatusEffect(new StatusEffectInstance(effect, effectEntry.duration(),
                        effectEntry.amplifier()));
            });
        }

        MAM.LOGGER.debug("Casting utility spell: {}", spell.getId());
    }

    private static void castTeleportation(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Get teleport range from spell range
        float range = spell.getRange();

        // Raycast to find target position
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVector();
        Vec3d end = start.add(direction.multiply(range));

        BlockHitResult hitResult = world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));

        BlockPos targetPos = hitResult.getBlockPos();
        Vec3d teleportPos =
                new Vec3d(targetPos.getX() + 0.5, targetPos.getY() + 1, targetPos.getZ() + 0.5);

        // Safety checks
        if (!isSafeTeleportLocation(world, teleportPos)) {
            player.sendMessage(Text.literal("§cCannot teleport to unsafe location!"), true);
            // Refund mana since teleport failed (restore to active priority pool)
            PlayerManaData manaData = castingData.getManaData();
            manaData.restoreMana(manaData.getActivePriority(), spell.getManaCost());
            ServerNetworkHandler.syncManaToClient(player);
            return;
        }

        // Spawn departure particles
        world.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(),
                30, 0.3, 0.5, 0.3, 0.5);

        // Play departure sound
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Teleport player (simple version)
        player.teleport(teleportPos.x, teleportPos.y, teleportPos.z, false);

        // Spawn arrival particles
        world.spawnParticles(ParticleTypes.PORTAL, teleportPos.x, teleportPos.y + 1, teleportPos.z,
                30, 0.3, 0.5, 0.3, 0.5);

        // Play arrival sound
        world.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.2f);

        MAM.LOGGER.debug("Player {} teleported to {}", player.getName().getString(), teleportPos);
    }

    /**
     * Check if a teleport location is safe (not inside blocks, not in lava/void, has floor)
     */
    private static boolean isSafeTeleportLocation(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        BlockPos below = blockPos.down();
        BlockPos above = blockPos.up();

        // Check if standing position is air or passable
        if (!world.getBlockState(blockPos).isAir()
                && !world.getBlockState(blockPos).isReplaceable()) {
            return false;
        }

        // Check head position is clear
        if (!world.getBlockState(above).isAir() && !world.getBlockState(above).isReplaceable()) {
            return false;
        }

        // Check has floor below (not void)
        if (below.getY() < world.getBottomY()) {
            return false;
        }

        // Check floor is solid or has safe landing
        if (world.getBlockState(below).isAir() && world.getBlockState(below.down()).isAir()) {
            // Would fall - check if fall distance is safe (within 10 blocks)
            int fallDist = 0;
            BlockPos checkPos = below;
            while (fallDist < 10 && checkPos.getY() > world.getBottomY()) {
                if (!world.getBlockState(checkPos).isAir()) {
                    break;
                }
                checkPos = checkPos.down();
                fallDist++;
            }

            if (fallDist >= 10 || checkPos.getY() <= world.getBottomY()) {
                return false; // Too far to fall or void below
            }
        }

        // Check not in lava
        if (!world.getFluidState(blockPos).isEmpty()) {
            return false; // Don't teleport into fluids
        }

        return true;
    }

    private static void castRitual(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Ritual spells require channeling - enter channeling state
        castingData.beginExecution();

        // Create ritual circle particles
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double radius = 3.0;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            world.spawnParticles(ParticleTypes.ENCHANT, x, player.getY(), z, 1, 0, 0, 0, 0);
        }

        // Apply powerful AoE effect after channeling
        double radius = spell.getAoeRadius() * 1.5; // Rituals have 50% larger radius
        float damage = spell.getDamage() * 2.0f * castingData.getConcentrationPowerMultiplier();

        world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius),
                entity -> entity != player && entity.squaredDistanceTo(player) <= radius * radius)
                .forEach(entity -> {
                    entity.damage(world, player.getDamageSources().playerAttack(player), damage);

                    // Apply status effects
                    for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
                        effectEntry.getStatusEffect().ifPresent(effect -> {
                            entity.addStatusEffect(new StatusEffectInstance(effect,
                                    effectEntry.duration() * 2, effectEntry.amplifier()));
                        });
                    }
                });

        MAM.LOGGER.debug("Casting ritual spell: {} with channeled power", spell.getId());
    }

    private static void castSynergy(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Check for nearby players who recently cast spells
        List<ServerPlayerEntity> nearbyPlayers =
                world.getPlayers(p -> p != player && p.squaredDistanceTo(player) < 100 // Within 10
                                                                                       // blocks
                );

        float synergyBonus = 1.0f;
        for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
            PlayerCastingData nearbyData = nearbyPlayer
                    .getAttachedOrCreate(ManaAttachments.PLAYER_CASTING, PlayerCastingData::new);

            float partnerBonus = castingData.getSynergyBonus(nearbyPlayer.getUuid());
            synergyBonus += partnerBonus;
        }

        // Cast as powerful AoE with synergy multiplier
        float damage =
                spell.getDamage() * synergyBonus * castingData.getConcentrationPowerMultiplier();
        double radius = spell.getAoeRadius() * (1.0 + synergyBonus * 0.2); // Radius grows with
                                                                           // synergy

        world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(radius),
                entity -> entity != player && entity.squaredDistanceTo(player) <= radius * radius)
                .forEach(entity -> {
                    entity.damage(world, player.getDamageSources().playerAttack(player), damage);
                });

        // Visual feedback for synergy
        world.spawnParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 2, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);

        MAM.LOGGER.debug("Casting synergy spell: {} with {}x synergy bonus", spell.getId(),
                synergyBonus);
    }

    private static void castSelfBuff(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        // Apply all status effects with enhanced duration
        int durationMultiplier = 1 + castingData.getComboCount();

        for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
            effectEntry.getStatusEffect().ifPresent(effect -> {
                int duration = effectEntry.duration() * durationMultiplier;
                player.addStatusEffect(
                        new StatusEffectInstance(effect, duration, effectEntry.amplifier()));
            });
        }

        // Visual particles
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1,
                player.getZ(), 15, 0.5, 0.5, 0.5, 0);

        MAM.LOGGER.debug("Casting self-buff spell: {} with {}x duration", spell.getId(),
                durationMultiplier);
    }

    private static void castSummon(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // TODO: Create actual summoned entities when entity types are implemented
        // For now, spawn particles to indicate summon location
        Vec3d summonPos = player.getEyePos().add(player.getRotationVector().multiply(2.0));

        world.spawnParticles(ParticleTypes.PORTAL, summonPos.x, summonPos.y, summonPos.z, 50, 0.5,
                1.0, 0.5, 0.1);

        player.sendMessage(Text.literal("§5Summoned entity at position"), true);

        MAM.LOGGER.debug("Casting summon spell: {} (entities not yet implemented)", spell.getId());
    }

    private static void castTransform(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        // Apply transformation status effects
        for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
            effectEntry.getStatusEffect().ifPresent(effect -> {
                // Transformation effects last longer
                int duration = effectEntry.duration() * 3;
                player.addStatusEffect(
                        new StatusEffectInstance(effect, duration, effectEntry.amplifier() + 1)); // Enhanced
                                                                                                  // amplifier
            });
        }

        // Visual transformation effect
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.spawnParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1, player.getZ(),
                30, 0.3, 1.0, 0.3, 0.05);

        player.sendMessage(Text.literal("§6You feel your form changing..."), true);

        MAM.LOGGER.debug("Casting transform spell: {}", spell.getId());
    }

    private static void castTrap(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Place trap at target location (raycast to find ground)
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d end = start.add(player.getRotationVector().multiply(spell.getRange()));

        BlockHitResult hitResult = world.raycast(new RaycastContext(start, end,
                RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));

        BlockPos trapPos = hitResult.getBlockPos().up();

        // TODO: Spawn actual trap entity when entity types are implemented
        // For now, mark location with particles
        world.spawnParticles(ParticleTypes.WITCH, trapPos.getX() + 0.5, trapPos.getY(),
                trapPos.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0);

        player.sendMessage(Text.literal("§cTrap placed at location"), true);

        MAM.LOGGER.debug("Casting trap spell: {} at {}", spell.getId(), trapPos);
    }

    private static void castBeam(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Raycast to find beam targets
        Vec3d start = player.getCameraPosVec(1.0f);
        Vec3d direction = player.getRotationVector();
        double range = spell.getRange();

        // Check for entities in beam path
        Vec3d end = start.add(direction.multiply(range));
        Box beamBox = new Box(start, end).expand(0.5);

        float damage = spell.getDamage() * castingData.getConcentrationPowerMultiplier();

        List<Entity> hitEntities =
                world.getOtherEntities(player, beamBox, entity -> entity instanceof LivingEntity);

        for (Entity entity : hitEntities) {
            if (entity instanceof LivingEntity living) {
                living.damage(world, player.getDamageSources().playerAttack(player), damage);

                // Knockback along beam direction
                living.addVelocity(direction.x * 0.5, 0.2, direction.z * 0.5);
                living.velocityDirty = true;
            }
        }

        // Spawn beam particles
        for (double d = 0; d < range; d += 0.5) {
            Vec3d particlePos = start.add(direction.multiply(d));
            world.spawnParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z,
                    1, 0.05, 0.05, 0.05, 0);
        }

        MAM.LOGGER.debug("Casting beam spell: {} hitting {} entities", spell.getId(),
                hitEntities.size());
    }

    /**
     * Records synergy with nearby players for combo bonuses.
     */
    private static void recordSynergyWithNearbyPlayers(ServerPlayerEntity player, Spell spell,
            PlayerCastingData castingData) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        List<ServerPlayerEntity> nearbyPlayers =
                world.getPlayers(p -> p != player && p.squaredDistanceTo(player) < 100);

        for (ServerPlayerEntity nearbyPlayer : nearbyPlayers) {
            castingData.recordSynergy(nearbyPlayer.getUuid(), spell.getSchool());

            // Also record on their end
            PlayerCastingData nearbyData = nearbyPlayer
                    .getAttachedOrCreate(ManaAttachments.PLAYER_CASTING, PlayerCastingData::new);
            nearbyData.recordSynergy(player.getUuid(), spell.getSchool());
        }
    }

    private static boolean hasRequiredSpellbook(ServerPlayerEntity player, Spell spell) {
        var main = player.getMainHandStack();
        var off = player.getOffHandStack();

        int tier = -1;
        if (main.getItem() instanceof dk.mosberg.item.SpellbookItem mainBook) {
            tier = mainBook.getTier();
        } else if (off.getItem() instanceof dk.mosberg.item.SpellbookItem offBook) {
            tier = offBook.getTier();
        }

        if (tier < 0) {
            player.sendMessage(Text.translatable("message.mam.no_spellbook"), true);
            return false;
        }

        if (spell.getTier() > tier) {
            player.sendMessage(
                    Text.translatable("message.mam.spell.too_high_tier", spell.getTier()), true);
            return false;
        }

        return true;
    }
}
