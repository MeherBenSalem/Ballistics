package com.nightbeam.remnants.ballistics.platform.services;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/** Loader hooks the common code needs. */
public interface IPlatformHelper {
    /** "Fabric" or "NeoForge". */
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    /** True on the physical client (including its integrated server), false on a dedicated server. */
    boolean isPhysicalClient();

    /** A creative tab builder; {@code after} orders the tab after another one where the loader supports it. */
    CreativeModeTab.Builder creativeTabBuilder(@Nullable ResourceKey<CreativeModeTab> after);

    /** Fires the loader's projectile-impact event; true if a listener cancelled the impact. */
    boolean cancelProjectileImpact(Projectile projectile, HitResult hit);

    /** A server-side fake player (GameTests drive weapons with one). */
    ServerPlayer fakePlayer(ServerLevel level, GameProfile profile);
}
