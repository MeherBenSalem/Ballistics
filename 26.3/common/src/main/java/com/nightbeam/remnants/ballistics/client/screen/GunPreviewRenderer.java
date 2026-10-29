package com.nightbeam.remnants.ballistics.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.platform.ClientServices;
import com.nightbeam.remnants.ballistics.client.render.GunRenderer;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.Nullable;

/**
 * The Gunsmith screen's rotating 3D weapon preview. GUIs draw 3D models into an offscreen texture
 * (picture-in-picture); each loader registers this renderer for {@link State}.
 */
public final class GunPreviewRenderer extends PictureInPictureRenderer<GunPreviewRenderer.State> {
    /** Pixels per model block at GUI scale 1. */
    private static final float SCALE = 30f;

    public record State(WeaponDefinition definition, GunAttachments attachments, float spin, int x0, int y0, int x1, int y1,
                        float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds)
            implements PictureInPictureRenderState {}

    /** Queues the preview in the rectangle {@code (x0, y0)-(x1, y1)}, rotated {@code spin} degrees around the vertical. */
    public static void submit(GuiGraphicsExtractor g, WeaponDefinition def, GunAttachments attachments, float spin, int x0, int y0, int x1, int y1) {
        ScreenRectangle scissor = ClientServices.PLATFORM.currentScissor(g);
        ClientServices.PLATFORM.submitPictureInPicture(g, new State(def, attachments, spin, x0, y0, x1, y1, SCALE, scissor,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissor)));
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected void renderToTexture(State state, PoseStack ps, SubmitNodeCollector collector) {
        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);
        // The texture's Y axis points down: flip the model upright, then tilt it towards the viewer and spin it.
        ps.rotate(Axis.ZP.rotationDegrees(180f));
        ps.rotate(Axis.XP.rotationDegrees(15f));
        ps.rotate(Axis.YP.rotationDegrees(state.spin()));
        ps.translate(-0.5f, -0.5f, -0.5f);
        WeaponDefinition def = state.definition();
        GunRenderer.render(def, GunVisualsLoader.INSTANCE.get(def), state.attachments(), null, ps, collector, 0xF000F0, OverlayTexture.NO_OVERLAY);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2f;
    }

    @Override
    protected String getTextureLabel() {
        return "remnants_ballistics gun preview";
    }
}
