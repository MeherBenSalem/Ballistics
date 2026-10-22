package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.recipe.AmmoPressRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, RemnantsBallistics.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, RemnantsBallistics.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AmmoPressRecipe>> AMMO_PRESS_TYPE = TYPES.register("ammo_press",
            () -> RecipeType.simple(RemnantsBallistics.id("ammo_press")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AmmoPressRecipe>> AMMO_PRESS_SERIALIZER = SERIALIZERS.register("ammo_press",
            AmmoPressRecipe.Serializer::new);

    private ModRecipes() {}
}
