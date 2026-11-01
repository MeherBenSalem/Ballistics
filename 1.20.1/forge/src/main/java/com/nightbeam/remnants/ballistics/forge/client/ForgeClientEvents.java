package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import com.nightbeam.remnants.ballistics.client.render.ViewmodelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Game-bus client listeners: forwards Forge events to the common client hooks. Mouse sensitivity while aiming has no
 * Forge event on 1.20.1; it is the {@code mixin.client.MouseHandlerMixin}.
 */
@Mod.EventBusSubscriber(modid = RemnantsBallistics.MODID, value = Dist.CLIENT)
public final class ForgeClientEvents {
    private ForgeClientEvents() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            ClientGunHandler.onClientTickPre();
        } else {
            ClientGunHandler.onClientTickPost();
            FxRenderer.tick();
        }
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if ((event.isAttack() || event.isUseItem()) && ClientGunHandler.blocksVanillaInteraction()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFrame(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) ClientGunHandler.onFrameStart();
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        event.setNewFovModifier(ClientGunHandler.fovModifier(event.getPlayer(), event.getNewFovModifier()));
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        event.setRoll(event.getRoll() + ClientGunHandler.shakeRoll());
        event.setPitch(event.getPitch() + ClientGunHandler.shakePitch());
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id()) && ClientGunHandler.hidesVanillaCrosshair()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (ViewmodelRenderer.renderHand(event.getHand(), event.getItemStack(), event.getPoseStack(), event.getMultiBufferSource(),
                event.getPackedLight(), event.getPartialTick(), event.getEquipProgress())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) FxRenderer.render(event.getCamera(), event.getPoseStack());
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        GunItemRenderer.setRenderingEntity(event.getEntity());
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        GunItemRenderer.setRenderingEntity(null);
    }

    /** Drops third-person animation state for entities that leave the client world. */
    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) RemoteWeaponStates.forget(event.getEntity().getId());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientGunHandler.onLogout();
    }
}
