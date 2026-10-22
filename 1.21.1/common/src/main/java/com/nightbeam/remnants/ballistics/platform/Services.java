package com.nightbeam.remnants.ballistics.platform;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;

import java.util.ServiceLoader;

/**
 * Loader services, found with {@link ServiceLoader}. Each loader module lists its implementations in
 * {@code META-INF/services}; common code never imports a loader API directly.
 */
public final class Services {
    public static final IPlatform PLATFORM = load(IPlatform.class);
    public static final INetwork NETWORK = load(INetwork.class);

    private Services() {}

    public static <T> T load(Class<T> type) {
        T service = ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No " + type.getName() + " implementation on the classpath"));
        RemnantsBallistics.LOGGER.debug("Loaded {} for {}", service, type.getSimpleName());
        return service;
    }
}
