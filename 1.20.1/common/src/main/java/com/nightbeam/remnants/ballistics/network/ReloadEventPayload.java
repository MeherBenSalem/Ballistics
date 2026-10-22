package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ReloadPhase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/** Server → trackers and self: a reload phase started (drives first/third-person animation and sounds). */
public record ReloadEventPayload(int entityId, ReloadPhase phase, int durationTicks, boolean empty) implements Payload {
    public static final ResourceLocation TYPE = RemnantsBallistics.id("reload_event");
    public static final Function<FriendlyByteBuf, ReloadEventPayload> DECODER = ReloadEventPayload::new;

    private ReloadEventPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), ReloadPhase.byId(buf.readByte()), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeByte(phase.ordinal());
        buf.writeVarInt(durationTicks);
        buf.writeBoolean(empty);
    }

    @Override
    public ResourceLocation channel() {
        return TYPE;
    }
}
