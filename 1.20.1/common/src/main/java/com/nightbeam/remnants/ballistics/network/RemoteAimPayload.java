package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Function;

/** Server → trackers: another player's aim/charge state, for third-person poses and effects. */
public record RemoteAimPayload(int entityId, boolean aiming, boolean charging) implements ModPayload {
    public static final Type<RemoteAimPayload> TYPE = new Type<>(RemnantsBallistics.id("remote_aim"), RemoteAimPayload.class);
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
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
