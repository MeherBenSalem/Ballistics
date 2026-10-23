package com.nightbeam.remnants.ballistics.smoketest.mixin;

import com.nightbeam.remnants.ballistics.smoketest.FabricSmokeTest;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** End of every frame, with the GUI drawn (NeoForge: RenderFrameEvent.Post). Development only. */
@Mixin(GameRenderer.class)
abstract class GameRendererFrameMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void remnants_ballistics_smoketest$afterFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        FabricSmokeTest.afterFrame();
    }
}
