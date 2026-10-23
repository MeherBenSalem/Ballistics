package com.nightbeam.remnants.ballistics.neoforge.loot;

import com.mojang.serialization.MapCodec;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** NeoForge-only registries: global loot modifier serializers and data-load condition codecs. */
public final class NeoForgeLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RemnantsBallistics.MODID);
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RemnantsBallistics.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ConfigurableLootModifier>> CONFIGURABLE =
            SERIALIZERS.register("configurable_table", () -> ConfigurableLootModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCondition>> CONFIG_CONDITION =
            CONDITIONS.register("config", () -> ConfigCondition.CODEC);

    private NeoForgeLootModifiers() {}
}
