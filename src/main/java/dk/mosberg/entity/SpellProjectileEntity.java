package dk.mosberg.entity;

import dk.mosberg.MAM;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Custom projectile entity for spell casting.
 */
public class SpellProjectileEntity extends ProjectileEntity {
    private SpellSchool school = SpellSchool.FIRE;
    private float damage = 2.0f;
    private float knockback = 0.0f;
    private int maxAge = 200; // 10 seconds
    private int age = 0;

    public SpellProjectileEntity(EntityType<? extends SpellProjectileEntity> entityType,
            World world) {
        super(entityType, world);
    }

    public SpellProjectileEntity(World world, LivingEntity owner, Spell spell) {
        super(MAMEntities.SPELL_PROJECTILE, world);
        this.setOwner(owner);
        this.setPosition(owner.getEyePos());
        this.school = spell.getSchool();
        this.damage = spell.getDamage();
        this.knockback = spell.getKnockback();

        // Set velocity based on player's look direction and spell speed
        Vec3d velocity = owner.getRotationVec(1.0F).multiply(spell.getProjectileSpeed());
        this.setVelocity(velocity);
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        // No additional data tracking needed for now
    }

    @Override
    public void tick() {
        super.tick();

        // Check age and remove if too old
        if (++age > maxAge) {
            this.discard();
            return;
        }

        // Check for collisions
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);

        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onCollision(hitResult);
        }

        // Update position
        Vec3d velocity = this.getVelocity();
        this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y,
                this.getZ() + velocity.z);

        // Apply gravity (slight downward force)
        this.setVelocity(velocity.x, velocity.y - 0.01, velocity.z);

        // Spawn particles on client
        if (this.getEntityWorld().isClient()) {
            spawnParticles();
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);

        Entity target = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        // Don't hit the owner
        if (target == owner) {
            return;
        }

        // Apply damage
        if (target instanceof LivingEntity livingTarget && this
                .getEntityWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            DamageSource damageSource = this.getDamageSources().create(
                    RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MAM.MOD_ID, "spell")),
                    this, owner);
            livingTarget.damage(serverWorld, damageSource, this.damage);

            // Apply knockback
            if (this.knockback > 0) {
                Vec3d knockbackVec = this.getVelocity().normalize().multiply(this.knockback);
                livingTarget.addVelocity(knockbackVec.x, 0.1, knockbackVec.z);
                livingTarget.setVelocityClient(
                        livingTarget.getVelocity().add(knockbackVec.x, 0.1, knockbackVec.z));
            }
        }

        // Remove projectile on hit
        this.discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        // Remove projectile when hitting anything
        if (!this.getEntityWorld().isClient()) {
            this.discard();
        }
    }

    private void spawnParticles() {
        // Particle spawning will be handled by renderer
        // This is a placeholder for future particle effects
    }

    public SpellSchool getSchool() {
        return school;
    }

    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putString("School", school.name());
        nbt.putFloat("Damage", damage);
        nbt.putFloat("Knockback", knockback);
        nbt.putInt("Age", age);
    }

    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("School")) {
            this.school = SpellSchool.valueOf(nbt.getString("School").orElse("FIRE"));
        }
        if (nbt.contains("Damage")) {
            this.damage = nbt.getFloat("Damage").orElse(2.0f);
        }
        if (nbt.contains("Knockback")) {
            this.knockback = nbt.getFloat("Knockback").orElse(0.0f);
        }
        if (nbt.contains("Age")) {
            this.age = nbt.getInt("Age").orElse(0);
        }
    }
}
