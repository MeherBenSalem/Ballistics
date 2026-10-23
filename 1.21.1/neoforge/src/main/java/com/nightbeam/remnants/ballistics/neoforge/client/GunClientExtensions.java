package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.client.render.GunArmPose;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Client item hooks for weapons: the custom renderer and third-person arm poses. */
public final class GunClientExtensions implements IClientItemExtensions {
    public static final GunClientExtensions INSTANCE = new GunClientExtensions();

    private GunClientExtensions() {}

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return GunItemRenderer.INSTANCE;
    }

    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return GunArmPose.of(entity, hand, stack);
    }
}
