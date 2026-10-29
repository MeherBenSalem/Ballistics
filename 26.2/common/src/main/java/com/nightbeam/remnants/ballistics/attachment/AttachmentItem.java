package com.nightbeam.remnants.ballistics.attachment;

import com.nightbeam.remnants.ballistics.core.StatModifier;
import com.nightbeam.remnants.ballistics.weapon.StatText;
import com.nightbeam.remnants.ballistics.weapon.WeaponCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** An attachment item. Fitted to weapons at the Gunsmithing Workbench. */
public class AttachmentItem extends Item {
    private final AttachmentDefinition definition;

    public AttachmentItem(Properties properties, AttachmentDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public AttachmentDefinition definition() {
        return definition;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId()).withStyle(definition.rarity().color());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(definition.slot().translationKey()).withStyle(ChatFormatting.GOLD));
        if (definition.zoom() > 0f) {
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.zoom", String.format(Locale.ROOT, "%.1f", definition.zoom()))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (definition.suppressed()) tooltip.accept(Component.translatable("tooltip.remnants_ballistics.suppressed").withStyle(ChatFormatting.AQUA));
        else if (definition.hidesFlash()) tooltip.accept(Component.translatable("tooltip.remnants_ballistics.hides_flash").withStyle(ChatFormatting.AQUA));
        if (definition.slot() == AttachmentSlot.SKIN) tooltip.accept(Component.translatable("tooltip.remnants_ballistics.cosmetic").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (StatModifier m : definition.modifiers()) tooltip.accept(StatText.modifier(m));
        String fits = definition.categories().stream().sorted()
                .map(WeaponCategory::translationKey).map(k -> Component.translatable(k).getString())
                .collect(Collectors.joining(", "));
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.fits", fits).withStyle(ChatFormatting.DARK_GRAY));
        if (definition.requiresMagazineReload()) {
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.magazine_fed_only").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
