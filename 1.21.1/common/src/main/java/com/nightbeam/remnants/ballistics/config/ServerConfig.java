package com.nightbeam.remnants.ballistics.config;

import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-world gameplay balance ({@code <world>/serverconfig/remnants_ballistics-server.toml}). The loader (or Forge Config API Port) syncs SERVER
 * configs to connecting clients, so client-side prediction uses the same numbers as the server.
 */
public final class ServerConfig {
    public static final ModConfigSpec SPEC;

    // Combat
    public static final ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PVP_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue MOB_DAMAGE_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue HEADSHOT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue BODY_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue LIMB_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue HEADSHOTS_ON_ALL_MOBS;
    public static final ModConfigSpec.BooleanValue IGNORE_INVULNERABILITY_FRAMES;
    public static final ModConfigSpec.DoubleValue KNOCKBACK;
    // Accuracy & handling
    public static final ModConfigSpec.DoubleValue SPREAD_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue RECOIL_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue RELOAD_SPEED_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ALLOW_SPRINT_FIRING;
    // Ammunition & durability
    public static final ModConfigSpec.BooleanValue REQUIRE_AMMO;
    public static final ModConfigSpec.BooleanValue CREATIVE_INFINITE_AMMO;
    public static final ModConfigSpec.BooleanValue USE_DURABILITY;
    // Block interaction
    public static final ModConfigSpec.BooleanValue BREAK_GLASS;
    public static final ModConfigSpec.BooleanValue PENETRATION;
    public static final ModConfigSpec.BooleanValue RICOCHET;
    public static final ModConfigSpec.DoubleValue RICOCHET_CHANCE;
    public static final ModConfigSpec.DoubleValue RICOCHET_MAX_ANGLE;
    public static final ModConfigSpec.BooleanValue ENERGY_BLOCK_DAMAGE;
    // Network & anti-cheat
    public static final ModConfigSpec.DoubleValue FIRE_RATE_TOLERANCE_TICKS;
    public static final ModConfigSpec.DoubleValue MAX_AIM_DEVIATION;
    public static final ModConfigSpec.BooleanValue LAG_COMPENSATION;
    public static final ModConfigSpec.IntValue MAX_LAG_COMPENSATION_MS;
    public static final ModConfigSpec.IntValue FX_BROADCAST_RANGE;

    private static final Map<String, WeaponOverrides> WEAPONS = new LinkedHashMap<>();

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Damage and hit zones").push("combat");
        DAMAGE_MULTIPLIER = b.comment("Global multiplier for all firearm damage").defineInRange("damageMultiplier", 1.0, 0.0, 100.0);
        PVP_DAMAGE_MULTIPLIER = b.comment("Extra multiplier applied when a player shoots another player").defineInRange("pvpDamageMultiplier", 1.0, 0.0, 100.0);
        MOB_DAMAGE_MULTIPLIER = b.comment("Extra multiplier applied when a player shoots a non-player entity").defineInRange("mobDamageMultiplier", 1.0, 0.0, 100.0);
        HEADSHOT_MULTIPLIER = b.comment("Damage multiplier for headshots (multiplied with the weapon's own headshot stat)").defineInRange("headshotMultiplier", 2.0, 0.0, 20.0);
        BODY_MULTIPLIER = b.comment("Damage multiplier for torso hits").defineInRange("bodyMultiplier", 1.0, 0.0, 20.0);
        LIMB_MULTIPLIER = b.comment("Damage multiplier for leg hits").defineInRange("limbMultiplier", 0.75, 0.0, 20.0);
        HEADSHOTS_ON_ALL_MOBS = b.comment("If false, only entities in the remnants_ballistics:humanoid_hit_zones tag use head/body/limb zones; others use a narrow eye-level head band")
                .define("headshotsOnAllMobs", false);
        IGNORE_INVULNERABILITY_FRAMES = b.comment("Bullets ignore and do not grant post-hit invulnerability, so automatic fire deals full damage")
                .define("ignoreInvulnerabilityFrames", true);
        KNOCKBACK = b.comment("Knockback strength per 10 damage dealt (vanilla sword hit is ~0.4)").defineInRange("knockback", 0.15, 0.0, 2.0);
        b.pop();

