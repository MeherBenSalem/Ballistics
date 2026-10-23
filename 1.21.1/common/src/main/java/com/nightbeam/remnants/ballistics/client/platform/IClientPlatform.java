package com.nightbeam.remnants.ballistics.client.platform;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/** Client loader hooks. */
public interface IClientPlatform {
    /**
     * A standalone model registered through the loader's extra-model hook ({@code GunModels.standaloneModels()}); each
     * loader keys these differently in the model manager.
     */
    BakedModel getStandaloneModel(ResourceLocation id);
}
