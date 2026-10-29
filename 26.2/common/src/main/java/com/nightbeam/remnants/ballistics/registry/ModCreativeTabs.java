package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Three creative tabs: weapons, ammunition & materials, attachments. */
public final class ModCreativeTabs {
    public static final RegistryHolder<CreativeModeTab> WEAPONS = Registrar.register(Registries.CREATIVE_MODE_TAB, "weapons",
            () -> Services.PLATFORM.creativeTabBuilder(ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.withDefaultNamespace("combat")))
                    .title(Component.translatable("itemGroup.remnants_ballistics.weapons"))
                    .icon(() -> new ItemStack(ModItems.gun("arx_16")))
                    .displayItems((params, output) -> {
                        for (RegistryHolder<GunItem> gun : ModItems.guns()) {
                            // Creative stacks come loaded so a weapon can be tested immediately.
                            output.accept(GunData.loaded(gun.get()));
                        }
                        output.accept(ModItems.GUNSMITH_WORKBENCH.get());
                        output.accept(ModItems.AMMO_PRESS.get());
                    }).build());

    public static final RegistryHolder<CreativeModeTab> AMMUNITION = Registrar.register(Registries.CREATIVE_MODE_TAB, "ammunition",
            () -> Services.PLATFORM.creativeTabBuilder(WEAPONS.key())
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

    public static final RegistryHolder<CreativeModeTab> ATTACHMENTS = Registrar.register(Registries.CREATIVE_MODE_TAB, "attachments",
            () -> Services.PLATFORM.creativeTabBuilder(AMMUNITION.key())
                    .title(Component.translatable("itemGroup.remnants_ballistics.attachments"))
                    .icon(() -> new ItemStack(ModItems.attachment("holo_reflex")))
                    .displayItems((params, output) -> ModItems.attachments().forEach(a -> output.accept(a.get())))
                    .build());

    private ModCreativeTabs() {}

    static void init() {}
}
