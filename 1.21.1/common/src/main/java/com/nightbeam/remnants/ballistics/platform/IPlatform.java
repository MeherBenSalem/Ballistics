package com.nightbeam.remnants.ballistics.platform;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.weapon.BlueprintItem;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.HitResult;

/** Everything common code needs from the mod loader that vanilla does not provide. */
public interface IPlatform {
    /** "NeoForge", "Fabric" or "Forge". */
    String loaderName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    <T> RegistrationProvider<T> registry(ResourceKey<? extends Registry<T>> registry, String modId);

    /** A plain particle type (the vanilla constructor is not public). */
    SimpleParticleType simpleParticle(boolean overrideLimiter);

    /** A creative tab builder that integrates with the loader's tab layout (pagination, ordering). */
    CreativeModeTab.Builder creativeTab();

    /**
     * Weapon item with the loader-specific hooks that keep firing from swinging the arm, hitting in melee or replaying
     * the draw animation whenever ammunition changes.
     */
    GunItem gunItem(Item.Properties properties, WeaponDefinition definition);

    /** Blueprint item that stays in the crafting grid (the loader decides how stack-aware remainders work). */
    BlueprintItem blueprintItem(Item.Properties properties, int tier);

    /** A server-side player with no connection, for GameTests. */
    ServerPlayer fakePlayer(ServerLevel level, GameProfile profile);

    /** Fires the loader's projectile-impact event; true if a listener cancelled the impact. */
    boolean projectileImpactCancelled(Projectile projectile, HitResult hit);
}
