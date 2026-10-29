package com.nightbeam.remnants.ballistics.client.screen;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** Ammunition press screen: three ingredient slots, a press arrow and the result slot. */
public class AmmoPressScreen extends AbstractContainerScreen<AmmoPressMenu> {
    private static final Identifier TEXTURE = RemnantsBallistics.id("textures/gui/container/ammo_press.png");

    public AmmoPressScreen(AmmoPressMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
        boolean ready = menu.getSlot(AmmoPressMenu.RESULT_SLOT).hasItem();
        // Arrow fills when a recipe matches.
        if (ready) g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 72, topPos + 34, 176f, 0f, 24, 17, 256, 256);
        if (!ready) {
            g.textWithWordWrap(font, Component.translatable("gui.remnants_ballistics.ammo_press_hint"), leftPos + 56, topPos + 58, 112, 0xFF707070, false);
        }
    }
}
