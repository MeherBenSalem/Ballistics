package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.ammo.AmmoItem;
import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentItem;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.weapon.BlueprintItem;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.RepairKitItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, RemnantsBallistics.MODID);

    private static final Map<String, RegistryObject<Item, GunItem>> GUNS = new LinkedHashMap<>();
    private static final Map<AmmoType, RegistryObject<Item, AmmoItem>> AMMO = new EnumMap<>(AmmoType.class);
    private static final Map<String, RegistryObject<Item, AttachmentItem>> ATTACHMENTS = new LinkedHashMap<>();

    static {
        for (WeaponDefinition def : Weapons.all()) {
            // Properties are built lazily: they reference data components, which only exist once registered.
            GUNS.put(def.name(), ITEMS.register(def.name(), () -> Services.PLATFORM.gunItem(GunItem.properties(def), def)));
        }
        for (AmmoType type : AmmoType.values()) {
            AMMO.put(type, ITEMS.register(type.itemName(), () -> new AmmoItem(new Item.Properties().stacksTo(type.stackSize()), type)));
        }
        for (AttachmentDefinition def : Attachments.all()) {
            ATTACHMENTS.put(def.name(), ITEMS.register(def.name(),
                    () -> new AttachmentItem(new Item.Properties().stacksTo(1).rarity(def.rarity().vanilla()), def)));
        }
    }

    // Crafting materials
    public static final RegistryObject<Item, Item> BRASS_INGOT = simple("brass_ingot", new Item.Properties());
    public static final RegistryObject<Item, Item> STEEL_INGOT = simple("steel_ingot", new Item.Properties());
    public static final RegistryObject<Item, Item> CASING = simple("casing", new Item.Properties());
    public static final RegistryObject<Item, Item> SHOTGUN_HULL = simple("shotgun_hull", new Item.Properties());
    public static final RegistryObject<Item, Item> POLYMER_SHEET = simple("polymer_sheet", new Item.Properties());
    public static final RegistryObject<Item, Item> GUN_PARTS = simple("gun_parts", new Item.Properties());
    public static final RegistryObject<Item, Item> PRECISION_PARTS = simple("precision_parts", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final RegistryObject<Item, Item> REMNANT_ALLOY = simple("remnant_alloy", new Item.Properties().rarity(Rarity.RARE));
    public static final RegistryObject<Item, Item> VOID_CORE = simple("void_core", new Item.Properties().rarity(Rarity.EPIC));
    public static final RegistryObject<Item, RepairKitItem> REPAIR_KIT = ITEMS.register("repair_kit",
            () -> new RepairKitItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item, BlueprintItem> BLUEPRINT_ADVANCED = ITEMS.register("blueprint_advanced",
            () -> Services.PLATFORM.blueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 3));
    public static final RegistryObject<Item, BlueprintItem> BLUEPRINT_PROTOTYPE = ITEMS.register("blueprint_prototype",
            () -> Services.PLATFORM.blueprintItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 4));

    // Stations
    public static final RegistryObject<Item, BlockItem> GUNSMITH_WORKBENCH = ITEMS.register("gunsmith_workbench",
            () -> new BlockItem(ModBlocks.GUNSMITH_WORKBENCH.get(), new Item.Properties()));
    public static final RegistryObject<Item, BlockItem> AMMO_PRESS = ITEMS.register("ammo_press",
            () -> new BlockItem(ModBlocks.AMMO_PRESS.get(), new Item.Properties()));

    private ModItems() {}

    static void init() {}

    private static RegistryObject<Item, Item> simple(String name, Item.Properties properties) {
        return ITEMS.register(name, () -> new Item(properties));
    }

    public static GunItem gun(String name) {
        return GUNS.get(name).get();
    }

    public static GunItem gun(WeaponDefinition def) {
        return gun(def.name());
    }

    public static AmmoItem ammo(AmmoType type) {
        return AMMO.get(type).get();
    }

    public static AttachmentItem attachment(String name) {
        RegistryObject<Item, AttachmentItem> item = ATTACHMENTS.get(name);
        return item == null ? null : item.get();
    }

    public static Collection<RegistryObject<Item, GunItem>> guns() {
        return Collections.unmodifiableCollection(GUNS.values());
    }

    public static Collection<RegistryObject<Item, AmmoItem>> ammo() {
        return Collections.unmodifiableCollection(AMMO.values());
    }

    public static Collection<RegistryObject<Item, AttachmentItem>> attachments() {
        return Collections.unmodifiableCollection(ATTACHMENTS.values());
    }
}
