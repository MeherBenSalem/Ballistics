package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.ClientHooks;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Per-frame recoil, optic zoom and firing camera shake (Forge: RenderTickEvent, ComputeFov, ComputeCameraAngles). */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void remnants_ballistics$frameStart(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        ClientHooks.frameStart();
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void remnants_ballistics$zoom(Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        if (useFovSetting) cir.setReturnValue(ClientGunHandler.zoomFov(cir.getReturnValueD()));
    }

    /** bobHurt tilts the projection for damage; the shake rides along as extra roll and pitch. */
    @Inject(method = "bobHurt", at = @At("TAIL"))
    private void remnants_ballistics$shake(PoseStack poseStack, float partialTicks, CallbackInfo ci) {
        float[] shake = ClientGunHandler.cameraShake();
        if (shake[0] != 0f) poseStack.mulPose(Axis.ZP.rotationDegrees(shake[0]));
        if (shake[1] != 0f) poseStack.mulPose(Axis.XP.rotationDegrees(shake[1]));
    }
}
