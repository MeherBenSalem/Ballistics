package com.nightbeam.remnants.ballistics.attachment;

import com.mojang.serialization.Codec;
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
