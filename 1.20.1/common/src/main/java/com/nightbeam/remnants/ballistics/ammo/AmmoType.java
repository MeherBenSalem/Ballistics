package com.nightbeam.remnants.ballistics.ammo;

import java.util.Locale;

/**
 * Ammunition calibres. Each type is backed by one registered item ({@code remnants_ballistics:<itemName>}).
 */
public enum AmmoType {
    PISTOL_9MM("ammo_9mm", 64, false),
    MAGNUM("ammo_magnum", 32, false),
    PDW_57("ammo_57", 64, false),
    RIFLE_556("ammo_556", 64, false),
    RIFLE_762("ammo_762", 64, false),
    SHOTGUN_12G("ammo_12g", 32, false),
    SNIPER_308("ammo_308", 32, false),
    HEAVY_50("ammo_50bmg", 16, false),
    /** One cell recharges the whole battery, regardless of how many charges it holds. */
    ENERGY_CELL("energy_cell", 16, true);

    private final String itemName;
    private final int stackSize;
    private final boolean fullReloadPerItem;

    AmmoType(String itemName, int stackSize, boolean fullReloadPerItem) {
        this.itemName = itemName;
        this.stackSize = stackSize;
        this.fullReloadPerItem = fullReloadPerItem;
    }

    public String itemName() {
        return itemName;
    }

    public int stackSize() {
        return stackSize;
    }

    /** True for batteries: a reload consumes exactly one item and fills the weapon. */
    public boolean fullReloadPerItem() {
        return fullReloadPerItem;
    }

    public String translationKey() {
        return "ammo_type.remnants_ballistics." + name().toLowerCase(Locale.ROOT);
    }
}
