package com.nightbeam.remnants.ballistics.smoketest;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Development-only companion mod that drives {@link ClientSmokeTest} in the {@code clientSmokeTest} run (frames are
 * captured from a mixin at the end of {@code GameRenderer#render}). Never packaged into the release jar.
 */
public final class SmokeTestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientSmokeTest.tick());
    }
}
