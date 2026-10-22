package com.nightbeam.remnants.ballistics.recipe;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * The {@code remnants_ballistics:config} recipe condition, e.g. {@code {"option": "weapon_crafting"}}. Recipes carry it
 * as both {@code neoforge:conditions} and {@code fabric:load_conditions}; each loader registers its own wrapper.
 */
public final class ConfigConditions {
    public static final ResourceLocation ID = RemnantsBallistics.id("config");

    private ConfigConditions() {}

    public static boolean test(String option) {
        return switch (option) {
            case "weapon_crafting" -> CommonConfig.get(CommonConfig.WEAPON_CRAFTING);
            case "ammo_crafting" -> CommonConfig.get(CommonConfig.AMMO_CRAFTING);
            case "attachment_crafting" -> CommonConfig.get(CommonConfig.ATTACHMENT_CRAFTING);
            default -> true;
        };
    }
}
