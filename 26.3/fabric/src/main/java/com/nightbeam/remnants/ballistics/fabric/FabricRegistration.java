package com.nightbeam.remnants.ballistics.fabric;

import com.nightbeam.remnants.ballistics.registry.Registrar;
import com.nightbeam.remnants.ballistics.registry.RegistryHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registers every {@link Registrar} entry directly into the vanilla registries, dependencies first. */
final class FabricRegistration {
    /** Registries in dependency order: items use data components and blocks, tabs show items. */
    private static final List<ResourceKey<? extends Registry<?>>> ORDER = List.of(
            Registries.DATA_COMPONENT_TYPE, Registries.SOUND_EVENT, Registries.BLOCK, Registries.ITEM, Registries.ENTITY_TYPE,
            Registries.MENU, Registries.RECIPE_TYPE, Registries.RECIPE_SERIALIZER, Registries.PARTICLE_TYPE,
            Registries.LOOT_CONDITION_TYPE, Registries.CREATIVE_MODE_TAB);

    private FabricRegistration() {}

    static void registerAll() {
        Map<ResourceKey<? extends Registry<?>>, List<RegistryHolder<?>>> remaining = new LinkedHashMap<>(Registrar.entries());
        List<ResourceKey<? extends Registry<?>>> order = new ArrayList<>(ORDER);
        remaining.keySet().stream().filter(key -> !order.contains(key)).forEach(order::add);
        for (ResourceKey<? extends Registry<?>> key : order) {
            List<RegistryHolder<?>> entries = remaining.remove(key);
            if (entries != null) entries.forEach(FabricRegistration::register);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void register(RegistryHolder<T> holder) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(holder.registry().identifier());
        if (registry == null) throw new IllegalStateException("Unknown registry " + holder.registry().identifier());
        T value = holder.create();
        Registry.register(registry, holder.id(), value);
        holder.bind(value);
    }
}
