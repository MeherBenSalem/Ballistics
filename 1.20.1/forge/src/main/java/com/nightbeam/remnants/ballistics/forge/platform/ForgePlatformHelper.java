package com.nightbeam.remnants.ballistics.forge.platform;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.platform.services.IPlatformHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

public final class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Forge";
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
        return ForgeEventFactory.onProjectileImpact(projectile, hit);
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayerFactory.get(level, profile);
    }
}
