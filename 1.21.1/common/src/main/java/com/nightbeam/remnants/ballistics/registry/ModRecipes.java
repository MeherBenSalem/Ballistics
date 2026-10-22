package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.recipe.AmmoPressRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipes {
    public static final RegistrationProvider<RecipeType<?>> TYPES = RegistrationProvider.get(Registries.RECIPE_TYPE, RemnantsBallistics.MODID);
    public static final RegistrationProvider<RecipeSerializer<?>> SERIALIZERS = RegistrationProvider.get(Registries.RECIPE_SERIALIZER, RemnantsBallistics.MODID);

    public static final RegistryObject<RecipeType<?>, RecipeType<AmmoPressRecipe>> AMMO_PRESS_TYPE = TYPES.register("ammo_press",
            () -> recipeType("ammo_press"));
    public static final RegistryObject<RecipeSerializer<?>, RecipeSerializer<AmmoPressRecipe>> AMMO_PRESS_SERIALIZER = SERIALIZERS.register("ammo_press",
            AmmoPressRecipe.Serializer::new);

    private ModRecipes() {}

    static void init() {}

    private static <T extends Recipe<?>> RecipeType<T> recipeType(String name) {
        String id = RemnantsBallistics.id(name).toString();
        return new RecipeType<>() {
            @Override
            public String toString() {
                return id;
            }
        };
    }
}
