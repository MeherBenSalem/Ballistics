package com.nightbeam.remnants.ballistics.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Function;

/**
 * A packet of the mod. Minecraft 1.20.1 has no {@code CustomPacketPayload}, so payloads implement this instead: the
 * {@link Type} names the channel and the payload class, {@link #write} serialises it, and {@code network.ModNetwork}
 * lists each payload's decoder. The loaders frame it as a custom-payload packet (Forge {@code SimpleChannel}, Fabric
 * networking API).
 */
public interface ModPayload {
    Type<? extends ModPayload> type();

    void write(FriendlyByteBuf buf);

    record Type<T extends ModPayload>(ResourceLocation id, Class<T> payloadClass) {}
}
