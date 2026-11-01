package com.nightbeam.remnants.ballistics.forge.loot;

import com.google.gson.JsonObject;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Recipe/data condition backed by the common config, e.g.
 * {@code "conditions": [{"type": "remnants_ballistics:config", "option": "weapon_crafting"}]}. Fabric reads the same
 * option from {@code fabric:load_conditions}.
 */
public record ConfigCondition(String option) implements ICondition {
    public static final ResourceLocation ID = RemnantsBallistics.id("config");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return CommonConfig.recipeEnabled(option);
    }

    public static final class Serializer implements IConditionSerializer<ConfigCondition> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public void write(JsonObject json, ConfigCondition value) {
            json.addProperty("option", value.option());
        }

        @Override
        public ConfigCondition read(JsonObject json) {
            return new ConfigCondition(GsonHelper.getAsString(json, "option"));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
