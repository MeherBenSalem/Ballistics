package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.registry.ModDataComponents;
import com.nightbeam.remnants.ballistics.client.ClientKeys;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A firearm. All behaviour is data-driven by its {@link WeaponDefinition}; firing and reloading are handled by the
 * network layer and {@code combat} package rather than vanilla use/attack, so the item suppresses vanilla
 * mining, melee and swing behaviour.
 */
public class GunItem extends Item {
    private final WeaponDefinition definition;

    public GunItem(Properties properties, WeaponDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    /** @param properties the registry's properties (with the item id set) */
    public static Properties properties(Properties properties, WeaponDefinition def) {
        return properties
                .stacksTo(1)
                .durability(def.durability())
                .rarity(def.rarity().vanilla())
                .component(ModDataComponents.AMMO.get(), 0)
                .component(ModDataComponents.FIRE_MODE.get(), def.defaultFireMode())
                .component(ModDataComponents.ATTACHMENTS.get(), new GunAttachments(def.defaultAttachments()))
                // Repairable with gun parts on an anvil. Delayed: the parts item registers after the weapons.
                .delayedComponent(DataComponents.REPAIRABLE, registries -> new Repairable(HolderSet.direct(
                        registries.lookupOrThrow(Registries.ITEM).getOrThrow(ModItems.GUN_PARTS.key()))));
    }

    public WeaponDefinition definition() {
        return definition;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId()).withStyle(definition.rarity().color());
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }

    // Loader item hooks. These carry no @Override: common code compiles against vanilla, and each method overrides the
    // loader's default at runtime (NeoForge IItemExtension, Fabric FabricItem). Fabric has no attack hook on items; it
    // cancels weapon melee with an AttackEntityCallback instead.

    /** NeoForge: weapons never melee. */
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return true;
    }

    /** NeoForge: no arm swing. */
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    /** NeoForge: ammo, fire mode and durability updates must not replay the draw animation. */
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || oldStack.getItem() != newStack.getItem();
    }

    /** Fabric: as above; only called when the item stayed the same and its components changed. */
    public boolean allowComponentsUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        WeaponStats stats = GunData.stats(stack, definition);
        tooltip.accept(Component.translatable(definition.category().translationKey())
                .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(definition.ammo().translationKey()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.ammo", GunData.ammo(stack), GunData.maxCapacity(definition, stats))
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.fire_mode",
                Component.translatable(GunData.fireMode(stack, definition).translationKey()),
                definition.fireModes().stream().map(m -> Component.translatable(m.translationKey()).getString()).collect(Collectors.joining(" / ")))
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.remnants_ballistics.rarity", Component.translatable(definition.rarity().translationKey()))
                .withStyle(definition.rarity().color()));

        if (GunData.isBroken(stack)) {
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.broken").withStyle(ChatFormatting.RED));
        }

        List<AttachmentDefinition> fitted = GunData.attachments(stack).resolved();
        if (!fitted.isEmpty()) {
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.attachments").withStyle(ChatFormatting.GRAY));
            for (AttachmentDefinition att : fitted) {
                tooltip.accept(Component.literal("  ").append(Component.translatable("item.remnants_ballistics." + att.name()))
                        .withStyle(att.rarity().color()));
            }
        }

        if (Services.PLATFORM.isPhysicalClient() && ClientKeys.shiftDown()) {
            int pellets = stats.getInt(Stat.PELLETS);
            tooltip.accept(StatText.line(Stat.DAMAGE, stats.get(Stat.DAMAGE))
                    .append(pellets > 1 ? Component.literal(" ×" + pellets).withStyle(ChatFormatting.GRAY) : Component.empty()));
            tooltip.accept(StatText.line(Stat.FIRE_RATE, stats.get(Stat.FIRE_RATE)));
            tooltip.accept(StatText.line(Stat.MAGAZINE, stats.get(Stat.MAGAZINE)));
            tooltip.accept(StatText.line(Stat.FALLOFF_END, stats.get(Stat.FALLOFF_END)));
            tooltip.accept(StatText.line(Stat.RELOAD_TIME, stats.get(Stat.RELOAD_TIME)));
            tooltip.accept(StatText.line(Stat.RECOIL_VERTICAL, stats.get(Stat.RECOIL_VERTICAL)));
            tooltip.accept(StatText.line(Stat.HIP_SPREAD, stats.get(Stat.HIP_SPREAD)));
            tooltip.accept(StatText.line(Stat.MOVE_SPEED, stats.get(Stat.MOVE_SPEED)));
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.tier", definition.tier()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltip.remnants_ballistics.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
