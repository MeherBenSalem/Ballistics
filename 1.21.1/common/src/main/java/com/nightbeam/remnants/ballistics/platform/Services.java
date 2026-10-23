package com.nightbeam.remnants.ballistics.platform;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.platform.services.INetworkHelper;
import com.nightbeam.remnants.ballistics.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

/**
 * Loader-specific implementations, found with {@link ServiceLoader} ({@code META-INF/services} in the fabric and
 * neoforge projects). Client-only services live in {@code client.platform.ClientServices} so a dedicated server never
 * loads them.
 */
public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    private Services() {}

    public static <T> T load(Class<T> type) {
        T service = ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No implementation of " + type.getName() + " for this loader"));
        RemnantsBallistics.LOGGER.debug("Loaded {} for service {}", service, type);
        return service;
    }
}
