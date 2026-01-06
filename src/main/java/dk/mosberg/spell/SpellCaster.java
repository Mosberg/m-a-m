package dk.mosberg.spell;

import java.util.Objects;
import dk.mosberg.MAM;
import dk.mosberg.entity.SpellProjectileEntity;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.PlayerCastingData;
import dk.mosberg.mana.PlayerManaData;
import dk.mosberg.network.ServerNetworkHandler;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/**
 * Handles spell casting logic on the server, including mana consumption and cooldown tracking.
 */
public class SpellCaster {

    public static void castSpell(ServerPlayerEntity player, Spell spell) {
        @SuppressWarnings("null")
        PlayerCastingData castingData = Objects.requireNonNull(
                player.getAttachedOrCreate(ManaAttachments.PLAYER_CASTING, PlayerCastingData::new),
                "Player casting data attachment should always exist");

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
        if (!manaData.consumeMana(spell.getManaCost())) {
            player.sendMessage(Text.translatable("mana.mam.insufficient"), true);
            return;
        }

        // Sync mana to client after consumption
        ServerNetworkHandler.syncManaToClient(player);

        // Start cooldown for this spell
        castingData.getCooldownTracker().startCooldown(spell.getId(), spell.getCooldown());

        // Cast spell based on type
        switch (spell.getCastType()) {
            case PROJECTILE -> castProjectile(player, spell);
            case AOE -> castAoE(player, spell);
            case UTILITY -> castUtility(player, spell);
            case RITUAL -> castRitual(player, spell);
            case SYNERGY -> castSynergy(player, spell);
        }

        // Play sound effect
        if (!spell.getSound().isEmpty()) {
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            MAM.LOGGER.debug("Spell {} has sound effect: {}", spell.getId(), spell.getSound());
        }

        MAM.LOGGER.debug("Player {} cast spell {}", player.getName().getString(), spell.getId());
    }

    private static void castProjectile(ServerPlayerEntity player, Spell spell) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Create and spawn projectile entity
        SpellProjectileEntity projectile = new SpellProjectileEntity(world, player, spell);
        world.spawnEntity(projectile);

        MAM.LOGGER.debug("Casting projectile spell: {}", spell.getId());
    }

    private static void castAoE(ServerPlayerEntity player, Spell spell) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Get entities in AoE radius
        double radius = spell.getAoeRadius();
        world.getEntitiesByClass(net.minecraft.entity.LivingEntity.class,
                player.getBoundingBox().expand(radius),
                entity -> entity != player && entity.squaredDistanceTo(player) <= radius * radius)
                .forEach(entity -> {
                    // Apply damage
                    entity.damage(world, player.getDamageSources().playerAttack(player),
                            spell.getDamage());

                    // Apply knockback
                    if (spell.getKnockback() > 0) {
                        net.minecraft.util.math.Vec3d knockbackVec =
                                new net.minecraft.util.math.Vec3d(entity.getX(), entity.getY(),
                                        entity.getZ()).subtract(
                                                new net.minecraft.util.math.Vec3d(player.getX(),
                                                        player.getY(), player.getZ()))
                                                .normalize().multiply(spell.getKnockback());
                        entity.addVelocity(knockbackVec.x, 0.2, knockbackVec.z);
                        entity.setVelocityClient(
                                entity.getVelocity().add(knockbackVec.x, 0.2, knockbackVec.z));
                    }
                });

        MAM.LOGGER.debug("Casting AoE spell: {} with radius {}", spell.getId(), radius);
    }

    private static void castUtility(ServerPlayerEntity player, Spell spell) {
        // Apply status effects to caster
        for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
            effectEntry.getStatusEffect().ifPresent(effect -> {
                player.addStatusEffect(new StatusEffectInstance(effect, effectEntry.duration(),
                        effectEntry.amplifier()));
            });
        }
    }

    private static void castRitual(ServerPlayerEntity player, Spell spell) {
        // TODO: Implement ritual casting with channeling
        // TODO: Add channeling UI feedback and interruption mechanics
        // TODO: Add ritual duration configuration
        // TODO: Add ritual area protection/blessing mechanics
        // For now, treat as instant utility
        castUtility(player, spell);
    }

    private static void castSynergy(ServerPlayerEntity player, Spell spell) {
        // TODO: Implement synergy spells combining multiple schools
        // TODO: Add multi-player synergy mechanics (combining casts)
        // TODO: Add spell combination/fusion system
        // TODO: Add synergy bonus scaling configuration
        // For now, treat as AoE
        castAoE(player, spell);
    }

    // TODO: Add spell interruption/cancellation mechanics
    // TODO: Add spell reflection/parry system
    // TODO: Add spell immunity/resistance mechanics
    // TODO: Add persistent spell effects (auras, buffs, debuffs)
    // TODO: Add spell combo system (chaining spells together)
    // TODO: Add spell projectile tracking and bouncing
    // TODO: Add spell beam/ray casting mechanics
    // TODO: Add spell teleportation mechanics
    // TODO: Add spell summoning mechanics (minions, totems)

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
