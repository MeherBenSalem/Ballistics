package com.nightbeam.remnants.ballistics.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Passes when the loot category ({@code structure} or {@code mob}) is enabled in the common config, with probability
 * {@code chance x lootChanceMultiplier}. Gates the pools Fabric injects into vanilla tables; NeoForge applies the same
 * rule in its global loot modifier.
 */
public record ConfigChanceCondition(String category, float chance) implements LootItemCondition {
    public static final MapCodec<ConfigChanceCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("category").forGetter(ConfigChanceCondition::category),
            Codec.floatRange(0f, 1f).optionalFieldOf("chance", 1f).forGetter(ConfigChanceCondition::chance)
    ).apply(inst, ConfigChanceCondition::new));

    @Override
    public MapCodec<ConfigChanceCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        return CommonConfig.lootEnabled(category) && context.getRandom().nextFloat() < CommonConfig.scaledLootChance(chance);
    }
}
