package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nightbeam.remnants.ballistics.client.anim.PoseState;
import com.nightbeam.remnants.ballistics.client.anim.WeaponAnimator;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Item renderer for weapons outside first person: the crisp 2D icon in GUIs, and the animated 3D part model in
 * third person, on the ground and in item frames. The item model JSON ({@code builtin/entity}) supplies the display
 * transforms; this renderer draws in item model space (0..1 = 16 pixels).
 */
public final class GunItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final GunItemRenderer INSTANCE = new GunItemRenderer();
    /** Entity currently being rendered by a living renderer, so held weapons can show that entity's animation. */
    private static LivingEntity renderingEntity;

    private GunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack ps, MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof GunItem gun)) return;
        WeaponDefinition def = gun.definition();
        if (context == ItemDisplayContext.GUI) {
            GunRenderer.draw(buffers.getBuffer(Sheets.cutoutBlockSheet()), ps, GunModels.quads(GunModels.icon(def)), light, overlay);
            return;
        }
        PoseState pose = null;
        LivingEntity holder = renderingEntity;
        if (holder != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) && holder.getMainHandItem() == stack) {
            pose = WeaponAnimator.thirdPerson(holder.getId(), holder == Minecraft.getInstance().player);
        }
        GunRenderer.render(def, GunVisualsLoader.INSTANCE.get(def), GunData.attachments(stack), pose, ps, buffers, light, overlay);
    }

    /**
     * Hook around every living-entity render (called by each loader): the entity being drawn, or null once it is done,
     * so a held weapon can show that entity's animation.
     */
    public static void setRenderingEntity(LivingEntity entity) {
        renderingEntity = entity;
    }
}
