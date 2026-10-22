package com.nightbeam.remnants.ballistics.forge;

import com.google.gson.JsonObject;
import com.nightbeam.remnants.ballistics.recipe.ConfigConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/** The {@code remnants_ballistics:config} recipe condition, read from a recipe's {@code conditions} list. */
public final class ForgeConditions {
    private ForgeConditions() {}

    public record ConfigCondition(String option) implements ICondition {
        @Override
        public ResourceLocation getID() {
            return ConfigConditions.ID;
        }

        @Override
        public boolean test(IContext context) {
            return ConfigConditions.test(option);
        }

        public enum Serializer implements IConditionSerializer<ConfigCondition> {
            INSTANCE;

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
                return ConfigConditions.ID;
            }
        }
    }
}