        b.comment("Accuracy, recoil and handling").push("handling");
        SPREAD_MULTIPLIER = b.comment("Multiplier for every spread value (lower = more accurate)").defineInRange("spreadMultiplier", 1.0, 0.0, 10.0);
        RECOIL_MULTIPLIER = b.comment("Multiplier for camera recoil").defineInRange("recoilMultiplier", 1.0, 0.0, 10.0);
        RELOAD_SPEED_MULTIPLIER = b.comment("Reload speed multiplier (2.0 = reloads take half as long)").defineInRange("reloadSpeedMultiplier", 1.0, 0.1, 10.0);
        ALLOW_SPRINT_FIRING = b.comment("Allow firing while sprinting (firing otherwise stops the sprint)").define("allowSprintFiring", false);
        b.pop();

        b.comment("Ammunition and durability").push("ammunition");
        REQUIRE_AMMO = b.comment("Survival players must carry ammunition to reload").define("requireAmmo", true);
        CREATIVE_INFINITE_AMMO = b.comment("Creative mode players reload without consuming ammunition").define("creativeInfiniteAmmo", true);
        USE_DURABILITY = b.comment("Weapons lose durability when fired and stop working at 0 until repaired").define("useDurability", true);
        b.pop();

        b.comment("Block interaction").push("blocks");
        BREAK_GLASS = b.comment("Bullets shatter blocks in the remnants_ballistics:bullet_breakable tag (glass, panes). Respects the mobGriefing rule for non-player shooters.")
                .define("breakGlass", true);
        PENETRATION = b.comment("Bullets may pass through soft blocks and entities, losing damage").define("penetration", true);
        RICOCHET = b.comment("Bullets may ricochet off hard surfaces at shallow angles").define("ricochet", true);
        RICOCHET_CHANCE = b.comment("Chance that a qualifying shallow impact ricochets").defineInRange("ricochetChance", 0.35, 0.0, 1.0);
        RICOCHET_MAX_ANGLE = b.comment("Maximum impact angle in degrees (0 = grazing) that can ricochet").defineInRange("ricochetMaxAngle", 22.0, 0.0, 89.0);
        ENERGY_BLOCK_DAMAGE = b.comment("Voidcaster orb bursts can destroy blocks").define("energyBlockDamage", false);
        b.pop();

        b.comment("Networking and anti-cheat").push("network");
        FIRE_RATE_TOLERANCE_TICKS = b.comment("How many ticks early a shot packet may arrive (absorbs network jitter)").defineInRange("fireRateToleranceTicks", 2.0, 0.0, 10.0);
        MAX_AIM_DEVIATION = b.comment("Maximum degrees between the client-reported aim and the server's view of the player; larger deviations are clamped")
                .defineInRange("maxAimDeviation", 30.0, 1.0, 180.0);
        LAG_COMPENSATION = b.comment("Rewind other players' hitboxes by the shooter's latency when tracing shots").define("lagCompensation", true);
        MAX_LAG_COMPENSATION_MS = b.comment("Upper bound for lag compensation rewind").defineInRange("maxLagCompensationMs", 250, 0, 1000);
        FX_BROADCAST_RANGE = b.comment("Players within this many blocks receive tracer/impact effects for a shot").defineInRange("fxBroadcastRange", 128, 16, 512);
        b.pop();

        b.comment("Per-weapon balance overrides. Values are absolute; set enabled=false to stop a weapon from firing.").push("weapons");
        for (WeaponDefinition def : Weapons.all()) {
            WEAPONS.put(def.name(), new WeaponOverrides(b, def));
        }
        b.pop();

