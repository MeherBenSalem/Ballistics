package com.nightbeam.remnants.ballistics.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * Recipe/data condition backed by the common config, e.g.
 * {@code {"type": "remnants_ballistics:config", "option": "weapon_crafting"}}.
 */
public record ConfigCondition(String option) implements ICondition {
    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)
    ).apply(b, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        return switch (option) {
            case "weapon_crafting" -> CommonConfig.get(CommonConfig.WEAPON_CRAFTING);
            case "ammo_crafting" -> CommonConfig.get(CommonConfig.AMMO_CRAFTING);
            case "attachment_crafting" -> CommonConfig.get(CommonConfig.ATTACHMENT_CRAFTING);
            default -> true;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
