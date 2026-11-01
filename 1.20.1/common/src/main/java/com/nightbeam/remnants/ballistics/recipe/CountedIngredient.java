package com.nightbeam.remnants.ballistics.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient with a count, written flat in JSON (the same shape as NeoForge's sized ingredients on 1.21, so recipes are
 * identical across loaders and versions): {@code {"item": "minecraft:gunpowder", "count": 2}} or
 * {@code {"tag": "c:steel_ingots"}}; the count defaults to 1.
 */
public record CountedIngredient(Ingredient ingredient, int count) {
    public static CountedIngredient fromJson(JsonElement element) {
        JsonObject json = GsonHelper.convertToJsonObject(element, "ingredient").deepCopy();
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count < 1) throw new JsonParseException("Ingredient count must be at least 1, got " + count);
        json.remove("count");
        Ingredient ingredient = Ingredient.fromJson(json, false);
        return new CountedIngredient(ingredient, count);
    }

    public static CountedIngredient fromNetwork(FriendlyByteBuf buf) {
        return new CountedIngredient(Ingredient.fromNetwork(buf), buf.readVarInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        ingredient.toNetwork(buf);
        buf.writeVarInt(count);
    }
}
