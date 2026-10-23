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

    // Blueprints stay in the grid after crafting. Loader hooks without @Override: common code compiles against vanilla,
    // and each method overrides the loader's default at runtime.

    /** NeoForge (IItemExtension). */
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    /** NeoForge (IItemExtension). */
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    /** Fabric (FabricItem). */
    public ItemStack getRecipeRemainder(ItemStack stack) {
        return stack.copyWithCount(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.blueprint", tier).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.blueprint_reusable").withStyle(ChatFormatting.DARK_GRAY));
    }
}
