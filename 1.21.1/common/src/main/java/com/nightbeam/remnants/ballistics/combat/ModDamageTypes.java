package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Data-driven damage types ({@code data/remnants_ballistics/damage_type/*.json}). They are tagged
 * {@code minecraft:is_projectile} (projectile protection applies) and {@code minecraft:bypasses_cooldown}.
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BULLET = key("bullet");
    public static final ResourceKey<DamageType> BULLET_HEADSHOT = key("bullet_headshot");
    public static final ResourceKey<DamageType> ENERGY = key("energy");

    private ModDamageTypes() {}

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, RemnantsBallistics.id(path));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, Entity direct, Entity causing) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), direct, causing);
    }

    public static boolean isOurs(DamageSource source) {
        return source.is(BULLET) || source.is(BULLET_HEADSHOT) || source.is(ENERGY);
    }
}
