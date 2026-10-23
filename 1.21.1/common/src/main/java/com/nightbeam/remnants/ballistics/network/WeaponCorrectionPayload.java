package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server → shooter: a predicted action was rejected; resynchronise the authoritative magazine count. Needed because
 * a rejected shot leaves the server stack unchanged, so vanilla slot sync would not correct the client's prediction.
 */
public record WeaponCorrectionPayload(int slot, int ammo, boolean cancelReload) implements CustomPacketPayload {
    public static final Type<WeaponCorrectionPayload> TYPE = new Type<>(RemnantsBallistics.id("weapon_correction"));
    public static final StreamCodec<FriendlyByteBuf, WeaponCorrectionPayload> STREAM_CODEC = StreamCodec.ofMember(WeaponCorrectionPayload::write, WeaponCorrectionPayload::new);

    private WeaponCorrectionPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
        buf.writeVarInt(ammo);
        buf.writeBoolean(cancelReload);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
