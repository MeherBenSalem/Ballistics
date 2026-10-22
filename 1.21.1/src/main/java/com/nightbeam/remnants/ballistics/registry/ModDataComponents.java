package com.nightbeam.remnants.ballistics.registry;

import com.mojang.serialization.Codec;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.weapon.FireMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item data components. All weapon state that must survive dropping, trading or storing the item lives here;
 * transient per-player state (reload timers, rate limiting) lives in {@link ModAttachmentTypes}.
 */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, RemnantsBallistics.MODID);

    /** Rounds currently loaded (magazine plus chamber). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> AMMO = REGISTER.registerComponentType("ammo",
            b -> b.persistent(Codec.intRange(0, 10_000)).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FireMode>> FIRE_MODE = REGISTER.registerComponentType("fire_mode",
            b -> b.persistent(FireMode.CODEC).networkSynchronized(FireMode.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GunAttachments>> ATTACHMENTS = REGISTER.registerComponentType("attachments",
            b -> b.persistent(GunAttachments.CODEC).networkSynchronized(GunAttachments.STREAM_CODEC).cacheEncoding());

    private ModDataComponents() {}
}
