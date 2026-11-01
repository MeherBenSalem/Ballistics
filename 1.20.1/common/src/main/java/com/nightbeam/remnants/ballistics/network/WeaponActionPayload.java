package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Function;

/** Client → server: a discrete weapon action on the weapon in {@code slot}. */
public record WeaponActionPayload(Action action, int slot, boolean flag) implements ModPayload {
    public static final Type<WeaponActionPayload> TYPE = new Type<>(RemnantsBallistics.id("weapon_action"), WeaponActionPayload.class);
    public static final Function<FriendlyByteBuf, WeaponActionPayload> DECODER = WeaponActionPayload::new;

    public enum Action {
        RELOAD, CYCLE_FIRE_MODE, AIM, CHARGE
    }

    private WeaponActionPayload(FriendlyByteBuf buf) {
        this(buf.readEnum(Action.class), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeVarInt(slot);
        buf.writeBoolean(flag);
    }

    @Override
    public Type<? extends ModPayload> type() {
        return TYPE;
    }
}
