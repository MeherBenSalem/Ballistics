package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.client.platform.IClientPlatform;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NeoForgeClientPlatform implements IClientPlatform {
    /** Standalone model keys by model id, filled when the models are registered. */
    private static final Map<Identifier, StandaloneModelKey<QuadCollection>> KEYS = new HashMap<>();

    /** Registers every {@link GunModels#standaloneModels()} entry, baked to its quads. */
    static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        for (Identifier id : GunModels.standaloneModels()) {
            StandaloneModelKey<QuadCollection> key = KEYS.computeIfAbsent(id, i -> new StandaloneModelKey<>(i::toString));
            event.register(key, SimpleUnbakedStandaloneModel.quadCollection(id));
        }
    }

    @Override
    public @Nullable List<BakedQuad> getStandaloneQuads(Identifier id) {
        StandaloneModelKey<QuadCollection> key = KEYS.get(id);
        if (key == null) return null;
        QuadCollection quads = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        return quads == null ? null : quads.getAll();
    }

    @Override
    public void submitPictureInPicture(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
        graphics.submitPictureInPictureRenderState(state);
    }

    @Override
    public @Nullable ScreenRectangle currentScissor(GuiGraphicsExtractor graphics) {
        return graphics.peekScissorStack();
    }
}
