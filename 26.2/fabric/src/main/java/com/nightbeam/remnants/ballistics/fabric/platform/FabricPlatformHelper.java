package com.nightbeam.remnants.ballistics.fabric.platform;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.platform.services.IPlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public final class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    /** Fabric orders mod tabs by registration order. */
    @Override
    public CreativeModeTab.Builder creativeTabBuilder(@Nullable ResourceKey<CreativeModeTab> after) {
        return FabricCreativeModeTab.builder();
    }

    /** Fabric has no projectile-impact event. */
    @Override
    public boolean cancelProjectileImpact(Projectile projectile, HitResult hit) {
        return false;
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayer.get(level, profile);
    }
}
