package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.registry.ModDataComponents;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A firearm. All behaviour is data-driven by its {@link WeaponDefinition}; firing and reloading are handled by the
 * network layer and {@code combat} package rather than vanilla use/attack, so the item suppresses vanilla
 * mining, melee and swing behaviour. Loader-specific subclasses (created through {@code IPlatform#gunItem}) add the
 * hooks that stop melee attacks, arm swings and re-equip animations while it is held.
 */
public class GunItem extends Item {
    private final WeaponDefinition definition;

    public GunItem(Properties properties, WeaponDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public static Properties properties(WeaponDefinition def) {
        return new Properties()
                .stacksTo(1)
                .durability(def.durability())
                .rarity(def.rarity().vanilla())
                .component(ModDataComponents.AMMO.get(), 0)
                .component(ModDataComponents.FIRE_MODE.get(), def.defaultFireMode())
                .component(ModDataComponents.ATTACHMENTS.get(), new GunAttachments(def.defaultAttachments()));
    }

    public WeaponDefinition definition() {
        return definition;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack)).withStyle(definition.rarity().color());
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    /**
     * Whether a held-stack change should replay the draw animation. Ammo, fire mode and durability updates must not;
     * each loader routes its re-equip hook here.
     */
    public static boolean causesReequip(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.GUN_PARTS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        WeaponStats stats = GunData.stats(stack, definition);
        tooltip.add(Component.translatable(definition.category().translationKey())
                .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(definition.ammo().translationKey()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.ammo", GunData.ammo(stack), GunData.maxCapacity(definition, stats))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.fire_mode",
                Component.translatable(GunData.fireMode(stack, definition).translationKey()),
                definition.fireModes().stream().map(m -> Component.translatable(m.translationKey()).getString()).collect(Collectors.joining(" / ")))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.remnants_ballistics.rarity", Component.translatable(definition.rarity().translationKey()))
                .withStyle(definition.rarity().color()));

        if (GunData.isBroken(stack)) {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.broken").withStyle(ChatFormatting.RED));
        }

        List<AttachmentDefinition> fitted = GunData.attachments(stack).resolved();
        if (!fitted.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.attachments").withStyle(ChatFormatting.GRAY));
            for (AttachmentDefinition att : fitted) {
                tooltip.add(Component.literal("  ").append(Component.translatable("item.remnants_ballistics." + att.name()))
                        .withStyle(att.rarity().color()));
            }
        }

        if (TooltipKeys.shiftDown()) {
            int pellets = stats.getInt(Stat.PELLETS);
            tooltip.add(StatText.line(Stat.DAMAGE, stats.get(Stat.DAMAGE))
                    .append(pellets > 1 ? Component.literal(" ×" + pellets).withStyle(ChatFormatting.GRAY) : Component.empty()));
            tooltip.add(StatText.line(Stat.FIRE_RATE, stats.get(Stat.FIRE_RATE)));
            tooltip.add(StatText.line(Stat.MAGAZINE, stats.get(Stat.MAGAZINE)));
            tooltip.add(StatText.line(Stat.FALLOFF_END, stats.get(Stat.FALLOFF_END)));
            tooltip.add(StatText.line(Stat.RELOAD_TIME, stats.get(Stat.RELOAD_TIME)));
            tooltip.add(StatText.line(Stat.RECOIL_VERTICAL, stats.get(Stat.RECOIL_VERTICAL)));
            tooltip.add(StatText.line(Stat.HIP_SPREAD, stats.get(Stat.HIP_SPREAD)));
            tooltip.add(StatText.line(Stat.MOVE_SPEED, stats.get(Stat.MOVE_SPEED)));
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.tier", definition.tier()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.remnants_ballistics.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
