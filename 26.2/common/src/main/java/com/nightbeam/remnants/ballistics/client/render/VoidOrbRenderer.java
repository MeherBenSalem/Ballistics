package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.entity.VoidOrbEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

/** Camera-facing, full-bright pulsing orb with an additive core. */
public class VoidOrbRenderer extends EntityRenderer<VoidOrbEntity, EntityRenderState> {
    private static final Identifier TEXTURE = RemnantsBallistics.id("textures/entity/void_orb.png");
    private static final RenderType RENDER_TYPE = RenderTypes.eyes(TEXTURE);

    public VoidOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void submit(EntityRenderState state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        ps.pushPose();
        ps.mulPose(camera.orientation);
        float pulse = 0.85f + 0.15f * (float) Math.sin(state.ageInTicks * 0.8f);
        float s = 0.35f * pulse;
        ps.mulPose(Axis.ZP.rotationDegrees(state.ageInTicks * 12f));
        collector.submitCustomGeometry(ps, RENDER_TYPE, (pose, vc) -> {
            Matrix4f m = pose.pose();
            vertex(vc, m, -s, -s, 0f, 1f);
            vertex(vc, m, s, -s, 1f, 1f);
            vertex(vc, m, s, s, 1f, 0f);
            vertex(vc, m, -s, s, 0f, 0f);
        });
        ps.popPose();
        super.submit(state, ps, collector, camera);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float u, float v) {
        vc.addVertex(m, x, y, 0f).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
    }
}
