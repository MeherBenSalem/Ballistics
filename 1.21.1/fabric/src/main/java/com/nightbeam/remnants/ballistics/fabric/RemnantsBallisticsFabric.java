package com.nightbeam.remnants.ballistics.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ServerHooks;
import com.nightbeam.remnants.ballistics.config.CommonConfig;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.loot.LootInjections;
import com.nightbeam.remnants.ballistics.recipe.ConfigConditions;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

/** Fabric entry point: registries, configs (through Forge Config API Port), payloads and server-side hooks. */
public final class RemnantsBallisticsFabric implements ModInitializer {
    private static final ResourceConditionType<ConfigCondition> CONFIG_CONDITION =
            ResourceConditionType.create(ConfigConditions.ID, ConfigCondition.CODEC);

    @Override
    public void onInitialize() {
        RemnantsBallistics.init();
        NeoForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(RemnantsBallistics.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);
        ResourceConditions.register(CONFIG_CONDITION);
        FabricNetwork.register();

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) ServerHooks.playerTick(player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> ServerHooks.playerLoggedOut(handler.player));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerHooks.serverStopped());
        EntityTrackingEvents.START_TRACKING.register((target, viewer) -> ServerHooks.startTracking(viewer, target));
        // Weapons are fired through payloads; the attack key never melees with one (the client also blocks it).
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
                player.getMainHandItem().getItem() instanceof GunItem ? InteractionResult.FAIL : InteractionResult.PASS);
        LootTableEvents.MODIFY.register((key, table, source, registries) -> {
            for (LootInjections.Injection injection : LootInjections.forTable(key.location())) table.withPool(LootInjections.pool(injection));
        });

        RemnantsBallistics.setup();
    }

    /** {@code {"condition": "remnants_ballistics:config", "option": "..."}} in {@code fabric:load_conditions}. */
    record ConfigCondition(String option) implements ResourceCondition {
        static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
                Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)
        ).apply(b, ConfigCondition::new));

        @Override
        public ResourceConditionType<?> getType() {
            return CONFIG_CONDITION;
        }

        @Override
        public boolean test(@Nullable HolderLookup.Provider registries) {
            return ConfigConditions.test(option);
        }
    }
}
