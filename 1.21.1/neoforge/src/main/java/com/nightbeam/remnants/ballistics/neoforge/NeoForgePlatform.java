package com.nightbeam.remnants.ballistics.neoforge;

import com.mojang.authlib.GameProfile;
import com.nightbeam.remnants.ballistics.platform.IPlatform;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.weapon.BlueprintItem;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/** NeoForge implementation of the platform services (listed in {@code META-INF/services}). */
public final class NeoForgePlatform implements IPlatform {
    /** Set by the mod constructor before any registry is created. */
    static IEventBus modBus;

    @Override
    public String loaderName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public <T> RegistrationProvider<T> registry(ResourceKey<? extends Registry<T>> registry, String modId) {
        return new Provider<>(registry, modId);
    }

    @Override
    public SimpleParticleType simpleParticle(boolean overrideLimiter) {
        return new SimpleParticleType(overrideLimiter);
    }

    @Override
    public CreativeModeTab.Builder creativeTab() {
        return CreativeModeTab.builder();
    }

    @Override
    public GunItem gunItem(Item.Properties properties, WeaponDefinition definition) {
        return new NeoForgeGunItem(properties, definition);
    }

    @Override
    public BlueprintItem blueprintItem(Item.Properties properties, int tier) {
        return new NeoForgeBlueprintItem(properties, tier);
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayerFactory.get(level, profile);
    }

    @Override
    public boolean projectileImpactCancelled(Projectile projectile, HitResult hit) {
        return EventHooks.onProjectileImpact(projectile, hit);
    }

    private static final class Provider<T> implements RegistrationProvider<T> {
        private final DeferredRegister<T> register;
        private final List<RegistryObject<T, ? extends T>> entries = new ArrayList<>();

        Provider(ResourceKey<? extends Registry<T>> key, String modId) {
            if (modBus == null) throw new IllegalStateException("Registry " + key.location() + " created before the mod constructor ran");
            register = DeferredRegister.create(key, modId);
            register.register(modBus);
        }

        @Override
        public <I extends T> RegistryObject<T, I> register(String name, Supplier<? extends I> factory) {
            DeferredHolder<T, I> holder = register.register(name, factory);
            RegistryObject<T, I> entry = new RegistryObject<>() {
                @Override
                public ResourceKey<T> getKey() {
                    return holder.getKey();
                }

                @Override
                public I get() {
                    return holder.get();
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

    /** Weapons never swing, hit in melee or replay their draw animation when only their data changes. */
    static final class NeoForgeGunItem extends GunItem {
        NeoForgeGunItem(Properties properties, WeaponDefinition definition) {
            super(properties, definition);
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            return true;
        }

        @Override
        public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
            return true;
        }

        @Override
        public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
            return causesReequip(oldStack, newStack, slotChanged);
        }
    }

    static final class NeoForgeBlueprintItem extends BlueprintItem {
        NeoForgeBlueprintItem(Properties properties, int tier) {
            super(properties, tier);
        }

        @Override
        public boolean hasCraftingRemainingItem(ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack getCraftingRemainingItem(ItemStack stack) {
            return remainder(stack);
        }
    }
}
