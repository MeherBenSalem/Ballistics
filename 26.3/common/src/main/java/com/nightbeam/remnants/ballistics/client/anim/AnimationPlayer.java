package com.nightbeam.remnants.ballistics.client.anim;

import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Plays one clip from an animation set in real time, time-scaled to a target duration, and fires its sound cues
 * exactly once as playback crosses them. Single-load phase clips ({@code reload_start}, {@code reload_insert}) hold
 * their last frame until the next clip or {@link #stop()}, so an open cylinder or bolt stays open between rounds.
 */
public final class AnimationPlayer {
    private Identifier set;
    private String clip;
    private long startNanos;
    private float speed = 1f;
    private float lastCueTime = -1f;
    private boolean active;
    private boolean hold;

    /**
     * @param durationSeconds desired real duration (0 = play at authored speed)
     */
    public void play(Identifier set, String clip, float durationSeconds) {
        GunAnimation anim = GunAnimations.INSTANCE.get(set, clip);
        this.set = set;
        this.clip = clip;
        this.startNanos = System.nanoTime();
        this.speed = anim != null && durationSeconds > 0f && anim.length() > 0f ? anim.length() / durationSeconds : 1f;
        this.lastCueTime = -1f;
        this.active = anim != null;
        this.hold = clip.equals("reload_start") || clip.equals("reload_insert");
    }

    public void stop() {
        active = false;
    }

    /** True while a finished single-load phase clip is holding its last frame. */
    public boolean isHolding() {
        return active && hold && finished();
    }

    public boolean isPlaying(String name) {
        return active && name.equals(clip) && !finished();
    }

    public boolean isActive() {
        return active && (hold || !finished());
    }

    public String clip() {
        return clip;
    }

    /** Seconds of clip time elapsed. */
    public float time() {
        return (System.nanoTime() - startNanos) / 1.0E9f * speed;
    }

    /** 0..1 progress through a non-looping clip. */
    public float progress() {
        GunAnimation anim = animation();
        if (anim == null || anim.length() <= 0f) return 1f;
        return Math.min(1f, time() / anim.length());
    }

    public boolean finished() {
        GunAnimation anim = animation();
        return anim == null || !anim.loop() && time() >= anim.length();
    }

    public GunAnimation animation() {
        return set == null || clip == null ? null : GunAnimations.INSTANCE.get(set, clip);
    }

    public void apply(PoseState pose, float weight) {
        if (!active || finished() && !hold) return;
        GunAnimation anim = animation();
        if (anim != null) anim.apply(hold ? Math.min(time(), anim.length()) : time(), weight, pose);
    }

    /** Plays sound cues crossed since the last call. Call once per client tick. */
    public void tickSounds(Entity at, boolean firstPerson) {
        if (!active) return;
        GunAnimation anim = animation();
        if (anim == null || anim.sounds().isEmpty()) return;
        float now = Math.min(time(), anim.length());
        for (GunAnimation.SoundCue cue : anim.sounds()) {
            if (cue.time() > lastCueTime && cue.time() <= now) {
                SoundEvent sound = ModSounds.byName(cue.sound());
                if (sound != null && Minecraft.getInstance().level != null) {
                    float volume = cue.volume() * (float) ClientConfig.get(ClientConfig.MECHANICAL_VOLUME) * (firstPerson ? 1f : 0.8f);
                    Minecraft.getInstance().level.playLocalSound(at.getX(), at.getEyeY() - 0.2, at.getZ(), sound, SoundSource.PLAYERS,
                            volume, cue.pitch(), false);
                }
            }
        }
        lastCueTime = now;
    }
}
