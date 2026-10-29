package com.nightbeam.remnants.ballistics.weapon;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;

/** Display rarity of weapons and attachments. Maps onto vanilla {@link Rarity} for compatibility. */
public enum WeaponRarity {
    COMMON(ChatFormatting.WHITE, Rarity.COMMON, 0xFFFFFF),
    UNCOMMON(ChatFormatting.GREEN, Rarity.UNCOMMON, 0x55FF55),
    RARE(ChatFormatting.AQUA, Rarity.RARE, 0x55FFFF),
    EPIC(ChatFormatting.LIGHT_PURPLE, Rarity.EPIC, 0xFF55FF),
    LEGENDARY(ChatFormatting.GOLD, Rarity.EPIC, 0xFFAA00);

    private final ChatFormatting color;
    private final Rarity vanilla;
    private final int rgb;

    WeaponRarity(ChatFormatting color, Rarity vanilla, int rgb) {
        this.color = color;
        this.vanilla = vanilla;
        this.rgb = rgb;
    }

    public ChatFormatting color() {
        return color;
    }

    public Rarity vanilla() {
        return vanilla;
    }

    public int rgb() {
        return rgb;
    }

    public String translationKey() {
        return "rarity.remnants_ballistics." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
