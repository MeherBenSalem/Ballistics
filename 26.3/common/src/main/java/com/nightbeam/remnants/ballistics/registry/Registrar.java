package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Every registry entry of the mod, grouped by registry in declaration order. The {@code Mod*} classes declare entries
 * here from their static initialisers; {@link #bootstrap()} runs those initialisers so the loader sees the full list.
 */
public final class Registrar {
    private static final Map<ResourceKey<? extends Registry<?>>, List<RegistryHolder<?>>> ENTRIES = new LinkedHashMap<>();
    private static boolean bootstrapped;

    private Registrar() {}

    public static <R, T extends R> RegistryHolder<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory) {
        RegistryHolder<T> holder = new RegistryHolder<>(registry, RemnantsBallistics.id(name), factory);
        ENTRIES.computeIfAbsent(registry, k -> new ArrayList<>()).add(holder);
        return holder;
    }

    /** Declares every entry. Idempotent; loaders call it before registering. */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        ModDataComponents.init();
        ModSounds.init();
        ModBlocks.init();
        ModItems.init();
        ModEntities.init();
        ModMenus.init();
        ModRecipes.init();
        ModParticles.init();
        ModLootConditions.init();
        ModCreativeTabs.init();
    }

    /** Entries per registry, in declaration order. */
    public static Map<ResourceKey<? extends Registry<?>>, List<RegistryHolder<?>>> entries() {
        bootstrap();
        return Collections.unmodifiableMap(ENTRIES);
    }
}
