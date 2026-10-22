package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.anim.BonePose;
import com.nightbeam.remnants.ballistics.client.anim.PoseState;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;


/** Renders a weapon from its part models and fitted attachments, applying animated bone poses. */
public final class GunRenderer {
    /** Model pixels where attachment models place their mount point. */
    private static final Vector3f ATTACHMENT_ORIGIN = new Vector3f(8f, 8f, 8f);

    private GunRenderer() {}

    /**
     * Draws the weapon in model space: the caller's pose maps model pixels / 16 to the target space.
     *
     * @param pose animated bones; may be null for a static pose
     */
    public static void render(WeaponDefinition def, GunVisuals visuals, GunAttachments attachments, PoseState pose,
                              PoseStack ps, MultiBufferSource buffers, int light, int overlay) {
        String skin = attachments.get(AttachmentSlot.SKIN).map(AttachmentDefinition::skinTexture).orElse(null);
        AttachmentDefinition magazine = attachments.get(AttachmentSlot.MAGAZINE).orElse(null);
        boolean drum = magazine != null && magazine.name().equals("drum_mag");
        boolean extended = magazine != null && magazine.name().equals("extended_mag");
        boolean hasOptic = attachments.get(AttachmentSlot.OPTIC).isPresent();
        boolean hasStock = attachments.get(AttachmentSlot.STOCK).isPresent();
        boolean hasMuzzle = attachments.get(AttachmentSlot.MUZZLE).isPresent();

        for (String part : GunModels.PARTS) {
            if (part.equals("sights") && hasOptic) continue;
            if (part.equals("stock") && hasStock) continue;
            if (part.equals("muzzle") && hasMuzzle) continue;
            if (part.equals("magazine") && drum) continue;
            ps.pushPose();
            if (part.equals("action")) applyBone(ps, pose == null ? null : pose.get(PoseState.ACTION), visuals.pivot("action"));
            if (part.equals("magazine")) {
                Vector3f pivot = visuals.pivot("magazine");
                applyBone(ps, pose == null ? null : pose.get(PoseState.MAGAZINE), pivot);
                if (extended) scaleAround(ps, pivot, 1f, 1.45f, 1f);
            }
            GunModels.INSTANCE.draw(buffers, ps, GunModels.part(def, part), skin, light, overlay);
            ps.popPose();
        }

        for (AttachmentDefinition att : attachments.resolved()) {
            if (att.slot() == AttachmentSlot.SKIN) continue;
            if (att.slot() == AttachmentSlot.MAGAZINE && !drum) continue;
            if (!def.slots().contains(att.slot())) continue;
            ps.pushPose();
            if (att.slot() == AttachmentSlot.MAGAZINE) applyBone(ps, pose == null ? null : pose.get(PoseState.MAGAZINE), visuals.pivot("magazine"));
            Vector3f mount = visuals.mount(att.slot());
            ps.translate((mount.x - ATTACHMENT_ORIGIN.x) / 16f, (mount.y - ATTACHMENT_ORIGIN.y) / 16f, (mount.z - ATTACHMENT_ORIGIN.z) / 16f);
            GunModels.INSTANCE.draw(buffers, ps, GunModels.attachment(att.name()), null, light, overlay);
            ps.popPose();
        }
    }

    /** Applies a bone transform (pixels and degrees, Z-Y-X order) around a pivot given in model pixels. */
    public static void applyBone(PoseStack ps, BonePose bone, Vector3f pivot) {
        if (bone == null) return;
        ps.translate((pivot.x + bone.position.x) / 16f, (pivot.y + bone.position.y) / 16f, (pivot.z + bone.position.z) / 16f);
        if (bone.rotation.z != 0f) ps.mulPose(Axis.ZP.rotationDegrees(bone.rotation.z));
        if (bone.rotation.y != 0f) ps.mulPose(Axis.YP.rotationDegrees(bone.rotation.y));
        if (bone.rotation.x != 0f) ps.mulPose(Axis.XP.rotationDegrees(bone.rotation.x));
        ps.scale(bone.scale.x, bone.scale.y, bone.scale.z);
        ps.translate(-pivot.x / 16f, -pivot.y / 16f, -pivot.z / 16f);
    }

    private static void scaleAround(PoseStack ps, Vector3f pivot, float x, float y, float z) {
        ps.translate(pivot.x / 16f, pivot.y / 16f, pivot.z / 16f);
        ps.scale(x, y, z);
        ps.translate(-pivot.x / 16f, -pivot.y / 16f, -pivot.z / 16f);
    }

    /** Height of the sight line above an optic's mount point, in model pixels. */
    public static float opticSightHeight(String opticName) {
        return switch (opticName) {
            case "holo_reflex" -> 2.5f;
            case "tripoint_holo" -> 3.0f;
            case "ranger_scope", "longeye_scope" -> 3.0f;
            default -> 2.5f;
        };
    }

    /** Length a muzzle device extends the barrel, in model pixels (moves the muzzle flash forward). */
    public static float muzzleExtension(GunAttachments attachments) {
        return attachments.get(AttachmentSlot.MUZZLE).map(a -> switch (a.name()) {
            case "suppressor" -> 7f;
            case "shotgun_choke" -> 2f;
            default -> 3f;
        }).orElse(0f);
    }
}
