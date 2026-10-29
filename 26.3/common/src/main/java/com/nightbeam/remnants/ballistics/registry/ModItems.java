package com.nightbeam.remnants.ballistics.registry;

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
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class ModItems {
    private static final Map<String, RegistryHolder<GunItem>> GUNS = new LinkedHashMap<>();
    private static final Map<AmmoType, RegistryHolder<AmmoItem>> AMMO = new EnumMap<>(AmmoType.class);
    private static final Map<String, RegistryHolder<AttachmentItem>> ATTACHMENTS = new LinkedHashMap<>();

    static {
        for (WeaponDefinition def : Weapons.all()) {
            // Properties are built lazily: they reference data components, which only exist once registered.
            GUNS.put(def.name(), register(def.name(), props -> new GunItem(GunItem.properties(props, def), def)));
        }
        for (AmmoType type : AmmoType.values()) {
            AMMO.put(type, register(type.itemName(), props -> new AmmoItem(props.stacksTo(type.stackSize()), type)));
        }
        for (AttachmentDefinition def : Attachments.all()) {
            ATTACHMENTS.put(def.name(), register(def.name(), props -> new AttachmentItem(props.stacksTo(1).rarity(def.rarity().vanilla()), def)));
        }
    }

    // Crafting materials
    public static final RegistryHolder<Item> BRASS_INGOT = simple("brass_ingot", UnaryOperator.identity());
    public static final RegistryHolder<Item> STEEL_INGOT = simple("steel_ingot", UnaryOperator.identity());
    public static final RegistryHolder<Item> CASING = simple("casing", UnaryOperator.identity());
    public static final RegistryHolder<Item> SHOTGUN_HULL = simple("shotgun_hull", UnaryOperator.identity());
    public static final RegistryHolder<Item> POLYMER_SHEET = simple("polymer_sheet", UnaryOperator.identity());
    public static final RegistryHolder<Item> GUN_PARTS = simple("gun_parts", UnaryOperator.identity());
    public static final RegistryHolder<Item> PRECISION_PARTS = simple("precision_parts", p -> p.rarity(Rarity.UNCOMMON));
    public static final RegistryHolder<Item> REMNANT_ALLOY = simple("remnant_alloy", p -> p.rarity(Rarity.RARE));
    public static final RegistryHolder<Item> VOID_CORE = simple("void_core", p -> p.rarity(Rarity.EPIC));
    public static final RegistryHolder<RepairKitItem> REPAIR_KIT = register("repair_kit", p -> new RepairKitItem(p.stacksTo(16)));
    public static final RegistryHolder<BlueprintItem> BLUEPRINT_ADVANCED = register("blueprint_advanced",
            p -> new BlueprintItem(p.stacksTo(1).rarity(Rarity.RARE), 3));
    public static final RegistryHolder<BlueprintItem> BLUEPRINT_PROTOTYPE = register("blueprint_prototype",
            p -> new BlueprintItem(p.stacksTo(1).rarity(Rarity.EPIC), 4));

    // Stations
    public static final RegistryHolder<BlockItem> GUNSMITH_WORKBENCH = blockItem("gunsmith_workbench", ModBlocks.GUNSMITH_WORKBENCH);
    public static final RegistryHolder<BlockItem> AMMO_PRESS = blockItem("ammo_press", ModBlocks.AMMO_PRESS);

    private ModItems() {}

    static void init() {}

    /** Items need their id in their properties, so each factory receives properties already keyed to its entry. */
    private static <T extends Item> RegistryHolder<T> register(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, RemnantsBallistics.id(name));
        return Registrar.register(Registries.ITEM, name, () -> factory.apply(new Item.Properties().setId(key)));
    }

    private static RegistryHolder<Item> simple(String name, UnaryOperator<Item.Properties> properties) {
        return register(name, p -> new Item(properties.apply(p)));
    }

    private static RegistryHolder<BlockItem> blockItem(String name, Supplier<? extends Block> block) {
        return register(name, p -> new BlockItem(block.get(), p.useBlockDescriptionPrefix()));
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
        RegistryHolder<AttachmentItem> item = ATTACHMENTS.get(name);
        return item == null ? null : item.get();
    }

    public static Collection<RegistryHolder<GunItem>> guns() {
        return Collections.unmodifiableCollection(GUNS.values());
    }

    public static Collection<RegistryHolder<AmmoItem>> ammo() {
        return Collections.unmodifiableCollection(AMMO.values());
    }

    public static Collection<RegistryHolder<AttachmentItem>> attachments() {
        return Collections.unmodifiableCollection(ATTACHMENTS.values());
    }
}
