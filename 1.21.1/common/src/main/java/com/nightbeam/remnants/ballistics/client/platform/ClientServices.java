package com.nightbeam.remnants.ballistics.client.platform;

import com.nightbeam.remnants.ballistics.platform.Services;

/** Client-only loader services. Never touched on a dedicated server. */
public final class ClientServices {
    public static final IClientPlatform PLATFORM = Services.load(IClientPlatform.class);

    private ClientServices() {}
}
