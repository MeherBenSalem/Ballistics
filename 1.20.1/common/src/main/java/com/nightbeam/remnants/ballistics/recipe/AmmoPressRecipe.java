package com.nightbeam.remnants.ballistics.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Shapeless recipe for the ammunition press: one to three ingredients with counts, any slot order.
 * <pre>{"type":"remnants_ballistics:ammo_press","ingredients":[{"ingredient":{"item":"..."},"count":1}],"result":{"item":"...","count":16}}</pre>
 */
public record AmmoPressRecipe(ResourceLocation id, List<CountedIngredient> ingredients, ItemStack result) implements Recipe<AmmoPressInput> {
    public static final int SLOTS = 3;

    @Override
    public boolean matches(AmmoPressInput input, Level level) {
        return assignment(input) != null;
    }

    /**
     * Maps each ingredient to a distinct input slot holding enough items; unused non-empty slots fail the match.
     *
     * @return slot index per ingredient, or null if the input does not match
     */
    public int[] assignment(AmmoPressInput input) {
        int[] slots = new int[ingredients.size()];
        boolean[] used = new boolean[input.size()];
        if (!assign(input, 0, slots, used)) return null;
        for (int i = 0; i < input.size(); i++) {
            if (!used[i] && !input.getItem(i).isEmpty()) return null;
        }
        return slots;
    }

    private boolean assign(AmmoPressInput input, int ingredientIndex, int[] slots, boolean[] used) {
        if (ingredientIndex == ingredients.size()) return true;
        CountedIngredient wanted = ingredients.get(ingredientIndex);
        for (int slot = 0; slot < input.size(); slot++) {
            if (used[slot]) continue;
            ItemStack stack = input.getItem(slot);
            if (stack.getCount() >= wanted.count() && wanted.ingredient().test(stack)) {
                used[slot] = true;
                slots[ingredientIndex] = slot;
                if (assign(input, ingredientIndex + 1, slots, used)) return true;
                used[slot] = false;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(AmmoPressInput input, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (CountedIngredient s : ingredients) list.add(s.ingredient());
        return list;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ModItems.AMMO_PRESS.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.AMMO_PRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.AMMO_PRESS_TYPE.get();
    }

    /**
     * The press has no recipe book. Special recipes are left out of the client recipe book, which otherwise logs
     * "Unknown recipe category" for every press recipe on loaders without a category API (Fabric).
     */
    @Override
    public boolean isSpecial() {
        return true;
    }

    public static final class Serializer implements RecipeSerializer<AmmoPressRecipe> {
        @Override
        public AmmoPressRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
            if (array.isEmpty() || array.size() > SLOTS) {
                throw new JsonSyntaxException("Ammo press recipes need 1 to " + SLOTS + " ingredients");
            }
            List<CountedIngredient> ingredients = new ArrayList<>();
            for (JsonElement element : array) ingredients.add(CountedIngredient.fromJson(GsonHelper.convertToJsonObject(element, "ingredient")));
            return new AmmoPressRecipe(id, List.copyOf(ingredients), ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")));
        }

        @Override
        public AmmoPressRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            List<CountedIngredient> ingredients = new ArrayList<>(size);
            for (int i = 0; i < size; i++) ingredients.add(CountedIngredient.fromNetwork(buf));
            return new AmmoPressRecipe(id, List.copyOf(ingredients), buf.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, AmmoPressRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (CountedIngredient ingredient : recipe.ingredients) ingredient.toNetwork(buf);
            buf.writeItem(recipe.result);
        }
    }
}
