package com.nightbeam.remnants.ballistics.client.platform;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

/** Client loader hooks. */
public interface IClientPlatform {
    /**
     * A standalone model registered through the loader's extra-model hook ({@code GunModels.standaloneModels()}); each
     * loader keys these differently in the model manager.
     */
    BakedModel getStandaloneModel(ResourceLocation id);

    /**
     * Forge: supplies a weapon's client item extensions (renderer and arm poses) to {@code Item#initializeClient}, whose
     * {@code Consumer<IClientItemExtensions>} common code cannot name. Fabric registers those through its own APIs.
     */
    default void initializeGunItem(Consumer<?> consumer) {}
}
