package com.nightbeam.remnants.ballistics.menu;

import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentItem;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.registry.ModBlocks;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.RepairKitItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Gunsmithing workbench. The attachment slots are a live view of the weapon's {@code attachments} component:
 * inserting or removing an attachment item edits the weapon directly. Attachment slots are never "returned" on
 * close because their items live on the weapon, which prevents duplication.
 */
public class GunsmithMenu extends AbstractContainerMenu {
    public static final int WEAPON_SLOT = 0;
    public static final int ATTACHMENT_START = 1;
    public static final int ATTACHMENT_COUNT = AttachmentSlot.values().length;
    public static final int REPAIR_SLOT = ATTACHMENT_START + ATTACHMENT_COUNT;
    private static final int INV_START = REPAIR_SLOT + 1;
    private static final int INV_END = INV_START + 36;
    public static final int BUTTON_REPAIR = 0;

    /** Slot screen positions, indexed by {@link AttachmentSlot#ordinal()}. */
    public static final int[][] ATTACHMENT_POS = {
            {80, 18},  // optic: above the weapon
            {116, 40}, // muzzle: front
            {98, 62},  // underbarrel: below
            {62, 62},  // magazine: below, behind the grip
            {44, 40},  // stock: rear
            {8, 62},   // skin
    };

    private final ContainerLevelAccess access;
    private final Player player;
    private boolean syncing;

