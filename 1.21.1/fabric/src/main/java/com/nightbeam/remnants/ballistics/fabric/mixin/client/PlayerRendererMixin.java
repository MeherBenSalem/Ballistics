package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.render.GunArmPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Third-person weapon arm poses (NeoForge: IClientItemExtensions#getArmPose). */
@Mixin(PlayerRenderer.class)
abstract class PlayerRendererMixin {
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void remnants_ballistics$weaponPose(AbstractClientPlayer player, InteractionHand hand,
                                                       CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        HumanoidModel.ArmPose pose = GunArmPose.of(player, hand, player.getItemInHand(hand));
        if (pose != null) cir.setReturnValue(pose);
    }
}
