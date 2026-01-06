package dk.mosberg.entity;

import dk.mosberg.MAM;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Custom projectile entity for spell casting. Implements FlyingItemEntity to support proper
 * rendering with ItemStack visuals.
 *
 * TODO: Implement homing projectile behavior (seeking target with radius) TODO: Add projectile
 * bounce/ricochet mechanics (configurable bounce count/angle) TODO: Implement piercing projectiles
 * that ignore entity collision up to max hits TODO: Add trajectory prediction/curves (parabolic,
 * sine wave, spiral patterns) TODO: Implement chaining projectiles (jump to nearby targets on hit)
 * TODO: Add detonation mechanics (delayed explosion, proximity trigger, on-water) TODO: Implement
 * frost trail effects (slowness aura, block freezing) TODO: Add fire trail effects (ignite blocks,
 * spreading fire) TODO: Implement particle trail customization per tier/school TODO: Add sound
 * effects on spawn, trail, impact (school-specific)
 */
public class SpellProjectileEntity extends ProjectileEntity implements FlyingItemEntity {
    private static final TrackedData<Byte> SCHOOL_TRACKER =
            DataTracker.registerData(SpellProjectileEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<ItemStack> ITEM = DataTracker
            .registerData(SpellProjectileEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    private SpellSchool school = SpellSchool.FIRE;
    private float damage = 2.0f;
    private float knockback = 0.0f;
    private int tier = 1; // Spell tier for visual scaling
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
        this.tier = spell.getTier();
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

    @SuppressWarnings("null")
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // Use safe defaults here; subclass fields are not initialized yet during super constructor
        builder.add(SCHOOL_TRACKER, (byte) SpellSchool.FIRE.ordinal());
        builder.add(ITEM, new ItemStack(MAM.PROJECTILE_FIRE));
    }

    private ItemStack getDefaultItemStack() {
        // Return the appropriate projectile item based on school
        return new ItemStack(switch (school) {
            case FIRE -> MAM.PROJECTILE_FIRE;
            case WATER -> MAM.PROJECTILE_WATER;
            case AIR -> MAM.PROJECTILE_AIR;
            case EARTH -> MAM.PROJECTILE_EARTH;
        });
    }

    @Override
    public ItemStack getStack() {
        return this.getDataTracker().get(ITEM);
    }

    private void updateItemStack() {
        this.getDataTracker().set(ITEM, getDefaultItemStack());
    }

    private void setSchool(SpellSchool school) {
        this.school = school;
        this.getDataTracker().set(SCHOOL_TRACKER, (byte) school.ordinal());
        updateItemStack();
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

        spawnParticles();
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

        spawnImpactParticles();

        // Remove projectile on hit
        this.discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        // Remove projectile when hitting anything
        if (!this.getEntityWorld().isClient()) {
            spawnImpactParticles();
            this.discard();
        }
    }

    private void spawnParticles() {
        // Server-side spawn so it replicates to all clients without client imports
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        ParticleEffect effect;
        int count;
        switch (getSchool()) {
            case FIRE -> {
                effect = ParticleTypes.FLAME;
                count = scaledTrailCount();
            }
            case WATER -> {
                effect = ParticleTypes.SPLASH;
                count = scaledTrailCount();
            }
            case AIR -> {
                effect = ParticleTypes.CLOUD;
                count = scaledTrailCount();
            }
            case EARTH -> {
                effect = new BlockStateParticleEffect(ParticleTypes.BLOCK,
                        Blocks.DIRT.getDefaultState());
                count = scaledTrailCount();
            }
            default -> {
                effect = ParticleTypes.CRIT;
                count = Math.max(1, scaledTrailCount() - 1);
            }
        }

        serverWorld.spawnParticles(effect, this.getX(), this.getBodyY(0.5), this.getZ(), count,
                0.05, 0.05, 0.05, 0.02);
    }

    private int scaledTrailCount() {
        // Scale trail particle count per tier: tier1=2, tier2=3-4, tier3=5-8, tier4=9-12
        return switch (this.tier) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 7;
            case 4 -> 11;
            default -> 2;
        };
    }

    private void spawnImpactParticles() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        ParticleEffect effect;
        int baseCount;
        switch (getSchool()) {
            case FIRE -> {
                effect = ParticleTypes.DRIPPING_OBSIDIAN_TEAR;
                baseCount = 18;
            }
            case WATER -> {
                effect = ParticleTypes.SPLASH;
                baseCount = 16;
            }
            case AIR -> {
                effect = ParticleTypes.HAPPY_VILLAGER;
                baseCount = 16;
            }
            case EARTH -> {
                effect = new BlockStateParticleEffect(ParticleTypes.BLOCK,
                        Blocks.DIRT.getDefaultState());
                baseCount = 18;
            }
            default -> {
                effect = ParticleTypes.CRIT;
                baseCount = 10;
            }
        }

        int count = scaledImpactCount(baseCount);
        serverWorld.spawnParticles(effect, this.getX(), this.getBodyY(0.5), this.getZ(), count, 0.3,
                0.3, 0.3, 0.05);
    }

    private int scaledImpactCount(int base) {
        // Scale burst intensity slightly with damage to keep higher-tiers looking stronger
        int bonus = (int) Math.max(0, Math.round(this.damage));
        return Math.max(base, base + bonus);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putString("School", school.name());
        view.putFloat("Damage", damage);
        view.putFloat("Knockback", knockback);
        view.putInt("Tier", tier);
        view.putInt("Age", age);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        String schoolName = view.getString("School", SpellSchool.FIRE.name());
        try {
            setSchool(SpellSchool.valueOf(schoolName));
        } catch (IllegalArgumentException ignored) {
            setSchool(SpellSchool.FIRE);
        }

        this.damage = view.getFloat("Damage", this.damage);
        this.knockback = view.getFloat("Knockback", this.knockback);
        this.tier = view.getInt("Tier", this.tier);
        this.age = view.getInt("Age", this.age);
    }
}
