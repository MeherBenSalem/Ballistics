package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server → owning client: the spread seed, so predicted shots match the server's authoritative spread. */
public record ShooterSeedPayload(long seed) implements CustomPacketPayload {
    public static final Type<ShooterSeedPayload> TYPE = new Type<>(RemnantsBallistics.id("shooter_seed"));
    public static final StreamCodec<FriendlyByteBuf, ShooterSeedPayload> STREAM_CODEC = StreamCodec.ofMember(ShooterSeedPayload::write, ShooterSeedPayload::new);

    private ShooterSeedPayload(FriendlyByteBuf buf) {
        this(buf.readLong());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeLong(seed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
