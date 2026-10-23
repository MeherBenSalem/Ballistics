package com.nightbeam.remnants.ballistics.smoketest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Development-only companion mod that drives {@link ClientSmokeTest} in the Fabric {@code clientSmokeTest} run. It lives
 * in its own source set and is never packaged into the release jar.
 */
public final class FabricSmokeTest implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientSmokeTest.tick());
    }

    /** Called after every rendered frame (see {@code mixin.GameRendererFrameMixin}). */
    public static void afterFrame() {
        ClientSmokeTest.afterFrame();
    }
}
