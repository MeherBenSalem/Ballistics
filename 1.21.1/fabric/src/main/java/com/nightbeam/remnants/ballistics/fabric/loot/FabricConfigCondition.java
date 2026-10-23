package com.nightbeam.remnants.ballistics.fabric.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Recipe condition backed by the common config: {@code "fabric:load_conditions": [{"condition":
 * "remnants_ballistics:config", "option": "weapon_crafting"}]}. The generated recipes carry it next to the identical
 * NeoForge condition.
 */
public record FabricConfigCondition(String option) implements ResourceCondition {
    public static final MapCodec<FabricConfigCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
            Codec.STRING.fieldOf("option").forGetter(FabricConfigCondition::option)
    ).apply(b, FabricConfigCondition::new));
    public static final ResourceConditionType<FabricConfigCondition> TYPE = ResourceConditionType.create(RemnantsBallistics.id("config"), CODEC);

    public static void register() {
        ResourceConditions.register(TYPE);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registries) {
        return CommonConfig.recipeEnabled(option);
    }
}
