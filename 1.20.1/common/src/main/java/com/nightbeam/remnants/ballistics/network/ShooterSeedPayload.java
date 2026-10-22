package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Server → owning client: the spread seed, so predicted shots match the server's authoritative spread. */
public record ShooterSeedPayload(long seed) implements Payload {
    public static final ResourceLocation TYPE = RemnantsBallistics.id("shooter_seed");
    public static final Function<FriendlyByteBuf, ShooterSeedPayload> DECODER = ShooterSeedPayload::new;

    private ShooterSeedPayload(FriendlyByteBuf buf) {
        this(buf.readLong());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(seed);
    }

    @Override
    public ResourceLocation channel() {
        return TYPE;
    }
}
