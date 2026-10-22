package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.config.ServerConfig;
import com.nightbeam.remnants.ballistics.core.ReloadMath;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.registry.ModDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Accessors for weapon state stored in item data components, plus effective stat computation
 * (definition → config overrides → attachments). Safe to call on both logical sides.
 */
public final class GunData {
    private GunData() {}

    public static Optional<WeaponDefinition> definition(ItemStack stack) {
        return stack.getItem() instanceof GunItem gun ? Optional.of(gun.definition()) : Optional.empty();
    }

    public static int ammo(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.AMMO, 0);
    }

    public static void setAmmo(ItemStack stack, int ammo) {
        stack.set(ModDataComponents.AMMO, Math.max(0, ammo));
    }

    public static FireMode fireMode(ItemStack stack, WeaponDefinition def) {
        FireMode mode = stack.get(ModDataComponents.FIRE_MODE);
        return mode != null && def.fireModes().contains(mode) ? mode : def.defaultFireMode();
    }

    public static void setFireMode(ItemStack stack, FireMode mode) {
        stack.set(ModDataComponents.FIRE_MODE, mode);
    }

    public static GunAttachments attachments(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.ATTACHMENTS, GunAttachments.EMPTY);
    }

    public static void setAttachments(ItemStack stack, GunAttachments attachments) {
        stack.set(ModDataComponents.ATTACHMENTS, attachments);
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
        setAmmo(stack, maxCapacity(def, stats(stack, def)));
        return stack;
    }
}
