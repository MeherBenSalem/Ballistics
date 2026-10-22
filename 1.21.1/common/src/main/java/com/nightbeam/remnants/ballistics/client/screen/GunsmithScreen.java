package com.nightbeam.remnants.ballistics.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentItem;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.render.GunRenderer;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import com.nightbeam.remnants.ballistics.weapon.StatText;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Gunsmithing workbench screen: attachment slots arranged around the weapon, a rotating 3D preview and a live
 * stat panel. Hovering or carrying an attachment previews the change before it is fitted.
 */
public class GunsmithScreen extends AbstractContainerScreen<GunsmithMenu> {
    private static final ResourceLocation TEXTURE = RemnantsBallistics.id("textures/gui/container/gunsmith.png");
    private static final ResourceLocation PANEL = RemnantsBallistics.id("textures/gui/container/stat_panel.png");
    private static final int PANEL_WIDTH = 128;
    private static final List<Stat> SHOWN = List.of(Stat.DAMAGE, Stat.FIRE_RATE, Stat.MAGAZINE, Stat.FALLOFF_END, Stat.RELOAD_TIME,
            Stat.ADS_TIME, Stat.HIP_SPREAD, Stat.ADS_SPREAD, Stat.RECOIL_VERTICAL, Stat.RECOIL_HORIZONTAL, Stat.MOVE_SPEED, Stat.ZOOM, Stat.LOUDNESS);

    private Button repairButton;

    public GunsmithScreen(GunsmithMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // The stat panel sits to the right of the standard-sized container.
        this.leftPos = (this.width - imageWidth - PANEL_WIDTH) / 2;
        this.titleLabelX = 8;
        repairButton = addRenderableWidget(Button.builder(Component.translatable("gui.remnants_ballistics.repair"), b -> {
            if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, GunsmithMenu.BUTTON_REPAIR);
        }).bounds(leftPos + 128, topPos + 18, 42, 14).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack weapon = menu.weapon();
        repairButton.active = weapon.isDamaged() && GunsmithMenu.isRepairItem(menu.repairItem());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int px = leftPos + imageWidth + 2;
        g.blit(PANEL, px, topPos, 0, 0, PANEL_WIDTH, imageHeight, PANEL_WIDTH, imageHeight);

        // Grey out slots the current weapon does not support.
        for (Slot slot : menu.slots) {
            if (slot instanceof GunsmithMenu.AttachmentSlotView view && menu.definition() != null && !view.isAvailable()) {
                g.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0x90202020);
            }
        }

        WeaponDefinition def = menu.definition();
        if (def == null) {
            g.drawWordWrap(font, Component.translatable("gui.remnants_ballistics.insert_weapon"), px + 6, topPos + 8, PANEL_WIDTH - 12, 0xA0A0A0);
            return;
        }
        renderPreview(g, def, px + PANEL_WIDTH / 2, topPos + 30);
        renderStats(g, def, px + 6, topPos + 58, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // No "Inventory" label: the bottom row of attachment slots fills the space it would take.
        g.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
    }

    /** Slowly rotating 3D model of the weapon with its current attachments. */
    private void renderPreview(GuiGraphics g, WeaponDefinition def, int cx, int cy) {
        PoseStack ps = g.pose();
        ps.pushPose();
        ps.translate(cx, cy, 150);
        float s = 30f;
        ps.scale(s, -s, s);
        float t = (System.currentTimeMillis() % 12000L) / 12000f * 360f;
        ps.mulPose(Axis.XP.rotationDegrees(15f));
        ps.mulPose(Axis.YP.rotationDegrees(t));
        ps.translate(-0.5f, -0.5f, -0.5f);
        GunRenderer.render(def, GunVisualsLoader.INSTANCE.get(def), menu.currentAttachments(), null, ps, g.bufferSource(), 0xF000F0,
                OverlayTexture.NO_OVERLAY);
        g.flush();
        ps.popPose();
    }

    private void renderStats(GuiGraphics g, WeaponDefinition def, int x, int y, int mouseX, int mouseY) {
        GunAttachments current = menu.currentAttachments();
        WeaponStats now = menu.statsWith(current);
        GunAttachments candidate = previewCandidate(def, current);
        WeaponStats preview = candidate == null ? null : menu.statsWith(candidate);
        g.drawString(font, Component.translatable("item.remnants_ballistics." + def.name()).withStyle(def.rarity().color()), x, y - 10, 0xFFFFFF, true);
        int row = 0;
        for (Stat stat : SHOWN) {
            int ry = y + row * 8;
            float value = now.get(stat);
            g.pose().pushPose();
            g.pose().translate(x, ry, 0);
            g.pose().scale(0.75f, 0.75f, 1f);
            g.drawString(font, StatText.name(stat), 0, 0, 0xB0B0B0, false);
            String text = StatText.value(stat, value);
            g.drawString(font, text, 92, 0, 0xFFFFFF, false);
            if (preview != null) {
                Component delta = StatText.delta(stat, value, preview.get(stat));
                g.drawString(font, delta, 92 + font.width(text) + 4, 0, 0xFFFFFF, false);
            }
            g.pose().popPose();
            row++;
        }
        ItemStack weapon = menu.weapon();
        if (weapon.isDamageableItem()) {
            int dy = y + row * 8 + 2;
            float durability = 1f - weapon.getDamageValue() / (float) weapon.getMaxDamage();
            g.fill(x, dy, x + PANEL_WIDTH - 12, dy + 3, 0xFF303030);
            g.fill(x, dy, x + (int) ((PANEL_WIDTH - 12) * durability), dy + 3, durability < 0.2f ? 0xFFE04040 : 0xFF50C050);
        }
    }

    /** The attachment set that would result from fitting the carried or hovered attachment. */
    private GunAttachments previewCandidate(WeaponDefinition def, GunAttachments current) {
        ItemStack candidate = menu.getCarried();
        if (candidate.isEmpty() && hoveredSlot != null) candidate = hoveredSlot.getItem();
        if (!(candidate.getItem() instanceof AttachmentItem item)) return null;
        AttachmentDefinition att = item.definition();
        if (!att.isCompatibleWith(def)) return null;
        AttachmentSlot slot = att.slot();
        if (att.name().equals(current.entries().get(slot))) return current.without(slot);
        return current.with(slot, att.name());
    }

    @Override
    protected void renderTooltip(GuiGraphics g, int mouseX, int mouseY) {
        super.renderTooltip(g, mouseX, mouseY);
        if (hoveredSlot instanceof GunsmithMenu.AttachmentSlotView view && !view.hasItem() && menu.getCarried().isEmpty()) {
            Component label = Component.translatable(view.type().translationKey())
                    .withStyle(view.isAvailable() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);
            g.renderTooltip(font, label, mouseX, mouseY);
        }
    }
}
