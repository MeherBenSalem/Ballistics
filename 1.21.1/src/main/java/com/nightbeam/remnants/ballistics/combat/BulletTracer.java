package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.BallisticMath;
import com.nightbeam.remnants.ballistics.core.DamageModel;
import com.nightbeam.remnants.ballistics.core.HitZones;
import com.nightbeam.remnants.ballistics.core.ShotRandom;
import com.nightbeam.remnants.ballistics.core.V3;
import com.nightbeam.remnants.ballistics.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Side-agnostic hitscan tracer. The server uses it authoritatively (with lag-compensated hitboxes) and the shooting
 * client uses it to predict impact effects instantly. It never mutates the world: block breaks and damage are
 * returned for the caller to apply.
 */
public final class BulletTracer {
    private static final double SEGMENT_LENGTH = 16.0;
    private static final int MAX_STEPS = 48;
    private static final int MAX_RICOCHETS = 1;
    private static final double WATER_RANGE = 3.0;
    private static final float ENTITY_RESISTANCE = 1.0f;
    private static final float GLASS_RESISTANCE = 0.1f;

    private BulletTracer() {}

    public record Params(float maxRange, float velocity, float gravity, float penetration,
                         boolean allowPenetration, boolean allowRicochet, float ricochetChance, float ricochetMaxAngle,
                         boolean breakGlass) {
        public static Params fromConfig(float maxRange, float velocity, float gravity, float penetration) {
            return new Params(maxRange, velocity, gravity, penetration,
                    ServerConfig.get(ServerConfig.PENETRATION), ServerConfig.get(ServerConfig.RICOCHET),
                    (float) ServerConfig.get(ServerConfig.RICOCHET_CHANCE), (float) ServerConfig.get(ServerConfig.RICOCHET_MAX_ANGLE),
                    ServerConfig.get(ServerConfig.BREAK_GLASS));
        }
    }

    public record Impact(Vec3 pos, Direction face, ImpactType type, int blockStateId, boolean ricochet) {}

    public record EntityHit(Entity entity, Vec3 pos, double distance, HitZones.Zone zone, float damageFactor) {}

    public record Result(List<Vec3> points, List<Impact> impacts, List<EntityHit> hits, List<BlockPos> brokenBlocks) {}

    /**
     * @param boxes hitbox provider; the server supplies lag-compensated boxes for players
     */
    public static Result trace(Level level, Entity shooter, Vec3 origin, Vec3 direction, Params p, ShotRandom rng,
                               Function<Entity, AABB> boxes, boolean allZonesHumanoid) {
        List<Vec3> points = new ArrayList<>();
        List<Impact> impacts = new ArrayList<>();
        List<EntityHit> hits = new ArrayList<>();
        List<BlockPos> broken = new ArrayList<>();
        Set<Entity> alreadyHit = Collections.newSetFromMap(new IdentityHashMap<>());
        points.add(origin);

        float initialPower = Math.max(0.0001f, p.penetration());
        float power = p.penetration();
        float damageFactor = 1f;
        double travelled = 0;
        int ricochets = 0;
        double waterLeft = isWater(level, origin) ? WATER_RANGE : -1;

        List<V3> path = BallisticMath.trajectory(v3(origin), v3(direction), p.velocity(), p.gravity(), p.maxRange(), SEGMENT_LENGTH);
        int segIndex = 1;
        Vec3 a = origin;
        int steps = 0;

        trace:
        while (segIndex < path.size() && steps++ < MAX_STEPS) {
            Vec3 b = vec(path.get(segIndex));
            Vec3 segDir = b.subtract(a).normalize();
            if (segDir.lengthSqr() < 1.0E-12) {
                segIndex++;
                continue;
            }

            BlockHitResult blockHit = level.clip(new ClipContext(a, b, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
            Vec3 end = blockHit.getType() == HitResult.Type.MISS ? b : blockHit.getLocation();
            boolean spentUnderwater = false;

            // Water entry: splash, then the round only travels a short distance.
            if (waterLeft < 0) {
                BlockHitResult fluidHit = level.clip(new ClipContext(a, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, shooter));
                if (fluidHit.getType() == HitResult.Type.BLOCK
                        && level.getFluidState(fluidHit.getBlockPos()).is(FluidTags.WATER)
                        && fluidHit.getLocation().distanceToSqr(a) + 1.0E-6 < end.distanceToSqr(a)) {
                    impacts.add(new Impact(fluidHit.getLocation(), fluidHit.getDirection(), ImpactType.WATER, 0, false));
                    waterLeft = WATER_RANGE + fluidHit.getLocation().distanceTo(a);
                }
            }
            if (waterLeft >= 0) {
                double segLen = end.distanceTo(a);
                if (segLen > waterLeft) {
                    end = a.add(segDir.scale(waterLeft));
                    spentUnderwater = true;
                    blockHit = BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end));
                }
            }
            float waterFactor = waterLeft >= 0 ? 0.6f : 1f;

            // Entities along this piece of the path, nearest first.
            for (EntityHit hit : entitiesAlong(level, shooter, a, end, boxes, allZonesHumanoid)) {
                if (!alreadyHit.add(hit.entity())) continue;
                double dist = travelled + a.distanceTo(hit.pos());
                hits.add(new EntityHit(hit.entity(), hit.pos(), dist, hit.zone(), damageFactor * waterFactor));
                impacts.add(new Impact(hit.pos(), Direction.UP, ImpactType.FLESH, 0, false));
                if (!p.allowPenetration() || power < ENTITY_RESISTANCE) {
                    points.add(hit.pos());
                    break trace;
                }
                power -= ENTITY_RESISTANCE;
                damageFactor *= DamageModel.penetrationDamageFactor(power, initialPower);
            }

            if (waterLeft >= 0) waterLeft -= end.distanceTo(a);

            if (blockHit.getType() == HitResult.Type.MISS) {
                travelled += end.distanceTo(a);
                a = end;
                if (spentUnderwater) {
                    points.add(end);
                    break;
                }
                points.add(end);
                segIndex++;
                continue;
            }

            BlockPos pos = blockHit.getBlockPos();
            BlockState state = level.getBlockState(pos);
            Vec3 hitLoc = blockHit.getLocation();
            Direction face = blockHit.getDirection();
            ImpactType type = ImpactType.of(state, level, pos);
            int stateId = Block.getId(state);
            travelled += hitLoc.distanceTo(a);

            if (p.breakGlass() && state.is(ModTags.Blocks.BULLET_BREAKABLE)) {
                broken.add(pos);
                impacts.add(new Impact(hitLoc, face, ImpactType.GLASS, stateId, false));
                power -= GLASS_RESISTANCE;
                damageFactor *= 0.9f;
                a = exitPoint(level, pos, state, hitLoc, segDir).add(segDir.scale(0.002));
                continue;
            }

            if (p.allowPenetration() && state.is(ModTags.Blocks.BULLET_PENETRABLE)) {
                float resistance = Math.max(0.15f, Math.min(3f, state.getDestroySpeed(level, pos) * 0.6f));
                if (power >= resistance) {
                    power -= resistance;
                    damageFactor *= DamageModel.penetrationDamageFactor(power, initialPower);
                    Vec3 exit = exitPoint(level, pos, state, hitLoc, segDir);
                    impacts.add(new Impact(hitLoc, face, type, stateId, false));
                    impacts.add(new Impact(exit, face.getOpposite(), type, stateId, false));
                    travelled += exit.distanceTo(hitLoc);
                    a = exit.add(segDir.scale(0.002));
                    continue;
                }
            }

            if (p.allowRicochet() && ricochets < MAX_RICOCHETS && type.canRicochet() && waterLeft < 0) {
                V3 normal = new V3(face.getStepX(), face.getStepY(), face.getStepZ());
                double angle = BallisticMath.impactAngleDeg(v3(segDir), normal);
                if (angle <= p.ricochetMaxAngle() && rng.nextFloat() < p.ricochetChance()) {
                    impacts.add(new Impact(hitLoc, face, type, stateId, true));
                    points.add(hitLoc);
                    V3 bounce = BallisticMath.spread(BallisticMath.reflect(v3(segDir), normal), 2f, rng);
                    double remaining = Math.max(0, (p.maxRange() - travelled) * 0.5);
                    Vec3 start = hitLoc.add(vec(bounce).scale(0.01));
                    path = BallisticMath.trajectory(v3(start), bounce, p.velocity() * 0.6, p.gravity(), remaining, SEGMENT_LENGTH);
                    segIndex = 1;
                    a = start;
                    damageFactor *= 0.5f;
                    ricochets++;
                    continue;
                }
            }

            impacts.add(new Impact(hitLoc, face, type, stateId, false));
            points.add(hitLoc);
            break;
        }

        if (points.size() == 1) points.add(a);
        return new Result(points, impacts, hits, broken);
    }

