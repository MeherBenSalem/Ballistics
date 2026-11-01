package com.nightbeam.remnants.ballistics.forge.loot;

import com.mojang.serialization.Codec;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Forge-only registry: global loot modifier serializers (data-load conditions are registered with CraftingHelper). */
public final class ForgeLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RemnantsBallistics.MODID);

    public static final RegistryObject<Codec<ConfigurableLootModifier>> CONFIGURABLE =
            SERIALIZERS.register("configurable_table", ConfigurableLootModifier.CODEC);

    private ForgeLootModifiers() {}
}
