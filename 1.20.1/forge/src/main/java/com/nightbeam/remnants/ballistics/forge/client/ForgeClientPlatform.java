package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.client.platform.IClientPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class ForgeClientPlatform implements IClientPlatform {
    /** Models added through {@code ModelEvent.RegisterAdditional} are looked up by their plain id. */
    @Override
    public BakedModel getStandaloneModel(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeGunItem(Consumer<?> consumer) {
        ((Consumer<IClientItemExtensions>) consumer).accept(GunClientExtensions.INSTANCE);
    }
}
