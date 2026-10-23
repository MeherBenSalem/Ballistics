package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.hud.WeaponHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The weapon HUD draws right after the crosshair layer, below the hotbar (NeoForge: a GUI layer above
 * {@code CROSSHAIR}), and replaces the vanilla crosshair while it shows its own.
 */
@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$replaceCrosshair(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (!ClientGunHandler.hidesVanillaCrosshair()) return;
        WeaponHud.INSTANCE.render(graphics, delta);
        ci.cancel();
    }

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    private void remnants_ballistics$weaponHud(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        WeaponHud.INSTANCE.render(graphics, delta);
    }
}
