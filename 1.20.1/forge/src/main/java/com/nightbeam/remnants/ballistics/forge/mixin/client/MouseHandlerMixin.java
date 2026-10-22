package com.nightbeam.remnants.ballistics.forge.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Aim sensitivity, scaled by optic zoom. Forge 47 ships plain Mixin (no MixinExtras), hence a redirect. */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    /** The first option read in turnPlayer is the mouse sensitivity slider. */
    @Redirect(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
    private Object remnants_ballistics$aimSensitivity(OptionInstance<?> option) {
        Object sensitivity = option.get();
        return sensitivity instanceof Double value ? (Object) ClientGunHandler.adjustSensitivity(value) : sensitivity;
    }
}
