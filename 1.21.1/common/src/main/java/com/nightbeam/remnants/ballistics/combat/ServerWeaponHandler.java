package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.ammo.AmmoInventory;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.BallisticMath;
import com.nightbeam.remnants.ballistics.core.DamageModel;
import com.nightbeam.remnants.ballistics.core.HitZones;
import com.nightbeam.remnants.ballistics.core.ReloadMath;
import com.nightbeam.remnants.ballistics.core.ShotRandom;
import com.nightbeam.remnants.ballistics.core.SpreadModel;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.V3;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.entity.VoidOrbEntity;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.network.FireWeaponPayload;
import com.nightbeam.remnants.ballistics.network.HitConfirmPayload;
import com.nightbeam.remnants.ballistics.network.PingPayload;
import com.nightbeam.remnants.ballistics.network.ReloadEventPayload;
import com.nightbeam.remnants.ballistics.network.RemoteAimPayload;
import com.nightbeam.remnants.ballistics.network.ShotEffectsPayload;
import com.nightbeam.remnants.ballistics.network.WeaponActionPayload;
import com.nightbeam.remnants.ballistics.network.WeaponCorrectionPayload;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.FireMode;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.ReloadType;
import com.nightbeam.remnants.ballistics.weapon.ShotType;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Authoritative weapon simulation. Every client request is validated here; clients only ever predict.
 * Runs on the server thread (payload handlers are main-thread by default).
 */
public final class ServerWeaponHandler {
    /** Server never accepts a spread below this fraction of its own estimate. */
    private static final float SPREAD_FLOOR = 0.75f;
    private static final int PING_INTERVAL_TICKS = 40;

    /**
     * Transient per-player shooting state: never saved, and discarded with the player entity (a respawn or a reconnect
     * creates a new one). Only touched on the server thread.
     */
    private static final Map<Player, ShooterState> STATES = new WeakHashMap<>();

    private ServerWeaponHandler() {}

    public static ShooterState state(Player player) {
        return STATES.computeIfAbsent(player, p -> new ShooterState());
    }

    // ------------------------------------------------------------------------------------------------------------
    // Payload entry points
    // ------------------------------------------------------------------------------------------------------------

    public static void onFire(FireWeaponPayload payload, ServerPlayer player) {
        fire(player, payload);
    }

    public static void onAction(WeaponActionPayload payload, ServerPlayer player) {
        switch (payload.action()) {
            case RELOAD -> startReload(player, payload.slot());
            case CYCLE_FIRE_MODE -> cycleFireMode(player, payload.slot());
            case AIM -> setAiming(player, payload.flag());
            case CHARGE -> setCharging(player, payload.slot(), payload.flag());
        }
    }

