package com.nightbeam.remnants.ballistics.forge.mixin.client;

import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Aim sensitivity (NeoForge 1.21: CalculatePlayerTurnEvent; Forge 1.20.1 has no such event). Vanilla turns the player
 * by {@code (0.6 s + 0.2)^3} for slider value {@code s}; {@link ClientGunHandler#mouseSensitivity} returns the slider
 * value that gives the aiming speed, so the turn is scaled by the ratio of the two speeds. {@code @ModifyArg} composes
 * with other mods' changes to the same call.
 */
@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    @ModifyArg(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), index = 0)
    private double remnants_ballistics$aimYaw(double yaw) {
        return yaw * remnants_ballistics$scale();
    }

    @ModifyArg(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), index = 1)
    private double remnants_ballistics$aimPitch(double pitch) {
        return pitch * remnants_ballistics$scale();
    }

    private static double remnants_ballistics$scale() {
        double slider = Minecraft.getInstance().options.sensitivity().get();
        double vanilla = 0.6 * slider + 0.2;
        double aimed = 0.6 * ClientGunHandler.mouseSensitivity(slider) + 0.2;
        return vanilla <= 0.0 ? 1.0 : (aimed * aimed * aimed) / (vanilla * vanilla * vanilla);
    }
}
