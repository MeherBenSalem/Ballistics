package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.ammo.AmmoItem;
import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentItem;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.weapon.BlueprintItem;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.RepairKitItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RemnantsBallistics.MODID);

    private static final Map<String, DeferredItem<GunItem>> GUNS = new LinkedHashMap<>();
    private static final Map<AmmoType, DeferredItem<AmmoItem>> AMMO = new EnumMap<>(AmmoType.class);
    private static final Map<String, DeferredItem<AttachmentItem>> ATTACHMENTS = new LinkedHashMap<>();

    static {
        for (WeaponDefinition def : Weapons.all()) {
            // Properties are built lazily: they reference data components, which only exist once registered.
            GUNS.put(def.name(), ITEMS.register(def.name(), () -> new GunItem(GunItem.properties(def), def)));
        }
        for (AmmoType type : AmmoType.values()) {
            AMMO.put(type, ITEMS.registerItem(type.itemName(), p -> new AmmoItem(p, type), new Item.Properties().stacksTo(type.stackSize())));
        }
        for (AttachmentDefinition def : Attachments.all()) {
            ATTACHMENTS.put(def.name(), ITEMS.registerItem(def.name(), p -> new AttachmentItem(p, def),
                    new Item.Properties().stacksTo(1).rarity(def.rarity().vanilla())));
        }
    }

    // Crafting materials
    public static final DeferredItem<Item> BRASS_INGOT = ITEMS.registerSimpleItem("brass_ingot");
    public static final DeferredItem<Item> STEEL_INGOT = ITEMS.registerSimpleItem("steel_ingot");
    public static final DeferredItem<Item> CASING = ITEMS.registerSimpleItem("casing");
    public static final DeferredItem<Item> SHOTGUN_HULL = ITEMS.registerSimpleItem("shotgun_hull");
    public static final DeferredItem<Item> POLYMER_SHEET = ITEMS.registerSimpleItem("polymer_sheet");
    public static final DeferredItem<Item> GUN_PARTS = ITEMS.registerSimpleItem("gun_parts");
    public static final DeferredItem<Item> PRECISION_PARTS = ITEMS.registerSimpleItem("precision_parts", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> REMNANT_ALLOY = ITEMS.registerSimpleItem("remnant_alloy", new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<Item> VOID_CORE = ITEMS.registerSimpleItem("void_core", new Item.Properties().rarity(Rarity.EPIC));
    public static final DeferredItem<RepairKitItem> REPAIR_KIT = ITEMS.registerItem("repair_kit", RepairKitItem::new, new Item.Properties().stacksTo(16));
    public static final DeferredItem<BlueprintItem> BLUEPRINT_ADVANCED = ITEMS.registerItem("blueprint_advanced",
            p -> new BlueprintItem(p, 3), new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<BlueprintItem> BLUEPRINT_PROTOTYPE = ITEMS.registerItem("blueprint_prototype",
            p -> new BlueprintItem(p, 4), new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    // Stations
    public static final DeferredItem<BlockItem> GUNSMITH_WORKBENCH = ITEMS.registerSimpleBlockItem("gunsmith_workbench", ModBlocks.GUNSMITH_WORKBENCH);
    public static final DeferredItem<BlockItem> AMMO_PRESS = ITEMS.registerSimpleBlockItem("ammo_press", ModBlocks.AMMO_PRESS);

    private ModItems() {}

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
        DeferredItem<AttachmentItem> item = ATTACHMENTS.get(name);
        return item == null ? null : item.get();
    }

    public static Collection<DeferredItem<GunItem>> guns() {
        return Collections.unmodifiableCollection(GUNS.values());
    }

    public static Collection<DeferredItem<AmmoItem>> ammo() {
        return Collections.unmodifiableCollection(AMMO.values());
    }

    public static Collection<DeferredItem<AttachmentItem>> attachments() {
        return Collections.unmodifiableCollection(ATTACHMENTS.values());
    }
}
