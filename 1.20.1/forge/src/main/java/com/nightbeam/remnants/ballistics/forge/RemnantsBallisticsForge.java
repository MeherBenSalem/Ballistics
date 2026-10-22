package com.nightbeam.remnants.ballistics.forge;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.forge.client.ForgeClientHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/** Forge entry point: registries, configs, the network channel and recipe conditions; gameplay hooks are in {@link ForgeEvents}. */
@Mod(RemnantsBallistics.MODID)
public final class RemnantsBallisticsForge {
    public RemnantsBallisticsForge(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ForgePlatform.modBus = modBus;
        RemnantsBallistics.init();
        ForgeNetwork.register();
        CraftingHelper.register(ForgeConditions.ConfigCondition.Serializer.INSTANCE);

        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
            ForgeClientHooks.init();
        }

        modBus.addListener((FMLCommonSetupEvent event) -> RemnantsBallistics.setup());
    }
}
