package com.nightbeam.remnants.ballistics.fabric;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.platform.IPlatform;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.weapon.BlueprintItem;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/** Fabric implementation of the platform services (listed in {@code META-INF/services}). */
public final class FabricPlatform implements IPlatform {
    @Override
    public String loaderName() {
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
    public <T> RegistrationProvider<T> registry(ResourceKey<? extends Registry<T>> registry, String modId) {
        return new Provider<>(registry, modId);
    }

    @Override
    public SimpleParticleType simpleParticle(boolean overrideLimiter) {
        return FabricParticleTypes.simple(overrideLimiter);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return FabricItemGroup.builder();
    }

    @Override
    public GunItem gunItem(Item.Properties properties, WeaponDefinition definition) {
        return new FabricGunItem(properties, definition);
    }

    @Override
    public BlueprintItem blueprintItem(Item.Properties properties, int tier) {
        return new FabricBlueprintItem(properties, tier);
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayer.get(level, profile);
    }

    @Override
    public boolean projectileImpactCancelled(Projectile projectile, HitResult hit) {
        return false; // Fabric has no projectile-impact event
    }

    /** Registers immediately: Fabric allows registration at any point before the registries freeze. */
    private static final class Provider<T> implements RegistrationProvider<T> {
        private final Registry<T> registry;
        private final ResourceKey<? extends Registry<T>> key;
        private final String modId;
        private final List<RegistryObject<T, ? extends T>> entries = new ArrayList<>();

        @SuppressWarnings("unchecked")
        Provider(ResourceKey<? extends Registry<T>> key, String modId) {
            this.key = key;
            this.modId = modId;
            this.registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
            if (registry == null) throw new IllegalArgumentException("Unknown registry " + key.location());
        }

        @Override
        public <I extends T> RegistryObject<T, I> register(String name, Supplier<? extends I> factory) {
            ResourceKey<T> entryKey = ResourceKey.create(key, ResourceLocation.fromNamespaceAndPath(modId, name));
            I value = factory.get();
            Holder.Reference<T> holder = Registry.registerForHolder(registry, entryKey, value);
            RegistryObject<T, I> entry = new RegistryObject<>() {
                @Override
                public ResourceKey<T> getKey() {
                    return entryKey;
                }

                @Override
                public I get() {
                    return value;
                }

                @Override
                public Holder<T> asHolder() {
                    return holder;
                }
            };
            entries.add(entry);
            return entry;
        }

        @Override
        public Collection<RegistryObject<T, ? extends T>> getEntries() {
            return Collections.unmodifiableList(entries);
        }
    }

    /** Weapons never replay their draw animation when only their data (ammo, mode, wear) changes. */
    static final class FabricGunItem extends GunItem {
        FabricGunItem(Properties properties, WeaponDefinition definition) {
            super(properties, definition);
        }

        @Override
        public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
            return causesReequip(oldStack, newStack, false);
        }
    }

    static final class FabricBlueprintItem extends BlueprintItem {
        FabricBlueprintItem(Properties properties, int tier) {
            super(properties, tier);
        }

        @Override
        public ItemStack getRecipeRemainder(ItemStack stack) {
            return remainder(stack);
        }
    }
}
