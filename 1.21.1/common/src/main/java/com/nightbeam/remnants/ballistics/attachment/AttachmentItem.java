package com.nightbeam.remnants.ballistics.attachment;

import com.nightbeam.remnants.ballistics.core.StatModifier;
import com.nightbeam.remnants.ballistics.weapon.StatText;
import com.nightbeam.remnants.ballistics.weapon.WeaponCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;
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
        return Component.translatable(getDescriptionId(stack)).withStyle(definition.rarity().color());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(definition.slot().translationKey()).withStyle(ChatFormatting.GOLD));
        if (definition.zoom() > 0f) {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.zoom", String.format(Locale.ROOT, "%.1f", definition.zoom()))
                    .withStyle(ChatFormatting.AQUA));
        }
        if (definition.suppressed()) tooltip.add(Component.translatable("tooltip.remnants_ballistics.suppressed").withStyle(ChatFormatting.AQUA));
        else if (definition.hidesFlash()) tooltip.add(Component.translatable("tooltip.remnants_ballistics.hides_flash").withStyle(ChatFormatting.AQUA));
        if (definition.slot() == AttachmentSlot.SKIN) tooltip.add(Component.translatable("tooltip.remnants_ballistics.cosmetic").withStyle(ChatFormatting.LIGHT_PURPLE));
        for (StatModifier m : definition.modifiers()) tooltip.add(StatText.modifier(m));
        String fits = definition.categories().stream().sorted()
                .map(WeaponCategory::translationKey).map(k -> Component.translatable(k).getString())
                .collect(Collectors.joining(", "));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.fits", fits).withStyle(ChatFormatting.DARK_GRAY));
        if (definition.requiresMagazineReload()) {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.magazine_fed_only").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
