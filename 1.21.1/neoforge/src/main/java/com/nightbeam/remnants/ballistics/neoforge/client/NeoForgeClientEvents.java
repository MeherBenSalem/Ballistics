package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import com.nightbeam.remnants.ballistics.client.render.ViewmodelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculatePlayerTurnEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

/** Game-bus client listeners: forwards NeoForge events to the common client hooks. */
@EventBusSubscriber(modid = RemnantsBallistics.MODID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {}

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        ClientGunHandler.onClientTickPre();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        ClientGunHandler.onClientTickPost();
        FxRenderer.tick();
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if ((event.isAttack() || event.isUseItem()) && ClientGunHandler.blocksVanillaInteraction()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onFrame(RenderFrameEvent.Pre event) {
        ClientGunHandler.onFrameStart();
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        event.setNewFovModifier(ClientGunHandler.fovModifier(event.getPlayer(), event.getNewFovModifier()));
    }

    @SubscribeEvent
    public static void onPlayerTurn(CalculatePlayerTurnEvent event) {
        event.setMouseSensitivity(ClientGunHandler.mouseSensitivity(event.getMouseSensitivity()));
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        event.setRoll(event.getRoll() + ClientGunHandler.shakeRoll());
        event.setPitch(event.getPitch() + ClientGunHandler.shakePitch());
    }

    @SubscribeEvent
    public static void onRenderLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.CROSSHAIR) && ClientGunHandler.hidesVanillaCrosshair()) event.setCanceled(true);
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
