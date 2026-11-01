package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.core.Stat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nightbeam.remnants.ballistics.attachment.AttachmentSlot.*;
import static com.nightbeam.remnants.ballistics.weapon.FireMode.*;
import static com.nightbeam.remnants.ballistics.weapon.WeaponCategory.*;

/**
 * The weapon roster. Damage is in half-hearts per bullet (or per pellet). Single-load weapons use
 * {@code reload(insertTicksPerRound, startPlusEndTicks)}.
 */
public final class Weapons {
    private static final Map<String, WeaponDefinition> BY_NAME = new LinkedHashMap<>();

    // ---- Pistols -------------------------------------------------------------------------------------------------
    public static final WeaponDefinition R9_SIDEARM = add(WeaponDefinition.builder("r9_sidearm", PISTOL, AmmoType.PISTOL_9MM)
            .damage(5f).rpm(420).magazine(15).reload(28, 36)
            .falloff(16, 48, 0.55f, 96).spread(2.2f, 0.35f, 1.2f).bloom(0.5f, 3f, 14f)
            .recoil(1.6f, 0.6f, 1.2f).handling(4, 1.15f, 1.0f, 0.85f).ballistics(360, 0, 0.4f)
            .stat(Stat.LOUDNESS, 56).stat(Stat.SWAY, 0.8f)
            .modes(SEMI).sound(SoundProfile.PISTOL).rarity(WeaponRarity.COMMON).durability(1200).tier(1)
            .slots(OPTIC, MUZZLE, MAGAZINE).build());

    public static final WeaponDefinition IRONFANG_50 = add(WeaponDefinition.builder("ironfang_50", PISTOL, AmmoType.MAGNUM)
            .damage(11f).rpm(160).magazine(7).reload(34, 42)
            .falloff(20, 56, 0.6f, 112).spread(3.0f, 0.4f, 1.6f).bloom(1.4f, 5f, 10f)
            .recoil(5.5f, 1.6f, 3.2f).handling(5, 1.2f, 0.98f, 0.82f).ballistics(420, 0, 1.0f)
            .stat(Stat.HEADSHOT, 1.1f).stat(Stat.LOUDNESS, 80).stat(Stat.SWAY, 1.1f)
            .modes(SEMI).sound(SoundProfile.MAGNUM).rarity(WeaponRarity.RARE).durability(900).tier(3)
            .slots(OPTIC, MUZZLE, MAGAZINE).build());

    public static final WeaponDefinition WARDEN_357 = add(WeaponDefinition.builder("warden_357", PISTOL, AmmoType.MAGNUM)
            .damage(9f).rpm(200).magazine(6).reload(10, 18).singleLoad()
            .falloff(22, 60, 0.6f, 112).spread(2.6f, 0.3f, 1.4f).bloom(1.0f, 4f, 11f)
            .recoil(4.2f, 1.0f, 2.6f).handling(5, 1.2f, 1.0f, 0.85f).ballistics(400, 0, 0.8f)
            .stat(Stat.LOUDNESS, 72)
            .modes(SEMI).sound(SoundProfile.REVOLVER).rarity(WeaponRarity.UNCOMMON).durability(1400).tier(2)
            .slots(OPTIC).build());

