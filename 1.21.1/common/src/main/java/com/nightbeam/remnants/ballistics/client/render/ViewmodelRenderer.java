package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.anim.BonePose;
import com.nightbeam.remnants.ballistics.client.anim.PoseState;
import com.nightbeam.remnants.ballistics.client.anim.WeaponAnimator;
import com.nightbeam.remnants.ballistics.client.fx.FxRenderer;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponCategory;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * First-person weapon rendering. Replaces the vanilla hand for weapons: positions the gun between hip and
 * aim-down-sights poses, applies procedural motion (sprint, bob, kick, equip) and keyframed bone animation, then
 * places the player's own arms so the hands meet the grip and support points.
 * <p>
 * Space conventions: the hand pose is view space (blocks; +X right, +Y up, -Z forward). Gun models use
 * Blockbench pixels with the barrel toward -Z; {@code scale} converts 16 pixels to view-space blocks.
 * {@code tools/preview_viewmodel.py} mirrors this math to preview poses offline.
 */
public final class ViewmodelRenderer {
    /** Where the shoulders sit in view space; arms stretch from the hands toward these points. */
    static final Vector3f RIGHT_SHOULDER = new Vector3f(0.42f, -0.72f, 0.18f);
    static final Vector3f LEFT_SHOULDER = new Vector3f(-0.36f, -0.74f, 0.12f);
    /** Arm thickness scale relative to the vanilla first-person arm. */
    static final float ARM_SCALE = 0.55f;
    /** Hand-end centre of the arm model in its own space (blocks), for wide (Steve) and slim (Alex) arms. */
    private static final Vector3f RIGHT_HAND_LOCAL = new Vector3f(-6f / 16f, 12f / 16f, 0f);
    private static final Vector3f LEFT_HAND_LOCAL = new Vector3f(6f / 16f, 12f / 16f, 0f);
    private static final float ARM_LENGTH = 12f / 16f;

    private ViewmodelRenderer() {}

