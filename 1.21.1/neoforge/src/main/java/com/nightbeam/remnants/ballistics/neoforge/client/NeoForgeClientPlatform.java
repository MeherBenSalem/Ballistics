package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.client.platform.IClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public final class NeoForgeClientPlatform implements IClientPlatform {
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(id));
    }
}