    // ---- SMGs ----------------------------------------------------------------------------------------------------
    public static final WeaponDefinition VIPER_9 = add(WeaponDefinition.builder("viper_9", SMG, AmmoType.PISTOL_9MM)
            .damage(3.8f).rpm(900).magazine(30).reload(32, 40)
            .falloff(12, 36, 0.45f, 72).spread(2.8f, 0.9f, 0.8f).bloom(0.25f, 3.5f, 16f)
            .recoil(0.9f, 0.7f, 0.8f).handling(4, 1.15f, 1.0f, 0.85f).ballistics(330, 0, 0.3f)
            .stat(Stat.LOUDNESS, 56)
            .modes(AUTO, SEMI).sound(SoundProfile.SMG).rarity(WeaponRarity.UNCOMMON).durability(2400).tier(2)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    public static final WeaponDefinition SPECTRE_PDW = add(WeaponDefinition.builder("spectre_pdw", SMG, AmmoType.PDW_57)
            .damage(4.2f).rpm(800).magazine(40).reload(36, 44)
            .falloff(16, 44, 0.5f, 80).spread(2.4f, 0.6f, 0.7f).bloom(0.2f, 2.8f, 18f)
            .recoil(0.75f, 0.45f, 0.7f).handling(4, 1.2f, 1.0f, 0.86f).ballistics(450, 0, 0.7f)
            .stat(Stat.LOUDNESS, 52)
            .modes(AUTO, BURST, SEMI).burst(3, 4).sound(SoundProfile.PDW).rarity(WeaponRarity.RARE).durability(2600).tier(3)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    // ---- Assault rifles ------------------------------------------------------------------------------------------
    public static final WeaponDefinition ARX_16 = add(WeaponDefinition.builder("arx_16", RIFLE, AmmoType.RIFLE_556)
            .damage(5.5f).rpm(720).magazine(30).reload(40, 50)
            .falloff(28, 80, 0.6f, 160).spread(3.2f, 0.25f, 1.8f).bloom(0.3f, 3.5f, 14f)
            .recoil(1.3f, 0.55f, 1.0f).handling(6, 1.3f, 0.95f, 0.8f).ballistics(600, 9.8f, 0.8f)
            .stat(Stat.LOUDNESS, 80)
            .modes(AUTO, SEMI).sound(SoundProfile.RIFLE).rarity(WeaponRarity.UNCOMMON).durability(3000).tier(2)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    public static final WeaponDefinition RAVEN_AK = add(WeaponDefinition.builder("raven_ak", RIFLE, AmmoType.RIFLE_762)
            .damage(6.8f).rpm(600).magazine(30).reload(44, 54)
            .falloff(26, 76, 0.6f, 160).spread(3.5f, 0.35f, 2.0f).bloom(0.4f, 4.2f, 12f)
            .recoil(2.0f, 0.9f, 1.4f).handling(7, 1.3f, 0.94f, 0.78f).ballistics(560, 9.8f, 1.2f)
            .stat(Stat.LOUDNESS, 88)
            .modes(AUTO, SEMI).sound(SoundProfile.AK).rarity(WeaponRarity.UNCOMMON).durability(3600).tier(2)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    public static final WeaponDefinition MANTIS_BR = add(WeaponDefinition.builder("mantis_br", RIFLE, AmmoType.RIFLE_556)
            .damage(6.0f).rpm(900).magazine(24).reload(40, 50)
            .falloff(34, 96, 0.65f, 192).spread(3.0f, 0.15f, 1.6f).bloom(0.25f, 2.5f, 16f)
            .recoil(1.1f, 0.35f, 0.9f).handling(6, 1.45f, 0.95f, 0.8f).ballistics(650, 9.8f, 0.9f)
            .stat(Stat.LOUDNESS, 80)
            .modes(BURST, SEMI).burst(3, 6).sound(SoundProfile.BURST).rarity(WeaponRarity.RARE).durability(2800).tier(3)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    // ---- Shotguns ------------------------------------------------------------------------------------------------
    public static final WeaponDefinition BREACHER_12 = add(WeaponDefinition.builder("breacher_12", SHOTGUN, AmmoType.SHOTGUN_12G)
            .damage(3.2f).stat(Stat.PELLETS, 9).rpm(240).magazine(8).reload(44, 52)
            .falloff(6, 20, 0.25f, 40).spread(5.5f, 4.0f, 1.0f).bloom(1.0f, 2f, 8f)
            .recoil(4.0f, 1.2f, 2.8f).handling(6, 1.15f, 0.94f, 0.8f).ballistics(300, 0, 0.2f)
            .stat(Stat.HEADSHOT, 0.9f).stat(Stat.LOUDNESS, 88)
            .modes(SEMI).sound(SoundProfile.SHOTGUN).rarity(WeaponRarity.RARE).durability(1800).tier(3)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).build());

    public static final WeaponDefinition THUNDERPUMP = add(WeaponDefinition.builder("thunderpump", SHOTGUN, AmmoType.SHOTGUN_12G)
            .damage(3.6f).stat(Stat.PELLETS, 8).rpm(70).magazine(6).reload(9, 16).singleLoad()
            .falloff(8, 22, 0.25f, 40).spread(5.0f, 3.6f, 1.0f).bloom(0f, 0f, 8f)
            .recoil(5.0f, 1.4f, 3.4f).handling(6, 1.15f, 0.95f, 0.8f).ballistics(300, 0, 0.2f)
            .stat(Stat.HEADSHOT, 0.9f).stat(Stat.LOUDNESS, 96)
            .modes(SEMI).action(ActionType.PUMP).sound(SoundProfile.PUMP).rarity(WeaponRarity.UNCOMMON).durability(2000).tier(2)
            .slots(OPTIC, MUZZLE, UNDERBARREL, STOCK).build());

    // ---- Sniper rifles -------------------------------------------------------------------------------------------
    public static final WeaponDefinition GHOSTLINE_M24 = add(WeaponDefinition.builder("ghostline_m24", SNIPER, AmmoType.SNIPER_308)
            .damage(18f).rpm(48).magazine(5).reload(12, 20).singleLoad()
            .falloff(80, 200, 0.8f, 320).spread(8.0f, 0.02f, 3.0f).bloom(0f, 0f, 10f)
            .recoil(6.0f, 1.0f, 3.0f).handling(9, 1.5f, 0.92f, 0.6f).ballistics(800, 9.8f, 2.0f)
            .stat(Stat.HEADSHOT, 1.25f).stat(Stat.LOUDNESS, 128).stat(Stat.SWAY, 1.6f)
            .modes(SEMI).action(ActionType.BOLT).sound(SoundProfile.SNIPER).rarity(WeaponRarity.EPIC).durability(1200).tier(4)
            .slots(OPTIC, MUZZLE, UNDERBARREL, STOCK).defaultAttachment(OPTIC, "longeye_scope").build());

    public static final WeaponDefinition LONGSHOT_50 = add(WeaponDefinition.builder("longshot_50", SNIPER, AmmoType.HEAVY_50)
            .damage(28f).rpm(36).magazine(5).reload(56, 70)
            .falloff(120, 300, 0.85f, 400).spread(10.0f, 0.02f, 3.5f).bloom(0f, 0f, 10f)
            .recoil(9.0f, 1.8f, 4.5f).handling(12, 1.5f, 0.86f, 0.5f).ballistics(850, 9.8f, 4.0f)
            .stat(Stat.HEADSHOT, 1.2f).stat(Stat.LOUDNESS, 160).stat(Stat.SWAY, 2.0f)
            .modes(SEMI).action(ActionType.BOLT).sound(SoundProfile.HEAVY_SNIPER).rarity(WeaponRarity.LEGENDARY).durability(900).tier(4)
            .slots(OPTIC, MUZZLE, UNDERBARREL, MAGAZINE, STOCK).defaultAttachment(OPTIC, "longeye_scope").build());

    // ---- LMGs ----------------------------------------------------------------------------------------------------
    public static final WeaponDefinition TITAN_56 = add(WeaponDefinition.builder("titan_56", LMG, AmmoType.RIFLE_556)
            .damage(5.2f).rpm(700).magazine(100).reload(90, 100).openBolt()
            .falloff(28, 80, 0.6f, 160).spread(4.5f, 0.8f, 2.5f).bloom(0.15f, 4.5f, 10f)
            .recoil(1.0f, 0.8f, 0.9f).handling(10, 1.25f, 0.85f, 0.6f).ballistics(600, 9.8f, 1.0f)
            .stat(Stat.LOUDNESS, 88)
            .modes(AUTO).sound(SoundProfile.LMG).rarity(WeaponRarity.EPIC).durability(5000).tier(3)
            .slots(OPTIC, MUZZLE, UNDERBARREL, STOCK).build());

    public static final WeaponDefinition BULWARK_762 = add(WeaponDefinition.builder("bulwark_762", LMG, AmmoType.RIFLE_762)
            .damage(6.6f).rpm(560).magazine(75).reload(100, 110).openBolt()
            .falloff(30, 90, 0.6f, 176).spread(4.8f, 0.9f, 2.8f).bloom(0.2f, 5f, 10f)
            .recoil(1.4f, 1.0f, 1.2f).handling(11, 1.25f, 0.83f, 0.58f).ballistics(580, 9.8f, 1.5f)
            .stat(Stat.LOUDNESS, 96)
            .modes(AUTO).sound(SoundProfile.HEAVY_LMG).rarity(WeaponRarity.EPIC).durability(5000).tier(4)
            .slots(OPTIC, MUZZLE, UNDERBARREL, STOCK).build());

    // ---- Experimental --------------------------------------------------------------------------------------------
    public static final WeaponDefinition VOIDCASTER = add(WeaponDefinition.builder("voidcaster", EXPERIMENTAL, AmmoType.ENERGY_CELL)
            .damage(8f).rpm(300).magazine(24).reload(40, 40).projectile().openBolt()
            .falloff(64, 96, 1f, 96).spread(1.5f, 0.5f, 0.6f).bloom(0.3f, 2f, 10f)
            .recoil(1.2f, 0.4f, 1.0f).handling(6, 1.2f, 0.95f, 0.8f).ballistics(30, 0, 0f)
            .stat(Stat.LOUDNESS, 48)
            .modes(SEMI, AUTO).action(ActionType.ENERGY).sound(SoundProfile.VOIDCASTER).rarity(WeaponRarity.EPIC).durability(1500).tier(4)
            .build());

    public static final WeaponDefinition ECLIPSE_RAILGUN = add(WeaponDefinition.builder("eclipse_railgun", EXPERIMENTAL, AmmoType.ENERGY_CELL)
            .damage(30f).rpm(40).magazine(6).reload(50, 50).openBolt().stat(Stat.CHARGE_TIME, 20)
            .falloff(300, 400, 1f, 256).spread(6.0f, 0f, 2.0f).bloom(0f, 0f, 10f)
            .recoil(7.0f, 0.5f, 4.0f).handling(8, 3.0f, 0.88f, 0.6f).ballistics(2000, 0, 6f)
            .stat(Stat.HEADSHOT, 1.0f).stat(Stat.LOUDNESS, 112)
            .modes(SEMI).action(ActionType.ENERGY).integratedOptic(OpticType.SCOPE).sound(SoundProfile.RAILGUN)
            .rarity(WeaponRarity.LEGENDARY).durability(800).tier(5).build());

    private Weapons() {}

    private static WeaponDefinition add(WeaponDefinition def) {
        if (BY_NAME.putIfAbsent(def.name(), def) != null) throw new IllegalStateException("Duplicate weapon " + def.name());
        return def;
    }

    public static List<WeaponDefinition> all() {
        return List.copyOf(BY_NAME.values());
    }

    public static WeaponDefinition byName(String name) {
        return BY_NAME.get(name);
    }

    /** Stable network index (registration order). */
    public static int indexOf(WeaponDefinition def) {
        int i = 0;
        for (WeaponDefinition d : BY_NAME.values()) {
            if (d == def) return i;
            i++;
        }
        return -1;
    }

    public static WeaponDefinition byIndex(int index) {
        if (index < 0 || index >= BY_NAME.size()) return null;
        int i = 0;
        for (WeaponDefinition d : BY_NAME.values()) {
            if (i++ == index) return d;
        }
        return null;
    }

    public static Map<String, WeaponDefinition> asMap() {
        return Collections.unmodifiableMap(BY_NAME);
    }

    /** Validates cross references (default attachments must exist and fit). Called during common setup. */
    public static void validate(java.util.function.Function<String, com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition> attachments) {
        for (WeaponDefinition def : BY_NAME.values()) {
            for (Map.Entry<AttachmentSlot, String> e : def.defaultAttachments().entrySet()) {
                var att = attachments.apply(e.getValue());
                if (att == null || att.slot() != e.getKey() || !att.isCompatibleWith(def)) {
                    throw new IllegalStateException("Weapon " + def.name() + " has invalid default attachment " + e.getValue());
                }
            }
        }
    }
}
