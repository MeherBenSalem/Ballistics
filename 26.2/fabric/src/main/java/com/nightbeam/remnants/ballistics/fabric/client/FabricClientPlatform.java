package com.nightbeam.remnants.ballistics.fabric.client;

import com.nightbeam.remnants.ballistics.client.platform.IClientPlatform;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FabricClientPlatform implements IClientPlatform {
    /** Extra model keys by model id, filled when the models are registered. */
    private static final Map<Identifier, ExtraModelKey<List<BakedQuad>>> KEYS = new HashMap<>();

    /** Registers every {@link GunModels#standaloneModels()} entry as an extra model baked to its quads. */
    static void registerStandaloneModels(ModelLoadingPlugin.Context context) {
        for (Identifier id : GunModels.standaloneModels()) {
            ExtraModelKey<List<BakedQuad>> key = KEYS.computeIfAbsent(id, i -> ExtraModelKey.create(i::toString));
            context.addModel(key, new SimpleUnbakedExtraModel<>(id, (model, baker) ->
                    model.bakeTopGeometry(model.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll()));
        }
    }

    @Override
    public @Nullable List<BakedQuad> getStandaloneQuads(Identifier id) {
        ExtraModelKey<List<BakedQuad>> key = KEYS.get(id);
        return key == null ? null : Minecraft.getInstance().getModelManager().getModel(key);
    }

    @Override
    public void submitPictureInPicture(GuiGraphicsExtractor graphics, PictureInPictureRenderState state) {
        graphics.guiRenderState.addPicturesInPictureState(state);
    }

    @Override
    public @Nullable ScreenRectangle currentScissor(GuiGraphicsExtractor graphics) {
        return graphics.scissorStack.peek();
    }
}
