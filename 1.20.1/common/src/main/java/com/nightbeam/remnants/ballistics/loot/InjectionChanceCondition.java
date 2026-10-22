package com.nightbeam.remnants.ballistics.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.registry.ModLootConditions;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/**
 * Passes with {@code chance × lootChanceMultiplier} when the injection's config category ({@code structure} or
 * {@code mob}) is enabled. Evaluated on every roll, so config changes apply without reloading loot tables.
 */
public record InjectionChanceCondition(String category, float chance) implements LootItemCondition {
    public static LootItemCondition.Builder of(String category, float chance) {
        return () -> new InjectionChanceCondition(category, chance);
    }

    @Override
    public boolean test(LootContext context) {
        boolean enabled = switch (category) {
            case "structure" -> CommonConfig.get(CommonConfig.STRUCTURE_LOOT);
            case "mob" -> CommonConfig.get(CommonConfig.MOB_DROPS);
            default -> true;
        };
        if (!enabled) return false;
        double scaled = Math.min(1.0, chance * CommonConfig.get(CommonConfig.LOOT_CHANCE_MULTIPLIER));
        return context.getRandom().nextFloat() < scaled;
    }

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.INJECTION_CHANCE.get();
    }

    public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<InjectionChanceCondition> {
        @Override
        public void serialize(JsonObject json, InjectionChanceCondition condition, JsonSerializationContext context) {
            json.addProperty("category", condition.category());
            json.addProperty("chance", condition.chance());
        }

        @Override
        public InjectionChanceCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new InjectionChanceCondition(GsonHelper.getAsString(json, "category"), GsonHelper.getAsFloat(json, "chance"));
        }
    }
}
