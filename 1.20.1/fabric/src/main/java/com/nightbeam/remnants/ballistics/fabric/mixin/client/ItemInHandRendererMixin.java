package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.client.render.ViewmodelRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** First-person weapons replace the vanilla hand (Forge: RenderHandEvent). */
@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$renderWeapon(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                                  float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack,
                                                  MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (ViewmodelRenderer.renderHand(hand, stack, partialTicks, equippedProgress, poseStack, buffers, light)) ci.cancel();
    }
}
