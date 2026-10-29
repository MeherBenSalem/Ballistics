package com.nightbeam.remnants.ballistics.client.fx;

import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.render.GunVisuals;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.combat.BulletTracer;
import com.nightbeam.remnants.ballistics.combat.ImpactType;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.BallisticMath;
import com.nightbeam.remnants.ballistics.core.ShotRandom;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.V3;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.network.ShotEffectsPayload;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.ShotType;
import com.nightbeam.remnants.ballistics.weapon.WeaponCategory;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Spawns and tracks shot effects: tracers, impacts (particles, sounds, decals) and third-person muzzle flashes.
 * The local shooter's effects are predicted from a client-side trace; everyone else's come from the server.
 */
public final class ClientEffects {
    /** Listeners at least this far from a shot also hear the far-field report (gun.distant.fire). */
    private static final double DISTANT_REPORT_MIN_DISTANCE = 64.0;

    static final List<Tracer> TRACERS = new ArrayList<>();
    static final ArrayDeque<BulletHole> HOLES = new ArrayDeque<>();
    static final List<Flash> FLASHES = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private ClientEffects() {}

    /** A visible streak travelling along a polyline. */
    static final class Tracer {
        final Vec3[] points;
        final double[] cumulative;
        final long startNanos;
        final float speed;
        final float streak;
        final float width;
        final float r, g, b;
        final boolean beam;

        Tracer(List<Vec3> pts, float speed, float streak, float width, float r, float g, float b, boolean beam) {
            this.points = pts.toArray(new Vec3[0]);
            this.cumulative = new double[points.length];
            for (int i = 1; i < points.length; i++) cumulative[i] = cumulative[i - 1] + points[i].distanceTo(points[i - 1]);
            this.startNanos = System.nanoTime();
            this.speed = speed;
            this.streak = streak;
            this.width = width;
            this.r = r;
            this.g = g;
            this.b = b;
            this.beam = beam;
        }

        double length() {
            return cumulative[cumulative.length - 1];
        }

        float age() {
            return (System.nanoTime() - startNanos) / 1.0E9f;
        }

        boolean expired() {
            return beam ? age() > 0.5f : age() * speed > length() + streak;
        }

        Vec3 at(double distance) {
            if (distance <= 0) return points[0];
            for (int i = 1; i < points.length; i++) {
                if (distance <= cumulative[i]) {
                    double seg = cumulative[i] - cumulative[i - 1];
                    double f = seg <= 0 ? 0 : (distance - cumulative[i - 1]) / seg;
                    return points[i - 1].lerp(points[i], f);
                }
            }
            return points[points.length - 1];
        }
    }

    record BulletHole(Vec3 pos, Direction face, long createdNanos, float rotation, float size, float shade) {}

    record Flash(Vec3 pos, long nanos, float size, boolean energy) {}

    public static void clear() {
        TRACERS.clear();
        HOLES.clear();
        FLASHES.clear();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Local prediction
    // ------------------------------------------------------------------------------------------------------------

    public static void predictShot(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals,
                                   int sequence, float spread, float yaw, float pitch) {
        ClientLevel level = (ClientLevel) player.level();
        GunAttachments attachments = GunData.attachments(stack);
        boolean suppressed = GunData.isSuppressed(attachments);

        SoundEvent sound = suppressed ? ModSounds.SUPPRESSED_FIRE.get() : ModSounds.fire(def.sound());
        float volume = (float) ClientConfig.get(ClientConfig.GUNSHOT_VOLUME) * (suppressed ? 0.7f : 1f);
        level.playLocalSound(player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume,
                0.95f + RANDOM.nextFloat() * 0.1f, false);

        float ads = ClientGunHandler.adsProgress(1f);
        Vec3 muzzle = firstPersonMuzzle(player, ads);
        if (!suppressed && !GunData.hidesFlash(attachments)) smoke(level, muzzle, 1);
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson() && !GunData.hidesFlash(attachments)
                && ClientConfig.get(ClientConfig.MUZZLE_FLASH)) {
            // In third-person view the viewmodel flash is not visible, so show the world-space flash instead.
            muzzle = thirdPersonMuzzle(player);
            FLASHES.add(new Flash(muzzle, System.nanoTime(), 0.45f * visuals.flashSize(), def.category() == WeaponCategory.EXPERIMENTAL));
        }
        if (ClientConfig.get(ClientConfig.SHELL_EJECTION) && !"none".equals(visuals.shell()) && def.shotType() == ShotType.HITSCAN) {
            ejectCasing(level, player, visuals.shell(), ads);
        }

        if (def.shotType() != ShotType.HITSCAN) return;
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        ShotRandom rng = ShotRandom.forShot(s.seed, sequence);
        V3 forward = BallisticMath.direction(yaw, pitch);
        BulletTracer.Params params = BulletTracer.Params.fromConfig(stats.get(Stat.MAX_RANGE), stats.get(Stat.MUZZLE_VELOCITY),
                stats.get(Stat.GRAVITY), stats.get(Stat.PENETRATION));
        boolean allHumanoid = ServerConfig.get(ServerConfig.HEADSHOTS_ON_ALL_MOBS);
        int pellets = Math.max(1, stats.getInt(Stat.PELLETS));
        for (int i = 0; i < pellets; i++) {
            V3 dir = BallisticMath.spread(forward, spread, rng);
            BulletTracer.Result result = BulletTracer.trace(level, player, player.getEyePosition(), new Vec3(dir.x(), dir.y(), dir.z()),
                    params, rng, Entity::getBoundingBox, allHumanoid);
            List<Vec3> points = new ArrayList<>(result.points());
            points.set(0, muzzle);
            addTracer(def, points, pellets > 1);
            for (BulletTracer.Impact impact : result.impacts()) {
                impact(level, impact.pos(), impact.face(), impact.type(), impact.blockStateId(), impact.ricochet());
            }
        }
    }

