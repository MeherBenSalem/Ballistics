package com.nightbeam.remnants.ballistics.forge;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Forge implementation of the platform services (listed in {@code META-INF/services}). */
public final class ForgePlatform implements IPlatform {
    /** Set by the mod constructor before any registry is created. */
    static IEventBus modBus;

    @Override
    public String loaderName() {
        return "Forge";
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
        return new ForgeGunItem(properties, definition);
    }

    @Override
    public BlueprintItem blueprintItem(Item.Properties properties, int tier) {
        return new ForgeBlueprintItem(properties, tier);
    }

    @Override
    public ServerPlayer fakePlayer(ServerLevel level, GameProfile profile) {
        return FakePlayerFactory.get(level, profile);
    }

    @Override
    public boolean projectileImpactCancelled(Projectile projectile, HitResult hit) {
        return ForgeEventFactory.onProjectileImpact(projectile, hit);
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
            net.minecraftforge.registries.RegistryObject<I> object = register.register(name, factory);
            RegistryObject<T, I> entry = new RegistryObject<>() {
                @Override
                @SuppressWarnings("unchecked")
                public ResourceKey<T> getKey() {
                    return (ResourceKey<T>) object.getKey();
                }

                @Override
                public I get() {
                    return object.get();
                }

                @Override
                @SuppressWarnings("unchecked")
                public Holder<T> asHolder() {
                    return (Holder<T>) object.getHolder().orElseThrow();
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
    static final class ForgeGunItem extends GunItem {
        ForgeGunItem(Properties properties, WeaponDefinition definition) {
            super(properties, definition);
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            return true;
        }

        @Override
        public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
            return true;
        }

        @Override
        public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
            return causesReequip(oldStack, newStack, slotChanged);
        }

        /** The item renderer and third-person arm pose; only called on the client. */
        @Override
        public void initializeClient(Consumer<IClientItemExtensions> consumer) {
            consumer.accept(com.nightbeam.remnants.ballistics.forge.client.ForgeClientHooks.GUN_EXTENSIONS);
        }
    }

    static final class ForgeBlueprintItem extends BlueprintItem {
        ForgeBlueprintItem(Properties properties, int tier) {
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
