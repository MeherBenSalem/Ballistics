package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.client.render.ViewmodelRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** First-person weapon viewmodel in place of the vanilla held item (NeoForge: RenderHandEvent). */
@Mixin(ItemInHandRenderer.class)
abstract class ItemInHandRendererMixin {
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$viewmodel(AbstractClientPlayer player, float partialTick, float xRot, InteractionHand hand, float attack,
                                               ItemStack stack, float equipProgress, PoseStack poseStack, SubmitNodeCollector collector,
                                               int light, CallbackInfo ci) {
        if (ViewmodelRenderer.renderHand(hand, stack, poseStack, collector, light, partialTick, equipProgress)) ci.cancel();
    }
}
