package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.loot.InjectionChanceCondition;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class ModLootConditions {
    public static final RegistrationProvider<LootItemConditionType> CONDITIONS = RegistrationProvider.get(Registries.LOOT_CONDITION_TYPE, RemnantsBallistics.MODID);

    public static final RegistryObject<LootItemConditionType, LootItemConditionType> INJECTION_CHANCE = CONDITIONS.register("injection_chance",
            () -> new LootItemConditionType(InjectionChanceCondition.CODEC));

    private ModLootConditions() {}

    static void init() {}
}
