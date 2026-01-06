package dk.mosberg.entity;

import dk.mosberg.MAM;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Custom projectile entity for spell casting.
 */
public class SpellProjectileEntity extends ProjectileEntity {
    private static final TrackedData<Byte> SCHOOL_TRACKER =
            DataTracker.registerData(SpellProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);

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
        setSchool(spell.getSchool());
        this.damage = spell.getDamage();
        this.knockback = spell.getKnockback();
        float speed = Math.max(0.05f, spell.getProjectileSpeed());
        this.maxAge = Math.min(200, Math.max(40, Math.round((spell.getRange() / speed) * 20f)));

        // Spawn a little in front of the caster to avoid self-collision
        Vec3d look = owner.getRotationVec(1.0F);
        Vec3d spawnPos = owner.getEyePos().add(look.multiply(0.4));
        this.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, owner.getYaw(),
                owner.getPitch());

        // FireCharge-inspired launch: use rotation with zero divergence and configurable speed
        this.setVelocity(owner, owner.getPitch(), owner.getYaw(), 0.0F, speed, 0.0F);
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(SCHOOL_TRACKER, (byte) school.ordinal());
    }

    private void setSchool(SpellSchool school) {
        this.school = school;
        this.getDataTracker().set(SCHOOL_TRACKER, (byte) school.ordinal());
    }

    public SpellSchool getSchool() {
        byte idx = this.getDataTracker().get(SCHOOL_TRACKER);
        SpellSchool[] values = SpellSchool.values();
        if (idx >= 0 && idx < values.length) {
            return values[idx];
        }
        return school;
    }

    @Override
    public void tick() {
        super.tick();

        if (++age > maxAge) {
            this.discard();
            return;
        }

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onCollision(hitResult);
            return;
        }

        Vec3d velocity = this.getVelocity();
        this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y,
                this.getZ() + velocity.z);
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);

        // Maintain speed with light drag; no gravity for straight shots
        float drag = this.isTouchingWater() ? 0.8f : 0.99f;
        this.setVelocity(velocity.multiply(drag));

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
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putString("School", school.name());
        view.putFloat("Damage", damage);
        view.putFloat("Knockback", knockback);
        view.putInt("Age", age);
    }

    @Override
    protected void readCustomData(ReadView view) {
        String schoolName = view.getString("School", SpellSchool.FIRE.name());
        try {
            setSchool(SpellSchool.valueOf(schoolName));
        } catch (IllegalArgumentException ignored) {
            setSchool(SpellSchool.FIRE);
        }

        this.damage = view.getFloat("Damage", this.damage);
        this.knockback = view.getFloat("Knockback", this.knockback);
        this.age = view.getInt("Age", this.age);
    }
}
