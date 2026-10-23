package com.nightbeam.remnants.ballistics.neoforge.platform;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.platform.services.IPlatformHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public final class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public boolean isPhysicalClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder(@Nullable ResourceKey<CreativeModeTab> after) {
        CreativeModeTab.Builder builder = CreativeModeTab.builder();
        return after == null ? builder : builder.withTabsBefore(after);
    }

    @Override
    public boolean cancelProjectileImpact(Projectile projectile, HitResult hit) {
        return EventHooks.onProjectileImpact(projectile, hit);
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayerFactory.get(level, profile);
    }
}
