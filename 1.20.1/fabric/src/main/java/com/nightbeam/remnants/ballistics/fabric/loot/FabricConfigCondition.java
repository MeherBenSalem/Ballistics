package com.nightbeam.remnants.ballistics.fabric.loot;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.util.GsonHelper;

/**
 * Recipe condition backed by the common config: {@code "fabric:load_conditions": [{"condition":
 * "remnants_ballistics:config", "option": "weapon_crafting"}]}. The generated recipes carry it next to the identical
 * Forge condition.
 */
public final class FabricConfigCondition {
    private FabricConfigCondition() {}

    public static void register() {
        ResourceConditions.register(RemnantsBallistics.id("config"), json -> CommonConfig.recipeEnabled(GsonHelper.getAsString(json, "option")));
    }
}
