package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.StatModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

/** Formats stats and modifiers for tooltips and the gunsmith preview. */
public final class StatText {
    private StatText() {}

    public static Component name(Stat stat) {
        return Component.translatable("stat.remnants_ballistics." + stat.key());
    }

    public static String value(Stat stat, float v) {
        return switch (stat.format()) {
            case INTEGER -> Integer.toString(Math.round(v));
            case TICKS -> String.format(Locale.ROOT, "%.2fs", v / 20f);
            case BLOCKS -> String.format(Locale.ROOT, "%.0fm", v);
            case DEGREES -> String.format(Locale.ROOT, "%.2f°", v);
            case PERCENT -> String.format(Locale.ROOT, "%.0f%%", v * 100f);
            case MULTIPLIER -> String.format(Locale.ROOT, "%.2fx", v);
            case DECIMAL -> String.format(Locale.ROOT, "%.1f", v);
        };
    }

    /** "Damage: 5.5" */
    public static MutableComponent line(Stat stat, float v) {
        return Component.empty()
                .append(name(stat).copy().withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(value(stat, v)).withStyle(ChatFormatting.WHITE));
    }

    /** "+15% Magazine" coloured green when beneficial, red otherwise. */
    public static MutableComponent modifier(StatModifier m) {
        String amount = m.op() == StatModifier.Op.MULTIPLY
                ? String.format(Locale.ROOT, "%+.0f%%", m.value() * 100f)
                : (m.value() >= 0 ? "+" : "") + value(m.stat(), m.value());
        return Component.literal(amount + " ")
                .append(name(m.stat()))
                .withStyle(m.isBeneficial() ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    /** Delta between two stat values for the live preview, e.g. "▲ 12%". Empty when unchanged. */
    public static MutableComponent delta(Stat stat, float before, float after) {
        if (Math.abs(after - before) < 1.0E-4f) return Component.empty();
        boolean better = (after > before) == stat.higherIsBetter();
        String arrow = after > before ? "▲" : "▼";
        String text = before != 0f
                ? String.format(Locale.ROOT, "%s%.0f%%", arrow, Math.abs((after - before) / before) * 100f)
                : arrow + value(stat, after);
        return Component.literal(text).withStyle(better ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
