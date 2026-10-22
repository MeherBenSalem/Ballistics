package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells the weapon item renderer which entity is being drawn, for third-person animation (Forge: RenderLivingEvent). */
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;"
            + "Lnet/minecraft/client/renderer/MultiBufferSource;I)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void remnants_ballistics$begin(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                                           int light, CallbackInfo ci) {
        GunItemRenderer.beginEntity(entity);
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void remnants_ballistics$end(LivingEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
                                         int light, CallbackInfo ci) {
        GunItemRenderer.endEntity();
    }
}
