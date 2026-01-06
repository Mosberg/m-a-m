package dk.mosberg.spell;

import dk.mosberg.MAM;
import dk.mosberg.mana.ManaAttachments;
import dk.mosberg.mana.PlayerManaData;
import dk.mosberg.network.ServerNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Handles spell casting logic on the server.
 */
public class SpellCaster {

    public static void castSpell(ServerPlayerEntity player, Spell spell) {
        PlayerManaData manaData = player.getAttachedOrCreate(ManaAttachments.PLAYER_MANA);

        // Check mana cost
        if (!manaData.consumeMana(spell.getManaCost())) {
            player.sendMessage(Text.translatable("mana.mam.insufficient"), true);
            return;
        }

        // Sync mana to client after consumption
        ServerNetworkHandler.syncManaToClient(player);

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
            try {
                Identifier soundId = Identifier.tryParse(spell.getSound());
                if (soundId != null) {
                    player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_EVOKER_CAST_SPELL, // Fallback sound
                            SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            } catch (Exception e) {
                MAM.LOGGER.warn("Invalid sound ID: {}", spell.getSound());
            }
        }

        MAM.LOGGER.debug("Player {} cast spell {}", player.getName().getString(), spell.getId());
    }

    private static void castProjectile(ServerPlayerEntity player, Spell spell) {
        // TODO: Create custom projectile entity
        // For now, apply damage to entities in front of player
        Vec3d lookVec = player.getRotationVec(1.0f);
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(lookVec.multiply(spell.getRange()));

        // Simple raycast for now
        List<Entity> entities = player.getWorld().getOtherEntities(player,
                new Box(start, end).expand(2.0), entity -> entity instanceof LivingEntity);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                living.damage(player.getDamageSources().magic(), spell.getDamage());

                // Apply status effects
                for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
                    effectEntry.getStatusEffect().ifPresent(effect -> {
                        living.addStatusEffect(new StatusEffectInstance(effect,
                                effectEntry.duration(), effectEntry.amplifier()));
                    });
                }

                // Apply knockback
                if (spell.getKnockback() > 0) {
                    Vec3d knockbackVec = lookVec.normalize().multiply(spell.getKnockback());
                    living.setVelocity(living.getVelocity().add(knockbackVec));
                    living.velocityModified = true;
                }

                break; // Hit first entity
            }
        }
    }

    private static void castAoE(ServerPlayerEntity player, Spell spell) {
        Vec3d center = player.getPos();
        double radius = spell.getAoeRadius();

        List<Entity> entities = player.getWorld().getOtherEntities(player,
                new Box(center, center).expand(radius), entity -> entity instanceof LivingEntity
                        && entity.squaredDistanceTo(center) <= radius * radius);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity living) {
                living.damage(player.getDamageSources().magic(), spell.getDamage());

                // Apply status effects
                for (Spell.StatusEffectEntry effectEntry : spell.getStatusEffects()) {
                    effectEntry.getStatusEffect().ifPresent(effect -> {
                        living.addStatusEffect(new StatusEffectInstance(effect,
                                effectEntry.duration(), effectEntry.amplifier()));
                    });
                }
            }
        }
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
        // For now, treat as instant utility
        castUtility(player, spell);
    }

    private static void castSynergy(ServerPlayerEntity player, Spell spell) {
        // TODO: Implement synergy spells combining multiple schools
        // For now, treat as AoE
        castAoE(player, spell);
    }
}
