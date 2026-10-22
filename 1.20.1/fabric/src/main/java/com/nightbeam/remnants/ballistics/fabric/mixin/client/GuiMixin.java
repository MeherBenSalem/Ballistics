package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The weapon HUD draws its own spread-aware crosshair (Forge: RenderGuiOverlayEvent on the crosshair overlay). */
@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$hideCrosshair(GuiGraphics graphics, CallbackInfo ci) {
        if (ClientGunHandler.hidesVanillaCrosshair()) ci.cancel();
    }
}
