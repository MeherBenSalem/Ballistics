package com.nightbeam.remnants.ballistics.client.platform;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Client loader hooks. */
public interface IClientPlatform {
    /**
     * The baked quads of a standalone model registered through the loader's extra-model hook
     * ({@code GunModels.standaloneModels()}), or null if the model was not registered or not baked yet.
     */
    @Nullable List<BakedQuad> getStandaloneQuads(Identifier id);

    /** Queues a picture-in-picture element (a 3D model drawn into the GUI) for this frame. */
    void submitPictureInPicture(GuiGraphicsExtractor graphics, PictureInPictureRenderState state);

    /** The innermost scissor rectangle of the GUI being drawn, if any. */
    @Nullable ScreenRectangle currentScissor(GuiGraphicsExtractor graphics);
}
