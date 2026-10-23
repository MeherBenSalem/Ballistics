package com.nightbeam.remnants.ballistics.neoforge.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe/data condition backed by the common config, e.g.
 * {@code "neoforge:conditions": [{"type": "remnants_ballistics:config", "option": "weapon_crafting"}]}. Fabric reads the
 * same option from {@code fabric:load_conditions}.
 */
public record ConfigCondition(String option) implements ICondition {
    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)
    ).apply(b, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        return CommonConfig.recipeEnabled(option);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
