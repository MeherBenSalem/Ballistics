package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client → server: a discrete weapon action on the weapon in {@code slot}. */
public record WeaponActionPayload(Action action, int slot, boolean flag) implements CustomPacketPayload {
    public static final Type<WeaponActionPayload> TYPE = new Type<>(RemnantsBallistics.id("weapon_action"));
    public static final StreamCodec<FriendlyByteBuf, WeaponActionPayload> STREAM_CODEC = StreamCodec.ofMember(WeaponActionPayload::write, WeaponActionPayload::new);

    public enum Action {
        RELOAD, CYCLE_FIRE_MODE, AIM, CHARGE
    }

    private WeaponActionPayload(FriendlyByteBuf buf) {
        this(buf.readEnum(Action.class), buf.readVarInt(), buf.readBoolean());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeVarInt(slot);
        buf.writeBoolean(flag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
