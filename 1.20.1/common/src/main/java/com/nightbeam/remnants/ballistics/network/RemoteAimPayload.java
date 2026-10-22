package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Server → trackers: another player's aim/charge state, for third-person poses and effects. */
public record RemoteAimPayload(int entityId, boolean aiming, boolean charging) implements Payload {
    public static final ResourceLocation TYPE = RemnantsBallistics.id("remote_aim");
    public static final Function<FriendlyByteBuf, RemoteAimPayload> DECODER = RemoteAimPayload::new;

    private RemoteAimPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(aiming);
        buf.writeBoolean(charging);
    }

    @Override
    public ResourceLocation channel() {
        return TYPE;
    }
}
