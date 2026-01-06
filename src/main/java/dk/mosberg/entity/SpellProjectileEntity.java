package dk.mosberg.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import dk.mosberg.MAM;
import dk.mosberg.spell.Spell;
import dk.mosberg.spell.SpellSchool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Custom projectile entity for spell casting with advanced behaviors: - Homing: Seeks nearby
 * targets within detection radius - Bouncing: Ricochets off blocks up to max bounce count -
 * Piercing: Passes through entities up to max pierce count - Chaining: Jumps to nearby targets on
 * hit - Trajectory curves: Parabolic, sine wave, spiral patterns - Detonation: Delayed explosion,
 * proximity trigger, on-water - Trail effects: Frost/fire trails with status effects - Sound
 * effects: School-specific sounds on spawn/trail/impact
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

    // Advanced behavior flags and counters
    private boolean homingEnabled = false;
    private float homingRadius = 8.0f;
    private float homingStrength = 0.05f;
    private LivingEntity homingTarget = null;

    private boolean bouncingEnabled = false;
    private int maxBounces = 0;
    private int bounceCount = 0;

    private boolean piercingEnabled = false;
    private int maxPierces = 0;
    private int pierceCount = 0;
    private final Set<UUID> hitEntities = new HashSet<>();

    private boolean chainingEnabled = false;
    private int maxChains = 0;
    private int chainCount = 0;
    private float chainRadius = 5.0f;

    private String trajectoryType = "straight"; // straight, parabolic, sine, spiral
    private float trajectoryAmplitude = 0.0f;
    private float trajectoryFrequency = 0.0f;

    private boolean detonationEnabled = false;
    private int detonationDelay = 0; // ticks until explosion
    private float detonationRadius = 3.0f;
    private boolean proximityTrigger = false;
    private float proximityRange = 2.0f;

    private boolean frostTrail = false;
    private boolean fireTrail = false;

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

        // Play spawn sound
        playSpawnSound();
    }

    // Behavior configuration methods
    public SpellProjectileEntity withHoming(float radius, float strength) {
        this.homingEnabled = true;
        this.homingRadius = radius;
        this.homingStrength = strength;
        return this;
    }

    public SpellProjectileEntity withBouncing(int maxBounces) {
        this.bouncingEnabled = true;
        this.maxBounces = maxBounces;
        return this;
    }

    public SpellProjectileEntity withPiercing(int maxPierces) {
        this.piercingEnabled = true;
        this.maxPierces = maxPierces;
        return this;
    }

    public SpellProjectileEntity withChaining(int maxChains, float chainRadius) {
        this.chainingEnabled = true;
        this.maxChains = maxChains;
        this.chainRadius = chainRadius;
        return this;
    }

    public SpellProjectileEntity withTrajectory(String type, float amplitude, float frequency) {
        this.trajectoryType = type;
        this.trajectoryAmplitude = amplitude;
        this.trajectoryFrequency = frequency;
        return this;
    }

    public SpellProjectileEntity withDetonation(int delay, float radius, boolean proximity) {
        this.detonationEnabled = true;
        this.detonationDelay = delay;
        this.detonationRadius = radius;
        this.proximityTrigger = proximity;
        return this;
    }

    public SpellProjectileEntity withFrostTrail() {
        this.frostTrail = true;
        return this;
    }

    public SpellProjectileEntity withFireTrail() {
        this.fireTrail = true;
        return this;
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
            if (detonationEnabled && detonationDelay <= 0) {
                explode();
            }
            this.discard();
            return;
        }

        // Detonation countdown
        if (detonationEnabled && detonationDelay > 0) {
            detonationDelay--;
            if (detonationDelay <= 0) {
                explode();
                return;
            }
        }

        // Proximity trigger check
        if (proximityTrigger && checkProximityTrigger()) {
            explode();
            return;
        }

        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onCollision(hitResult);
            return;
        }

        Vec3d velocity = this.getVelocity();

        // Apply homing behavior
        if (homingEnabled) {
            velocity = applyHoming(velocity);
        }

        // Apply trajectory curves
        velocity = applyTrajectory(velocity);

        this.setPosition(this.getX() + velocity.x, this.getY() + velocity.y,
                this.getZ() + velocity.z);
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);

        // Maintain speed with light drag; no gravity for straight shots
        float drag = this.isTouchingWater() ? 0.8f : 0.99f;
        this.setVelocity(velocity.multiply(drag));

        // Trail effects
        applyTrailEffects();

        spawnParticles();

        // Play trail sound periodically
        if (age % 20 == 0) {
            playTrailSound();
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

        // Skip if already hit (for piercing)
        if (hitEntities.contains(target.getUuid())) {
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

            hitEntities.add(target.getUuid());
        }

        spawnImpactParticles();
        playImpactSound();

        // Chaining behavior
        if (chainingEnabled && chainCount < maxChains && target instanceof LivingEntity) {
            LivingEntity nextTarget = findNearestTarget((LivingEntity) target);
            if (nextTarget != null) {
                chainToTarget(nextTarget);
                chainCount++;
                return; // Don't discard, continue to next target
            }
        }

        // Piercing behavior
        if (piercingEnabled && pierceCount < maxPierces) {
            pierceCount++;
            return; // Don't discard, continue through
        }

        // Remove projectile on hit
        this.discard();
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        // Handle block bouncing
        if (hitResult.getType() == HitResult.Type.BLOCK && bouncingEnabled
                && bounceCount < maxBounces) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            Vec3d normal = new Vec3d(blockHit.getSide().getUnitVector());
            Vec3d velocity = this.getVelocity();

            // Reflect velocity across surface normal
            Vec3d reflected = velocity.subtract(normal.multiply(2 * velocity.dotProduct(normal)));
            this.setVelocity(reflected.multiply(0.8)); // Lose some energy on bounce

            bounceCount++;
            spawnImpactParticles();
            playImpactSound();
            return;
        }

        // Remove projectile when hitting anything
        if (!this.getEntityWorld().isClient()) {
            spawnImpactParticles();
            playImpactSound();

            if (detonationEnabled) {
                explode();
            }

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

    // Advanced behavior helper methods

    private Vec3d applyHoming(Vec3d velocity) {
        if (homingTarget == null || !homingTarget.isAlive()) {
            homingTarget = findHomingTarget();
        }

        if (homingTarget != null) {
            Vec3d targetPos = new Vec3d(homingTarget.getX(),
                    homingTarget.getY() + homingTarget.getHeight() / 2, homingTarget.getZ());
            Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());
            Vec3d toTarget = targetPos.subtract(currentPos).normalize();
            return velocity.normalize().lerp(toTarget, homingStrength).multiply(velocity.length());
        }

        return velocity;
    }

    private LivingEntity findHomingTarget() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return null;
        }

        Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());
        Box searchBox = new Box(currentPos.subtract(homingRadius, homingRadius, homingRadius),
                currentPos.add(homingRadius, homingRadius, homingRadius));
        @SuppressWarnings("null")
        List<LivingEntity> entities = serverWorld.getEntitiesByClass(LivingEntity.class, searchBox,
                entity -> entity != this.getOwner() && entity.isAlive()
                        && !hitEntities.contains(entity.getUuid()));

        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double dist = this.squaredDistanceTo(entity);
            if (dist < closestDist) {
                closest = entity;
                closestDist = dist;
            }
        }

        return closest;
    }

    private Vec3d applyTrajectory(Vec3d velocity) {
        if ("straight".equals(trajectoryType)) {
            return velocity;
        }

        float t = age * trajectoryFrequency;
        Vec3d perpendicular = new Vec3d(-velocity.z, 0, velocity.x).normalize();

        switch (trajectoryType) {
            case "sine" -> {
                float offset = (float) Math.sin(t) * trajectoryAmplitude;
                return velocity.add(perpendicular.multiply(offset));
            }
            case "spiral" -> {
                float offsetX = (float) Math.cos(t) * trajectoryAmplitude;
                float offsetZ = (float) Math.sin(t) * trajectoryAmplitude;
                Vec3d cross = velocity.crossProduct(new Vec3d(0, 1, 0)).normalize();
                return velocity.add(perpendicular.multiply(offsetX)).add(cross.multiply(offsetZ));
            }
            case "parabolic" -> {
                // Add slight downward arc over time
                return velocity.add(0, -0.01 * age * trajectoryAmplitude, 0);
            }
        }

        return velocity;
    }

    private void applyTrailEffects() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        BlockPos pos = this.getBlockPos();
        Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());

        if (frostTrail) {
            // Apply slowness to nearby entities
            Box areaBox = new Box(currentPos.subtract(2, 2, 2), currentPos.add(2, 2, 2));
            @SuppressWarnings("null")
            List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, areaBox,
                    entity -> entity != this.getOwner());

            for (LivingEntity entity : nearby) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 0));
            }

            // Freeze water blocks
            if (age % 5 == 0) {
                BlockState state = serverWorld.getBlockState(pos);
                if (state.getBlock() == Blocks.WATER) {
                    serverWorld.setBlockState(pos, Blocks.ICE.getDefaultState());
                }
            }
        }

        if (fireTrail) {
            // Ignite entities
            Box areaBox =
                    new Box(currentPos.subtract(1.5, 1.5, 1.5), currentPos.add(1.5, 1.5, 1.5));
            @SuppressWarnings("null")
            List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, areaBox,
                    entity -> entity != this.getOwner());

            for (LivingEntity entity : nearby) {
                entity.setOnFireFor(3);
            }

            // Set fire to blocks occasionally
            if (age % 10 == 0 && serverWorld.isAir(pos.up())) {
                serverWorld.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
            }
        }
    }

    private boolean checkProximityTrigger() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());
        Box searchBox = new Box(currentPos.subtract(proximityRange, proximityRange, proximityRange),
                currentPos.add(proximityRange, proximityRange, proximityRange));
        @SuppressWarnings("null")
        List<LivingEntity> nearby = serverWorld.getEntitiesByClass(LivingEntity.class, searchBox,
                entity -> entity != this.getOwner());

        return !nearby.isEmpty();
    }

    private void explode() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        serverWorld.createExplosion(this,
                this.getDamageSources().explosion(this,
                        this.getOwner() instanceof LivingEntity le ? le : null),
                null, this.getX(), this.getY(), this.getZ(), detonationRadius, false,
                World.ExplosionSourceType.TNT);

        this.discard();
    }

    private LivingEntity findNearestTarget(LivingEntity currentTarget) {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return null;
        }

        Vec3d targetPos =
                new Vec3d(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ());
        Box searchBox = new Box(targetPos.subtract(chainRadius, chainRadius, chainRadius),
                targetPos.add(chainRadius, chainRadius, chainRadius));
        @SuppressWarnings("null")
        List<LivingEntity> entities = serverWorld.getEntitiesByClass(LivingEntity.class, searchBox,
                entity -> entity != this.getOwner() && entity != currentTarget
                        && !hitEntities.contains(entity.getUuid()) && entity.isAlive());

        if (entities.isEmpty()) {
            return null;
        }

        LivingEntity closest = entities.get(0);
        double closestDist = currentTarget.squaredDistanceTo(closest);

        for (LivingEntity entity : entities) {
            double dist = currentTarget.squaredDistanceTo(entity);
            if (dist < closestDist) {
                closest = entity;
                closestDist = dist;
            }
        }

        return closest;
    }

    private void chainToTarget(LivingEntity target) {
        Vec3d targetPos =
                new Vec3d(target.getX(), target.getY() + target.getHeight() / 2, target.getZ());
        Vec3d currentPos = new Vec3d(this.getX(), this.getY(), this.getZ());
        Vec3d direction = targetPos.subtract(currentPos).normalize();
        float speed = (float) this.getVelocity().length();
        this.setVelocity(direction.multiply(speed));

        // Spawn chain effect particles
        if (this.getEntityWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY(),
                    this.getZ(), 20, 0.3, 0.3, 0.3, 0.1);
        }
    }

    private void playSpawnSound() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        switch (school) {
            case FIRE -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.5f, 1.0f);
            case WATER -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_BOAT_PADDLE_WATER, SoundCategory.PLAYERS, 0.5f, 1.2f);
            case AIR -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 0.5f, 1.3f);
            case EARTH -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_GRAVEL_BREAK, SoundCategory.PLAYERS, 0.5f, 0.8f);
        }
    }

    private void playTrailSound() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        switch (school) {
            case FIRE -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.PLAYERS, 0.2f, 1.5f);
            case WATER -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, SoundCategory.PLAYERS,
                    0.1f, 2.0f);
            case AIR -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS, 0.1f, 1.8f);
            case EARTH -> {
            } // Earth is silent
        }
    }

    private void playImpactSound() {
        if (!(this.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        switch (school) {
            case FIRE -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.4f, 1.5f);
            case WATER -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 0.6f, 1.0f);
            case AIR -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENTITY_BREEZE_HURT, SoundCategory.PLAYERS, 0.5f, 1.2f);
            case EARTH -> serverWorld.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 0.6f, 0.9f);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putString("School", school.name());
        view.putFloat("Damage", damage);
        view.putFloat("Knockback", knockback);
        view.putInt("Tier", tier);
        view.putInt("Age", age);

        // Advanced behaviors
        view.putBoolean("HomingEnabled", homingEnabled);
        view.putFloat("HomingRadius", homingRadius);
        view.putFloat("HomingStrength", homingStrength);

        view.putBoolean("BouncingEnabled", bouncingEnabled);
        view.putInt("MaxBounces", maxBounces);
        view.putInt("BounceCount", bounceCount);

        view.putBoolean("PiercingEnabled", piercingEnabled);
        view.putInt("MaxPierces", maxPierces);
        view.putInt("PierceCount", pierceCount);

        view.putBoolean("ChainingEnabled", chainingEnabled);
        view.putInt("MaxChains", maxChains);
        view.putInt("ChainCount", chainCount);
        view.putFloat("ChainRadius", chainRadius);

        view.putString("TrajectoryType", trajectoryType);
        view.putFloat("TrajectoryAmplitude", trajectoryAmplitude);
        view.putFloat("TrajectoryFrequency", trajectoryFrequency);

        view.putBoolean("DetonationEnabled", detonationEnabled);
        view.putInt("DetonationDelay", detonationDelay);
        view.putFloat("DetonationRadius", detonationRadius);
        view.putBoolean("ProximityTrigger", proximityTrigger);
        view.putFloat("ProximityRange", proximityRange);

        view.putBoolean("FrostTrail", frostTrail);
        view.putBoolean("FireTrail", fireTrail);
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

        // Advanced behaviors
        this.homingEnabled = view.getBoolean("HomingEnabled", false);
        this.homingRadius = view.getFloat("HomingRadius", 8.0f);
        this.homingStrength = view.getFloat("HomingStrength", 0.05f);

        this.bouncingEnabled = view.getBoolean("BouncingEnabled", false);
        this.maxBounces = view.getInt("MaxBounces", 0);
        this.bounceCount = view.getInt("BounceCount", 0);

        this.piercingEnabled = view.getBoolean("PiercingEnabled", false);
        this.maxPierces = view.getInt("MaxPierces", 0);
        this.pierceCount = view.getInt("PierceCount", 0);

        this.chainingEnabled = view.getBoolean("ChainingEnabled", false);
        this.maxChains = view.getInt("MaxChains", 0);
        this.chainCount = view.getInt("ChainCount", 0);
        this.chainRadius = view.getFloat("ChainRadius", 5.0f);

        this.trajectoryType = view.getString("TrajectoryType", "straight");
        this.trajectoryAmplitude = view.getFloat("TrajectoryAmplitude", 0.0f);
        this.trajectoryFrequency = view.getFloat("TrajectoryFrequency", 0.0f);

        this.detonationEnabled = view.getBoolean("DetonationEnabled", false);
        this.detonationDelay = view.getInt("DetonationDelay", 0);
        this.detonationRadius = view.getFloat("DetonationRadius", 3.0f);
        this.proximityTrigger = view.getBoolean("ProximityTrigger", false);
        this.proximityRange = view.getFloat("ProximityRange", 2.0f);

        this.frostTrail = view.getBoolean("FrostTrail", false);
        this.fireTrail = view.getBoolean("FireTrail", false);
    }
}
