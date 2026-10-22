package com.nightbeam.remnants.ballistics.forge.client;

import com.nightbeam.remnants.ballistics.client.ClientHooks;
import com.nightbeam.remnants.ballistics.client.render.GunArmPose;
import com.nightbeam.remnants.ballistics.client.render.GunItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Client-only state the common mod constructor reaches through {@code FMLEnvironment.dist}. */
public final class ForgeClientHooks {
    /** Weapon item renderer and third-person arm pose (Forge attaches these in {@code Item#initializeClient}). */
    public static final IClientItemExtensions GUN_EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return GunItemRenderer.INSTANCE;
        }

        @Override
        public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
            return GunArmPose.armPose(entity, hand, stack);
        }
    };

    private ForgeClientHooks() {}

    public static void init() {
        ClientHooks.init();
    }
}
