package com.nightbeam.remnants.ballistics.attachment;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** Mount points on a weapon. Each weapon accepts at most one attachment per slot. */
public enum AttachmentSlot implements StringRepresentable {
    OPTIC("optic"),
    MUZZLE("muzzle"),
    UNDERBARREL("underbarrel"),
    MAGAZINE("magazine"),
    STOCK("stock"),
    SKIN("skin");

    public static final Codec<AttachmentSlot> CODEC = StringRepresentable.fromEnum(AttachmentSlot::values);
    public static final StreamCodec<ByteBuf, AttachmentSlot> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> values()[Math.floorMod(i, values().length)], AttachmentSlot::ordinal);

    private final String name;

    AttachmentSlot(String name) {
        this.name = name;
    }

    public String translationKey() {
        return "attachment_slot.remnants_ballistics." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