    /** Approximate world position of the first-person muzzle, blending to the screen centre while aiming. */
    private static Vec3 firstPersonMuzzle(Player player, float ads) {
        Vec3 look = player.getViewVector(1f);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1.0E-6) right = new Vec3(1, 0, 0);
        Vec3 up = right.cross(look).normalize();
        double side = 0.22 * (1 - ads);
        double drop = 0.12 * (1 - ads) + 0.04;
        return player.getEyePosition().add(look.scale(0.9)).add(right.scale(side)).subtract(up.scale(drop));
    }

    private static void ejectCasing(ClientLevel level, Player player, String shell, float ads) {
        Vec3 look = player.getViewVector(1f);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1.0E-6) right = new Vec3(1, 0, 0);
        Vec3 pos = player.getEyePosition().add(look.scale(0.45)).add(right.scale(0.18 * (1 - ads) + 0.05)).add(0, -0.12, 0);
        Vec3 vel = right.scale(0.09 + RANDOM.nextFloat() * 0.04).add(0, 0.1 + RANDOM.nextFloat() * 0.05, 0)
                .add(player.getDeltaMovement());
        ParticleOptions type = "shotgun_shell".equals(shell) ? ModParticles.SHOTGUN_SHELL.get() : ModParticles.CASING.get();
        level.addParticle(type, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Remote shots
    // ------------------------------------------------------------------------------------------------------------

    public static void onRemoteShot(ShotEffectsPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        WeaponDefinition def = Weapons.byIndex(payload.weaponIndex());
        Entity shooter = level.getEntity(payload.shooterId());
        Vec3 muzzle = payload.origin();
        if (shooter != null) {
            muzzle = thirdPersonMuzzle(shooter);
            RemoteWeaponStates.State state = RemoteWeaponStates.get(shooter);
            state.lastShotNanos = System.nanoTime();
            if (def != null && shooter instanceof Player p) {
                GunVisuals visuals = GunVisualsLoader.INSTANCE.get(def);
                state.fire.play(visuals.animations(), "fire", 0f);
                if (ClientConfig.get(ClientConfig.SHELL_EJECTION) && !"none".equals(visuals.shell()) && def.shotType() == ShotType.HITSCAN) {
                    ejectCasing(level, p, visuals.shell(), 0.5f);
                }
            }
        }
        boolean energy = def != null && def.category() == WeaponCategory.EXPERIMENTAL;
        distantReport(level, mc.player, payload, energy);
        if (!payload.has(ShotEffectsPayload.FLAG_HIDE_FLASH) && ClientConfig.get(ClientConfig.MUZZLE_FLASH)) {
            float size = def != null ? GunVisualsLoader.INSTANCE.get(def).flashSize() : 1f;
            FLASHES.add(new Flash(muzzle, System.nanoTime(), 0.45f * size, energy));
            if (!payload.has(ShotEffectsPayload.FLAG_SUPPRESSED)) smoke(level, muzzle, 1);
        }
        if (payload.has(ShotEffectsPayload.FLAG_NO_TRACER)) return;
        LocalPlayer local = mc.player;
        for (ShotEffectsPayload.Trace trace : payload.traces()) {
            List<Vec3> points = new ArrayList<>(trace.points());
            if (points.size() < 2) continue;
            points.set(0, muzzle);
            addTracer(def, points, trace.points().size() > 0 && payload.traces().size() > 1);
            if (local != null) whizIfClose(level, local, points);
            for (ShotEffectsPayload.Impact impact : trace.impacts()) {
                impact(level, impact.pos(), impact.face(), impact.type(), impact.blockStateId(), impact.ricochet());
            }
        }
    }

    /**
     * Unsuppressed gunfire beyond normal earshot also plays a low, rolling far-field report, so firefights can be
     * heard (and located) across a valley. The sound's range is set by its volume; its gain falls off linearly.
     */
    private static void distantReport(ClientLevel level, LocalPlayer listener, ShotEffectsPayload payload, boolean energy) {
        if (listener == null || energy || payload.has(ShotEffectsPayload.FLAG_SUPPRESSED)) return;
        Vec3 origin = payload.origin();
        double distance = listener.position().distanceTo(origin);
        if (distance < DISTANT_REPORT_MIN_DISTANCE) return;
        float range = (float) (distance + 48.0) / 16f * (float) ClientConfig.get(ClientConfig.GUNSHOT_VOLUME);
        if (range <= 0f) return;
        level.playLocalSound(origin.x, origin.y, origin.z, ModSounds.DISTANT_FIRE.get(), SoundSource.PLAYERS, range,
                0.92f + level.getRandom().nextFloat() * 0.12f, true);
    }

    private static Vec3 thirdPersonMuzzle(Entity shooter) {
        Vec3 look = shooter.getViewVector(1f);
        Vec3 right = look.cross(new Vec3(0, 1, 0)).normalize();
        if (right.lengthSqr() < 1.0E-6) right = new Vec3(1, 0, 0);
        return shooter.getEyePosition().add(look.scale(1.0)).add(right.scale(0.32)).add(0, -0.32, 0);
    }

    /** Plays a supersonic crack when a remote bullet passes near the local player's head. */
    private static void whizIfClose(ClientLevel level, LocalPlayer local, List<Vec3> points) {
        Vec3 eye = local.getEyePosition();
        for (int i = 1; i < points.size(); i++) {
            Vec3 a = points.get(i - 1);
            Vec3 b = points.get(i);
            Vec3 ab = b.subtract(a);
            double len2 = ab.lengthSqr();
            if (len2 < 1.0E-6) continue;
            double t = Math.max(0, Math.min(1, eye.subtract(a).dot(ab) / len2));
            Vec3 closest = a.add(ab.scale(t));
            if (closest.distanceToSqr(eye) < 4.0 && a.distanceToSqr(eye) > 9.0) {
                level.playLocalSound(closest.x, closest.y, closest.z, ModSounds.WHIZ.get(), SoundSource.PLAYERS, 0.8f,
                        0.9f + RANDOM.nextFloat() * 0.2f, false);
                return;
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Shared
    // ------------------------------------------------------------------------------------------------------------

    private static void addTracer(WeaponDefinition def, List<Vec3> points, boolean pellet) {
        if (!ClientConfig.get(ClientConfig.TRACERS) || points.size() < 2) return;
        if (TRACERS.size() > 256) TRACERS.removeFirst();
        boolean railgun = def != null && def.isCharged();
        if (railgun) {
            TRACERS.add(new Tracer(points, 0f, 0f, 0.09f, 0.45f, 0.85f, 1f, true));
        } else {
            float width = pellet ? 0.012f : 0.02f;
            TRACERS.add(new Tracer(points, 420f, pellet ? 2.5f : 5f, width, 1f, 0.86f, 0.55f, false));
        }
    }

    private static void smoke(ClientLevel level, Vec3 pos, int count) {
        int n = Math.round(count * (float) ClientConfig.get(ClientConfig.PARTICLE_INTENSITY));
        for (int i = 0; i < n; i++) {
            level.addParticle(ModParticles.MUZZLE_SMOKE.get(), pos.x, pos.y, pos.z,
                    (RANDOM.nextDouble() - 0.5) * 0.01, 0.01 + RANDOM.nextDouble() * 0.01, (RANDOM.nextDouble() - 0.5) * 0.01);
        }
    }

    /** Particles, sound and decal for one impact. */
    public static void impact(ClientLevel level, Vec3 pos, Direction face, ImpactType type, int stateId, boolean ricochet) {
        float intensity = (float) ClientConfig.get(ClientConfig.PARTICLE_INTENSITY);
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        BlockState state = Block.stateById(stateId);
        int debris = Math.round(4 * intensity);
        SoundEvent sound;
        switch (type) {
            case WATER -> {
                for (int i = 0; i < Math.round(8 * intensity); i++) {
                    level.addParticle(ParticleTypes.SPLASH, pos.x, pos.y + 0.05, pos.z, (RANDOM.nextDouble() - 0.5) * 0.2, 0.25, (RANDOM.nextDouble() - 0.5) * 0.2);
                }
                for (int i = 0; i < Math.round(3 * intensity); i++) {
                    level.addParticle(ParticleTypes.BUBBLE, pos.x, pos.y - 0.2, pos.z, 0, 0.05, 0);
                }
                sound = ModSounds.IMPACT_WATER.get();
            }
            case FLESH -> {
                for (int i = 0; i < Math.round(3 * intensity); i++) {
                    level.addParticle(new DustParticleOptions(0x730D0D, 0.9f), pos.x, pos.y, pos.z, 0, 0, 0);
                }
                sound = ModSounds.IMPACT_FLESH.get();
            }
            case ENERGY -> {
                burst(level, ModParticles.VOID_SPARK.get(), pos, normal, Math.round(6 * intensity), 0.15);
                sound = ModSounds.IMPACT_ENERGY.get();
            }
            default -> {
                if (!state.isAir()) burst(level, new BlockParticleOption(ParticleTypes.BLOCK, state), pos, normal, debris, 0.12);
                burst(level, ModParticles.IMPACT_DUST.get(), pos, normal, Math.max(1, Math.round(2 * intensity)), 0.03);
                if (type == ImpactType.METAL || ricochet) burst(level, ModParticles.SPARK.get(), pos, normal, Math.round(5 * intensity), 0.25);
                sound = switch (type) {
                    case METAL -> ModSounds.IMPACT_METAL.get();
                    case WOOD -> ModSounds.IMPACT_WOOD.get();
                    case GLASS -> ModSounds.IMPACT_GLASS.get();
                    case DIRT, SOFT -> ModSounds.IMPACT_DIRT.get();
                    default -> ModSounds.IMPACT_STONE.get();
                };
                if (type != ImpactType.GLASS && type != ImpactType.SOFT) addHole(pos, face, type);
            }
        }
        level.playLocalSound(pos.x, pos.y, pos.z, ricochet ? ModSounds.RICOCHET.get() : sound, SoundSource.PLAYERS,
                ricochet ? 0.8f : 0.55f, 0.9f + RANDOM.nextFloat() * 0.2f, false);
    }

    private static void burst(ClientLevel level, ParticleOptions particle, Vec3 pos, Vec3 normal, int count, double speed) {
        for (int i = 0; i < count; i++) {
            Vec3 v = normal.scale(speed * (0.5 + RANDOM.nextDouble()))
                    .add((RANDOM.nextDouble() - 0.5) * speed, (RANDOM.nextDouble() - 0.5) * speed + speed * 0.3, (RANDOM.nextDouble() - 0.5) * speed);
            level.addParticle(particle, pos.x + normal.x * 0.02, pos.y + normal.y * 0.02, pos.z + normal.z * 0.02, v.x, v.y, v.z);
        }
    }

    private static void addHole(Vec3 pos, Direction face, ImpactType type) {
        int max = ClientConfig.get(ClientConfig.MAX_BULLET_HOLES);
        if (max <= 0) return;
        while (HOLES.size() >= max) HOLES.removeFirst();
        float shade = type == ImpactType.METAL ? 0.75f : type == ImpactType.WOOD ? 0.55f : 0.35f;
        HOLES.addLast(new BulletHole(pos, face, System.nanoTime(), RANDOM.nextFloat() * 360f, 0.07f + RANDOM.nextFloat() * 0.03f, shade));
    }

    /** Called each client tick: expires effects and decals whose blocks are gone. */
    public static void tick(ClientLevel level) {
        TRACERS.removeIf(Tracer::expired);
        long now = System.nanoTime();
        FLASHES.removeIf(f -> now - f.nanos() > 60_000_000L);
        Iterator<BulletHole> it = HOLES.iterator();
        while (it.hasNext()) {
            BulletHole hole = it.next();
            if (now - hole.createdNanos() > 30_000_000_000L) {
                it.remove();
                continue;
            }
            BlockPos pos = BlockPos.containing(hole.pos().subtract(hole.face().getStepX() * 0.01, hole.face().getStepY() * 0.01, hole.face().getStepZ() * 0.01));
            if (level.getBlockState(pos).isAir()) it.remove();
        }
    }
}
