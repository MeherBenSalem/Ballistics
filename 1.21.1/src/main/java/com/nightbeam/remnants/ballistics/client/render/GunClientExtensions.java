package com.nightbeam.remnants.ballistics.client.render;

import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.client.Minecraft;
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

    /**
     * Aiming raises both arms along the view (bow pose); long guns are otherwise carried at the ready
     * (crossbow pose); pistols hang in one hand. Sprinting lowers the weapon.
     */
    @Override
    public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
        if (hand != InteractionHand.MAIN_HAND || !(stack.getItem() instanceof GunItem gun)) return null;
        boolean aiming;
        if (entity == Minecraft.getInstance().player) {
            aiming = ClientWeaponState.INSTANCE.aiming;
        } else {
            RemoteWeaponStates.State st = RemoteWeaponStates.peek(entity.getId());
            aiming = st != null && (st.aiming || st.charging || System.nanoTime() - st.lastShotNanos < 1_500_000_000L);
        }
        if (entity.isSprinting()) return HumanoidModel.ArmPose.ITEM;
        if (aiming) return HumanoidModel.ArmPose.BOW_AND_ARROW;
        return gun.definition().category().isTwoHanded() ? HumanoidModel.ArmPose.CROSSBOW_HOLD : HumanoidModel.ArmPose.ITEM;
    }
}
