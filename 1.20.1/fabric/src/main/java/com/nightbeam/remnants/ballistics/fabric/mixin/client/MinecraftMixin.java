package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No melee, mining, item use or arm swing while a weapon is held (Forge: InteractionKeyMappingTriggered). */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ClientGunHandler.blocksVanillaInteraction()) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockMining(boolean leftClick, CallbackInfo ci) {
        if (ClientGunHandler.blocksVanillaInteraction()) ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockUse(CallbackInfo ci) {
        if (ClientGunHandler.blocksVanillaInteraction()) ci.cancel();
    }
}
