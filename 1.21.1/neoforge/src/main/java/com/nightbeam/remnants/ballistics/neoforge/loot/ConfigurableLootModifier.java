package com.nightbeam.remnants.ballistics.neoforge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Rolls an extra loot table into matching tables, gated by the common config ({@code structure} or {@code mob}
 * category) and scaled by {@code lootChanceMultiplier}. Target tables are chosen with standard loot conditions.
 * Fabric reads the same JSON and injects a pool instead (see {@code FabricLootInjection}).
 */
public class ConfigurableLootModifier extends LootModifier {
    public static final MapCodec<ConfigurableLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(inst.group(
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(m -> m.table),
            Codec.STRING.fieldOf("category").forGetter(m -> m.category),
            Codec.floatRange(0f, 1f).optionalFieldOf("chance", 1f).forGetter(m -> m.chance)
    )).apply(inst, ConfigurableLootModifier::new));

    private final ResourceKey<LootTable> table;
    private final String category;
    private final float chance;

    public ConfigurableLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table, String category, float chance) {
        super(conditions);
        this.table = table;
        this.category = category;
        this.chance = chance;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!CommonConfig.lootEnabled(category)) return generatedLoot;
        if (context.getRandom().nextFloat() >= CommonConfig.scaledLootChance(chance)) return generatedLoot;
        // Raw roll: modifiers are not re-applied to the sub-table (same approach as neoforge:add_table).
        context.getResolver().get(Registries.LOOT_TABLE, table).ifPresent(extra ->
                extra.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add)));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
