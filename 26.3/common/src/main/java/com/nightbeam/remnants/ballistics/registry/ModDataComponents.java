package com.nightbeam.remnants.ballistics.registry;

import com.mojang.serialization.Codec;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.weapon.FireMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

/**
 * Item data components. All weapon state that must survive dropping, trading or storing the item lives here;
 * transient per-player state (reload timers, rate limiting) lives in {@code combat.ShooterState}.
 */
public final class ModDataComponents {
    /** Rounds currently loaded (magazine plus chamber). */
    public static final RegistryHolder<DataComponentType<Integer>> AMMO = Registrar.register(Registries.DATA_COMPONENT_TYPE, "ammo",
            () -> DataComponentType.<Integer>builder().persistent(Codec.intRange(0, 10_000)).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    public static final RegistryHolder<DataComponentType<FireMode>> FIRE_MODE = Registrar.register(Registries.DATA_COMPONENT_TYPE, "fire_mode",
            () -> DataComponentType.<FireMode>builder().persistent(FireMode.CODEC).networkSynchronized(FireMode.STREAM_CODEC).build());

    public static final RegistryHolder<DataComponentType<GunAttachments>> ATTACHMENTS = Registrar.register(Registries.DATA_COMPONENT_TYPE, "attachments",
            () -> DataComponentType.<GunAttachments>builder().persistent(GunAttachments.CODEC).networkSynchronized(GunAttachments.STREAM_CODEC)
                    .cacheEncoding().build());

    private ModDataComponents() {}

    static void init() {}
}