    private final SimpleContainer weapon = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            onWeaponChanged();
        }
    };
    private final SimpleContainer attachments = new SimpleContainer(ATTACHMENT_COUNT) {
        @Override
        public void setChanged() {
            super.setChanged();
            onAttachmentsChanged();
        }
    };
    private final SimpleContainer repair = new SimpleContainer(1);

    public GunsmithMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public GunsmithMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.GUNSMITH.get(), id);
        this.access = access;
        this.player = inventory.player;

        addSlot(new Slot(weapon, 0, 80, 40) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof GunItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        for (AttachmentSlot type : AttachmentSlot.values()) {
            int[] pos = ATTACHMENT_POS[type.ordinal()];
            addSlot(new AttachmentSlotView(type, pos[0], pos[1]));
        }
        addSlot(new Slot(repair, 0, 152, 62) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isRepairItem(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    public static boolean isRepairItem(ItemStack stack) {
        return stack.getItem() instanceof RepairKitItem || stack.is(ModItems.GUN_PARTS.get());
    }

    public ItemStack weapon() {
        return weapon.getItem(0);
    }

    public ItemStack repairItem() {
        return repair.getItem(0);
    }

    /** Current weapon definition or null. */
    public WeaponDefinition definition() {
        return weapon().getItem() instanceof GunItem gun ? gun.definition() : null;
    }

    /** Fitted attachments as currently shown in the slots. */
    public GunAttachments currentAttachments() {
        return GunData.attachments(weapon());
    }

    /** Stats with the given attachment set, for the live preview. */
    public WeaponStats statsWith(GunAttachments set) {
        WeaponDefinition def = definition();
        return def == null ? null : GunData.stats(def, set);
    }

    private void onWeaponChanged() {
        if (syncing) return;
        syncing = true;
        try {
            ItemStack gun = weapon();
            Map<AttachmentSlot, ItemStack> shown = new EnumMap<>(AttachmentSlot.class);
            if (gun.getItem() instanceof GunItem) {
                GunAttachments fitted = GunData.attachments(gun);
                for (AttachmentSlot type : AttachmentSlot.values()) {
                    ItemStack view = fitted.entries().containsKey(type) ? itemFor(fitted.entries().get(type)) : ItemStack.EMPTY;
                    shown.put(type, view);
                }
            }
            for (AttachmentSlot type : AttachmentSlot.values()) {
                attachments.setItem(type.ordinal(), shown.getOrDefault(type, ItemStack.EMPTY));
            }
        } finally {
            syncing = false;
        }
    }

    private static ItemStack itemFor(String attachmentName) {
        AttachmentItem item = ModItems.attachment(attachmentName);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void onAttachmentsChanged() {
        if (syncing) return;
        ItemStack gun = weapon();
        if (!(gun.getItem() instanceof GunItem gunItem)) return;
        WeaponDefinition def = gunItem.definition();
        GunAttachments previous = GunData.attachments(gun);
        Map<AttachmentSlot, String> next = new EnumMap<>(AttachmentSlot.class);
        for (AttachmentSlot type : AttachmentSlot.values()) {
            ItemStack stack = attachments.getItem(type.ordinal());
            if (stack.getItem() instanceof AttachmentItem att && att.definition().slot() == type && att.definition().isCompatibleWith(def)) {
                next.put(type, att.definition().name());
            } else if (!stack.isEmpty() && previous.entries().containsKey(type)) {
                // Unknown/legacy attachment names stay on the weapon untouched.
                next.put(type, previous.entries().get(type));
            }
        }
        GunAttachments updated = new GunAttachments(next);
        if (updated.equals(previous)) return;
        syncing = true;
        try {
            GunData.setAttachments(gun, updated);
            clampAmmo(gun, def);
        } finally {
            syncing = false;
        }
        access.execute((level, pos) -> level.playSound(null, pos, ModSounds.GUNSMITH_USE.get(), SoundSource.BLOCKS, 0.7f, 1.0f + level.getRandom().nextFloat() * 0.2f));
    }

    /** Removing a magazine upgrade must not leave more rounds loaded than fit; extras return as ammunition. */
    private void clampAmmo(ItemStack gun, WeaponDefinition def) {
        int max = GunData.maxCapacity(def, GunData.stats(gun, def));
        int ammo = GunData.ammo(gun);
        if (ammo <= max) return;
        GunData.setAmmo(gun, max);
        int excess = ammo - max;
        if (def.ammo().fullReloadPerItem()) return;
        access.execute((level, pos) -> {
            ItemStack rounds = new ItemStack(ModItems.ammo(def.ammo()), excess);
            player.getInventory().placeItemBackInInventory(rounds, Prediction.SERVER_ONLY);
        });
    }

    @Override
    public boolean clickMenuButton(Player p, int buttonId) {
        if (buttonId != BUTTON_REPAIR) return false;
        ItemStack gun = weapon();
        ItemStack material = repairItem();
        if (!(gun.getItem() instanceof GunItem) || !gun.isDamaged() || !isRepairItem(material)) return false;
        float fraction = material.getItem() instanceof RepairKitItem ? RepairKitItem.REPAIR_FRACTION : 0.1f;
        int amount = Math.max(1, Math.round(gun.getMaxDamage() * fraction));
        gun.setDamageValue(Math.max(0, gun.getDamageValue() - amount));
        material.shrink(1);
        repair.setChanged();
        weapon.setChanged();
        access.execute((level, pos) -> level.playSound(null, pos, ModSounds.GUNSMITH_USE.get(), SoundSource.BLOCKS, 1f, 0.7f));
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < INV_START) {
            if (!moveItemStackTo(stack, INV_START, INV_END, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof GunItem) {
            if (!moveItemStackTo(stack, WEAPON_SLOT, WEAPON_SLOT + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof AttachmentItem att) {
            int target = ATTACHMENT_START + att.definition().slot().ordinal();
            if (!slots.get(target).mayPlace(stack) || !moveItemStackTo(stack, target, target + 1, false)) return ItemStack.EMPTY;
        } else if (isRepairItem(stack)) {
            if (!moveItemStackTo(stack, REPAIR_SLOT, REPAIR_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(p, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player p) {
        return stillValid(access, p, ModBlocks.GUNSMITH_WORKBENCH.get());
    }

    @Override
    public void removed(Player p) {
        super.removed(p);
        // Attachment slots mirror the weapon and must not be returned separately.
        access.execute((level, pos) -> {
            clearContainer(p, weapon);
            clearContainer(p, repair);
        });
    }

    /** A slot that mirrors one attachment position on the weapon. */
    public final class AttachmentSlotView extends Slot {
        private final AttachmentSlot type;

        AttachmentSlotView(AttachmentSlot type, int x, int y) {
            super(attachments, type.ordinal(), x, y);
            this.type = type;
        }

        public AttachmentSlot type() {
            return type;
        }

        /** Whether the current weapon accepts this slot at all (used to grey out unusable slots). */
        public boolean isAvailable() {
            WeaponDefinition def = definition();
            return def != null && def.slots().contains(type);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            WeaponDefinition def = definition();
            if (def == null || !(stack.getItem() instanceof AttachmentItem att)) return false;
            AttachmentDefinition a = att.definition();
            return a.slot() == type && a.isCompatibleWith(def);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public boolean isActive() {
            return isAvailable();
        }
    }
}
