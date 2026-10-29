package com.nightbeam.remnants.ballistics.neoforge.client;

import com.nightbeam.remnants.ballistics.client.render.GunArmPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

/** Client item hooks for weapons: third-person arm poses (the weapon model itself is the item model type). */
public final class GunClientExtensions implements IClientItemExtensions {
    public static final GunClientExtensions INSTANCE = new GunClientExtensions();

    private GunClientExtensions() {}

    @Override
    public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        return GunArmPose.of(entity, hand, stack);
    }
}
