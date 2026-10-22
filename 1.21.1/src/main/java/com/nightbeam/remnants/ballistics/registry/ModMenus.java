package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, RemnantsBallistics.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GunsmithMenu>> GUNSMITH = MENUS.register("gunsmith",
            () -> new MenuType<>(GunsmithMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<AmmoPressMenu>> AMMO_PRESS = MENUS.register("ammo_press",
            () -> new MenuType<>(AmmoPressMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenus() {}
}
