package com.nightbeam.remnants.ballistics.client.screen;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Ammunition press screen: three ingredient slots, a press arrow and the result slot. */
public class AmmoPressScreen extends AbstractContainerScreen<AmmoPressMenu> {
    private static final ResourceLocation TEXTURE = RemnantsBallistics.id("textures/gui/container/ammo_press.png");

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        boolean ready = menu.getSlot(AmmoPressMenu.RESULT_SLOT).hasItem();
        // Arrow fills when a recipe matches.
        if (ready) g.blit(TEXTURE, leftPos + 72, topPos + 34, 176, 0, 24, 17);
        if (!ready) {
            g.drawWordWrap(font, Component.translatable("gui.remnants_ballistics.ammo_press_hint"), leftPos + 56, topPos + 58, 112, 0x707070);
        }
    }
}
