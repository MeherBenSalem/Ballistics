package com.nightbeam.remnants.ballistics.smoketest;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Development-only companion mod that drives {@link ClientSmokeTest} in the {@code clientSmokeTest} run. It lives in its
 * own source set and is never packaged into the release jar.
 */
@Mod(SmokeTestMod.MODID)
public final class SmokeTestMod {
    public static final String MODID = "remnants_ballistics_smoketest";

    public SmokeTestMod() {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientSmokeTest.tick();
        });
        // RenderTickEvent END fires right after the frame is rendered, before it is shown.
        MinecraftForge.EVENT_BUS.addListener((TickEvent.RenderTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) ClientSmokeTest.afterFrame();
        });
    }
}
