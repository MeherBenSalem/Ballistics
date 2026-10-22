package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.platform.Services;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;

/**
 * Particle types. Tracers, muzzle flashes and bullet holes are drawn by the client FX renderer instead of the
 * particle engine because they need oriented geometry and precise lifetimes.
 */
public final class ModParticles {
    public static final RegistrationProvider<ParticleType<?>> PARTICLES = RegistrationProvider.get(Registries.PARTICLE_TYPE, RemnantsBallistics.MODID);

    public static final RegistryObject<ParticleType<?>, SimpleParticleType> SPARK = simple("spark");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> MUZZLE_SMOKE = simple("muzzle_smoke");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> CASING = simple("casing");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> SHOTGUN_SHELL = simple("shotgun_shell");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> VOID_SPARK = simple("void_spark");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> RAIL_SPARK = simple("rail_spark");
    public static final RegistryObject<ParticleType<?>, SimpleParticleType> IMPACT_DUST = simple("impact_dust");

    private ModParticles() {}

    static void init() {}

    private static RegistryObject<ParticleType<?>, SimpleParticleType> simple(String name) {
        return PARTICLES.register(name, () -> Services.PLATFORM.simpleParticle(false));
    }
}