        SPEC = b.build();
    }

    private ServerConfig() {}

    public static double get(ModConfigSpec.DoubleValue value) {
        return SPEC.isLoaded() ? value.getAsDouble() : value.getDefault();
    }

    public static int get(ModConfigSpec.IntValue value) {
        return SPEC.isLoaded() ? value.getAsInt() : value.getDefault();
    }

    public static boolean get(ModConfigSpec.BooleanValue value) {
        return SPEC.isLoaded() ? value.getAsBoolean() : value.getDefault();
    }

    /** Base stats of a weapon with config overrides and global multipliers applied (before attachments). */
    public static WeaponStats configuredStats(WeaponDefinition def) {
        WeaponOverrides o = WEAPONS.get(def.name());
        WeaponStats s = def.baseStats();
        if (o != null) s = o.apply(s);
        float reloadSpeed = (float) get(RELOAD_SPEED_MULTIPLIER);
        float spread = (float) get(SPREAD_MULTIPLIER);
        float recoil = (float) get(RECOIL_MULTIPLIER);
        return s.scaled(Stat.DAMAGE, (float) get(DAMAGE_MULTIPLIER))
                .scaled(Stat.RELOAD_TIME, 1f / reloadSpeed)
                .scaled(Stat.EMPTY_RELOAD_TIME, 1f / reloadSpeed)
                .scaled(Stat.HIP_SPREAD, spread).scaled(Stat.ADS_SPREAD, spread).scaled(Stat.MOVE_SPREAD, spread)
                .scaled(Stat.RECOIL_VERTICAL, recoil).scaled(Stat.RECOIL_HORIZONTAL, recoil);
    }

    public static boolean isEnabled(WeaponDefinition def) {
        WeaponOverrides o = WEAPONS.get(def.name());
        return o == null || get(o.enabled);
    }

    private static final class WeaponOverrides {
        final ModConfigSpec.BooleanValue enabled;
        final ModConfigSpec.DoubleValue damage;
        final ModConfigSpec.IntValue fireRate;
        final ModConfigSpec.IntValue magazine;
        final ModConfigSpec.IntValue reload;
        final ModConfigSpec.IntValue emptyReload;
        final ModConfigSpec.DoubleValue recoil;
        final ModConfigSpec.DoubleValue spread;

        WeaponOverrides(ModConfigSpec.Builder b, WeaponDefinition def) {
            WeaponStats s = def.baseStats();
            b.push(def.name());
            enabled = b.define("enabled", true);
            damage = b.comment("Damage per bullet/pellet in half-hearts").defineInRange("damage", (double) s.get(Stat.DAMAGE), 0.0, 200.0);
            fireRate = b.comment("Rounds per minute").defineInRange("fireRate", s.getInt(Stat.FIRE_RATE), 1, 2400);
            magazine = b.comment("Magazine capacity").defineInRange("magazine", s.getInt(Stat.MAGAZINE), 1, 500);
            reload = b.comment(def.reloadType() == com.nightbeam.remnants.ballistics.weapon.ReloadType.SINGLE
                    ? "Ticks to insert one round" : "Tactical reload ticks").defineInRange("reloadTicks", s.getInt(Stat.RELOAD_TIME), 1, 400);
            emptyReload = b.comment(def.reloadType() == com.nightbeam.remnants.ballistics.weapon.ReloadType.SINGLE
                    ? "Ticks to open and close the action" : "Empty reload ticks").defineInRange("emptyReloadTicks", s.getInt(Stat.EMPTY_RELOAD_TIME), 1, 400);
            recoil = b.comment("Recoil multiplier for this weapon").defineInRange("recoilMultiplier", 1.0, 0.0, 10.0);
            spread = b.comment("Spread multiplier for this weapon").defineInRange("spreadMultiplier", 1.0, 0.0, 10.0);
            b.pop();
        }

        WeaponStats apply(WeaponStats s) {
            float rec = (float) get(recoil);
            float spr = (float) get(spread);
            return s.withValue(Stat.DAMAGE, (float) get(damage))
                    .withValue(Stat.FIRE_RATE, get(fireRate))
                    .withValue(Stat.MAGAZINE, get(magazine))
                    .withValue(Stat.RELOAD_TIME, get(reload))
                    .withValue(Stat.EMPTY_RELOAD_TIME, get(emptyReload))
                    .scaled(Stat.RECOIL_VERTICAL, rec).scaled(Stat.RECOIL_HORIZONTAL, rec)
                    .scaled(Stat.HIP_SPREAD, spr).scaled(Stat.ADS_SPREAD, spr).scaled(Stat.MOVE_SPREAD, spr);
        }
    }
}
