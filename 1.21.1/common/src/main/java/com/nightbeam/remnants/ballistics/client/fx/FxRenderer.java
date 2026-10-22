package com.nightbeam.remnants.ballistics.client.fx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws tracers, bullet-hole decals and third-person muzzle flashes after particles. Geometry is built relative
 * to the camera, which keeps float precision far from the world origin.
 */
public final class FxRenderer {
    public static final ResourceLocation BULLET_HOLE = RemnantsBallistics.id("textures/fx/bullet_hole.png");
    public static final ResourceLocation MUZZLE_FLASH = RemnantsBallistics.id("textures/fx/muzzle_flash.png");
    public static final ResourceLocation ENERGY_FLASH = RemnantsBallistics.id("textures/fx/energy_flash.png");
    private static final int FLASH_FRAMES = 4;

    private FxRenderer() {}

    /**
     * Draws all world effects. Called once per frame after particles, with the pose stack holding the camera rotation
     * (the camera position is subtracted here).
     */
    public static void render(PoseStack poseStack, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 cam = camera.getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        if (!ClientEffects.HOLES.isEmpty()) {
            RenderType type = RenderType.entityTranslucent(BULLET_HOLE);
            VertexConsumer vc = buffers.getBuffer(type);
            for (ClientEffects.BulletHole hole : ClientEffects.HOLES) renderHole(vc, poseStack.last(), cam, hole);
            buffers.endBatch(type);
        }
        if (!ClientEffects.TRACERS.isEmpty()) {
            RenderType type = RenderType.lightning();
            VertexConsumer vc = buffers.getBuffer(type);
            Matrix4f matrix = poseStack.last().pose();
            for (ClientEffects.Tracer tracer : ClientEffects.TRACERS) renderTracer(vc, matrix, cam, tracer);
            buffers.endBatch(type);
        }
        if (!ClientEffects.FLASHES.isEmpty()) {
            float brightness = (float) ClientConfig.get(ClientConfig.FLASH_BRIGHTNESS);
            renderFlashes(buffers, poseStack, camera, cam, brightness, false);
            renderFlashes(buffers, poseStack, camera, cam, brightness, true);
        }
    }

    private static void renderHole(VertexConsumer vc, PoseStack.Pose pose, Vec3 cam, ClientEffects.BulletHole hole) {
        Direction face = hole.face();
        Vector3f n = new Vector3f(face.getStepX(), face.getStepY(), face.getStepZ());
        // Two tangents spanning the face plane, rotated by the hole's random angle.
        Vector3f t1 = Math.abs(n.y) > 0.5f ? new Vector3f(1, 0, 0) : new Vector3f(0, 1, 0);
        Vector3f t2 = new Vector3f(n).cross(t1).normalize();
        t1 = new Vector3f(t2).cross(n).normalize();
        float angle = (float) Math.toRadians(hole.rotation());
        Vector3f u = new Vector3f(t1).mul((float) Math.cos(angle)).add(new Vector3f(t2).mul((float) Math.sin(angle))).mul(hole.size());
        Vector3f v = new Vector3f(n).cross(u);
        Vec3 c = hole.pos().add(n.x * 0.004, n.y * 0.004, n.z * 0.004);
        float x = (float) (c.x - cam.x), y = (float) (c.y - cam.y), z = (float) (c.z - cam.z);
        float age = (System.nanoTime() - hole.createdNanos()) / 1.0E9f;
        int alpha = (int) (255 * Math.min(1f, Math.max(0f, (30f - age) / 5f)));
        int shade = (int) (255 * hole.shade());
        Minecraft mc = Minecraft.getInstance();
        int light = LevelRenderer.getLightColor(mc.level, BlockPos.containing(c.x + n.x * 0.5, c.y + n.y * 0.5, c.z + n.z * 0.5));
        Vector3f nn = pose.normal().transform(new Vector3f(n));
        Matrix4f m = pose.pose();
        vertex(vc, m, x - u.x - v.x, y - u.y - v.y, z - u.z - v.z, shade, alpha, 0, 1, light, nn);
        vertex(vc, m, x + u.x - v.x, y + u.y - v.y, z + u.z - v.z, shade, alpha, 1, 1, light, nn);
        vertex(vc, m, x + u.x + v.x, y + u.y + v.y, z + u.z + v.z, shade, alpha, 1, 0, light, nn);
        vertex(vc, m, x - u.x + v.x, y - u.y + v.y, z - u.z + v.z, shade, alpha, 0, 0, light, nn);
    }

