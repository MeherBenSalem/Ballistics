package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client entry point. This class is never loaded on a dedicated server. */
@Mod(value = RemnantsBallistics.MODID, dist = Dist.CLIENT)
public final class RemnantsBallisticsNeoForgeClient {
    public RemnantsBallisticsNeoForgeClient(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
