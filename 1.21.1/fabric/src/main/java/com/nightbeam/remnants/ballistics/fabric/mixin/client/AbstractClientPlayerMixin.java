package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Aim-down-sights zoom (NeoForge: ComputeFovModifierEvent, which also sees the final modifier). */
@Mixin(AbstractClientPlayer.class)
abstract class AbstractClientPlayerMixin {
    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void remnants_ballistics$zoom(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(ClientGunHandler.fovModifier((Player) (Object) this, cir.getReturnValueF()));
    }
}
