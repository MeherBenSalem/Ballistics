package com.nightbeam.remnants.ballistics.registry;

import com.mojang.serialization.Codec;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.weapon.FireMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.UnaryOperator;

/**
 * Item data components. All weapon state that must survive dropping, trading or storing the item lives here;
 * transient per-player state (reload timers, rate limiting) lives in {@code ServerWeaponHandler}.
 */
public final class ModDataComponents {
    public static final RegistrationProvider<DataComponentType<?>> REGISTER = RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE, RemnantsBallistics.MODID);

    /** Rounds currently loaded (magazine plus chamber). */
    public static final RegistryObject<DataComponentType<?>, DataComponentType<Integer>> AMMO = register("ammo",
            b -> b.persistent(Codec.intRange(0, 10_000)).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final RegistryObject<DataComponentType<?>, DataComponentType<FireMode>> FIRE_MODE = register("fire_mode",
            b -> b.persistent(FireMode.CODEC).networkSynchronized(FireMode.STREAM_CODEC));

    public static final RegistryObject<DataComponentType<?>, DataComponentType<GunAttachments>> ATTACHMENTS = register("attachments",
            b -> b.persistent(GunAttachments.CODEC).networkSynchronized(GunAttachments.STREAM_CODEC).cacheEncoding());

    private ModDataComponents() {}

    static void init() {}

    private static <T> RegistryObject<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                        UnaryOperator<DataComponentType.Builder<T>> builder) {
        return REGISTER.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }
}
