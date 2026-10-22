package com.nightbeam.remnants.ballistics.smoketest.mixin;

import com.nightbeam.remnants.ballistics.smoketest.ClientSmokeTest;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Screenshots are taken once a whole frame, GUI included, has been drawn. */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void remnants_ballistics_smoketest$afterFrame(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        ClientSmokeTest.afterFrame();
    }
}
