package com.nightbeam.remnants.ballistics.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient with a count, written flat in JSON (the same shape as NeoForge's sized ingredients, so recipes are
 * identical on both loaders): {@code {"item": "minecraft:gunpowder", "count": 2}} or {@code {"tag": "c:ingots/steel"}};
 * the count defaults to 1.
 */
public record CountedIngredient(Ingredient ingredient, int count) {
    public static final Codec<CountedIngredient> FLAT_CODEC = Codec.PASSTHROUGH.comapFlatMap(CountedIngredient::decode, CountedIngredient::encode);

    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
            ByteBufCodecs.VAR_INT, CountedIngredient::count,
            CountedIngredient::new);

    private static DataResult<CountedIngredient> decode(Dynamic<?> json) {
        int count = json.get("count").asInt(1);
        if (count < 1) return DataResult.error(() -> "Ingredient count must be at least 1, got " + count);
        return Ingredient.CODEC_NONEMPTY.parse(json.remove("count")).map(ingredient -> new CountedIngredient(ingredient, count));
    }

    private static Dynamic<?> encode(CountedIngredient value) {
        JsonElement ingredient = Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, value.ingredient()).getOrThrow();
        Dynamic<JsonElement> json = new Dynamic<>(JsonOps.INSTANCE, ingredient);
        return value.count() == 1 ? json : json.set("count", json.createInt(value.count()));
    }
}
