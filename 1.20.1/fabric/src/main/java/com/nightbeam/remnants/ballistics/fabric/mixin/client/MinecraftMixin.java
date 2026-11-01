package com.nightbeam.remnants.ballistics.fabric.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** While a weapon is held, the attack and use keys fire and aim instead of mining, meleeing, using or swinging. */
@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ClientGunHandler.blocksVanillaInteraction()) cir.setReturnValue(false);
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockMining(boolean leftClick, CallbackInfo ci) {
        if (!ClientGunHandler.blocksVanillaInteraction()) return;
        if (gameMode != null) gameMode.stopDestroyBlock();
        ci.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void remnants_ballistics$blockUse(CallbackInfo ci) {
        if (ClientGunHandler.blocksVanillaInteraction()) ci.cancel();
    }
}
