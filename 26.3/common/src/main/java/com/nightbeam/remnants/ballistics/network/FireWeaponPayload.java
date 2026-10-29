package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Client → server: request to fire one shot. The server re-derives everything that matters (ammo, timing, spread
 * floor, aim deviation) and treats these values as claims to validate.
 *
 * @param slot         selected hotbar slot the shot was fired from
 * @param sequence     strictly increasing shot counter (duplicates and replays are rejected)
 * @param yaw          client view yaw including recoil
 * @param pitch        client view pitch including recoil
 * @param clientSpread spread cone the client used for prediction; the server never accepts less than its own floor
 */
public record FireWeaponPayload(int slot, int sequence, float yaw, float pitch, float clientSpread) implements CustomPacketPayload {
    public static final Type<FireWeaponPayload> TYPE = new Type<>(RemnantsBallistics.id("fire"));
    public static final StreamCodec<FriendlyByteBuf, FireWeaponPayload> STREAM_CODEC = StreamCodec.ofMember(FireWeaponPayload::write, FireWeaponPayload::new);

    private FireWeaponPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
        buf.writeVarInt(sequence);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(clientSpread);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
