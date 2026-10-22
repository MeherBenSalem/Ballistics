package com.nightbeam.remnants.ballistics.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

/** An ingredient with a required stack size: {@code {"ingredient": {"item": "..."}, "count": 2}}. */
public record CountedIngredient(Ingredient ingredient, int count) {
    public static CountedIngredient fromJson(JsonObject json) {
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1) throw new IllegalArgumentException("count must be positive, was " + count);
        return new CountedIngredient(Ingredient.fromJson(json.get("ingredient")), count);
    }

    public static CountedIngredient fromNetwork(FriendlyByteBuf buf) {
        return new CountedIngredient(Ingredient.fromNetwork(buf), buf.readVarInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeVarInt(count);
    }
}
