package com.nightbeam.remnants.ballistics.smoketest;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Development-only companion mod that drives {@link ClientSmokeTest} in the {@code clientSmokeTest} run. It lives in its
 * own source set and is never packaged into the release jar.
 */
@Mod(SmokeTestMod.MODID)
public final class SmokeTestMod {
    public static final String MODID = "remnants_ballistics_smoketest";

    public SmokeTestMod() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientSmokeTest.tick();
        });
        // END of the render tick: after the frame, GUI included.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.RenderTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientSmokeTest.afterFrame();
        });
    }
}
