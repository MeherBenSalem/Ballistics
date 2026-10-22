package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Round-trip latency probe used for lag compensation. Sent server → client and echoed back unchanged.
 */
public record PingPayload(int id) implements Payload {
    public static final ResourceLocation TO_CLIENT = RemnantsBallistics.id("ping");
    public static final ResourceLocation TO_SERVER = RemnantsBallistics.id("pong");
    public static final Function<FriendlyByteBuf, PingPayload> DECODER = PingPayload::new;

    private PingPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(id);
    }

    @Override
    public ResourceLocation channel() {
        return TO_CLIENT;
    }

    /** The echo travels on its own channel so each direction has a single registered handler. */
    public Pong pong() {
        return new Pong(id);
    }

    public record Pong(int id) implements Payload {
        public static final Function<FriendlyByteBuf, Pong> DECODER = Pong::new;

        private Pong(FriendlyByteBuf buf) {
            this(buf.readVarInt());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(id);
        }

        @Override
        public ResourceLocation channel() {
            return TO_SERVER;
        }
    }
}
