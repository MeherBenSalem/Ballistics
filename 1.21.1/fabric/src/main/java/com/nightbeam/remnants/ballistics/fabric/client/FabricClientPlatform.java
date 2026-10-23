package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.client.platform.IClientPlatform;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public final class FabricClientPlatform implements IClientPlatform {
    /** Models added through {@code ModelLoadingPlugin#addModels} are looked up by their plain id. */
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(id);
    }
}
