package com.nightbeam.remnants.ballistics.weapon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum FireMode implements StringRepresentable {
    SEMI("semi"),
    BURST("burst"),
    AUTO("auto");

    public static final Codec<FireMode> CODEC = StringRepresentable.fromEnum(FireMode::values);
    public static final StreamCodec<ByteBuf, FireMode> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> values()[Math.floorMod(i, values().length)], FireMode::ordinal);

    private final String name;

    FireMode(String name) {
        this.name = name;
    }

    public String translationKey() {
        return "fire_mode.remnants_ballistics." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
