package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Three creative tabs: weapons, ammunition & materials, attachments. The loader places them after the vanilla tabs. */
public final class ModCreativeTabs {
    public static final RegistrationProvider<CreativeModeTab> TABS = RegistrationProvider.get(Registries.CREATIVE_MODE_TAB, RemnantsBallistics.MODID);

    public static final RegistryObject<CreativeModeTab, CreativeModeTab> WEAPONS = TABS.register("weapons", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.remnants_ballistics.weapons"))
            .icon(() -> new ItemStack(ModItems.gun("arx_16")))
            .displayItems((params, output) -> {
                for (RegistryObject<Item, GunItem> gun : ModItems.guns()) {
                    // Creative stacks come loaded so a weapon can be tested immediately.
                    output.accept(GunData.loaded(gun.get()));
                }
                output.accept(ModItems.GUNSMITH_WORKBENCH.get());
                output.accept(ModItems.AMMO_PRESS.get());
            }).build());

    public static final RegistryObject<CreativeModeTab, CreativeModeTab> AMMUNITION = TABS.register("ammunition", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.remnants_ballistics.ammunition"))
            .icon(() -> new ItemStack(ModItems.ammo(AmmoType.RIFLE_556)))
            .displayItems((params, output) -> {
                ModItems.ammo().forEach(a -> output.accept(a.get()));
                output.accept(ModItems.BRASS_INGOT.get());
                output.accept(ModItems.STEEL_INGOT.get());
                output.accept(ModItems.CASING.get());
                output.accept(ModItems.SHOTGUN_HULL.get());
                output.accept(ModItems.POLYMER_SHEET.get());
                output.accept(ModItems.GUN_PARTS.get());
                output.accept(ModItems.PRECISION_PARTS.get());
                output.accept(ModItems.REMNANT_ALLOY.get());
                output.accept(ModItems.VOID_CORE.get());
                output.accept(ModItems.REPAIR_KIT.get());
                output.accept(ModItems.BLUEPRINT_ADVANCED.get());
                output.accept(ModItems.BLUEPRINT_PROTOTYPE.get());
            }).build());

    public static final RegistryObject<CreativeModeTab, CreativeModeTab> ATTACHMENTS = TABS.register("attachments", () -> Services.PLATFORM.creativeTab()
            .title(Component.translatable("itemGroup.remnants_ballistics.attachments"))
            .icon(() -> new ItemStack(ModItems.attachment("holo_reflex")))
            .displayItems((params, output) -> ModItems.attachments().forEach(a -> output.accept(a.get())))
            .build());

    private ModCreativeTabs() {}

    static void init() {}
}