    public static void onPong(PingPayload.Pong payload, ServerPlayer player) {
        ShooterState st = state(player);
        if (payload.id() != st.pingId || st.pingSentNanos == 0L) return;
        float rtt = (System.nanoTime() - st.pingSentNanos) / 1_000_000f;
        st.rttMs = st.rttMs * 0.7f + Math.min(rtt, 2000f) * 0.3f;
        st.pingSentNanos = 0L;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Firing
    // ------------------------------------------------------------------------------------------------------------

    public static void fire(ServerPlayer player, FireWeaponPayload payload) {
        if (!player.isAlive() || player.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        ShooterState st = state(player);
        trackHeldItem(player, st, now);

        ItemStack stack = player.getInventory().getSelected();
        if (payload.slot() != player.getInventory().selected || !(stack.getItem() instanceof GunItem gun)) {
            correct(player, payload.slot(), false);
            return;
        }
        // Duplicates and replays are dropped silently: the original was already processed.
        if (payload.sequence() <= st.lastShotSeq) return;
        st.lastShotSeq = payload.sequence();

        WeaponDefinition def = gun.definition();
        WeaponStats stats = GunData.stats(stack, def);
        int ammo = GunData.ammo(stack);

        if (!ServerConfig.isEnabled(def) || ammo <= 0
                || (ServerConfig.get(ServerConfig.USE_DURABILITY) && GunData.isBroken(stack))
                || now + 1 < st.equipReadyTick) {
            correct(player, payload.slot(), false);
            return;
        }
        if (st.reload != null) {
            // Single-load weapons may interrupt loading to fire; magazine swaps must finish.
            if (def.reloadType() == ReloadType.SINGLE) cancelReload(player, st);
            else {
                correct(player, payload.slot(), false);
                return;
            }
        }
        if (def.isCharged()) {
            int chargeTicks = Math.round(stats.get(Stat.CHARGE_TIME));
            if (st.chargeStartTick < 0 || now - st.chargeStartTick < chargeTicks - 2) {
                correct(player, payload.slot(), false);
                return;
            }
        }
        // Bursts are fired at the weapon's RPM; the pause between bursts is client pacing, so only the raw rate is enforced.
        if (!st.limiter.tryFire(now, stats.ticksPerShot())) {
            correct(player, payload.slot(), false);
            return;
        }

        if (player.isSprinting() && !ServerConfig.get(ServerConfig.ALLOW_SPRINT_FIRING)) player.setSprinting(false);

        // Aim: accept the client's view (it includes recoil the server never sees) within a deviation bound.
        float yaw = payload.yaw();
        float pitch = payload.pitch();
        double maxDev = ServerConfig.get(ServerConfig.MAX_AIM_DEVIATION);
        if (!Float.isFinite(yaw) || !Float.isFinite(pitch)
                || Math.abs(Mth.wrapDegrees(yaw - player.getYRot())) > maxDev || Math.abs(pitch - player.getXRot()) > maxDev) {
            yaw = player.getYRot();
            pitch = player.getXRot();
        }
        pitch = Mth.clamp(pitch, -90f, 90f);

        GunData.setAmmo(stack, ammo - 1);
        if (ServerConfig.get(ServerConfig.USE_DURABILITY) && !player.getAbilities().instabuild) GunData.wear(stack, 1);

        // Spread: never below the server's own estimate (scaled by a tolerance for movement-estimate differences).
        float secondsSinceLast = (now - st.lastShotTick) / 20f;
        st.bloom = SpreadModel.recoverBloom(stats, st.bloom, secondsSinceLast);
        st.sprayCount = secondsSinceLast > 0.35f ? 0 : st.sprayCount + 1;
        float serverSpread = SpreadModel.spread(stats, st.adsProgress(now, stats.get(Stat.ADS_TIME)), movement(player), st.bloom, 1f);
        float spread = Float.isFinite(payload.clientSpread()) ? Math.max(payload.clientSpread(), serverSpread * SPREAD_FLOOR) : serverSpread;
        spread = Math.min(spread, 45f);
        st.bloom = SpreadModel.addBloom(stats, st.bloom);
        st.lastShotTick = now;
        if (def.isCharged()) st.chargeStartTick = -1;

        Vec3 origin = player.getEyePosition();
        V3 forward = BallisticMath.direction(yaw, pitch);
        ShotRandom rng = ShotRandom.forShot(st.spreadSeed, payload.sequence());
        GunAttachments attachments = GunData.attachments(stack);
        boolean suppressed = GunData.isSuppressed(attachments);
        int flags = (suppressed ? ShotEffectsPayload.FLAG_SUPPRESSED : 0) | (GunData.hidesFlash(attachments) ? ShotEffectsPayload.FLAG_HIDE_FLASH : 0);
        List<ShotEffectsPayload.Trace> traces = new ArrayList<>();

        if (def.shotType() == ShotType.PROJECTILE) {
            V3 dir = BallisticMath.spread(forward, spread, rng);
            VoidOrbEntity orb = new VoidOrbEntity(level, player, stats.get(Stat.DAMAGE));
            Vec3 velocity = new Vec3(dir.x(), dir.y(), dir.z()).scale(stats.get(Stat.MUZZLE_VELOCITY) / 20.0);
            orb.setPos(origin.x + dir.x() * 0.6, origin.y - 0.1 + dir.y() * 0.6, origin.z + dir.z() * 0.6);
            orb.setDeltaMovement(velocity);
            level.addFreshEntity(orb);
            flags |= ShotEffectsPayload.FLAG_NO_TRACER;
        } else {
            BulletTracer.Params params = BulletTracer.Params.fromConfig(stats.get(Stat.MAX_RANGE), stats.get(Stat.MUZZLE_VELOCITY),
                    stats.get(Stat.GRAVITY), stats.get(Stat.PENETRATION));
            float rewindTicks = ServerConfig.get(ServerConfig.LAG_COMPENSATION)
                    ? Math.min(st.rttMs, ServerConfig.get(ServerConfig.MAX_LAG_COMPENSATION_MS)) / 50f + 1f : 0f;
            Function<Entity, AABB> boxes = e -> LagCompensation.boxAt(e, now, rewindTicks);
            boolean allHumanoid = ServerConfig.get(ServerConfig.HEADSHOTS_ON_ALL_MOBS);
            HitTally tally = new HitTally();
            int pellets = Math.max(1, stats.getInt(Stat.PELLETS));
            for (int i = 0; i < pellets; i++) {
                V3 dir = BallisticMath.spread(forward, spread, rng);
                BulletTracer.Result result = BulletTracer.trace(level, player, origin, new Vec3(dir.x(), dir.y(), dir.z()), params, rng, boxes, allHumanoid);
                applyHits(player, level, stats, result, def.isCharged(), tally);
                breakBlocks(player, level, result.brokenBlocks());
                traces.add(ShotEffectsPayload.Trace.of(result));
            }
            if (tally.hits > 0) {
                Services.NETWORK.sendToPlayer(player, new HitConfirmPayload(tally.headshot, tally.kill, tally.damage));
            }
        }

        float volume = stats.get(Stat.LOUDNESS) / 16f;
        SoundEvent sound = suppressed ? ModSounds.SUPPRESSED_FIRE.get() : ModSounds.fire(def.sound());
        level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume,
                0.95f + level.random.nextFloat() * 0.1f);
        if (!suppressed) player.gameEvent(GameEvent.PROJECTILE_SHOOT);

        double fxRange = ServerConfig.get(ServerConfig.FX_BROADCAST_RANGE);
        Services.NETWORK.sendToPlayersNear(level, player, origin.x, origin.y, origin.z, fxRange,
                new ShotEffectsPayload(player.getId(), Weapons.indexOf(def), origin, flags, traces));
    }

    private static final class HitTally {
        int hits;
        boolean headshot;
        boolean kill;
        float damage;
    }

    /** @param energy charged energy weapons deal energy damage instead of bullet damage */
    private static void applyHits(ServerPlayer shooter, ServerLevel level, WeaponStats stats, BulletTracer.Result result, boolean energy, HitTally tally) {
        float base = stats.get(Stat.DAMAGE);
        float headshot = (float) ServerConfig.get(ServerConfig.HEADSHOT_MULTIPLIER) * stats.get(Stat.HEADSHOT);
        float body = (float) ServerConfig.get(ServerConfig.BODY_MULTIPLIER);
        float limb = (float) ServerConfig.get(ServerConfig.LIMB_MULTIPLIER);
        boolean ignoreFrames = ServerConfig.get(ServerConfig.IGNORE_INVULNERABILITY_FRAMES);
        double knockback = ServerConfig.get(ServerConfig.KNOCKBACK);
        Vec3 dir = result.points().get(result.points().size() - 1).subtract(result.points().getFirst()).normalize();

        for (BulletTracer.EntityHit hit : result.hits()) {
            Entity target = hit.entity();
            if (target instanceof Player victim && !shooter.canHarmPlayer(victim)) continue;
            float zone = switch (hit.zone()) {
                case HEAD -> headshot;
                case BODY -> body;
                case LIMB -> limb;
            };
            float sideMultiplier = (float) (target instanceof Player ? ServerConfig.get(ServerConfig.PVP_DAMAGE_MULTIPLIER)
                    : ServerConfig.get(ServerConfig.MOB_DAMAGE_MULTIPLIER));
            float damage = base * DamageModel.falloff(stats, hit.distance()) * zone * hit.damageFactor() * sideMultiplier;
            if (damage <= 0f) continue;

            var type = energy ? ModDamageTypes.ENERGY : hit.zone() == HitZones.Zone.HEAD ? ModDamageTypes.BULLET_HEADSHOT : ModDamageTypes.BULLET;
            boolean wasAlive = target.isAlive();
            if (ignoreFrames) target.invulnerableTime = 0;
            boolean damaged = target.hurt(ModDamageTypes.source(level, type, shooter, shooter), damage);
            ModDamageTypes.clearHitInvulnerability(target);
            if (damaged) {
                tally.hits++;
                tally.damage += damage;
                tally.headshot |= hit.zone() == HitZones.Zone.HEAD;
                tally.kill |= wasAlive && !target.isAlive();
                if (knockback > 0 && target instanceof LivingEntity living) {
                    living.knockback(knockback * damage / 10.0, -dir.x, -dir.z);
                }
            }
        }
    }

    private static void breakBlocks(ServerPlayer shooter, ServerLevel level, List<BlockPos> positions) {
        if (positions.isEmpty() || !shooter.getAbilities().mayBuild) return;
        for (BlockPos pos : positions) {
            if (level.mayInteract(shooter, pos)) level.destroyBlock(pos, false, shooter);
        }
    }

    /** Speed estimate from the last tick's movement, relative to walking speed (~0.216 blocks/tick). */
    public static SpreadModel.Movement movement(Player player) {
        double dx = player.getX() - player.xo;
        double dz = player.getZ() - player.zo;
        float speed = (float) (Math.sqrt(dx * dx + dz * dz) / 0.216);
        return new SpreadModel.Movement(speed, player.isSprinting(), player.isCrouching(), !player.onGround() && !player.isPassenger());
    }

    // ------------------------------------------------------------------------------------------------------------
    // Reloading
    // ------------------------------------------------------------------------------------------------------------

    public static void startReload(ServerPlayer player, int slot) {
        ShooterState st = state(player);
        long now = player.serverLevel().getGameTime();
        trackHeldItem(player, st, now);
        ItemStack stack = player.getInventory().getSelected();
        if (slot != player.getInventory().selected || !(stack.getItem() instanceof GunItem gun) || st.reload != null) {
            if (st.reload == null) correct(player, slot, true);
            return;
        }
        WeaponDefinition def = gun.definition();
        WeaponStats stats = GunData.stats(stack, def);
        int ammo = GunData.ammo(stack);
        int magazine = stats.getInt(Stat.MAGAZINE);
        int reserve = AmmoInventory.count(player, def.ammo());
        if (!ReloadMath.canReload(ammo, magazine, def.chamber(), reserve)) {
            correct(player, slot, true);
            return;
        }
        boolean empty = ammo == 0;
        st.chargeStartTick = -1;
        if (def.reloadType() == ReloadType.MAGAZINE) {
            int duration = Math.max(1, stats.getInt(empty ? Stat.EMPTY_RELOAD_TIME : Stat.RELOAD_TIME));
            st.reload = new ShooterState.ReloadState(slot, stack.getItem(), ReloadPhase.MAGAZINE, now + duration, empty);
            broadcastReload(player, ReloadPhase.MAGAZINE, duration, empty);
        } else {
            int duration = Math.max(1, stats.getInt(Stat.EMPTY_RELOAD_TIME) / 2);
            st.reload = new ShooterState.ReloadState(slot, stack.getItem(), ReloadPhase.START, now + duration, empty);
            broadcastReload(player, ReloadPhase.START, duration, empty);
        }
    }

    private static void tickReload(ServerPlayer player, ShooterState st, long now) {
        ShooterState.ReloadState r = st.reload;
        if (r == null) return;
        ItemStack stack = player.getInventory().getSelected();
        if (player.getInventory().selected != r.slot || stack.getItem() != r.item || !(stack.getItem() instanceof GunItem gun)) {
            cancelReload(player, st);
            return;
        }
        if (now < r.phaseEndTick) return;

        WeaponDefinition def = gun.definition();
        WeaponStats stats = GunData.stats(stack, def);
        int magazine = stats.getInt(Stat.MAGAZINE);
        switch (r.phase) {
            case MAGAZINE -> {
                loadMagazine(player, stack, def, magazine);
                finishReload(player, st);
            }
            case START, INSERT -> {
                if (r.phase == ReloadPhase.INSERT) loadOne(player, stack, def, magazine);
                int ammo = GunData.ammo(stack);
                boolean more = ammo < magazine && AmmoInventory.count(player, def.ammo()) > 0;
                if (more) {
                    int duration = Math.max(1, stats.getInt(Stat.RELOAD_TIME));
                    r.phase = ReloadPhase.INSERT;
                    r.phaseEndTick = now + duration;
                    broadcastReload(player, ReloadPhase.INSERT, duration, r.empty);
                } else {
                    int duration = Math.max(1, stats.getInt(Stat.EMPTY_RELOAD_TIME) - stats.getInt(Stat.EMPTY_RELOAD_TIME) / 2);
                    r.phase = ReloadPhase.END;
                    r.phaseEndTick = now + duration;
                    broadcastReload(player, ReloadPhase.END, duration, r.empty);
                }
            }
            case END -> finishReload(player, st);
            default -> st.reload = null;
        }
    }

    private static void loadMagazine(ServerPlayer player, ItemStack stack, WeaponDefinition def, int magazine) {
        int current = GunData.ammo(stack);
        int capacity = ReloadMath.reloadCapacity(magazine, def.chamber(), current);
        if (def.ammo().fullReloadPerItem()) {
            if (AmmoInventory.consume(player, def.ammo(), 1) == 1) GunData.setAmmo(stack, capacity);
            return;
        }
        int wanted = ReloadMath.roundsToLoad(current, capacity, AmmoInventory.count(player, def.ammo()));
        int loaded = AmmoInventory.consume(player, def.ammo(), wanted);
        GunData.setAmmo(stack, current + loaded);
    }

    private static void loadOne(ServerPlayer player, ItemStack stack, WeaponDefinition def, int magazine) {
        int current = GunData.ammo(stack);
        if (current < magazine && AmmoInventory.consume(player, def.ammo(), 1) == 1) GunData.setAmmo(stack, current + 1);
    }

    private static void finishReload(ServerPlayer player, ShooterState st) {
        st.reload = null;
        broadcastReload(player, ReloadPhase.DONE, 0, false);
    }

    public static void cancelReload(ServerPlayer player, ShooterState st) {
        if (st.reload == null) return;
        st.reload = null;
        broadcastReload(player, ReloadPhase.CANCEL, 0, false);
    }

    private static void broadcastReload(ServerPlayer player, ReloadPhase phase, int duration, boolean empty) {
        Services.NETWORK.sendToPlayersTrackingEntityAndSelf(player, new ReloadEventPayload(player.getId(), phase, duration, empty));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Fire mode, aim, charge
    // ------------------------------------------------------------------------------------------------------------

    public static void cycleFireMode(ServerPlayer player, int slot) {
        ItemStack stack = player.getInventory().getSelected();
        if (slot != player.getInventory().selected || !(stack.getItem() instanceof GunItem gun)) return;
        List<FireMode> modes = gun.definition().fireModes();
        if (modes.size() < 2) return;
        FireMode current = GunData.fireMode(stack, gun.definition());
        GunData.setFireMode(stack, modes.get((modes.indexOf(current) + 1) % modes.size()));
    }

    public static void setAiming(ServerPlayer player, boolean aiming) {
        ShooterState st = state(player);
        if (st.aiming == aiming) return;
        st.aiming = aiming;
        st.aimChangeTick = player.serverLevel().getGameTime();
        Services.NETWORK.sendToPlayersTrackingEntity(player, new RemoteAimPayload(player.getId(), aiming, st.chargeStartTick >= 0));
    }

    public static void setCharging(ServerPlayer player, int slot, boolean charging) {
        ShooterState st = state(player);
        ItemStack stack = player.getInventory().getSelected();
        if (slot != player.getInventory().selected || !(stack.getItem() instanceof GunItem gun) || !gun.definition().isCharged()) return;
        if (charging && (GunData.ammo(stack) <= 0 || st.reload != null)) return;
        boolean wasCharging = st.chargeStartTick >= 0;
        st.chargeStartTick = charging ? player.serverLevel().getGameTime() : -1;
        if (charging && !wasCharging) {
            player.serverLevel().playSound(player, player.getX(), player.getEyeY(), player.getZ(), ModSounds.RAILGUN_CHARGE.get(),
                    SoundSource.PLAYERS, 1.5f, 1f);
        }
        Services.NETWORK.sendToPlayersTrackingEntity(player, new RemoteAimPayload(player.getId(), st.aiming, charging));
    }

    // ------------------------------------------------------------------------------------------------------------
    // Ticking and helpers
    // ------------------------------------------------------------------------------------------------------------

    /** New observers learn whether a player is currently aiming so third-person poses are correct immediately. */
    public static void onStartTracking(ServerPlayer viewer, Entity target) {
        if (!(target instanceof ServerPlayer shooter)) return;
        ShooterState st = state(shooter);
        if (st.aiming || st.chargeStartTick >= 0) {
            Services.NETWORK.sendToPlayer(viewer, new RemoteAimPayload(shooter.getId(), st.aiming, st.chargeStartTick >= 0));
        }
    }

    /** Called every server tick for every player (see the loader's server events). */
    public static void tick(ServerPlayer player) {
        ShooterState st = state(player);
        long now = player.serverLevel().getGameTime();
        if (!st.seedSent) {
            st.spreadSeed = new java.security.SecureRandom().nextLong();
            st.seedSent = true;
            Services.NETWORK.sendToPlayer(player, new com.nightbeam.remnants.ballistics.network.ShooterSeedPayload(st.spreadSeed));
        }
        trackHeldItem(player, st, now);
        tickReload(player, st, now);
        WeaponMovement.apply(player, st);
        LagCompensation.record(player, now);
        if (now % PING_INTERVAL_TICKS == player.getId() % PING_INTERVAL_TICKS) {
            st.pingId++;
            st.pingSentNanos = System.nanoTime();
            Services.NETWORK.sendToPlayer(player, new PingPayload(st.pingId));
        }
    }

    /** Detects weapon switches: starts the draw delay and cancels reloads/charges for the previous weapon. */
    private static void trackHeldItem(ServerPlayer player, ShooterState st, long now) {
        int slot = player.getInventory().selected;
        var item = player.getInventory().getSelected().getItem();
        if (slot == st.heldSlot && item == st.heldItem) return;
        st.heldSlot = slot;
        st.heldItem = item;
        st.equipReadyTick = now + ShooterState.EQUIP_TICKS;
        st.chargeStartTick = -1;
        st.sprayCount = 0;
        st.bloom = 0f;
        if (st.reload != null) cancelReload(player, st);
        if (st.aiming) setAiming(player, false);
    }

    /** Sends the authoritative magazine count for {@code slot} after rejecting a predicted action. */
    private static void correct(ServerPlayer player, int slot, boolean cancelReload) {
        if (slot < 0 || slot >= 9) return;
        ItemStack stack = player.getInventory().getItem(slot);
        int ammo = stack.getItem() instanceof GunItem ? GunData.ammo(stack) : 0;
        Services.NETWORK.sendToPlayer(player, new WeaponCorrectionPayload(slot, ammo, cancelReload));
    }
}
