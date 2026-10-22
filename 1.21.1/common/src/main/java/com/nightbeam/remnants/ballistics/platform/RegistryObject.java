package com.nightbeam.remnants.ballistics.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/** A registered (or soon to be registered) entry. {@link #get()} fails until the loader has registered it. */
public interface RegistryObject<R, T extends R> extends Supplier<T> {
    ResourceKey<R> getKey();

    default ResourceLocation getId() {
        return getKey().location();
    }

    Holder<R> asHolder();
}
