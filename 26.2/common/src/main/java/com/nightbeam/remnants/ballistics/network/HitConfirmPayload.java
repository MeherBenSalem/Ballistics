package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → shooter: authoritative hit feedback for the hit marker. */
public record HitConfirmPayload(boolean headshot, boolean kill, float damage) implements CustomPacketPayload {
    public static final Type<HitConfirmPayload> TYPE = new Type<>(RemnantsBallistics.id("hit_confirm"));
    public static final StreamCodec<FriendlyByteBuf, HitConfirmPayload> STREAM_CODEC = StreamCodec.ofMember(HitConfirmPayload::write, HitConfirmPayload::new);

    private HitConfirmPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readFloat());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(headshot);
        buf.writeBoolean(kill);
        buf.writeFloat(damage);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
