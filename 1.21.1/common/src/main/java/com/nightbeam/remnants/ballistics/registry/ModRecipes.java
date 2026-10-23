package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.recipe.AmmoPressRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
    public static final RegistryHolder<RecipeType<AmmoPressRecipe>> AMMO_PRESS_TYPE = Registrar.register(Registries.RECIPE_TYPE, "ammo_press",
            () -> new RecipeType<AmmoPressRecipe>() {
                @Override
                public String toString() {
                    return RemnantsBallistics.id("ammo_press").toString();
                }
            });
    public static final RegistryHolder<RecipeSerializer<AmmoPressRecipe>> AMMO_PRESS_SERIALIZER = Registrar.register(Registries.RECIPE_SERIALIZER,
            "ammo_press", AmmoPressRecipe.Serializer::new);

    private ModRecipes() {}

    static void init() {}
}
