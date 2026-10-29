package com.nightbeam.remnants.ballistics.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers recent player hitboxes so a shot can be traced against where the shooter saw their target.
 * Only players are rewound: they are the targets where latency matters (PvP) and their count is small.
 */
public final class LagCompensation {
    private static final int HISTORY = 24; // 1.2 s at 20 TPS
    private static final Map<UUID, History> HISTORIES = new ConcurrentHashMap<>();

    private LagCompensation() {}

    /** Called at the end of every server tick for each player. */
    public static void record(Player player, long gameTime) {
        HISTORIES.computeIfAbsent(player.getUUID(), k -> new History()).push(gameTime, player.getBoundingBox());
    }

    public static void forget(Player player) {
        HISTORIES.remove(player.getUUID());
    }

    public static void clear() {
        HISTORIES.clear();
    }

    /**
     * @return the target's bounding box {@code rewindTicks} ago (interpolated), or its current box if unknown
     */
    public static AABB boxAt(Entity target, long nowTick, float rewindTicks) {
        if (rewindTicks <= 0f || !(target instanceof Player)) return target.getBoundingBox();
        History history = HISTORIES.get(target.getUUID());
        if (history == null) return target.getBoundingBox();
        AABB box = history.at(nowTick - rewindTicks);
        return box != null ? box : target.getBoundingBox();
    }

    private static final class History {
        private final long[] ticks = new long[HISTORY];
        private final AABB[] boxes = new AABB[HISTORY];
        private int head;
        private int size;

        synchronized void push(long tick, AABB box) {
            ticks[head] = tick;
            boxes[head] = box;
            head = (head + 1) % HISTORY;
            size = Math.min(HISTORY, size + 1);
        }

        synchronized AABB at(double tick) {
            AABB newer = null;
            long newerTick = 0;
            for (int i = 1; i <= size; i++) {
                int idx = Math.floorMod(head - i, HISTORY);
                long t = ticks[idx];
                AABB b = boxes[idx];
                if (t <= tick) {
                    if (newer == null) return b;
                    double f = (tick - t) / (double) Math.max(1, newerTick - t);
                    return lerp(b, newer, f);
                }
                newer = b;
                newerTick = t;
            }
            return newer;
        }

        private static AABB lerp(AABB a, AABB b, double f) {
            return new AABB(
                    a.minX + (b.minX - a.minX) * f, a.minY + (b.minY - a.minY) * f, a.minZ + (b.minZ - a.minZ) * f,
                    a.maxX + (b.maxX - a.maxX) * f, a.maxY + (b.maxY - a.maxY) * f, a.maxZ + (b.maxZ - a.maxZ) * f);
        }
    }
}
