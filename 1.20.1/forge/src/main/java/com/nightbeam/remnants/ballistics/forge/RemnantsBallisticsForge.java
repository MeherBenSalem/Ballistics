package com.nightbeam.remnants.ballistics.forge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.forge.client.ForgeClientSetup;
import com.nightbeam.remnants.ballistics.forge.loot.ConfigCondition;
import com.nightbeam.remnants.ballistics.forge.loot.ForgeLootModifiers;
import com.nightbeam.remnants.ballistics.forge.platform.ForgeNetwork;
import com.nightbeam.remnants.ballistics.registry.Registrar;
import com.nightbeam.remnants.ballistics.registry.RegistryHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

import java.util.List;

/**
 * Forge entry point. Registers the common content, configs and payloads; gameplay listeners are in
 * {@link ForgeServerEvents}, client setup in {@code forge.client}.
 */
@Mod(RemnantsBallistics.MODID)
public final class RemnantsBallisticsForge {
    // The static context accessors work on every Forge 47 build; 47.4 only flags them for its 1.21-style API.
    @SuppressWarnings("removal")
    public RemnantsBallisticsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registrar.bootstrap();
        modBus.addListener(RemnantsBallisticsForge::register);
        ForgeLootModifiers.SERIALIZERS.register(modBus);
        CraftingHelper.register(ConfigCondition.Serializer.INSTANCE);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        ForgeNetwork.register();
        modBus.addListener((FMLCommonSetupEvent event) -> RemnantsBallistics.commonSetup());
        // The client class is only loaded on the physical client.
        if (FMLEnvironment.dist == Dist.CLIENT) ForgeClientSetup.init(modBus);
    }

    /** Forge fires one RegisterEvent per registry, in its own dependency order. */
    private static void register(RegisterEvent event) {
        List<RegistryHolder<?>> entries = Registrar.entries().get(event.getRegistryKey());
        if (entries != null) entries.forEach(holder -> registerEntry(event, holder));
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerEntry(RegisterEvent event, RegistryHolder<T> holder) {
        event.register((ResourceKey<? extends Registry<T>>) holder.registry(), holder.id(), () -> {
            T value = holder.create();
            holder.bind(value);
            return value;
        });
    }
}
