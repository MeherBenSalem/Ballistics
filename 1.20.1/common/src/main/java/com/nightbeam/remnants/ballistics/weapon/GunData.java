package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.ReloadMath;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Accessors for weapon state stored in the item's NBT (1.20.1 has no data components), plus effective stat computation
 * (definition → config overrides → attachments). Safe to call on both logical sides. A weapon without a tag has an
 * empty magazine, the definition's default fire mode and its default attachments, like a fresh 1.21 stack.
 */
public final class GunData {
    /** Rounds currently loaded (magazine plus chamber). */
    public static final String AMMO = "Ammo";
    public static final String FIRE_MODE = "FireMode";
    /** Fitted attachments, slot name → attachment name ({@link GunAttachments#save()}). */
    public static final String ATTACHMENTS = "Attachments";

    private static final int MAX_AMMO = 10_000;

    private GunData() {}

    public static Optional<WeaponDefinition> definition(ItemStack stack) {
        return stack.getItem() instanceof GunItem gun ? Optional.of(gun.definition()) : Optional.empty();
    }

    public static int ammo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, Math.min(MAX_AMMO, tag.getInt(AMMO)));
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(AMMO, Math.max(0, Math.min(MAX_AMMO, ammo)));
    }

    public static FireMode fireMode(ItemStack stack, WeaponDefinition def) {
        CompoundTag tag = stack.getTag();
        FireMode mode = tag != null && tag.contains(FIRE_MODE, Tag.TAG_STRING) ? FireMode.byName(tag.getString(FIRE_MODE)) : null;
        return mode != null && def.fireModes().contains(mode) ? mode : def.defaultFireMode();
    }

    public static void setFireMode(ItemStack stack, FireMode mode) {
        stack.getOrCreateTag().putString(FIRE_MODE, mode.getSerializedName());
    }

    public static GunAttachments attachments(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(ATTACHMENTS, Tag.TAG_COMPOUND)) return GunAttachments.load(tag.getCompound(ATTACHMENTS));
        // Never written: the weapon's factory attachments (the 1.21 builds' default component value).
        return stack.getItem() instanceof GunItem gun ? gun.defaultAttachments() : GunAttachments.EMPTY;
    }

    public static void setAttachments(ItemStack stack, GunAttachments attachments) {
        stack.getOrCreateTag().put(ATTACHMENTS, attachments.save());
    }

    /** Effective stats with config overrides and fitted attachments. */
    public static WeaponStats stats(ItemStack stack, WeaponDefinition def) {
        return stats(def, attachments(stack));
    }

    public static WeaponStats stats(WeaponDefinition def, GunAttachments attachments) {
        WeaponStats stats = ServerConfig.configuredStats(def);
        AttachmentDefinition optic = attachments.get(AttachmentSlot.OPTIC).orElse(null);
        stats = stats.with(attachments.modifiers());
        if (optic != null && optic.zoom() > 0f) stats = stats.withValue(Stat.ZOOM, optic.zoom());
        return stats;
    }

    /** Rounds the weapon holds when fully loaded, including a chambered round where applicable. */
    public static int capacity(WeaponDefinition def, WeaponStats stats, int currentAmmo) {
        return ReloadMath.reloadCapacity(stats.getInt(Stat.MAGAZINE), def.chamber(), currentAmmo);
    }

    /** Absolute upper bound used to validate stored ammo (magazine + chamber). */
    public static int maxCapacity(WeaponDefinition def, WeaponStats stats) {
        return stats.getInt(Stat.MAGAZINE) + (def.chamber() ? 1 : 0);
    }

    public static OpticType optic(ItemStack stack, WeaponDefinition def) {
        return attachments(stack).get(AttachmentSlot.OPTIC).map(AttachmentDefinition::optic).orElse(def.integratedOptic());
    }

    public static boolean isSuppressed(GunAttachments attachments) {
        return attachments.get(AttachmentSlot.MUZZLE).map(AttachmentDefinition::suppressed).orElse(false);
    }

    public static boolean hidesFlash(GunAttachments attachments) {
        return attachments.get(AttachmentSlot.MUZZLE).map(AttachmentDefinition::hidesFlash).orElse(false);
    }

    /** A weapon at its last point of durability is "broken": it cannot fire until repaired, but is never destroyed. */
    public static boolean isBroken(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    /** Wears the weapon by one shot without ever destroying it. */
    public static void wear(ItemStack stack, int amount) {
        if (!stack.isDamageableItem()) return;
        stack.setDamageValue(Math.min(stack.getMaxDamage() - 1, stack.getDamageValue() + amount));
    }

    /** Creates a stack with a full magazine and the definition's default attachments. */
    public static ItemStack loaded(GunItem item) {
        ItemStack stack = new ItemStack(item);
        WeaponDefinition def = item.definition();
        setFireMode(stack, def.defaultFireMode());
        setAttachments(stack, item.defaultAttachments());
        setAmmo(stack, maxCapacity(def, stats(stack, def)));
        return stack;
    }
}
