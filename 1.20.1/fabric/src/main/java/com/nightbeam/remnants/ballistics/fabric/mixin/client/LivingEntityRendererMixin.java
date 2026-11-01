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

/** Tells the weapon item renderer which entity is being drawn (NeoForge: RenderLivingEvent.Pre/Post). */
@Mixin(LivingEntityRenderer.class)
abstract class LivingEntityRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void remnants_ballistics$begin(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                                           int light, CallbackInfo ci) {
        GunItemRenderer.setRenderingEntity(entity);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void remnants_ballistics$end(LivingEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                                         int light, CallbackInfo ci) {
        GunItemRenderer.setRenderingEntity(null);
    }
}
