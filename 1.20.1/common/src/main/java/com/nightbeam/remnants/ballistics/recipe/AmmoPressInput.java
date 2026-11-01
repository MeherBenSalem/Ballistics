package com.nightbeam.remnants.ballistics.recipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The three input slots of the ammo press (a container, as 1.20.1 recipes match against one). */
public final class AmmoPressInput extends SimpleContainer {
    public AmmoPressInput(List<ItemStack> items) {
        super(items.toArray(new ItemStack[0]));
    }

    public int size() {
        return getContainerSize();
    }
}
