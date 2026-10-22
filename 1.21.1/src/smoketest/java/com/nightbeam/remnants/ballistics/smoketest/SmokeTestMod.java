package com.nightbeam.remnants.ballistics.smoketest;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Development-only companion mod that drives {@link ClientSmokeTest} in the {@code clientSmokeTest} run. It lives in its
 * own source set and is never packaged into the release jar.
 */
@Mod(value = SmokeTestMod.MODID, dist = Dist.CLIENT)
public final class SmokeTestMod {
    public static final String MODID = "remnants_ballistics_smoketest";

    public SmokeTestMod() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> ClientSmokeTest.tick());
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Post.class, event -> ClientSmokeTest.afterFrame());
    }
}
