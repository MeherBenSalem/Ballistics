package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Function;

/**
 * Round-trip latency probe used for lag compensation. Sent server → client and echoed back unchanged.
 */
public record PingPayload(int id) implements ModPayload {
    public static final Type<PingPayload> TO_CLIENT = new Type<>(RemnantsBallistics.id("ping"), PingPayload.class);
    public static final Type<Pong> TO_SERVER = new Type<>(RemnantsBallistics.id("pong"), Pong.class);
    public static final Function<FriendlyByteBuf, PingPayload> DECODER = PingPayload::new;

    private PingPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TO_CLIENT;
    }

    /** The echo travels on its own channel so each direction has a single registered handler. */
    public Pong pong() {
        return new Pong(id);
    }

    public record Pong(int id) implements ModPayload {
        public static final Function<FriendlyByteBuf, Pong> DECODER = Pong::new;

        private Pong(FriendlyByteBuf buf) {
            this(buf.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(id);
        }

        @Override
        public Type<? extends ModPayload> type() {
            return TO_SERVER;
        }
    }
}
