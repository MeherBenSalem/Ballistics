package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    /** Start of every frame: smooth recoil (NeoForge: RenderFrameEvent.Pre). */
    @Inject(method = "render", at = @At("HEAD"))
    private void remnants_ballistics$frameStart(CallbackInfo ci) {
        ClientGunHandler.onFrameStart();
    }

    /** Recoil camera shake, applied with the hurt camera tilt (NeoForge: ViewportEvent.ComputeCameraAngles). */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobHurt("
            + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void remnants_ballistics$cameraShake(CallbackInfo ci, @Local(ordinal = 0) PoseStack bobStack) {
        float roll = ClientGunHandler.shakeRoll();
        float pitch = ClientGunHandler.shakePitch();
        if (roll != 0f) bobStack.rotate(Axis.ZP.rotationDegrees(roll));
        if (pitch != 0f) bobStack.rotate(Axis.XP.rotationDegrees(pitch));
    }
}
