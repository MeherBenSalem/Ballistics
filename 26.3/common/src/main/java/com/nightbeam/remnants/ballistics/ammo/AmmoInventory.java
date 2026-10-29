package com.nightbeam.remnants.ballistics.ammo;

import com.nightbeam.remnants.ballistics.config.ServerConfig;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Counts and consumes reserve ammunition from a player's inventory. Works on both logical sides. */
public final class AmmoInventory {
    /** Reported reserve for players who do not need ammunition. */
    public static final int INFINITE = 9999;

    private AmmoInventory() {}

    public static boolean isInfinite(Player player) {
        if (!ServerConfig.get(ServerConfig.REQUIRE_AMMO)) return true;
        return player.getAbilities().instabuild && ServerConfig.get(ServerConfig.CREATIVE_INFINITE_AMMO);
    }

    /** Number of ammo items of the given type the player carries (main inventory, hotbar, offhand). */
    public static int count(Player player, AmmoType type) {
        if (isInfinite(player)) return INFINITE;
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof AmmoItem ammo && ammo.type() == type) total += stack.getCount();
        }
        return total;
    }

    /**
     * Removes up to {@code amount} ammo items, preferring the smallest stacks first so partial stacks are used up.
     *
     * @return number actually removed (equals {@code amount} for infinite-ammo players)
     */
    public static int consume(Player player, AmmoType type, int amount) {
        if (amount <= 0) return 0;
        if (isInfinite(player)) return amount;
        Inventory inv = player.getInventory();
        int remaining = amount;
        while (remaining > 0) {
            int best = -1;
            int bestCount = Integer.MAX_VALUE;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof AmmoItem ammo && ammo.type() == type && stack.getCount() < bestCount) {
                    best = i;
                    bestCount = stack.getCount();
                }
            }
            if (best < 0) break;
            ItemStack stack = inv.getItem(best);
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            if (stack.isEmpty()) inv.setItem(best, ItemStack.EMPTY);
            remaining -= take;
        }
        if (remaining != amount) inv.setChanged();
        return amount - remaining;
    }
}
