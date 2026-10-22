package com.nightbeam.remnants.ballistics.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * A network message. Minecraft 1.20.1 has no payload types of its own, so each payload names its channel id and writes
 * itself; {@link ModNetwork} pairs every id with a decoder and handler for the loaders to register.
 */
public interface Payload {
    ResourceLocation channel();

    void write(FriendlyByteBuf buf);
}
