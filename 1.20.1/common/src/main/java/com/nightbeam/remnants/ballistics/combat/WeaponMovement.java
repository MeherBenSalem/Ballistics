package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Applies the held weapon's mobility (and the extra aiming penalty) as a transient movement speed modifier.
 * Server-side only; vanilla attribute sync carries the value to the client.
 */
public final class WeaponMovement {
    /** Attribute modifiers are keyed by UUID before 1.21; this one is derived from the 1.21 builds' modifier id. */
    public static final UUID MODIFIER_ID = UUID.nameUUIDFromBytes("remnants_ballistics:weapon_handling".getBytes(StandardCharsets.UTF_8));
    private static final String MODIFIER_NAME = "remnants_ballistics:weapon_handling";

    private WeaponMovement() {}

    public static void apply(Player player, ShooterState st) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        double amount = 0.0;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof GunItem gun) {
            WeaponStats stats = GunData.stats(stack, gun.definition());
            double multiplier = stats.get(Stat.MOVE_SPEED) * (st.aiming ? stats.get(Stat.ADS_MOVE_SPEED) : 1f);
            amount = multiplier - 1.0;
        }
        AttributeModifier existing = speed.getModifier(MODIFIER_ID);
        if (Math.abs(amount) < 1.0E-4) {
            if (existing != null) speed.removeModifier(MODIFIER_ID);
            return;
        }
        if (existing != null && Math.abs(existing.getAmount() - amount) < 1.0E-4) return;
        if (existing != null) speed.removeModifier(MODIFIER_ID);
        speed.addTransientModifier(new AttributeModifier(MODIFIER_ID, MODIFIER_NAME, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
}
