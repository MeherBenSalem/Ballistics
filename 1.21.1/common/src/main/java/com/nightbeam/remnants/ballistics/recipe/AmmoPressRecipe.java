package com.nightbeam.remnants.ballistics.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Shapeless recipe for the ammunition press: one to three ingredients with counts, any slot order.
 * <pre>{"type":"remnants_ballistics:ammo_press","ingredients":[{"ingredient":{"item":"..."},"count":1}],"result":{"id":"...","count":16}}</pre>
 */
public record AmmoPressRecipe(List<CountedIngredient> ingredients, ItemStack result) implements Recipe<AmmoPressInput> {
    public static final int SLOTS = 3;

    public static final MapCodec<AmmoPressRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            CountedIngredient.CODEC.listOf()
                    .validate(l -> l.isEmpty() || l.size() > SLOTS
                            ? DataResult.error(() -> "Ammo press recipes need 1 to " + SLOTS + " ingredients")
                            : DataResult.success(l))
                    .fieldOf("ingredients").forGetter(AmmoPressRecipe::ingredients),
            ItemStack.CODEC.fieldOf("result").forGetter(AmmoPressRecipe::result)
    ).apply(inst, AmmoPressRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmmoPressRecipe> STREAM_CODEC = StreamCodec.composite(
            CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(SLOTS)), AmmoPressRecipe::ingredients,
            ItemStack.STREAM_CODEC, AmmoPressRecipe::result,
            AmmoPressRecipe::new);

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
    public ItemStack assemble(AmmoPressInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
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
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.AMMO_PRESS_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.AMMO_PRESS_TYPE.get();
    }

    public static final class Serializer implements RecipeSerializer<AmmoPressRecipe> {
        @Override
        public MapCodec<AmmoPressRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AmmoPressRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
