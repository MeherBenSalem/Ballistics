package com.nightbeam.remnants.ballistics.client.particle;

import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** All custom particles. Each nested provider is registered in {@code ClientSetup}. */
public final class BallisticParticles {
    private static final int FULL_BRIGHT = 0xF000F0;

    private BallisticParticles() {}

    /** Hot metal spark: fast, short-lived, falls and shrinks. */
    public static final class Spark extends SingleQuadParticle {
        Spark(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, RandomSource random) {
            super(level, x, y, z, sprites.get(random));
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.gravity = 0.7f;
            this.friction = 0.9f;
            this.lifetime = 5 + random.nextInt(5);
            this.quadSize = 0.035f + random.nextFloat() * 0.02f;
            this.hasPhysics = true;
        }

        @Override
        public float getQuadSize(float partialTick) {
            return quadSize * (1f - (age + partialTick) / lifetime);
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        protected Layer getLayer() {
            return Layer.OPAQUE;
        }

        public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                return new Spark(level, x, y, z, dx, dy, dz, sprites, random);
            }
        }
    }

    /** Soft smoke puff that grows, rises and fades; also used for impact dust with a different sprite set. */
    public static final class Smoke extends SingleQuadParticle {
        private final SpriteSet sprites;
        private final float startAlpha;

        Smoke(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, float grey, float alpha, int life) {
            super(level, x, y, z, sprites.first());
            this.sprites = sprites;
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.gravity = -0.01f;
            this.friction = 0.92f;
            this.lifetime = life + random.nextInt(life / 2 + 1);
            this.quadSize = 0.08f + random.nextFloat() * 0.05f;
            this.rCol = this.gCol = this.bCol = grey;
            this.startAlpha = alpha;
            this.alpha = alpha;
            this.roll = this.oRoll = random.nextFloat() * Mth.TWO_PI;
            setSprite(sprites.get(0, lifetime));
        }

        @Override
        public void tick() {
            super.tick();
            if (!removed) {
                setSprite(sprites.get(age, lifetime));
                quadSize *= 1.04f;
                alpha = startAlpha * (1f - (float) age / lifetime);
            }
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }

        public record MuzzleProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                return new Smoke(level, x, y, z, dx, dy, dz, sprites, 0.75f, 0.45f, 18);
            }
        }

        public record DustProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                Smoke smoke = new Smoke(level, x, y, z, dx, dy, dz, sprites, 0.6f, 0.6f, 12);
                smoke.gravity = 0.02f;
                return smoke;
            }
        }
    }

    /** Ejected cartridge case or shotgun hull: tumbles, bounces once or twice and clinks on first contact. */
    public static final class Casing extends SingleQuadParticle {
        private final SoundEvent sound;
        private boolean clinked;
        private final float spin;

        Casing(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, SoundEvent sound, float size,
               RandomSource random) {
            super(level, x, y, z, sprites.get(random));
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.gravity = 1.1f;
            this.friction = 0.98f;
            this.lifetime = 50 + random.nextInt(20);
            this.quadSize = size;
            this.hasPhysics = true;
            this.sound = sound;
            this.spin = (random.nextFloat() - 0.5f) * 1.6f;
            this.roll = this.oRoll = random.nextFloat() * Mth.TWO_PI;
        }

        @Override
        public void tick() {
            xo = x;
            yo = y;
            zo = z;
            if (age++ >= lifetime) {
                remove();
                return;
            }
            double preYd = yd;
            yd -= 0.04 * gravity;
            move(xd, yd, zd);
            oRoll = roll;
            if (onGround) {
                if (preYd < -0.06) {
                    yd = -preYd * 0.3;
                    if (!clinked) {
                        clinked = true;
                        level.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, 0.35f * (float) ClientConfig.get(ClientConfig.MECHANICAL_VOLUME),
                                0.9f + random.nextFloat() * 0.3f, false);
                    }
                }
                xd *= 0.6;
                zd *= 0.6;
            } else {
                roll += spin;
            }
            xd *= friction;
            zd *= friction;
            if (lifetime - age < 10) alpha = (lifetime - age) / 10f;
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }

        public record BrassProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                return new Casing(level, x, y, z, dx, dy, dz, sprites, ModSounds.CASING_DROP.get(), 0.045f, random);
            }
        }

        public record ShellProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                return new Casing(level, x, y, z, dx, dy, dz, sprites, ModSounds.SHELL_DROP.get(), 0.065f, random);
            }
        }
    }

    /** Glowing energy mote (Voidcaster purple / railgun cyan): drifts, flickers and fades. */
    public static final class Energy extends SingleQuadParticle {
        Energy(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites, RandomSource random) {
            super(level, x, y, z, sprites.get(random));
            this.xd = dx;
            this.yd = dy;
            this.zd = dz;
            this.gravity = 0f;
            this.friction = 0.85f;
            this.lifetime = 10 + random.nextInt(10);
            this.quadSize = 0.06f + random.nextFloat() * 0.05f;
        }

        @Override
        public void tick() {
            super.tick();
            alpha = (1f - (float) age / lifetime) * (0.7f + random.nextFloat() * 0.3f);
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return FULL_BRIGHT;
        }

        @Override
        protected Layer getLayer() {
            return Layer.TRANSLUCENT;
        }

        public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz,
                                           RandomSource random) {
                return new Energy(level, x, y, z, dx, dy, dz, sprites, random);
            }
        }
    }
}