    /**
     * Called for each hand before vanilla draws it in first person.
     *
     * @return true if vanilla must skip this hand (we drew the weapon, or the off hand is hidden behind a long gun)
     */
    public static boolean renderHand(InteractionHand hand, ItemStack stack, float partialTick, float equipProgress, PoseStack ps,
                                     MultiBufferSource buffers, int light) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        if (hand == InteractionHand.OFF_HAND) {
            // Two-handed weapons occupy both hands; hide whatever is in the off hand.
            return main.getItem() instanceof GunItem gun && gun.definition().category().isTwoHanded();
        }
        if (!(stack.getItem() instanceof GunItem gun)) return false;
        render(player, stack, gun.definition(), partialTick, equipProgress, ps, buffers, light);
        return true;
    }

    private static void render(LocalPlayer player, ItemStack stack, WeaponDefinition def, float pt, float equipProgress, PoseStack ps,
                               MultiBufferSource buffers, int light) {
        GunVisuals v = GunVisualsLoader.INSTANCE.get(def);
        WeaponStats stats = GunData.stats(stack, def);
        GunAttachments attachments = GunData.attachments(stack);
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        float ads = ClientGunHandler.adsProgress(pt);
        float sprint = ClientGunHandler.sprintProgress(pt);
        OpticType optic = GunData.optic(stack, def);
        if (optic == OpticType.SCOPE && ads > 0.92f) return; // the HUD draws the scope overlay instead

        ps.pushPose();
        // Undo vanilla's delayed hand rotation while aiming so the sights stay on the crosshair.
        float xLag = (player.getViewXRot(pt) - Mth.lerp(pt, player.xBobO, player.xBob)) * 0.1f;
        float yLag = (player.getViewYRot(pt) - Mth.lerp(pt, player.yBobO, player.yBob)) * 0.1f;
        ps.mulPose(Axis.YP.rotationDegrees(-yLag * ads));
        ps.mulPose(Axis.XP.rotationDegrees(-xLag * ads));
        Matrix4f base = new Matrix4f(ps.last().pose());

        // Root placement: hip ↔ aim-down-sights, plus sprint, equip and walking bob.
        Vector3f sight = sightPoint(v, attachments, optic);
        float scale = v.scale();
        Vector3f adsPos = new Vector3f(0f, 0f, -v.adsDistance()).sub(new Vector3f(sight).sub(v.pivot()).mul(scale / 16f));
        Vector3f hip = new Vector3f(v.hip()).add((float) ClientConfig.get(ClientConfig.VIEWMODEL_FOV_OFFSET_X),
                (float) ClientConfig.get(ClientConfig.VIEWMODEL_FOV_OFFSET_Y), (float) ClientConfig.get(ClientConfig.VIEWMODEL_OFFSET_Z));
        Vector3f pos = hip.lerp(adsPos, ads).add(new Vector3f(v.sprintPosition()).mul(sprint));
        pos.y -= equipProgress * 0.6f;
        float walk = -Mth.lerp(pt, player.walkDistO, player.walkDist);
        double horizontalSpeed = player.getDeltaMovement().horizontalDistance();
        float bobAmp = (float) Math.min(1.0, horizontalSpeed / 0.2) * (float) ClientConfig.get(ClientConfig.WEAPON_BOB) * (1f - 0.85f * ads)
                * (player.onGround() ? 1f : 0.2f);
        pos.x += Mth.sin(walk * Mth.PI) * 0.012f * bobAmp;
        pos.y -= Math.abs(Mth.cos(walk * Mth.PI)) * 0.018f * bobAmp;
        ps.translate(pos.x, pos.y, pos.z);

        Vector3f rot = new Vector3f(v.hipRotation()).mul(1f - ads).add(new Vector3f(v.sprintRotation()).mul(sprint));
        ps.mulPose(Axis.ZP.rotationDegrees(rot.z));
        ps.mulPose(Axis.YP.rotationDegrees(rot.y));
        ps.mulPose(Axis.XP.rotationDegrees(rot.x));

        // Visual kick (spring driven): push back and tilt the muzzle up.
        float kick = s.kick;
        ps.translate(0f, kick * 0.004f, kick * 0.03f);
        ps.mulPose(Axis.XP.rotationDegrees(kick * (def.category() == WeaponCategory.PISTOL ? 2.4f : 1.2f)));

        // Weapon sway while aiming (visual only); stronger for heavy optics.
        float sway = stats.get(Stat.SWAY) * (float) ClientConfig.get(ClientConfig.WEAPON_SWAY) * ads * (optic == OpticType.SCOPE ? 1f : 0.35f);
        if (sway > 0f) {
            double t = System.nanoTime() / 1.0E9;
            float crouch = player.isCrouching() ? 0.5f : 1f;
            ps.mulPose(Axis.YP.rotationDegrees((float) Math.sin(t * 0.9) * 0.18f * sway * crouch));
            ps.mulPose(Axis.XP.rotationDegrees((float) Math.sin(t * 1.7) * 0.12f * sway * crouch));
        }

        // Model space: pixels / 16 around the grip pivot, then the animated root bone.
        ps.scale(scale, scale, scale);
        ps.translate(-v.pivot().x / 16f, -v.pivot().y / 16f, -v.pivot().z / 16f);
        PoseState pose = WeaponAnimator.local(ads);
        GunRenderer.applyBone(ps, pose.get(PoseState.GUN), v.pivot());

        GunRenderer.render(def, v, attachments, pose, ps, buffers, light, OverlayTexture.NO_OVERLAY);

        Matrix4f gunToBase = new Matrix4f(base).invert().mul(ps.last().pose());
        Vector3f muzzleBase = transform(gunToBase, new Vector3f(v.muzzle()).sub(0f, 0f, GunRenderer.muzzleExtension(attachments)));
        Vector3f rightHand = transform(gunToBase, offsetBy(v.pivot(), pose.get(PoseState.RIGHT_ARM)));
        Vector3f leftHand = transform(gunToBase, offsetBy(v.leftHand(), pose.get(PoseState.LEFT_ARM)));
        ps.popPose();

        // Arms are placed in "base" space (view space with the same lag correction as above).
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-yLag * ads));
        ps.mulPose(Axis.XP.rotationDegrees(-xLag * ads));
        if (!player.isInvisible()) {
            PlayerRenderer renderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
            renderArm(ps, buffers, light, player, renderer, true, rightHand, RIGHT_SHOULDER);
            if (def.category().isTwoHanded() || ads > 0.3f || s.isReloading()) {
                renderArm(ps, buffers, light, player, renderer, false, leftHand, LEFT_SHOULDER);
            }
        }
        renderMuzzleFlash(ps, buffers, def, attachments, muzzleBase, v.flashSize());
        ps.popPose();
    }

    private static Vector3f sightPoint(GunVisuals v, GunAttachments attachments, OpticType optic) {
        AttachmentDefinition fitted = attachments.get(AttachmentSlot.OPTIC).orElse(null);
        if (fitted == null) return new Vector3f(v.sight());
        Vector3f mount = v.mount(AttachmentSlot.OPTIC);
        return new Vector3f(mount.x, mount.y + GunRenderer.opticSightHeight(fitted.name()), mount.z);
    }

    private static Vector3f offsetBy(Vector3f point, BonePose bone) {
        Vector3f p = new Vector3f(point);
        return bone == null ? p : p.add(bone.position);
    }

    /** Transforms a model-pixel point by a matrix whose input unit is blocks. */
    private static Vector3f transform(Matrix4f m, Vector3f pixels) {
        return m.transformPosition(new Vector3f(pixels).div(16f));
    }

    /**
     * Places a player arm so its hand end sits at {@code hand} and it extends toward {@code shoulder}
     * (stretching if needed so it never ends in mid-air on screen).
     */
    private static void renderArm(PoseStack ps, MultiBufferSource buffers, int light, LocalPlayer player, PlayerRenderer renderer,
                                  boolean right, Vector3f hand, Vector3f shoulder) {
        Vector3f dir = new Vector3f(hand).sub(shoulder);
        float distance = dir.length();
        if (distance < 1.0E-4f) return;
        dir.div(distance);
        Quaternionf rotation = new Quaternionf().rotationTo(0f, 1f, 0f, dir.x, dir.y, dir.z);
        float lengthScale = Math.max(ARM_SCALE, distance / ARM_LENGTH);
        Vector3f handLocal = right ? RIGHT_HAND_LOCAL : LEFT_HAND_LOCAL;
        ps.pushPose();
        ps.translate(hand.x, hand.y, hand.z);
        ps.mulPose(rotation);
        // Roll the arm so the palm faces the weapon rather than the camera.
        ps.mulPose(Axis.YP.rotationDegrees(right ? 90f : -90f));
        ps.scale(ARM_SCALE, lengthScale, ARM_SCALE);
        ps.translate(-handLocal.x, -handLocal.y, -handLocal.z);
        if (right) renderer.renderRightHand(ps, buffers, light, player);
        else renderer.renderLeftHand(ps, buffers, light, player);
        ps.popPose();
    }

    private static void renderMuzzleFlash(PoseStack ps, MultiBufferSource buffers, WeaponDefinition def, GunAttachments attachments,
                                          Vector3f muzzle, float size) {
        if (!ClientConfig.get(ClientConfig.MUZZLE_FLASH) || GunData.hidesFlash(attachments)) return;
        long since = System.nanoTime() - ClientWeaponState.INSTANCE.lastShotNanos;
        if (since > 50_000_000L || since < 0) return;
        float age = since / 5.0E7f;
        int frame = Math.min(3, (int) (age * 4));
        boolean energy = def.category() == WeaponCategory.EXPERIMENTAL;
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(energy ? FxRenderer.ENERGY_FLASH : FxRenderer.MUZZLE_FLASH));
        int c = (int) (255 * ClientConfig.get(ClientConfig.FLASH_BRIGHTNESS) * (1f - age * 0.5f));
        float half = 0.09f * size * (0.85f + age * 0.3f);
        float u0 = frame / 4f, u1 = (frame + 1) / 4f;
        Matrix4f m = ps.last().pose();
        // A view-facing star plus a forward-facing plane gives the flash some depth.
        float[][] corners = {{-half, -half}, {half, -half}, {half, half}, {-half, half}};
        float[][] uv = {{u0, 1f}, {u1, 1f}, {u1, 0f}, {u0, 0f}};
        for (int i = 0; i < 4; i++) {
            vc.addVertex(m, muzzle.x + corners[i][0], muzzle.y + corners[i][1], muzzle.z).setColor(c, c, c, 255).setUv(uv[i][0], uv[i][1])
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 0f, 1f);
        }
        for (int i = 0; i < 4; i++) {
            vc.addVertex(m, muzzle.x + corners[i][0] * 0.7f, muzzle.y, muzzle.z + corners[i][1] * 2.2f - half).setColor(c, c, c, 255)
                    .setUv(uv[i][0], uv[i][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0f, 1f, 0f);
        }
    }
}
