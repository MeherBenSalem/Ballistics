package com.nightbeam.remnants.ballistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.client.fx.ClientEffects;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Client lifecycle hooks. Each loader calls these from its own events or mixins; rendering hooks live in the
 * classes they belong to ({@code ViewmodelRenderer.renderHand}, {@code GunItemRenderer}, {@code GunArmPose},
 * {@code WeaponHud}) and camera hooks in {@link ClientGunHandler}.
 */
public final class ClientHooks {
    private ClientHooks() {}

    public static void tickStart() {
        ClientGunHandler.clientTickStart();
    }

    public static void tickEnd() {
        ClientGunHandler.clientTickEnd();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) ClientEffects.tick(mc.level);
    }

    public static void frameStart() {
        ClientGunHandler.frameStart();
    }

    /** World effects (tracers, bullet holes, third-person flashes), after particles. */
    public static void renderLevelEffects(PoseStack poseStack, Camera camera) {
        FxRenderer.render(poseStack, camera);
    }

    public static void loggedOut() {
        ClientGunHandler.loggedOut();
    }

    public static void entityLeftLevel(Entity entity) {
        RemoteWeaponStates.forget(entity.getId());
    }
}
