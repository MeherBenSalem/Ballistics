package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → trackers: another player's aim/charge state, for third-person poses and effects. */
public record RemoteAimPayload(int entityId, boolean aiming, boolean charging) implements CustomPacketPayload {
    public static final Type<RemoteAimPayload> TYPE = new Type<>(RemnantsBallistics.id("remote_aim"));
    public static final StreamCodec<FriendlyByteBuf, RemoteAimPayload> STREAM_CODEC = StreamCodec.ofMember(RemoteAimPayload::write, RemoteAimPayload::new);

    private RemoteAimPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(aiming);
        buf.writeBoolean(charging);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
