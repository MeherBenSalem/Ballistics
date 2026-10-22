package com.nightbeam.remnants.ballistics;

import com.mojang.logging.LogUtils;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.network.ModNetwork;
import com.nightbeam.remnants.ballistics.registry.ModAttachmentTypes;
import com.nightbeam.remnants.ballistics.registry.ModBlocks;
import com.nightbeam.remnants.ballistics.registry.ModCreativeTabs;
import com.nightbeam.remnants.ballistics.registry.ModDataComponents;
import com.nightbeam.remnants.ballistics.registry.ModEntities;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModLootModifiers;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import com.nightbeam.remnants.ballistics.registry.ModRecipes;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Common entry point. Registers content and configs; gameplay listeners live in {@code @EventBusSubscriber}
 * classes, and client-only setup is in {@code com.nightbeam.remnants.ballistics.client.RemnantsBallisticsClient}.
 */
@Mod(RemnantsBallistics.MODID)
public final class RemnantsBallistics {
    public static final String MODID = "remnants_ballistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RemnantsBallistics(IEventBus modBus, ModContainer container) {
        ModDataComponents.REGISTER.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModLootModifiers.SERIALIZERS.register(modBus);
        ModLootModifiers.CONDITIONS.register(modBus);
        ModAttachmentTypes.TYPES.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);

        modBus.addListener(ModNetwork::register);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Weapons.validate(Attachments::byName);
        LOGGER.info("Remnants: Ballistics loaded {} weapons and {} attachments", Weapons.all().size(), Attachments.all().size());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
