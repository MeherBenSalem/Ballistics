package com.nightbeam.remnants.ballistics.registry;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

/**
 * Particle types. Tracers, muzzle flashes and bullet holes are drawn by the client FX renderer instead of the
 * particle engine because they need oriented geometry and precise lifetimes.
 */
public final class ModParticles {
    public static final RegistryHolder<SimpleParticleType> SPARK = register("spark");
    public static final RegistryHolder<SimpleParticleType> MUZZLE_SMOKE = register("muzzle_smoke");
    public static final RegistryHolder<SimpleParticleType> CASING = register("casing");
    public static final RegistryHolder<SimpleParticleType> SHOTGUN_SHELL = register("shotgun_shell");
    public static final RegistryHolder<SimpleParticleType> VOID_SPARK = register("void_spark");
    public static final RegistryHolder<SimpleParticleType> RAIL_SPARK = register("rail_spark");
    public static final RegistryHolder<SimpleParticleType> IMPACT_DUST = register("impact_dust");

    private ModParticles() {}

    static void init() {}

    private static RegistryHolder<SimpleParticleType> register(String name) {
        // The constructor is protected in vanilla; an anonymous subclass is the loader-neutral way to call it.
        return Registrar.register(Registries.PARTICLE_TYPE, name, () -> new SimpleParticleType(false) {});
    }
}
