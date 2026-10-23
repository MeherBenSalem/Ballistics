package com.nightbeam.remnants.ballistics.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A registry entry declared by common code ({@link Registrar#register}) and registered by the loader: Fabric registers
 * every entry during initialisation, NeoForge inside its {@code RegisterEvent}. {@link #get()} returns the registered
 * value once the loader has bound it.
 */
public final class RegistryHolder<T> implements Supplier<T> {
    private final ResourceKey<? extends Registry<? super T>> registry;
    private final ResourceLocation id;
    private final Supplier<? extends T> factory;
    private T value;

    RegistryHolder(ResourceKey<? extends Registry<? super T>> registry, ResourceLocation id, Supplier<? extends T> factory) {
        this.registry = registry;
        this.id = id;
        this.factory = factory;
    }

    public ResourceKey<? extends Registry<? super T>> registry() {
        return registry;
    }

    public ResourceLocation id() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public ResourceKey<T> key() {
        return ResourceKey.create((ResourceKey<? extends Registry<T>>) registry, id);
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value, () -> "Registry entry " + id + " used before it was registered");
    }

    public boolean isBound() {
        return value != null;
    }

    /** The registry's own holder for this entry (for APIs that take a {@link Holder}). */
    @SuppressWarnings("unchecked")
    public Holder<T> holder() {
        Registry<T> reg = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        return Objects.requireNonNull(reg, () -> "Unknown registry " + registry.location()).getHolderOrThrow(key());
    }

    /** Called by the loader: creates the value to register. */
    public T create() {
        return factory.get();
    }

    /** Called by the loader once {@link #create()}'s value is registered. */
    public void bind(T value) {
        this.value = value;
    }
}
