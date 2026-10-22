package com.nightbeam.remnants.ballistics.recipe;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The three input slots of the ammo press, as the container recipes match against. */
public final class AmmoPressInput extends SimpleContainer {
    public AmmoPressInput(List<ItemStack> items) {
        super(items.toArray(ItemStack[]::new));
    }

    public int size() {
        return getContainerSize();
    }
}
