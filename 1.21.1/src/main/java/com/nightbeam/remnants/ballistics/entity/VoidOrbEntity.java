package com.nightbeam.remnants.ballistics.entity;

import com.nightbeam.remnants.ballistics.combat.ModDamageTypes;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.neoforge.event.EventHooks;

/**
 * Voidcaster projectile: a slow, gravity-free orb that bursts on impact, damaging the target and (at half strength)
 * anything nearby. Block damage is off unless {@code energyBlockDamage} is enabled.
 */
public class VoidOrbEntity extends Projectile {
    private static final int MAX_LIFE = 80;
    private static final double BURST_RADIUS = 2.5;
    private float damage = 8f;
    private int life;

    public VoidOrbEntity(EntityType<? extends VoidOrbEntity> type, Level level) {
        super(type, level);
    }

    public VoidOrbEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.VOID_ORB.get(), level);
        setOwner(owner);
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && ++life > MAX_LIFE) {
            burst(position(), null);
            return;
        }
        Vec3 motion = getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hit)) {
            hitTargetOrDeflectSelf(hit);
        }
        if (isRemoved()) return;
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        updateRotation();
        if (level().isClientSide) {
            level().addParticle(ModParticles.VOID_SPARK.get(), getX(), getY() + 0.1, getZ(),
                    (random.nextDouble() - 0.5) * 0.04, (random.nextDouble() - 0.5) * 0.04, (random.nextDouble() - 0.5) * 0.04);
        } else if (life % 10 == 1) {
            level().playSound(null, getX(), getY(), getZ(), ModSounds.VOID_ORB_HUM.get(), SoundSource.PLAYERS, 0.5f, 0.9f + random.nextFloat() * 0.2f);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        Entity owner = getOwner();
        if (owner instanceof Player shooter && target instanceof Player victim && !shooter.canHarmPlayer(victim)) {
            burst(result.getLocation(), target);
            return;
        }
        target.invulnerableTime = 0;
        float multiplier = (float) (target instanceof Player ? ServerConfig.get(ServerConfig.PVP_DAMAGE_MULTIPLIER) : ServerConfig.get(ServerConfig.MOB_DAMAGE_MULTIPLIER));
        target.hurt(damageSource(), damage * multiplier);
        burst(result.getLocation(), target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) burst(result.getLocation(), null);
    }

    private DamageSource damageSource() {
        return ModDamageTypes.source(level(), ModDamageTypes.ENERGY, this, getOwner() != null ? getOwner() : this);
    }

    /** Area damage (half strength, linear falloff), particles and sound; then the orb is removed. */
    private void burst(Vec3 at, Entity directTarget) {
        if (!(level() instanceof ServerLevel server)) return;
        Entity owner = getOwner();
        AABB area = new AABB(at, at).inflate(BURST_RADIUS);
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class, area, e -> e != directTarget && e.isAlive())) {
            if (owner instanceof Player shooter && living instanceof Player victim && (victim == shooter || !shooter.canHarmPlayer(victim))) continue;
            double dist = living.position().add(0, living.getBbHeight() * 0.5, 0).distanceTo(at);
            if (dist > BURST_RADIUS) continue;
            float splash = (float) (damage * 0.5 * (1.0 - dist / BURST_RADIUS));
            if (splash > 0.25f) living.hurt(damageSource(), splash);
        }
        server.sendParticles(ModParticles.VOID_SPARK.get(), at.x, at.y, at.z, 24, 0.3, 0.3, 0.3, 0.15);
        server.playSound(null, at.x, at.y, at.z, ModSounds.IMPACT_ENERGY.get(), SoundSource.PLAYERS, 1.2f, 0.9f + random.nextFloat() * 0.2f);
        if (ServerConfig.get(ServerConfig.ENERGY_BLOCK_DAMAGE)) {
            server.explode(this, at.x, at.y, at.z, 1.2f, Level.ExplosionInteraction.MOB);
        }
        discard();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
        tag.putInt("Life", life);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("Damage");
        life = tag.getInt("Life");
    }
}
