package com.nightbeam.remnants.ballistics.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.registry.ModLootConditions;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Passes when the loot category ({@code structure} or {@code mob}) is enabled in the common config, with probability
 * {@code chance x lootChanceMultiplier}. Gates the pools Fabric injects into vanilla tables; Forge applies the same
 * rule in its global loot modifier.
 */
public record ConfigChanceCondition(String category, float chance) implements LootItemCondition {
    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.CONFIG_CHANCE.get();
    }

    @Override
    public boolean test(LootContext context) {
        return CommonConfig.lootEnabled(category) && context.getRandom().nextFloat() < CommonConfig.scaledLootChance(chance);
    }

    /** {@code {"condition": "remnants_ballistics:config_chance", "category": "structure", "chance": 0.5}} */
    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConfigChanceCondition> {
        @Override
        public void serialize(JsonObject json, ConfigChanceCondition condition, JsonSerializationContext context) {
            json.addProperty("category", condition.category());
            json.addProperty("chance", condition.chance());
        }

        @Override
        public ConfigChanceCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new ConfigChanceCondition(GsonHelper.getAsString(json, "category"),
                    Mth.clamp(GsonHelper.getAsFloat(json, "chance", 1f), 0f, 1f));
        }
    }
}
