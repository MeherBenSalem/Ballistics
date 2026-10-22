package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Three creative tabs: weapons, ammunition & materials, attachments. */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RemnantsBallistics.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WEAPONS = TABS.register("weapons", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.remnants_ballistics.weapons"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> new ItemStack(ModItems.gun("arx_16")))
            .displayItems((params, output) -> {
                for (DeferredItem<GunItem> gun : ModItems.guns()) {
                    // Creative stacks come loaded so a weapon can be tested immediately.
                    output.accept(GunData.loaded(gun.get()));
                }
                output.accept(ModItems.GUNSMITH_WORKBENCH.get());
                output.accept(ModItems.AMMO_PRESS.get());
            }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AMMUNITION = TABS.register("ammunition", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.remnants_ballistics.ammunition"))
            .withTabsBefore(WEAPONS.getKey())
            .icon(() -> new ItemStack(ModItems.ammo(com.nightbeam.remnants.ballistics.ammo.AmmoType.RIFLE_556)))
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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ATTACHMENTS = TABS.register("attachments", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.remnants_ballistics.attachments"))
            .withTabsBefore(AMMUNITION.getKey())
            .icon(() -> new ItemStack(ModItems.attachment("holo_reflex")))
            .displayItems((params, output) -> ModItems.attachments().forEach(a -> output.accept(a.get())))
            .build());

    private ModCreativeTabs() {}
}
