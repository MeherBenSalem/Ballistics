package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.entity.VoidOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** Camera-facing, full-bright pulsing orb with an additive core. */
public class VoidOrbRenderer extends EntityRenderer<VoidOrbEntity> {
    private static final ResourceLocation TEXTURE = RemnantsBallistics.id("textures/entity/void_orb.png");

    public VoidOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VoidOrbEntity entity, float yaw, float partialTick, PoseStack ps, MultiBufferSource buffers, int light) {
        ps.pushPose();
        ps.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float pulse = 0.85f + 0.15f * (float) Math.sin((entity.tickCount + partialTick) * 0.8f);
        float s = 0.35f * pulse;
        ps.mulPose(Axis.ZP.rotationDegrees((entity.tickCount + partialTick) * 12f));
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(TEXTURE));
        Matrix4f m = ps.last().pose();
        vertex(vc, m, -s, -s, 0f, 1f);
        vertex(vc, m, s, -s, 1f, 1f);
        vertex(vc, m, s, s, 1f, 0f);
        vertex(vc, m, -s, s, 0f, 0f);
        ps.popPose();
        super.render(entity, yaw, partialTick, ps, buffers, light);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float u, float v) {
        vc.vertex(m, x, y, 0f).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0f, 1f, 0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(VoidOrbEntity entity) {
        return TEXTURE;
    }
}
