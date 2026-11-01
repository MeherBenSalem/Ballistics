package com.nightbeam.remnants.ballistics.attachment;

import com.nightbeam.remnants.ballistics.core.StatModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The attachments fitted to a weapon, keyed by slot and stored by attachment name (an NBT compound on the weapon,
 * {@code weapon.GunData#ATTACHMENTS}). Unknown names (e.g. after an attachment was removed in an update) are kept in
 * storage but ignored at runtime.
 */
public final class GunAttachments {
    public static final GunAttachments EMPTY = new GunAttachments(Map.of());

    private final Map<AttachmentSlot, String> entries;

    public GunAttachments(Map<AttachmentSlot, String> entries) {
        if (entries.isEmpty()) {
            this.entries = Map.of();
        } else {
            EnumMap<AttachmentSlot, String> copy = new EnumMap<>(AttachmentSlot.class);
            copy.putAll(entries);
            this.entries = Collections.unmodifiableMap(copy);
        }
    }

    public Map<AttachmentSlot, String> entries() {
        return entries;
    }

    public Optional<AttachmentDefinition> get(AttachmentSlot slot) {
        String name = entries.get(slot);
        return name == null ? Optional.empty() : Optional.ofNullable(Attachments.byName(name));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public GunAttachments with(AttachmentSlot slot, String name) {
        EnumMap<AttachmentSlot, String> copy = toEnumMap(this);
        copy.put(slot, name);
        return new GunAttachments(copy);
    }

    public GunAttachments without(AttachmentSlot slot) {
        if (!entries.containsKey(slot)) return this;
        EnumMap<AttachmentSlot, String> copy = toEnumMap(this);
        copy.remove(slot);
        return new GunAttachments(copy);
    }

    /** Slot name → attachment name, the same shape as the 1.21 builds' attachments component. */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        entries.forEach((slot, name) -> tag.putString(slot.getSerializedName(), name));
        return tag;
    }

    public static GunAttachments load(CompoundTag tag) {
        EnumMap<AttachmentSlot, String> map = new EnumMap<>(AttachmentSlot.class);
        for (AttachmentSlot slot : AttachmentSlot.values()) {
            if (tag.contains(slot.getSerializedName(), Tag.TAG_STRING)) map.put(slot, tag.getString(slot.getSerializedName()));
        }
        return new GunAttachments(map);
    }

    // EnumMap's copy constructor rejects empty non-EnumMap maps, so always copy through putAll.
    private static EnumMap<AttachmentSlot, String> toEnumMap(GunAttachments attachments) {
        EnumMap<AttachmentSlot, String> map = new EnumMap<>(AttachmentSlot.class);
        map.putAll(attachments.entries);
        return map;
    }

    /** All resolved definitions in slot order. */
    public List<AttachmentDefinition> resolved() {
        List<AttachmentDefinition> list = new ArrayList<>(entries.size());
        for (AttachmentSlot slot : AttachmentSlot.values()) get(slot).ifPresent(list::add);
        return list;
    }

    public List<StatModifier> modifiers() {
        List<StatModifier> mods = new ArrayList<>();
        for (AttachmentDefinition def : resolved()) mods.addAll(def.modifiers());
        return mods;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof GunAttachments other && entries.equals(other.entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String toString() {
        return "GunAttachments" + entries;
    }
}
