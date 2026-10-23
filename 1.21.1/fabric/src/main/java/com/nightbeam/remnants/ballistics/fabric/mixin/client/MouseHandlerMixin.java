package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Aim sensitivity (NeoForge: CalculatePlayerTurnEvent). The first option read in turnPlayer is the sensitivity slider. */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object remnants_ballistics$aimSensitivity(Object sensitivity) {
        return ClientGunHandler.mouseSensitivity((Double) sensitivity);
    }
}
