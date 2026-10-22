package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Aim sensitivity, scaled by optic zoom (NeoForge: CalculatePlayerTurnEvent). */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    /** The first option read in turnPlayer is the mouse sensitivity slider. */
    @ModifyExpressionValue(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object remnants_ballistics$aimSensitivity(Object sensitivity) {
        return sensitivity instanceof Double value ? (Object) ClientGunHandler.adjustSensitivity(value) : sensitivity;
    }
}