    private static List<EntityHit> entitiesAlong(Level level, Entity shooter, Vec3 a, Vec3 b, Function<Entity, AABB> boxes, boolean allZonesHumanoid) {
        AABB sweep = new AABB(a, b).inflate(1.5);
        List<EntityHit> found = new ArrayList<>();
        for (Entity e : level.getEntities(shooter, sweep, target -> canHit(shooter, target))) {
            AABB box = boxes.apply(e).inflate(0.1);
            Optional<Vec3> clip = box.contains(a) ? Optional.of(a) : box.clip(a, b);
            if (clip.isEmpty()) continue;
            Vec3 hitPos = clip.get();
            boolean humanoid = allZonesHumanoid || e instanceof Player || e.getType().is(ModTags.Entities.HUMANOID_HIT_ZONES);
            HitZones.Zone zone = HitZones.classify(hitPos.y, box.minY + 0.1, box.getYsize() - 0.2, e.getEyeHeight(), humanoid);
            // distance holds the squared distance from the segment start here; it is only used for ordering.
            found.add(new EntityHit(e, hitPos, a.distanceToSqr(hitPos), zone, 1f));
        }
        found.sort(Comparator.comparingDouble(EntityHit::distance));
        return found;
    }

    public static boolean canHit(Entity shooter, Entity target) {
        return target != shooter && target.isAlive() && target.isPickable() && !target.isSpectator()
                && !shooter.isPassengerOfSameVehicle(target);
    }

    /** Point where a straight ray leaves the block's collision shape. */
    private static Vec3 exitPoint(Level level, BlockPos pos, BlockState state, Vec3 entry, Vec3 dir) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) shape = state.getShape(level, pos);
        if (shape.isEmpty()) return entry;
        Vec3 far = entry.add(dir.scale(2.0));
        BlockHitResult back = shape.clip(far, entry, pos);
        return back != null ? back.getLocation() : entry.add(dir.scale(0.5));
    }

    private static boolean isWater(Level level, Vec3 pos) {
        return level.getFluidState(BlockPos.containing(pos)).is(FluidTags.WATER);
    }

    private static V3 v3(Vec3 v) {
        return new V3(v.x, v.y, v.z);
    }

    private static Vec3 vec(V3 v) {
        return new Vec3(v.x(), v.y(), v.z());
    }
}
