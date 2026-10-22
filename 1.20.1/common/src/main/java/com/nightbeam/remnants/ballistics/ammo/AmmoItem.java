package com.nightbeam.remnants.ballistics.ammo;

import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/** Loose ammunition of one {@link AmmoType}. Reserve ammo is simply these items anywhere in the inventory. */
public class AmmoItem extends Item {
    private final AmmoType type;

    public AmmoItem(Properties properties, AmmoType type) {
        super(properties);
        this.type = type;
    }

    public AmmoType type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String users = Weapons.all().stream().filter(w -> w.ammo() == type).map(WeaponDefinition::name)
                .map(n -> Component.translatable("item.remnants_ballistics." + n).getString())
                .collect(Collectors.joining(", "));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.ammo_used_by", users).withStyle(ChatFormatting.GRAY));
        if (type.fullReloadPerItem()) {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.energy_cell").withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
