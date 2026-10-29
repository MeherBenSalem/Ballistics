package com.nightbeam.remnants.ballistics.client;

import com.nightbeam.remnants.ballistics.ammo.AmmoInventory;
import com.nightbeam.remnants.ballistics.client.anim.AnimationPlayer;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.fx.ClientEffects;
import com.nightbeam.remnants.ballistics.client.render.GunVisuals;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.combat.ReloadPhase;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.RecoilModel;
import com.nightbeam.remnants.ballistics.core.ReloadMath;
import com.nightbeam.remnants.ballistics.core.ShotRandom;
import com.nightbeam.remnants.ballistics.core.SpreadModel;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.network.FireWeaponPayload;
import com.nightbeam.remnants.ballistics.network.WeaponActionPayload;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.ActionType;
import com.nightbeam.remnants.ballistics.weapon.FireMode;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.ReloadType;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Local player input, prediction and camera effects. The client fires immediately (sound, flash, tracer, ammo,
 * recoil) and the server validates; corrections arrive via {@code WeaponCorrectionPayload} and slot sync.
 * <p>
 * The public {@code on*} and query methods are hooks: each loader calls them from its events (NeoForge) or its
 * callbacks and mixins (Fabric).
 */
public final class ClientGunHandler {
    private static final ClientWeaponState S = ClientWeaponState.INSTANCE;
    private static long lastFrameNanos;
    private static float shake;

    private ClientGunHandler() {}

    // ------------------------------------------------------------------------------------------------------------
    // Input capture
    // ------------------------------------------------------------------------------------------------------------

