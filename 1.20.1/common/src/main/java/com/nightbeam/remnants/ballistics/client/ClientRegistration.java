package com.nightbeam.remnants.ballistics.client;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.particle.BallisticParticles;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.registry.ModParticles;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.List;
import java.util.function.Function;

/**
 * Client-side registrations that every loader performs: particle providers and resource reload listeners. Screens, the
 * void orb renderer, key bindings, the HUD and the weapon item renderer are registered directly by each loader's client
 * entry point with the classes they name.
 */
public final class ClientRegistration {
    public record Particle(RegistryObject<ParticleType<?>, SimpleParticleType> type,
                           Function<SpriteSet, ParticleProvider<SimpleParticleType>> provider) {}

    public record ReloadListener(ResourceLocation id, PreparableReloadListener listener) {}

    public static final List<Particle> PARTICLES = List.of(
            new Particle(ModParticles.SPARK, BallisticParticles.Spark.Provider::new),
            new Particle(ModParticles.MUZZLE_SMOKE, BallisticParticles.Smoke.MuzzleProvider::new),
            new Particle(ModParticles.IMPACT_DUST, BallisticParticles.Smoke.DustProvider::new),
            new Particle(ModParticles.CASING, BallisticParticles.Casing.BrassProvider::new),
            new Particle(ModParticles.SHOTGUN_SHELL, BallisticParticles.Casing.ShellProvider::new),
            new Particle(ModParticles.VOID_SPARK, BallisticParticles.Energy.Provider::new),
            new Particle(ModParticles.RAIL_SPARK, BallisticParticles.Energy.Provider::new));

    /** Order matters: visuals name the animation sets, so both load alongside the models they describe. */
    public static final List<ReloadListener> RELOAD_LISTENERS = List.of(
            new ReloadListener(RemnantsBallistics.id("gun_models"), GunModels.INSTANCE),
            new ReloadListener(RemnantsBallistics.id("gun_animations"), GunAnimations.INSTANCE),
            new ReloadListener(RemnantsBallistics.id("gun_visuals"), GunVisualsLoader.INSTANCE));

    private ClientRegistration() {}
}
