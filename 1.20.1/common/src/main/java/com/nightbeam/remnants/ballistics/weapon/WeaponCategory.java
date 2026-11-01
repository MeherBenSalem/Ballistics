package com.nightbeam.remnants.ballistics.weapon;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Weapon families. Categories drive attachment compatibility, animations and default config groups. */
public enum WeaponCategory implements StringRepresentable {
    PISTOL("pistol", false),
    SMG("smg", true),
    RIFLE("rifle", true),
    SHOTGUN("shotgun", true),
    SNIPER("sniper", true),
    LMG("lmg", true),
    EXPERIMENTAL("experimental", true);

    public static final Codec<WeaponCategory> CODEC = StringRepresentable.fromEnum(WeaponCategory::values);

    private final String name;
    private final boolean twoHanded;

    WeaponCategory(String name, boolean twoHanded) {
        this.name = name;
        this.twoHanded = twoHanded;
    }

    public boolean isTwoHanded() {
        return twoHanded;
    }

    public String translationKey() {
        return "weapon_category.remnants_ballistics." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
