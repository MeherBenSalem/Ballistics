package com.nightbeam.remnants.ballistics.client.network;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.fx.ClientEffects;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.network.HitConfirmPayload;
import com.nightbeam.remnants.ballistics.network.PingPayload;
import com.nightbeam.remnants.ballistics.network.ReloadEventPayload;
import com.nightbeam.remnants.ballistics.network.RemoteAimPayload;
import com.nightbeam.remnants.ballistics.network.ShooterSeedPayload;
import com.nightbeam.remnants.ballistics.network.ShotEffectsPayload;
import com.nightbeam.remnants.ballistics.network.WeaponCorrectionPayload;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-bound payload handlers. Only ever loaded on the physical client (see {@code ModNetwork}). */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void onPing(PingPayload payload, IPayloadContext context) {
        PacketDistributor.sendToServer(payload.pong());
    }

    public static void onSeed(ShooterSeedPayload payload, IPayloadContext context) {
        ClientWeaponState.INSTANCE.seed = payload.seed();
        ClientWeaponState.INSTANCE.hasSeed = true;
    }

    public static void onShotEffects(ShotEffectsPayload payload, IPayloadContext context) {
        ClientEffects.onRemoteShot(payload);
    }

    public static void onHitConfirm(HitConfirmPayload payload, IPayloadContext context) {
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        s.hitMarkerNanos = System.nanoTime();
        s.hitMarkerHeadshot = payload.headshot();
        s.hitMarkerKill = payload.kill();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientConfig.get(ClientConfig.HITMARKERS)) return;
        float volume = (float) ClientConfig.get(ClientConfig.HITMARKER_VOLUME);
        var sound = payload.kill() ? ModSounds.KILL.get() : payload.headshot() ? ModSounds.HEADSHOT.get() : ModSounds.HITMARKER.get();
        player.level().playLocalSound(player.getX(), player.getEyeY(), player.getZ(), sound, SoundSource.PLAYERS, volume, 1f, false);
    }

    public static void onReloadEvent(ReloadEventPayload payload, IPayloadContext context) {
        LocalPlayer local = Minecraft.getInstance().player;
        if (local == null) return;
        if (payload.entityId() == local.getId()) {
            ClientGunHandler.onReloadPhase(payload.phase(), payload.durationTicks(), payload.empty());
            return;
        }
        Entity entity = local.level().getEntity(payload.entityId());
        if (!(entity instanceof Player other) || !(other.getMainHandItem().getItem() instanceof GunItem gun)) return;
        RemoteWeaponStates.State state = RemoteWeaponStates.get(entity);
        ResourceLocation set = GunVisualsLoader.INSTANCE.get(gun.definition()).animations();
        float seconds = payload.durationTicks() / 20f;
        String clip = switch (payload.phase()) {
            case MAGAZINE -> payload.empty() ? "reload_empty" : "reload";
            case START -> "reload_start";
            case INSERT -> "reload_insert";
            case END -> "reload_end";
            default -> null;
        };
        if (clip == null) {
            if (payload.phase() == com.nightbeam.remnants.ballistics.combat.ReloadPhase.CANCEL || state.action.isHolding()) state.action.stop();
            return;
        }
        if (GunAnimations.INSTANCE.get(set, clip) == null) clip = "reload";
        state.action.play(set, clip, seconds);
    }

    public static void onRemoteAim(RemoteAimPayload payload, IPayloadContext context) {
        RemoteWeaponStates.State state = RemoteWeaponStates.get(payload.entityId());
        state.aiming = payload.aiming();
        state.charging = payload.charging();
    }

    public static void onCorrection(WeaponCorrectionPayload payload, IPayloadContext context) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || payload.slot() < 0 || payload.slot() >= 9) return;
        ItemStack stack = player.getInventory().getItem(payload.slot());
        if (stack.getItem() instanceof GunItem) GunData.setAmmo(stack, payload.ammo());
        if (payload.cancelReload()) ClientGunHandler.onReloadPhase(com.nightbeam.remnants.ballistics.combat.ReloadPhase.CANCEL, 0, false);
        ClientWeaponState.INSTANCE.burstRemaining = 0;
    }
}
