package com.nightbeam.remnants.ballistics.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An ingredient with a count, in the same JSON shape as NeoForge's sized ingredients, so recipes are identical on both
 * loaders: {@code {"ingredient": "minecraft:gunpowder", "count": 2}} or {@code {"ingredient": "#c:ingots/steel"}};
 * the count defaults to 1.
 */
public record CountedIngredient(Ingredient ingredient, int count) {
    public static final Codec<CountedIngredient> FLAT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(CountedIngredient::ingredient),
            Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(CountedIngredient::count)
    ).apply(inst, CountedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CountedIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CountedIngredient::ingredient,
            ByteBufCodecs.VAR_INT, CountedIngredient::count,
            CountedIngredient::new);
}
