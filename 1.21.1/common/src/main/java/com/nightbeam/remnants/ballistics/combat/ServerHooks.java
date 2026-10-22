package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.network.RemoteAimPayload;
import com.nightbeam.remnants.ballistics.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Server-side lifecycle hooks; each loader calls these from its own events. */
public final class ServerHooks {
    private ServerHooks() {}

    /** After a player's tick: reload timers, charge, rate limiting, ping. */
    public static void playerTick(ServerPlayer player) {
        ServerWeaponHandler.tick(player);
    }

    public static void playerLoggedOut(Player player) {
        LagCompensation.forget(player);
        ServerWeaponHandler.forget(player);
    }

    public static void serverStopped() {
        LagCompensation.clear();
    }

    /** New observers learn whether a player is currently aiming so third-person poses are correct immediately. */
    public static void startTracking(ServerPlayer viewer, Entity target) {
        if (target instanceof ServerPlayer shooter) {
            ShooterState st = ServerWeaponHandler.state(shooter);
            if (st.aiming || st.chargeStartTick >= 0) {
                Services.NETWORK.sendToPlayer(viewer, new RemoteAimPayload(shooter.getId(), st.aiming, st.chargeStartTick >= 0));
            }
        }
    }
}
