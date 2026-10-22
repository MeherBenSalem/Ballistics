package com.nightbeam.remnants.ballistics.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.recipe.ConfigConditions;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** The {@code remnants_ballistics:config} recipe condition, read from {@code neoforge:conditions}. */
public final class NeoForgeConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, RemnantsBallistics.MODID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCondition>> CONFIG =
            CONDITIONS.register("config", () -> ConfigCondition.CODEC);

    private NeoForgeConditions() {}

    public record ConfigCondition(String option) implements ICondition {
        public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)
        ).apply(b, ConfigCondition::new));

        @Override
        public boolean test(IContext context) {
            return ConfigConditions.test(option);
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }
}
