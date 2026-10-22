package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Server → shooter: authoritative hit feedback for the hit marker. */
public record HitConfirmPayload(boolean headshot, boolean kill, float damage) implements Payload {
    public static final ResourceLocation TYPE = RemnantsBallistics.id("hit_confirm");
    public static final Function<FriendlyByteBuf, HitConfirmPayload> DECODER = HitConfirmPayload::new;

    private HitConfirmPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean(), buf.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(headshot);
        buf.writeBoolean(kill);
        buf.writeFloat(damage);
    }

    @Override
    public ResourceLocation channel() {
        return TYPE;
    }
}
