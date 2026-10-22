package com.nightbeam.remnants.ballistics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Installation-wide settings ({@code config/remnants_ballistics-common.toml}) that must be known before datapacks
 * load: recipe conditions and loot injection.
 */
public final class CommonConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WEAPON_CRAFTING;
    public static final ModConfigSpec.BooleanValue AMMO_CRAFTING;
    public static final ModConfigSpec.BooleanValue ATTACHMENT_CRAFTING;
    public static final ModConfigSpec.BooleanValue STRUCTURE_LOOT;
    public static final ModConfigSpec.BooleanValue MOB_DROPS;
    public static final ModConfigSpec.DoubleValue LOOT_CHANCE_MULTIPLIER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Recipe toggles. Changes apply on the next datapack reload (/reload) or world load.").push("crafting");
        WEAPON_CRAFTING = b.comment("Enable crafting recipes for weapons").define("weaponCrafting", true);
        AMMO_CRAFTING = b.comment("Enable ammunition press recipes").define("ammoCrafting", true);
        ATTACHMENT_CRAFTING = b.comment("Enable crafting recipes for attachments").define("attachmentCrafting", true);
        b.pop();

        b.comment("Loot injection").push("loot");
        STRUCTURE_LOOT = b.comment("Add weapons, ammunition, attachments and blueprints to structure chests").define("structureLoot", true);
        MOB_DROPS = b.comment("Armed mobs (pillagers, vindicators, piglin brutes) may drop ammunition and parts").define("mobDrops", true);
        LOOT_CHANCE_MULTIPLIER = b.comment("Multiplier applied to every injected loot chance").defineInRange("lootChanceMultiplier", 1.0, 0.0, 10.0);
        b.pop();
        SPEC = b.build();
    }

    private CommonConfig() {}

    public static boolean get(ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.getAsBoolean() : value.getDefault();
    }

    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.getAsDouble() : value.getDefault();
    }
}
