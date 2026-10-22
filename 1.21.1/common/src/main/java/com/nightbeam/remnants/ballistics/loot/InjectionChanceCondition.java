package com.nightbeam.remnants.ballistics.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.registry.ModLootConditions;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Passes with {@code chance × lootChanceMultiplier} when the injection's config category ({@code structure} or
 * {@code mob}) is enabled. Evaluated on every roll, so config changes apply without reloading loot tables.
 */
public record InjectionChanceCondition(String category, float chance) implements LootItemCondition {
    public static final MapCodec<InjectionChanceCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Codec.STRING.fieldOf("category").forGetter(InjectionChanceCondition::category),
            Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(InjectionChanceCondition::chance)
    ).apply(b, InjectionChanceCondition::new));

    public static LootItemCondition.Builder of(String category, float chance) {
        return () -> new InjectionChanceCondition(category, chance);
    }

    @Override
    public boolean test(LootContext context) {
        boolean enabled = switch (category) {
            case "structure" -> CommonConfig.get(CommonConfig.STRUCTURE_LOOT);
            case "mob" -> CommonConfig.get(CommonConfig.MOB_DROPS);
            default -> true;
        };
        if (!enabled) return false;
        double scaled = Math.min(1.0, chance * CommonConfig.get(CommonConfig.LOOT_CHANCE_MULTIPLIER));
        return context.getRandom().nextFloat() < scaled;
    }

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.INJECTION_CHANCE.get();
    }
}
