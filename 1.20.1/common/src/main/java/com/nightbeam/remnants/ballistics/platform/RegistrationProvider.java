package com.nightbeam.remnants.ballistics.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Loader-neutral deferred registration. Forge backs it with a {@code DeferredRegister}; Fabric registers
 * immediately. Entries are created in static initialisers, which the loader entry points trigger through
 * {@link com.nightbeam.remnants.ballistics.RemnantsBallistics#init()}.
 */
public interface RegistrationProvider<T> {
    static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> registry, String modId) {
        return Services.PLATFORM.registry(registry, modId);
    }

    <I extends T> RegistryObject<T, I> register(String name, Supplier<? extends I> factory);

    /** Every entry registered so far, in registration order. */
    Collection<RegistryObject<T, ? extends T>> getEntries();
}
