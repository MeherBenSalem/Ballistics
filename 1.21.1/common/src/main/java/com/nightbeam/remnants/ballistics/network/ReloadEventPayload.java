package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ReloadPhase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → trackers and self: a reload phase started (drives first/third-person animation and sounds). */
public record ReloadEventPayload(int entityId, ReloadPhase phase, int durationTicks, boolean empty) implements CustomPacketPayload {
    public static final Type<ReloadEventPayload> TYPE = new Type<>(RemnantsBallistics.id("reload_event"));
    public static final StreamCodec<FriendlyByteBuf, ReloadEventPayload> STREAM_CODEC = StreamCodec.ofMember(ReloadEventPayload::write, ReloadEventPayload::new);

    private ReloadEventPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), ReloadPhase.byId(buf.readByte()), buf.readVarInt(), buf.readBoolean());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeByte(phase.ordinal());
        buf.writeVarInt(durationTicks);
        buf.writeBoolean(empty);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
