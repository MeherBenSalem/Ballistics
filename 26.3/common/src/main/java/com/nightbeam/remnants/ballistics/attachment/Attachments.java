package com.nightbeam.remnants.ballistics.attachment;

import com.nightbeam.remnants.ballistics.weapon.WeaponRarity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.nightbeam.remnants.ballistics.core.Stat.*;
import static com.nightbeam.remnants.ballistics.core.StatModifier.percent;
import static com.nightbeam.remnants.ballistics.weapon.WeaponCategory.*;

/** The attachment roster. Every attachment is a trade-off except cosmetic skins. */
public final class Attachments {
    private static final Map<String, AttachmentDefinition> BY_NAME = new LinkedHashMap<>();

    // ---- Optics --------------------------------------------------------------------------------------------------
    public static final AttachmentDefinition HOLO_REFLEX = add(AttachmentDefinition.builder("holo_reflex", AttachmentSlot.OPTIC)
            .optic(OpticType.REFLEX, 1.35f).mod(percent(ADS_TIME, 0.05f)).mod(percent(ADS_SPREAD, -0.05f))
            .except(SNIPER).rarity(WeaponRarity.COMMON).build());
    public static final AttachmentDefinition TRIPOINT_HOLO = add(AttachmentDefinition.builder("tripoint_holo", AttachmentSlot.OPTIC)
            .optic(OpticType.REFLEX, 1.6f).mod(percent(ADS_TIME, 0.08f)).mod(percent(ADS_SPREAD, -0.12f))
            .only(SMG, RIFLE, SHOTGUN, LMG).rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition RANGER_SCOPE = add(AttachmentDefinition.builder("ranger_scope", AttachmentSlot.OPTIC)
            .optic(OpticType.SCOPE, 4f).mod(percent(ADS_TIME, 0.25f)).mod(percent(ADS_SPREAD, -0.2f)).mod(percent(SWAY, 0.1f))
            .mod(percent(ADS_MOVE_SPEED, -0.05f)).only(RIFLE, LMG, SNIPER).rarity(WeaponRarity.RARE).build());
    public static final AttachmentDefinition LONGEYE_SCOPE = add(AttachmentDefinition.builder("longeye_scope", AttachmentSlot.OPTIC)
            .optic(OpticType.SCOPE, 8f).mod(percent(ADS_TIME, 0.4f)).mod(percent(SWAY, 0.2f)).mod(percent(ADS_MOVE_SPEED, -0.1f))
            .only(RIFLE, SNIPER).rarity(WeaponRarity.EPIC).build());

    // ---- Muzzle --------------------------------------------------------------------------------------------------
    public static final AttachmentDefinition SUPPRESSOR = add(AttachmentDefinition.builder("suppressor", AttachmentSlot.MUZZLE)
            .suppressed().mod(percent(LOUDNESS, -0.75f)).mod(percent(DAMAGE, -0.05f)).mod(percent(FALLOFF_END, -0.1f))
            .mod(percent(MUZZLE_VELOCITY, -0.1f)).except(SHOTGUN, EXPERIMENTAL).rarity(WeaponRarity.RARE).build());
    public static final AttachmentDefinition COMPENSATOR = add(AttachmentDefinition.builder("compensator", AttachmentSlot.MUZZLE)
            .mod(percent(RECOIL_HORIZONTAL, -0.3f)).mod(percent(RECOIL_VERTICAL, -0.05f)).mod(percent(LOUDNESS, 0.1f))
            .except(SHOTGUN, EXPERIMENTAL).rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition MUZZLE_BRAKE = add(AttachmentDefinition.builder("muzzle_brake", AttachmentSlot.MUZZLE)
            .mod(percent(RECOIL_VERTICAL, -0.2f)).mod(percent(KICK, -0.2f)).mod(percent(LOUDNESS, 0.2f)).mod(percent(HIP_SPREAD, 0.05f))
            .except(PISTOL, EXPERIMENTAL).rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition FLASH_HIDER = add(AttachmentDefinition.builder("flash_hider", AttachmentSlot.MUZZLE)
            .hidesFlash().mod(percent(RECOIL_VERTICAL, -0.05f)).except(EXPERIMENTAL).rarity(WeaponRarity.COMMON).build());
    public static final AttachmentDefinition SHOTGUN_CHOKE = add(AttachmentDefinition.builder("shotgun_choke", AttachmentSlot.MUZZLE)
            .mod(percent(HIP_SPREAD, -0.35f)).mod(percent(ADS_SPREAD, -0.35f)).mod(percent(FALLOFF_END, 0.2f))
            .only(SHOTGUN).rarity(WeaponRarity.UNCOMMON).build());

    // ---- Underbarrel ---------------------------------------------------------------------------------------------
    public static final AttachmentDefinition VERTICAL_GRIP = add(AttachmentDefinition.builder("vertical_grip", AttachmentSlot.UNDERBARREL)
            .mod(percent(RECOIL_VERTICAL, -0.15f)).mod(percent(ADS_TIME, 0.05f)).rarity(WeaponRarity.COMMON).build());
    public static final AttachmentDefinition ANGLED_GRIP = add(AttachmentDefinition.builder("angled_grip", AttachmentSlot.UNDERBARREL)
            .mod(percent(ADS_TIME, -0.15f)).mod(percent(RECOIL_HORIZONTAL, -0.1f)).mod(percent(RECOIL_VERTICAL, 0.05f))
            .rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition STUBBY_GRIP = add(AttachmentDefinition.builder("stubby_grip", AttachmentSlot.UNDERBARREL)
            .mod(percent(HIP_SPREAD, -0.15f)).mod(percent(MOVE_SPREAD, -0.15f)).mod(percent(ADS_SPREAD, 0.1f))
            .rarity(WeaponRarity.UNCOMMON).build());

    // ---- Magazines -----------------------------------------------------------------------------------------------
    public static final AttachmentDefinition EXTENDED_MAG = add(AttachmentDefinition.builder("extended_mag", AttachmentSlot.MAGAZINE)
            .magazineFed().mod(percent(MAGAZINE, 0.5f)).mod(percent(RELOAD_TIME, 0.15f)).mod(percent(EMPTY_RELOAD_TIME, 0.15f))
            .mod(percent(ADS_TIME, 0.05f)).rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition DRUM_MAG = add(AttachmentDefinition.builder("drum_mag", AttachmentSlot.MAGAZINE)
            .magazineFed().mod(percent(MAGAZINE, 1.0f)).mod(percent(RELOAD_TIME, 0.35f)).mod(percent(EMPTY_RELOAD_TIME, 0.35f))
            .mod(percent(ADS_TIME, 0.1f)).mod(percent(MOVE_SPEED, -0.03f)).only(SMG, RIFLE, SHOTGUN).rarity(WeaponRarity.RARE).build());
    public static final AttachmentDefinition QUICK_MAG = add(AttachmentDefinition.builder("quick_mag", AttachmentSlot.MAGAZINE)
            .magazineFed().mod(percent(RELOAD_TIME, -0.25f)).mod(percent(EMPTY_RELOAD_TIME, -0.25f)).mod(percent(MAGAZINE, -0.1f))
            .rarity(WeaponRarity.UNCOMMON).build());

    // ---- Stocks --------------------------------------------------------------------------------------------------
    public static final AttachmentDefinition TACTICAL_STOCK = add(AttachmentDefinition.builder("tactical_stock", AttachmentSlot.STOCK)
            .mod(percent(RECOIL_VERTICAL, -0.1f)).mod(percent(SWAY, -0.25f)).mod(percent(ADS_TIME, 0.05f))
            .except(PISTOL).rarity(WeaponRarity.COMMON).build());
    public static final AttachmentDefinition LIGHT_STOCK = add(AttachmentDefinition.builder("light_stock", AttachmentSlot.STOCK)
            .mod(percent(ADS_TIME, -0.15f)).mod(percent(MOVE_SPEED, 0.03f)).mod(percent(RECOIL_VERTICAL, 0.05f))
            .except(PISTOL).rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition HEAVY_STOCK = add(AttachmentDefinition.builder("heavy_stock", AttachmentSlot.STOCK)
            .mod(percent(RECOIL_VERTICAL, -0.2f)).mod(percent(RECOIL_HORIZONTAL, -0.15f)).mod(percent(ADS_TIME, 0.12f))
            .mod(percent(MOVE_SPEED, -0.02f)).except(PISTOL).rarity(WeaponRarity.RARE).build());

    // ---- Skins (cosmetic) ----------------------------------------------------------------------------------------
    public static final AttachmentDefinition SKIN_DESERT = add(AttachmentDefinition.builder("skin_desert", AttachmentSlot.SKIN)
            .skin("desert").rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition SKIN_ARCTIC = add(AttachmentDefinition.builder("skin_arctic", AttachmentSlot.SKIN)
            .skin("arctic").rarity(WeaponRarity.UNCOMMON).build());
    public static final AttachmentDefinition SKIN_CRIMSON = add(AttachmentDefinition.builder("skin_crimson", AttachmentSlot.SKIN)
            .skin("crimson").rarity(WeaponRarity.RARE).build());
    public static final AttachmentDefinition SKIN_GILDED = add(AttachmentDefinition.builder("skin_gilded", AttachmentSlot.SKIN)
            .skin("gilded").rarity(WeaponRarity.LEGENDARY).build());
    public static final AttachmentDefinition SKIN_VOID = add(AttachmentDefinition.builder("skin_void", AttachmentSlot.SKIN)
            .skin("void").rarity(WeaponRarity.EPIC).build());

    private Attachments() {}

    private static AttachmentDefinition add(AttachmentDefinition def) {
        if (BY_NAME.putIfAbsent(def.name(), def) != null) throw new IllegalStateException("Duplicate attachment " + def.name());
        return def;
    }

    public static List<AttachmentDefinition> all() {
        return List.copyOf(BY_NAME.values());
    }

    public static AttachmentDefinition byName(String name) {
        return BY_NAME.get(name);
    }
}
