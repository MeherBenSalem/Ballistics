package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Round-trip latency probe used for lag compensation. Sent server → client and echoed back unchanged.
 */
public record PingPayload(int id) implements CustomPacketPayload {
    public static final Type<PingPayload> TO_CLIENT = new Type<>(RemnantsBallistics.id("ping"));
    public static final Type<Pong> TO_SERVER = new Type<>(RemnantsBallistics.id("pong"));
    public static final StreamCodec<FriendlyByteBuf, PingPayload> STREAM_CODEC = StreamCodec.ofMember(PingPayload::write, PingPayload::new);

    private PingPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TO_CLIENT;
    }

    /** The echo travels on its own channel so each direction has a single registered handler. */
    public Pong pong() {
        return new Pong(id);
    }

    public record Pong(int id) implements CustomPacketPayload {
        public static final StreamCodec<FriendlyByteBuf, Pong> STREAM_CODEC = StreamCodec.ofMember(Pong::write, Pong::new);

        private Pong(FriendlyByteBuf buf) {
            this(buf.readVarInt());
        }

        private void write(FriendlyByteBuf buf) {
            buf.writeVarInt(id);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TO_SERVER;
        }
    }
}
