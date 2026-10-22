package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final RegistrationProvider<MenuType<?>> MENUS = RegistrationProvider.get(Registries.MENU, RemnantsBallistics.MODID);

    public static final RegistryObject<MenuType<?>, MenuType<GunsmithMenu>> GUNSMITH = MENUS.register("gunsmith",
            () -> new MenuType<>(GunsmithMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final RegistryObject<MenuType<?>, MenuType<AmmoPressMenu>> AMMO_PRESS = MENUS.register("ammo_press",
            () -> new MenuType<>(AmmoPressMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}

    static void init() {}
}
