package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.loot.ConfigChanceCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ModLootConditions {
    public static final RegistryHolder<LootItemConditionType> CONFIG_CHANCE = Registrar.register(Registries.LOOT_CONDITION_TYPE, "config_chance",
            () -> new LootItemConditionType(ConfigChanceCondition.CODEC));

    private ModLootConditions() {}

    static void init() {}
}
