package com.nightbeam.remnants.ballistics.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

/** Drops third-person animation state for entities that leave the client world. */
@EventBusSubscriber(modid = RemnantsBallistics.MODID, value = Dist.CLIENT)
public final class ClientEntityEvents {
    private ClientEntityEvents() {}

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) RemoteWeaponStates.forget(event.getEntity().getId());
    }
}
