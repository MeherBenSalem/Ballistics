package com.nightbeam.remnants.ballistics.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Weapon schematics found while exploring. Required by higher-tier weapon recipes and returned after crafting,
 * so a blueprint is a permanent unlock rather than a consumable.
 */
public class BlueprintItem extends Item {
    private final int tier;

    public BlueprintItem(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.blueprint", tier).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.blueprint_reusable").withStyle(ChatFormatting.DARK_GRAY));
    }
}
