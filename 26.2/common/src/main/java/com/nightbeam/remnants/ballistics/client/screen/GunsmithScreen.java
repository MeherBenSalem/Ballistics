package com.nightbeam.remnants.ballistics.client.screen;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentItem;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import com.nightbeam.remnants.ballistics.weapon.StatText;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Gunsmithing workbench screen: attachment slots arranged around the weapon, a rotating 3D preview and a live
 * stat panel. Hovering or carrying an attachment previews the change before it is fitted.
 */
public class GunsmithScreen extends AbstractContainerScreen<GunsmithMenu> {
    private static final Identifier TEXTURE = RemnantsBallistics.id("textures/gui/container/gunsmith.png");
    private static final Identifier PANEL = RemnantsBallistics.id("textures/gui/container/stat_panel.png");
    private static final int PANEL_WIDTH = 128;
    private static final List<Stat> SHOWN = List.of(Stat.DAMAGE, Stat.FIRE_RATE, Stat.MAGAZINE, Stat.FALLOFF_END, Stat.RELOAD_TIME,
            Stat.ADS_TIME, Stat.HIP_SPREAD, Stat.ADS_SPREAD, Stat.RECOIL_VERTICAL, Stat.RECOIL_HORIZONTAL, Stat.MOVE_SPEED, Stat.ZOOM, Stat.LOUDNESS);

    private Button repairButton;

    public GunsmithScreen(GunsmithMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    protected void init() {
        super.init();
        // The stat panel sits to the right of the standard-sized container.
        this.leftPos = (this.width - imageWidth - PANEL_WIDTH) / 2;
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
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
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
        int px = leftPos + imageWidth + 2;
        g.blit(RenderPipelines.GUI_TEXTURED, PANEL, px, topPos, 0f, 0f, PANEL_WIDTH, imageHeight, PANEL_WIDTH, imageHeight);

        // Grey out slots the current weapon does not support.
        for (Slot slot : menu.slots) {
            if (slot instanceof GunsmithMenu.AttachmentSlotView view && menu.definition() != null && !view.isAvailable()) {
                g.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, 0x90202020);
            }
        }

        WeaponDefinition def = menu.definition();
        if (def == null) {
            g.textWithWordWrap(font, Component.translatable("gui.remnants_ballistics.insert_weapon"), px + 6, topPos + 8, PANEL_WIDTH - 12, 0xFFA0A0A0, false);
            return;
        }
        renderPreview(g, def, px + PANEL_WIDTH / 2, topPos + 30);
        renderStats(g, def, px + 6, topPos + 58, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, titleLabelX, titleLabelY, 0xFF404040, false);
        g.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
    }

    /** Slowly rotating 3D model of the weapon with its current attachments, drawn into a picture-in-picture element. */
    private void renderPreview(GuiGraphicsExtractor g, WeaponDefinition def, int cx, int cy) {
        float spin = (System.currentTimeMillis() % 12000L) / 12000f * 360f;
        GunPreviewRenderer.submit(g, def, menu.currentAttachments(), spin, cx - PANEL_WIDTH / 2 + 4, cy - 24, cx + PANEL_WIDTH / 2 - 4, cy + 24);
    }

    private void renderStats(GuiGraphicsExtractor g, WeaponDefinition def, int x, int y, int mouseX, int mouseY) {
        GunAttachments current = menu.currentAttachments();
        WeaponStats now = menu.statsWith(current);
        GunAttachments candidate = previewCandidate(def, current);
        WeaponStats preview = candidate == null ? null : menu.statsWith(candidate);
        g.text(font, Component.translatable("item.remnants_ballistics." + def.name()).withStyle(def.rarity().color()), x, y - 10, 0xFFFFFFFF, true);
        int row = 0;
        for (Stat stat : SHOWN) {
            int ry = y + row * 8;
            float value = now.get(stat);
            g.pose().pushMatrix();
            g.pose().translate(x, ry);
            g.pose().scale(0.75f, 0.75f);
            g.text(font, StatText.name(stat), 0, 0, 0xFFB0B0B0, false);
            String text = StatText.value(stat, value);
            g.text(font, text, 92, 0, 0xFFFFFFFF, false);
            if (preview != null) {
                Component delta = StatText.delta(stat, value, preview.get(stat));
                g.text(font, delta, 92 + font.width(text) + 4, 0, 0xFFFFFFFF, false);
            }
            g.pose().popMatrix();
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
    protected void extractTooltip(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractTooltip(g, mouseX, mouseY);
        if (hoveredSlot instanceof GunsmithMenu.AttachmentSlotView view && !view.hasItem() && menu.getCarried().isEmpty()) {
            Component label = Component.translatable(view.type().translationKey())
                    .withStyle(view.isAvailable() ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);
            g.setTooltipForNextFrame(font, label, mouseX, mouseY);
        }
    }
}