    private static void vertex(VertexConsumer vc, Matrix4f m, float x, float y, float z, int shade, int alpha, float u, float v, int light, Vector3f n) {
        vc.addVertex(m, x, y, z).setColor(shade, shade, shade, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(n.x, n.y, n.z);
    }

    private static void renderTracer(VertexConsumer vc, Matrix4f m, Vec3 cam, ClientEffects.Tracer tracer) {
        double length = tracer.length();
        double head;
        double tail;
        float fade;
        if (tracer.beam) {
            head = length;
            tail = 0;
            fade = Math.max(0f, 1f - tracer.age() / 0.5f);
        } else {
            head = Math.min(length, tracer.age() * tracer.speed);
            tail = Math.max(0, head - tracer.streak);
            fade = 1f;
            if (head - tail < 0.05) return;
        }
        // Walk the polyline between tail and head so curved (gravity/ricochet) paths render correctly.
        double step = Math.max(1.0, (head - tail) / 8.0);
        Vec3 prev = tracer.at(tail);
        for (double d = tail + step; ; d += step) {
            double dd = Math.min(d, head);
            Vec3 next = tracer.at(dd);
            float tailAlpha = (float) ((dd - step - tail) / Math.max(1.0E-3, head - tail));
            float headAlpha = (float) ((dd - tail) / Math.max(1.0E-3, head - tail));
            quad(vc, m, prev.subtract(cam), next.subtract(cam), tracer.width, tracer.r, tracer.g, tracer.b,
                    Math.max(0f, tailAlpha) * fade * 0.9f, headAlpha * fade * 0.9f);
            prev = next;
            if (dd >= head) break;
        }
    }

    private static void quad(VertexConsumer vc, Matrix4f m, Vec3 a, Vec3 b, float width, float r, float g, float bl, float alphaA, float alphaB) {
        Vec3 dir = b.subtract(a);
        Vec3 mid = a.add(b).scale(0.5);
        Vec3 side = dir.cross(mid).normalize().scale(width);
        if (side.lengthSqr() < 1.0E-12) return;
        int ia = (int) (alphaA * 255), ib = (int) (alphaB * 255);
        int ri = (int) (r * 255), gi = (int) (g * 255), bi = (int) (bl * 255);
        // Emitted with both windings: the lightning render type culls back faces.
        for (int pass = 0; pass < 2; pass++) {
            double sgn = pass == 0 ? 1 : -1;
            vc.addVertex(m, (float) (a.x + side.x * sgn), (float) (a.y + side.y * sgn), (float) (a.z + side.z * sgn)).setColor(ri, gi, bi, ia);
            vc.addVertex(m, (float) (a.x - side.x * sgn), (float) (a.y - side.y * sgn), (float) (a.z - side.z * sgn)).setColor(ri, gi, bi, ia);
            vc.addVertex(m, (float) (b.x - side.x * sgn), (float) (b.y - side.y * sgn), (float) (b.z - side.z * sgn)).setColor(ri, gi, bi, ib);
            vc.addVertex(m, (float) (b.x + side.x * sgn), (float) (b.y + side.y * sgn), (float) (b.z + side.z * sgn)).setColor(ri, gi, bi, ib);
        }
    }

    private static void renderFlashes(MultiBufferSource.BufferSource buffers, PoseStack poseStack, Camera camera, Vec3 cam, float brightness, boolean energy) {
        RenderType type = RenderType.eyes(energy ? ENERGY_FLASH : MUZZLE_FLASH);
        VertexConsumer vc = buffers.getBuffer(type);
        Quaternionf rotation = camera.rotation();
        Matrix4f m = poseStack.last().pose();
        long now = System.nanoTime();
        for (ClientEffects.Flash flash : ClientEffects.FLASHES) {
            if (flash.energy() != energy) continue;
            float age = (now - flash.nanos()) / 6.0E7f;
            if (age >= 1f) continue;
            int frame = Math.min(FLASH_FRAMES - 1, (int) (age * FLASH_FRAMES));
            float u0 = frame / (float) FLASH_FRAMES, u1 = (frame + 1) / (float) FLASH_FRAMES;
            int c = (int) (255 * brightness * (1f - age * 0.6f));
            float s = flash.size() * (0.8f + age * 0.4f);
            Vec3 p = flash.pos().subtract(cam);
            Vector3f[] corners = {new Vector3f(-s, -s, 0), new Vector3f(s, -s, 0), new Vector3f(s, s, 0), new Vector3f(-s, s, 0)};
            float[][] uv = {{u0, 1}, {u1, 1}, {u1, 0}, {u0, 0}};
            for (int i = 0; i < 4; i++) corners[i].rotate(rotation).add((float) p.x, (float) p.y, (float) p.z);
            // Both windings so the billboard is visible regardless of the render type's culling.
            int[][] orders = {{0, 1, 2, 3}, {3, 2, 1, 0}};
            for (int[] order : orders) {
                for (int i : order) {
                    Vector3f corner = corners[i];
                    vc.addVertex(m, corner.x, corner.y, corner.z).setColor(c, c, c, 255).setUv(uv[i][0], uv[i][1])
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0, 1, 0);
                }
            }
        }
        buffers.endBatch(type);
    }
}