    /** Start of the client tick, before vanilla processes key bindings, so weapon clicks become shots, not attacks. */
    public static void onClientTickPre() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof GunItem)) return;
        while (mc.options.keyAttack.consumeClick()) S.pendingClick = true;
        while (mc.options.keyUse.consumeClick()) {
            if (ClientConfig.get(ClientConfig.TOGGLE_AIM)) S.aimToggled = !S.aimToggled;
        }
    }

    /** True while a weapon is held: vanilla mining, melee, item use and arm swings are blocked. */
    public static boolean blocksVanillaInteraction() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.getMainHandItem().getItem() instanceof GunItem;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Tick logic
    // ------------------------------------------------------------------------------------------------------------

    /** End of the client tick. */
    public static void onClientTickPost() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        long now = mc.level.getGameTime();
        S.prevAdsProgress = S.adsProgress;
        S.prevSprintProgress = S.sprintProgress;

        ItemStack stack = player.getMainHandItem();
        if (player.getInventory().getSelectedSlot() != S.slot || stack.getItem() != S.item) onWeaponSwitched(player, stack, now);

        S.action.tickSounds(player, true);
        S.fire.tickSounds(player, true);

        if (!(stack.getItem() instanceof GunItem gun)) {
            S.adsProgress = Math.max(0f, S.adsProgress - 0.25f);
            S.sprintProgress = 0f;
            return;
        }
        WeaponDefinition def = gun.definition();
        WeaponStats stats = GunData.stats(stack, def);
        GunVisuals visuals = GunVisualsLoader.INSTANCE.get(def);
        boolean canAct = mc.gui.screen() == null && player.isAlive() && !player.isSpectator();

        while (Keybinds.RELOAD.consumeClick()) if (canAct) requestReload(player, stack, def, stats, visuals);
        while (Keybinds.FIRE_MODE.consumeClick()) if (canAct) cycleFireMode(player, stack, def);
        while (Keybinds.INSPECT.consumeClick()) if (canAct) inspect(player, visuals);

        // Aiming: sprinting and magazine reloads override it.
        boolean wantAim = canAct && (ClientConfig.get(ClientConfig.TOGGLE_AIM) ? S.aimToggled : mc.options.keyUse.isDown());
        boolean blocked = player.isSprinting() || S.reloadPhase == ReloadPhase.MAGAZINE || S.action.isPlaying("inspect");
        setAiming(player, wantAim && !blocked);
        S.adsProgress = approach(S.adsProgress, S.aiming ? 1f : 0f, 1f / Math.max(1f, stats.get(Stat.ADS_TIME)));
        S.sprintProgress = approach(S.sprintProgress, player.isSprinting() && !S.aiming ? 1f : 0f, 0.25f);

        boolean triggerDown = canAct && mc.options.keyAttack.isDown();
        boolean clicked = S.pendingClick && canAct;
        S.pendingClick = false;
        handleTrigger(player, stack, def, stats, visuals, triggerDown, clicked, now);
        S.triggerWasDown = triggerDown;

        if (!S.idle.isActive()) S.idle.play(visuals.animations(), "idle", 0f);
    }

    private static void onWeaponSwitched(LocalPlayer player, ItemStack stack, long now) {
        if (S.aiming) setAiming(player, false);
        if (S.chargeStartTick >= 0) sendAction(WeaponActionPayload.Action.CHARGE, false);
        S.resetWeapon();
        S.slot = player.getInventory().getSelectedSlot();
        S.item = stack.getItem();
        S.equipReadyTick = now + com.nightbeam.remnants.ballistics.combat.ShooterState.EQUIP_TICKS;
        S.nextShotTick = now;
        if (stack.getItem() instanceof GunItem gun) {
            GunVisuals visuals = GunVisualsLoader.INSTANCE.get(gun.definition());
            S.idle.play(visuals.animations(), "idle", 0f);
            S.action.play(visuals.animations(), "equip", com.nightbeam.remnants.ballistics.combat.ShooterState.EQUIP_TICKS / 20f);
            playLocal(player, ModSounds.EQUIP.get(), 0.6f, 1f, true);
        } else {
            S.idle.stop();
        }
    }

    private static void handleTrigger(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals,
                                      boolean triggerDown, boolean clicked, long now) {
        if (def.isCharged()) {
            handleCharge(player, stack, def, stats, visuals, triggerDown, now);
            return;
        }
        FireMode mode = GunData.fireMode(stack, def);
        if (S.nextShotTick < now - 1) S.nextShotTick = now;
        switch (mode) {
            case SEMI -> {
                if (clicked) tryFire(player, stack, def, stats, visuals, now, true);
            }
            case AUTO -> {
                int shots = 0;
                while (triggerDown && shots++ < 4 && S.nextShotTick <= now + 1.0E-6) {
                    if (!fireOnce(player, stack, def, stats, visuals, now, clicked && shots == 1)) break;
                    S.nextShotTick += stats.ticksPerShot();
                }
            }
            case BURST -> {
                if (clicked && S.burstRemaining == 0 && S.nextShotTick <= now + 1.0E-6) S.burstRemaining = def.burstCount();
                int shots = 0;
                while (S.burstRemaining > 0 && shots++ < 4 && S.nextShotTick <= now + 1.0E-6) {
                    if (!fireOnce(player, stack, def, stats, visuals, now, shots == 1 && clicked)) {
                        S.burstRemaining = 0;
                        break;
                    }
                    S.burstRemaining--;
                    S.nextShotTick += stats.ticksPerShot();
                    if (S.burstRemaining == 0) S.nextShotTick += def.burstCooldownTicks();
                }
            }
        }
    }

    private static void tryFire(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals, long now, boolean click) {
        if (S.nextShotTick > now + 1.0E-6) return;
        if (fireOnce(player, stack, def, stats, visuals, now, click)) S.nextShotTick += stats.ticksPerShot();
    }

    private static void handleCharge(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals,
                                     boolean triggerDown, long now) {
        int chargeTicks = Math.round(stats.get(Stat.CHARGE_TIME));
        if (triggerDown && GunData.ammo(stack) > 0 && S.reloadPhase == null && now >= S.equipReadyTick && S.nextShotTick <= now) {
            if (S.chargeStartTick < 0) {
                S.chargeStartTick = now;
                sendAction(WeaponActionPayload.Action.CHARGE, true);
                playLocal(player, ModSounds.RAILGUN_CHARGE.get(), 1f, 1f, false);
                S.action.play(visuals.animations(), "charge", chargeTicks / 20f);
            } else if (now - S.chargeStartTick >= chargeTicks) {
                if (fireOnce(player, stack, def, stats, visuals, now, true)) S.nextShotTick = now + stats.ticksPerShot();
                S.chargeStartTick = -1;
            }
        } else if (S.chargeStartTick >= 0) {
            S.chargeStartTick = -1;
            sendAction(WeaponActionPayload.Action.CHARGE, false);
            if (S.action.isPlaying("charge")) S.action.stop();
        } else if (triggerDown && !S.triggerWasDown && GunData.ammo(stack) <= 0) {
            dryFire(player, stack, def, stats, visuals, now);
        }
    }

    /**
     * Fires one predicted shot. Returns false (and plays feedback) if the weapon cannot fire right now.
     */
    private static boolean fireOnce(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals,
                                    long now, boolean freshPress) {
        int ammo = GunData.ammo(stack);
        if (S.reloadPhase != null) {
            if (def.reloadType() == ReloadType.SINGLE && ammo > 0) {
                S.reloadPhase = null;
                S.action.stop();
            } else {
                return false;
            }
        }
        if (now < S.equipReadyTick || !ServerConfig.isEnabled(def)) return false;
        if (ServerConfig.get(ServerConfig.USE_DURABILITY) && GunData.isBroken(stack)) {
            if (freshPress) playLocal(player, ModSounds.JAMMED.get(), 0.8f, 1f, true);
            return false;
        }
        if (ammo <= 0) {
            if (freshPress) dryFire(player, stack, def, stats, visuals, now);
            return false;
        }
        if (player.isSprinting() && !ServerConfig.get(ServerConfig.ALLOW_SPRINT_FIRING)) {
            player.setSprinting(false);
            return false;
        }
        if (S.sprintProgress > 0.35f && !ServerConfig.get(ServerConfig.ALLOW_SPRINT_FIRING)) return false;

        int sequence = S.nextSequence++;
        float secondsSince = (now - S.lastShotTick) / 20f;
        S.bloom = SpreadModel.recoverBloom(stats, S.bloom, secondsSince);
        S.sprayCount = secondsSince > 0.35f ? 0 : S.sprayCount + 1;
        float ads = adsProgress(1f);
        float spread = SpreadModel.spread(stats, ads, ServerWeaponHandler.movement(player), S.bloom, 1f);
        S.bloom = SpreadModel.addBloom(stats, S.bloom);

        float yaw = player.getYRot();
        float pitch = player.getXRot();
        Services.NETWORK.sendToServer(new FireWeaponPayload(player.getInventory().getSelectedSlot(), sequence, yaw, pitch, spread));
        GunData.setAmmo(stack, ammo - 1);
        if (ServerConfig.get(ServerConfig.USE_DURABILITY) && !player.getAbilities().instabuild) GunData.wear(stack, 1);

        // Recoil: random component from a local stream (not security relevant), applied smoothly over frames.
        RecoilModel.Kick kick = RecoilModel.kick(stats, S.sprayCount, ads, player.isCrouching(),
                new ShotRandom(ShotRandom.mix(S.seed + sequence * 7919L)), 1f);
        S.pendingPitch += kick.pitch();
        S.pendingYaw += kick.yaw();
        S.recoveryPitch += kick.pitch() * stats.get(Stat.RECOIL_RECOVERY);
        S.kickVelocity += stats.get(Stat.KICK) * (1f - 0.4f * ads) * 9f;
        shake = Math.min(1.5f, shake + stats.get(Stat.RECOIL_VERTICAL) * 0.15f);
        S.lastShotTick = now;
        S.lastShotNanos = System.nanoTime();

        ClientEffects.predictShot(player, stack, def, stats, visuals, sequence, spread, yaw, pitch);

        boolean empty = ammo - 1 == 0;
        S.fire.play(visuals.animations(), empty && hasClip(visuals.animations(), "fire_last") ? "fire_last" : "fire", 0f);
        if (!empty && (def.action() == ActionType.PUMP || def.action() == ActionType.BOLT)) {
            // Cycle the action for the next round; the server enforces the matching fire rate.
            S.action.play(visuals.animations(), "cycle", (float) (stats.ticksPerShot() / 20.0) * 0.9f);
        }
        return true;
    }

    private static void dryFire(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals, long now) {
        S.lastDryFireTick = now;
        playLocal(player, ModSounds.DRY_FIRE.get(), 0.8f, 1f, true);
        // Auto-reload on an empty trigger pull when ammunition is available.
        requestReload(player, stack, def, stats, visuals);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------------------------------------------

    public static void requestReload(LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats, GunVisuals visuals) {
        if (S.reloadPhase != null || S.chargeStartTick >= 0) return;
        int ammo = GunData.ammo(stack);
        int reserve = AmmoInventory.count(player, def.ammo());
        if (!ReloadMath.canReload(ammo, stats.getInt(Stat.MAGAZINE), def.chamber(), reserve)) return;
        boolean empty = ammo == 0;
        S.burstRemaining = 0;
        if (def.reloadType() == ReloadType.MAGAZINE) {
            S.reloadPhase = ReloadPhase.MAGAZINE;
            int ticks = stats.getInt(empty ? Stat.EMPTY_RELOAD_TIME : Stat.RELOAD_TIME);
            playClip(S.action, visuals.animations(), empty ? "reload_empty" : "reload", "reload", ticks / 20f);
        } else {
            S.reloadPhase = ReloadPhase.START;
            playClip(S.action, visuals.animations(), "reload_start", "reload", stats.getInt(Stat.EMPTY_RELOAD_TIME) / 2 / 20f);
        }
        S.reloadPredicted = true;
        sendAction(WeaponActionPayload.Action.RELOAD, false);
    }

    /** Server-confirmed reload phase for the local player. */
    public static void onReloadPhase(ReloadPhase phase, int durationTicks, boolean empty) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof GunItem gun)) {
            S.reloadPhase = null;
            return;
        }
        Identifier set = GunVisualsLoader.INSTANCE.get(gun.definition()).animations();
        float seconds = durationTicks / 20f;
        switch (phase) {
            case MAGAZINE, START -> {
                if (!S.reloadPredicted || S.reloadPhase == null) {
                    if (phase == ReloadPhase.MAGAZINE) playClip(S.action, set, empty ? "reload_empty" : "reload", "reload", seconds);
                    else playClip(S.action, set, "reload_start", "reload", seconds);
                }
                S.reloadPhase = phase;
                S.reloadPredicted = false;
            }
            case INSERT -> {
                S.reloadPhase = phase;
                playClip(S.action, set, "reload_insert", "reload", seconds);
            }
            case END -> {
                S.reloadPhase = phase;
                playClip(S.action, set, "reload_end", "reload", seconds);
            }
            case DONE -> {
                S.reloadPhase = null;
                S.reloadPredicted = false;
                if (S.action.isHolding()) S.action.stop();
            }
            case CANCEL -> {
                S.reloadPhase = null;
                S.reloadPredicted = false;
                if (!S.action.isPlaying("equip")) S.action.stop();
            }
        }
    }

    private static void cycleFireMode(LocalPlayer player, ItemStack stack, WeaponDefinition def) {
        if (def.fireModes().size() < 2) return;
        FireMode current = GunData.fireMode(stack, def);
        GunData.setFireMode(stack, def.fireModes().get((def.fireModes().indexOf(current) + 1) % def.fireModes().size()));
        S.burstRemaining = 0;
        playLocal(player, ModSounds.FIRE_MODE.get(), 0.7f, 1f, true);
        sendAction(WeaponActionPayload.Action.CYCLE_FIRE_MODE, false);
    }

    private static void inspect(LocalPlayer player, GunVisuals visuals) {
        if (S.aiming || S.reloadPhase != null || S.action.isActive()) return;
        S.action.play(visuals.animations(), "inspect", 0f);
        playLocal(player, ModSounds.INSPECT.get(), 0.6f, 1f, true);
    }

    private static void setAiming(LocalPlayer player, boolean aiming) {
        if (S.aiming == aiming) return;
        S.aiming = aiming;
        sendAction(WeaponActionPayload.Action.AIM, aiming);
        playLocal(player, aiming ? ModSounds.AIM_IN.get() : ModSounds.AIM_OUT.get(), 0.35f, 1f, true);
    }

    private static void sendAction(WeaponActionPayload.Action action, boolean flag) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Services.NETWORK.sendToServer(new WeaponActionPayload(action, player.getInventory().getSelectedSlot(), flag));
    }

    private static boolean hasClip(Identifier set, String clip) {
        return GunAnimations.INSTANCE.get(set, clip) != null;
    }

    private static void playClip(AnimationPlayer player, Identifier set, String clip, String fallback, float seconds) {
        player.play(set, hasClip(set, clip) ? clip : fallback, seconds);
    }

    private static void playLocal(Player player, SoundEvent sound, float volume, float pitch, boolean mechanical) {
        float scale = mechanical ? (float) ClientConfig.get(ClientConfig.MECHANICAL_VOLUME) : 1f;
        player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume * scale, pitch, false);
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    /** Smoothed aim progress for rendering and spread (ease in/out). */
    public static float adsProgress(float partialTick) {
        float t = Mth.lerp(partialTick, S.prevAdsProgress, S.adsProgress);
        return t * t * (3f - 2f * t);
    }

    public static float sprintProgress(float partialTick) {
        float t = Mth.lerp(partialTick, S.prevSprintProgress, S.sprintProgress);
        return t * t * (3f - 2f * t);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Camera: recoil, zoom, sensitivity, shake
    // ------------------------------------------------------------------------------------------------------------

    /** Start of every rendered frame: applies recoil smoothly and steps the weapon-kick spring. */
    public static void onFrameStart() {
        long nanos = System.nanoTime();
        float dt = lastFrameNanos == 0L ? 0f : Math.min(0.1f, (nanos - lastFrameNanos) / 1.0E9f);
        lastFrameNanos = nanos;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || dt <= 0f || Minecraft.getInstance().isPaused()) return;

        float apply = Math.min(1f, dt * 28f);
        float dPitch = S.pendingPitch * apply;
        float dYaw = S.pendingYaw * apply;
        S.pendingPitch -= dPitch;
        S.pendingYaw -= dYaw;
        float recover = 0f;
        long sinceShot = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() - S.lastShotTick : 100;
        if (sinceShot > 3 && S.recoveryPitch > 0f) {
            recover = Math.min(S.recoveryPitch, S.recoveryPitch * dt * 7f + dt * 0.4f);
            S.recoveryPitch -= recover;
        }
        if (dPitch != 0f || dYaw != 0f || recover != 0f) {
            // Entity#turn scales its input by 0.15 and also shifts the previous-frame rotation, keeping motion smooth.
            player.turn(dYaw / 0.15, (-dPitch + recover) / 0.15);
        }

        // Critically damped spring for the visual weapon kick.
        float stiffness = 180f;
        float damping = 2f * (float) Math.sqrt(stiffness);
        S.kickVelocity += (-stiffness * S.kick - damping * S.kickVelocity) * dt;
        S.kick += S.kickVelocity * dt;
        shake = Math.max(0f, shake - dt * 4f);
    }

    /** Aim-down-sights zoom: the FOV modifier vanilla computed for {@code player}, scaled by the weapon's zoom. */
    public static float fovModifier(Player player, float modifier) {
        if (!(player.getMainHandItem().getItem() instanceof GunItem gun)) return modifier;
        float ads = adsProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (ads <= 0f) return modifier;
        float zoom = GunData.stats(player.getMainHandItem(), gun.definition()).get(Stat.ZOOM);
        return modifier / Mth.lerp(ads, 1f, zoom);
    }

    /** Aim sensitivity: the mouse sensitivity slider value vanilla turns the player with, adjusted while aiming. */
    public static double mouseSensitivity(double s) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getMainHandItem().getItem() instanceof GunItem gun)) return s;
        float ads = adsProgress(1f);
        if (ads <= 0f) return s;
        double factor = ClientConfig.get(ClientConfig.AIM_SENSITIVITY);
        if (ClientConfig.get(ClientConfig.SCALE_SENSITIVITY_WITH_ZOOM)) {
            factor /= GunData.stats(player.getMainHandItem(), gun.definition()).get(Stat.ZOOM);
        }
        factor = Mth.lerp(ads, 1.0, factor);
        // Vanilla maps the slider s to speed (0.6s + 0.2)^3, so scale inside the cube root to get a linear factor.
        double scaled = (Math.cbrt(factor) * (0.6 * s + 0.2) - 0.2) / 0.6;
        return Math.max(-1.0 / 3.0, scaled);
    }

    /** Recoil camera shake to add to the camera roll this frame, in degrees. */
    public static float shakeRoll() {
        if (shake <= 0f) return 0f;
        float amount = shake * (float) ClientConfig.get(ClientConfig.CAMERA_SHAKE) * 0.35f;
        return (float) Math.sin(System.nanoTime() / 1.0E9 * 61.0) * amount;
    }

    /** Recoil camera shake to add to the camera pitch this frame, in degrees. */
    public static float shakePitch() {
        if (shake <= 0f) return 0f;
        float amount = shake * (float) ClientConfig.get(ClientConfig.CAMERA_SHAKE) * 0.35f;
        return (float) Math.sin(System.nanoTime() / 1.0E9 * 47.0 + 1.3) * amount * 0.5f;
    }

    /** Our HUD draws its own spread-aware crosshair (and hides it while aiming down sights). */
    public static boolean hidesVanillaCrosshair() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.getMainHandItem().getItem() instanceof GunItem
                && (ClientConfig.get(ClientConfig.DYNAMIC_CROSSHAIR) || S.aiming);
    }

    /** Leaving a world or server. */
    public static void onLogout() {
        S.resetAll();
        RemoteWeaponStates.clear();
        ClientEffects.clear();
    }
}
