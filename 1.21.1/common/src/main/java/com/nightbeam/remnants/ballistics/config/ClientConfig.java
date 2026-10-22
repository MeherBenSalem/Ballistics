package com.nightbeam.remnants.ballistics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Per-player presentation settings ({@code config/remnants_ballistics-client.toml}). Lives in the common package so
 * the spec can be referenced from the client entry point, but is only registered on the physical client.
 */
public final class ClientConfig {
    public static final ModConfigSpec SPEC;

    // HUD
    public static final ModConfigSpec.BooleanValue HUD_ENABLED;
    public static final ModConfigSpec.EnumValue<HudAnchor> HUD_ANCHOR;
    public static final ModConfigSpec.IntValue HUD_OFFSET_X;
    public static final ModConfigSpec.IntValue HUD_OFFSET_Y;
    public static final ModConfigSpec.DoubleValue HUD_SCALE;
    public static final ModConfigSpec.DoubleValue HUD_OPACITY;
    public static final ModConfigSpec.BooleanValue DYNAMIC_CROSSHAIR;
    public static final ModConfigSpec.IntValue CROSSHAIR_COLOR;
    public static final ModConfigSpec.BooleanValue HITMARKERS;
    public static final ModConfigSpec.BooleanValue HIGH_CONTRAST;
    public static final ModConfigSpec.BooleanValue RELOAD_TEXT;
    // Controls
    public static final ModConfigSpec.BooleanValue TOGGLE_AIM;
    public static final ModConfigSpec.DoubleValue AIM_SENSITIVITY;
    public static final ModConfigSpec.BooleanValue SCALE_SENSITIVITY_WITH_ZOOM;
    // Visuals
    public static final ModConfigSpec.DoubleValue WEAPON_BOB;
    public static final ModConfigSpec.DoubleValue WEAPON_SWAY;
    public static final ModConfigSpec.DoubleValue CAMERA_SHAKE;
    public static final ModConfigSpec.BooleanValue MUZZLE_FLASH;
    public static final ModConfigSpec.DoubleValue FLASH_BRIGHTNESS;
    public static final ModConfigSpec.BooleanValue TRACERS;
    public static final ModConfigSpec.BooleanValue SHELL_EJECTION;
    public static final ModConfigSpec.IntValue MAX_BULLET_HOLES;
    public static final ModConfigSpec.DoubleValue PARTICLE_INTENSITY;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_FOV_OFFSET_X;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_FOV_OFFSET_Y;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_OFFSET_Z;
    // Audio
    public static final ModConfigSpec.DoubleValue GUNSHOT_VOLUME;
    public static final ModConfigSpec.DoubleValue MECHANICAL_VOLUME;
    public static final ModConfigSpec.DoubleValue HITMARKER_VOLUME;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Weapon HUD").push("hud");
        HUD_ENABLED = b.define("enabled", true);
        HUD_ANCHOR = b.comment("Screen corner the ammo panel is anchored to").defineEnum("anchor", HudAnchor.BOTTOM_RIGHT);
        HUD_OFFSET_X = b.comment("Horizontal offset from the anchor in GUI pixels").defineInRange("offsetX", 8, -2000, 2000);
        HUD_OFFSET_Y = b.comment("Vertical offset from the anchor in GUI pixels").defineInRange("offsetY", 8, -2000, 2000);
        HUD_SCALE = b.defineInRange("scale", 1.0, 0.5, 3.0);
        HUD_OPACITY = b.defineInRange("opacity", 0.85, 0.1, 1.0);
        DYNAMIC_CROSSHAIR = b.comment("Replace the vanilla crosshair with a spread-aware crosshair while holding a weapon").define("dynamicCrosshair", true);
        CROSSHAIR_COLOR = b.comment("Crosshair colour as 0xRRGGBB").defineInRange("crosshairColor", 0xFFFFFF, 0, 0xFFFFFF);
        HITMARKERS = b.define("hitmarkers", true);
        HIGH_CONTRAST = b.comment("Draw HUD text and crosshair with dark outlines for readability").define("highContrast", false);
        RELOAD_TEXT = b.comment("Show a 'RELOADING' label in addition to the progress bar").define("reloadText", true);
        b.pop();

        b.comment("Controls").push("controls");
        TOGGLE_AIM = b.comment("Aim is toggled by a click instead of held").define("toggleAim", false);
        AIM_SENSITIVITY = b.comment("Mouse sensitivity multiplier while aiming").defineInRange("aimSensitivity", 0.85, 0.05, 2.0);
        SCALE_SENSITIVITY_WITH_ZOOM = b.comment("Additionally divide sensitivity by optic magnification").define("scaleSensitivityWithZoom", true);
        b.pop();

        b.comment("Visual effects. Reduce these for accessibility or performance.").push("visuals");
        WEAPON_BOB = b.defineInRange("weaponBob", 1.0, 0.0, 2.0);
        WEAPON_SWAY = b.defineInRange("weaponSway", 1.0, 0.0, 2.0);
        CAMERA_SHAKE = b.comment("Camera shake from firing (0 disables; does not change recoil itself)").defineInRange("cameraShake", 1.0, 0.0, 2.0);
        MUZZLE_FLASH = b.define("muzzleFlash", true);
        FLASH_BRIGHTNESS = b.comment("Muzzle flash brightness (lower for photosensitivity)").defineInRange("flashBrightness", 1.0, 0.0, 1.0);
        TRACERS = b.define("tracers", true);
        SHELL_EJECTION = b.define("shellEjection", true);
        MAX_BULLET_HOLES = b.defineInRange("maxBulletHoles", 256, 0, 4096);
        PARTICLE_INTENSITY = b.comment("Scales impact particle counts (0 disables)").defineInRange("particleIntensity", 1.0, 0.0, 2.0);
        VIEWMODEL_FOV_OFFSET_X = b.comment("Viewmodel horizontal offset").defineInRange("viewmodelOffsetX", 0.0, -0.5, 0.5);
        VIEWMODEL_FOV_OFFSET_Y = b.comment("Viewmodel vertical offset").defineInRange("viewmodelOffsetY", 0.0, -0.5, 0.5);
        VIEWMODEL_OFFSET_Z = b.comment("Viewmodel depth offset").defineInRange("viewmodelOffsetZ", 0.0, -0.5, 0.5);
        b.pop();

        b.comment("Audio").push("audio");
        GUNSHOT_VOLUME = b.defineInRange("gunshotVolume", 1.0, 0.0, 2.0);
        MECHANICAL_VOLUME = b.comment("Reloads, bolts, pumps, casings").defineInRange("mechanicalVolume", 1.0, 0.0, 2.0);
        HITMARKER_VOLUME = b.defineInRange("hitmarkerVolume", 0.7, 0.0, 2.0);
        b.pop();
        SPEC = b.build();
    }

    private ClientConfig() {}

    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.getAsDouble() : value.getDefault();
    }

    public static int get(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.getAsInt() : value.getDefault();
    }

    public static boolean get(ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.getAsBoolean() : value.getDefault();
    }

    public static <E extends Enum<E>> E get(ModConfigSpec.EnumValue<E> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public enum HudAnchor {
        BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT
    }
}
