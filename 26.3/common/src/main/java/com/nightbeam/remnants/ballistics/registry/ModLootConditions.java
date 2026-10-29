package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.loot.ConfigChanceCondition;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class ModLootConditions {
    public static final RegistryHolder<MapCodec<? extends LootItemCondition>> CONFIG_CHANCE = Registrar.register(Registries.LOOT_CONDITION_TYPE,
            "config_chance", () -> ConfigChanceCondition.CODEC);

    private ModLootConditions() {}

    static void init() {}
}
