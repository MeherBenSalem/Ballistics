package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Server → shooter: a predicted action was rejected; resynchronise the authoritative magazine count. Needed because
 * a rejected shot leaves the server stack unchanged, so vanilla slot sync would not correct the client's prediction.
 */
public record WeaponCorrectionPayload(int slot, int ammo, boolean cancelReload) implements Payload {
    public static final ResourceLocation TYPE = RemnantsBallistics.id("weapon_correction");
    public static final Function<FriendlyByteBuf, WeaponCorrectionPayload> DECODER = WeaponCorrectionPayload::new;

    private WeaponCorrectionPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
        buf.writeVarInt(ammo);
        buf.writeBoolean(cancelReload);
    }

    @Override
    public ResourceLocation channel() {
        return TYPE;
    }
}
