package com.nightbeam.remnants.ballistics.forge.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.function.Supplier;

/**
 * Rolls an extra loot table into matching tables, gated by the common config ({@code structure} or {@code mob}
 * category) and scaled by {@code lootChanceMultiplier}. Target tables are chosen with standard loot conditions.
 * Fabric reads the same JSON and injects a pool instead (see {@code FabricLootInjection}).
 */
public class ConfigurableLootModifier extends LootModifier {
    public static final Supplier<Codec<ConfigurableLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
            ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table),
            Codec.STRING.fieldOf("category").forGetter(m -> m.category),
            Codec.floatRange(0f, 1f).optionalFieldOf("chance", 1f).forGetter(m -> m.chance)
    )).apply(inst, ConfigurableLootModifier::new)));

    private final ResourceLocation table;
    private final String category;
    private final float chance;

    public ConfigurableLootModifier(LootItemCondition[] conditions, ResourceLocation table, String category, float chance) {
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
        // Raw roll: modifiers are not re-applied to the sub-table (same approach as Forge's add_table modifiers).
        LootTable extra = context.getResolver().getLootTable(table);
        if (extra != LootTable.EMPTY) extra.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
