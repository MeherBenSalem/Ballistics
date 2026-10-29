package com.nightbeam.remnants.ballistics.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;


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
    public ItemStackTemplate getCraftingRemainder(ItemInstance instance) {
        return new ItemStackTemplate(this);
    }

    /** Fabric (FabricItem). */
    public ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        return new ItemStackTemplate(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.blueprint", tier).withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.blueprint_reusable").withStyle(ChatFormatting.DARK_GRAY));
    }
}
