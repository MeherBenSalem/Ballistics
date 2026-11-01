package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final RegistryHolder<MenuType<GunsmithMenu>> GUNSMITH = Registrar.register(Registries.MENU, "gunsmith",
            () -> new MenuType<>(GunsmithMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryHolder<MenuType<AmmoPressMenu>> AMMO_PRESS = Registrar.register(Registries.MENU, "ammo_press",
            () -> new MenuType<>(AmmoPressMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}

    static void init() {}
}
