package com.nightbeam.remnants.ballistics.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;


/** Restores a fraction of a weapon's durability when used in the Gunsmithing Workbench repair slot. */
public class RepairKitItem extends Item {
    public static final float REPAIR_FRACTION = 0.35f;

    public RepairKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.repair_kit", Math.round(REPAIR_FRACTION * 100)).withStyle(ChatFormatting.GRAY));
    }
}
