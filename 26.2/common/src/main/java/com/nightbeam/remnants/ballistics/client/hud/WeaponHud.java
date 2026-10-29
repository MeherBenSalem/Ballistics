package com.nightbeam.remnants.ballistics.client.hud;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.ammo.AmmoInventory;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.client.ClientGunHandler;
import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.combat.ServerWeaponHandler;
import com.nightbeam.remnants.ballistics.config.ClientConfig;
import com.nightbeam.remnants.ballistics.core.SpreadModel;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Weapon HUD layer: scope overlay, reflex reticle, spread-aware crosshair, hit markers and the ammo panel.
 * Position, scale and opacity come from the client config. Each loader draws it right above the crosshair.
 */
public final class WeaponHud {
    public static final Identifier ID = RemnantsBallistics.id("weapon_hud");
    public static final WeaponHud INSTANCE = new WeaponHud();
    private static final Identifier SCOPE = RemnantsBallistics.id("hud/scope");
    private static final Identifier REFLEX_DOT = RemnantsBallistics.id("hud/reflex_dot");
    private static final Identifier HOLO_RETICLE = RemnantsBallistics.id("hud/holo_reticle");
    private static final int PANEL_WIDTH = 104;
    private static final int PANEL_HEIGHT = 38;

    private WeaponHud() {}

    public void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.hud.isHidden() || !(player.getMainHandItem().getItem() instanceof GunItem gun)) return;
        ItemStack stack = player.getMainHandItem();
        WeaponDefinition def = gun.definition();
        WeaponStats stats = GunData.stats(stack, def);
        float pt = delta.getGameTimeDeltaPartialTick(true);
        float ads = ClientGunHandler.adsProgress(pt);
        OpticType optic = GunData.optic(stack, def);
        int w = g.guiWidth();
        int h = g.guiHeight();

        if (optic == OpticType.SCOPE && ads > 0.92f) {
            renderScope(g, w, h);
        } else if (optic == OpticType.REFLEX && ads > 0.75f) {
            AttachmentDefinition fitted = GunData.attachments(stack).get(AttachmentSlot.OPTIC).orElse(null);
            Identifier reticle = fitted != null && fitted.name().equals("tripoint_holo") ? HOLO_RETICLE : REFLEX_DOT;
            float a = Mth.clamp((ads - 0.75f) / 0.25f, 0f, 1f);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, reticle, w / 2 - 8, h / 2 - 8, 16, 16, a);
        }

        if (ClientConfig.get(ClientConfig.DYNAMIC_CROSSHAIR) && ads < 0.5f && mc.options.getCameraType().isFirstPerson()) {
            renderCrosshair(g, mc, player, stats, ads, w, h);
        }
        renderHitMarker(g, w, h);
        if (ClientConfig.get(ClientConfig.HUD_ENABLED)) renderPanel(g, mc, player, stack, def, stats);
    }

    private static void renderScope(GuiGraphicsExtractor g, int w, int h) {
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        if (x > 0) {
            g.fill(0, 0, x, h, 0xFF000000);
            g.fill(x + size, 0, w, h, 0xFF000000);
        }
        if (y > 0) {
            g.fill(0, 0, w, y, 0xFF000000);
            g.fill(0, y + size, w, h, 0xFF000000);
        }
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SCOPE, x, y, size, size);
    }

    private static void renderCrosshair(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, WeaponStats stats, float ads, int w, int h) {
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        long now = player.level().getGameTime();
        float bloom = SpreadModel.recoverBloom(stats, s.bloom, (now - s.lastShotTick) / 20f);
        float spread = SpreadModel.spread(stats, ads, ServerWeaponHandler.movement(player), bloom, 1f);
        double fov = Math.toRadians(mc.options.fov().get());
        int gap = 2 + (int) Math.round(h / 2.0 * Math.tan(Math.toRadians(spread)) / Math.tan(fov / 2.0));
        gap = Math.min(gap, h / 4);
        int color = ClientConfig.get(ClientConfig.CROSSHAIR_COLOR);
        int alpha = (int) (255 * ClientConfig.get(ClientConfig.HUD_OPACITY) * (1f - ads * 2f));
        if (alpha <= 8) return;
        int argb = (alpha << 24) | color;
        int cx = w / 2;
        int cy = h / 2;
        int len = 4;
        boolean outline = ClientConfig.get(ClientConfig.HIGH_CONTRAST);
        int shadow = (alpha << 24);
        if (outline) {
            g.fill(cx - gap - len - 1, cy - 1, cx - gap + 1, cy + 2, shadow);
            g.fill(cx + gap - 1, cy - 1, cx + gap + len + 1, cy + 2, shadow);
            g.fill(cx - 1, cy - gap - len - 1, cx + 2, cy - gap + 1, shadow);
            g.fill(cx - 1, cy + gap - 1, cx + 2, cy + gap + len + 1, shadow);
        }
        g.fill(cx - gap - len, cy, cx - gap, cy + 1, argb);
        g.fill(cx + gap, cy, cx + gap + len, cy + 1, argb);
        g.fill(cx, cy - gap - len, cx + 1, cy - gap, argb);
        g.fill(cx, cy + gap, cx + 1, cy + gap + len, argb);
        g.fill(cx, cy, cx + 1, cy + 1, argb);
    }

    private static void renderHitMarker(GuiGraphicsExtractor g, int w, int h) {
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        if (!ClientConfig.get(ClientConfig.HITMARKERS)) return;
        long age = System.nanoTime() - s.hitMarkerNanos;
        if (s.hitMarkerNanos == 0L || age > 300_000_000L) return;
        float fade = 1f - age / 3.0E8f;
        int rgb = s.hitMarkerKill ? 0xFF3030 : s.hitMarkerHeadshot ? 0xFFB020 : 0xFFFFFF;
        int argb = ((int) (255 * fade) << 24) | rgb;
        int cx = w / 2;
        int cy = h / 2;
        int inner = 3;
        int outer = s.hitMarkerKill ? 8 : 6;
        for (int i = inner; i <= outer; i++) {
            g.fill(cx - i, cy - i, cx - i + 1, cy - i + 1, argb);
            g.fill(cx + i, cy - i, cx + i + 1, cy - i + 1, argb);
            g.fill(cx - i, cy + i, cx - i + 1, cy + i + 1, argb);
            g.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, argb);
        }
    }

    private static void renderPanel(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, ItemStack stack, WeaponDefinition def, WeaponStats stats) {
        Font font = mc.font;
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        float scale = (float) ClientConfig.get(ClientConfig.HUD_SCALE);
        float opacity = (float) ClientConfig.get(ClientConfig.HUD_OPACITY);
        int alpha = (int) (255 * opacity) << 24;
        int w = (int) (g.guiWidth() / scale);
        int h = (int) (g.guiHeight() / scale);
        int ox = ClientConfig.get(ClientConfig.HUD_OFFSET_X);
        int oy = ClientConfig.get(ClientConfig.HUD_OFFSET_Y);
        int x;
        int y;
        switch (ClientConfig.get(ClientConfig.HUD_ANCHOR)) {
            case BOTTOM_LEFT -> {
                x = ox;
                y = h - PANEL_HEIGHT - oy - 22;
            }
            case TOP_RIGHT -> {
                x = w - PANEL_WIDTH - ox;
                y = oy;
            }
            case TOP_LEFT -> {
                x = ox;
                y = oy;
            }
            default -> {
                x = w - PANEL_WIDTH - ox;
                y = h - PANEL_HEIGHT - oy;
            }
        }
        boolean contrast = ClientConfig.get(ClientConfig.HIGH_CONTRAST);
        g.pose().pushMatrix();
        g.pose().scale(scale, scale);
        int bg = ((int) (opacity * (contrast ? 200 : 120)) << 24);
        g.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, bg);
        g.fill(x, y, x + 2, y + PANEL_HEIGHT, alpha | def.rarity().rgb());

        int ammo = GunData.ammo(stack);
        int capacity = GunData.maxCapacity(def, stats);
        int reserve = AmmoInventory.count(player, def.ammo());
        boolean infinite = reserve >= AmmoInventory.INFINITE;
        g.text(font, Component.translatable(stack.getItem().getDescriptionId()), x + 6, y + 4, alpha | def.rarity().rgb(), contrast);

        g.pose().pushMatrix();
        g.pose().translate(x + 6, y + 15);
        g.pose().scale(1.5f, 1.5f);
        int ammoColor = ammo == 0 ? 0xFF4040 : ammo <= Math.max(1, capacity / 4) ? 0xFFB040 : 0xFFFFFF;
        String ammoText = Integer.toString(ammo);
        g.text(font, ammoText, 0, 0, alpha | ammoColor, true);
        g.pose().popMatrix();
        int ammoWidth = (int) (font.width(ammoText) * 1.5f);
        String reserveText = infinite ? "/ ∞" : "/ " + reserve;
        g.text(font, reserveText, x + 9 + ammoWidth, y + 19, alpha | 0xA0A0A0, contrast);

        String mode = Component.translatable(GunData.fireMode(stack, def).translationKey()).getString();
        g.text(font, mode, x + PANEL_WIDTH - 5 - font.width(mode), y + 19, alpha | 0xC8C8C8, contrast);

        // Status line: reload progress, charge progress or durability warnings.
        int barY = y + PANEL_HEIGHT - 6;
        if (s.isReloading()) {
            float progress = s.action.progress();
            g.fill(x + 6, barY, x + PANEL_WIDTH - 6, barY + 2, alpha | 0x303030);
            g.fill(x + 6, barY, x + 6 + (int) ((PANEL_WIDTH - 12) * progress), barY + 2, alpha | 0x60C0FF);
            if (ClientConfig.get(ClientConfig.RELOAD_TEXT)) {
                Component text = Component.translatable("hud.remnants_ballistics.reloading");
                g.text(font, text, x + PANEL_WIDTH / 2 - font.width(text) / 2, y - 10, alpha | 0x60C0FF, true);
            }
        } else if (s.chargeStartTick >= 0) {
            float charge = Mth.clamp((player.level().getGameTime() - s.chargeStartTick) / Math.max(1f, stats.get(Stat.CHARGE_TIME)), 0f, 1f);
            g.fill(x + 6, barY, x + PANEL_WIDTH - 6, barY + 2, alpha | 0x303030);
            g.fill(x + 6, barY, x + 6 + (int) ((PANEL_WIDTH - 12) * charge), barY + 2, alpha | (charge >= 1f ? 0x80FFFF : 0x40A0FF));
        } else if (GunData.isBroken(stack)) {
            Component text = Component.translatable("hud.remnants_ballistics.broken");
            g.text(font, text, x + PANEL_WIDTH / 2 - font.width(text) / 2, y - 10, alpha | 0xFF4040, true);
        } else if (ammo == 0) {
            Component text = reserve > 0 ? Component.translatable("hud.remnants_ballistics.press_reload", ClientKeyName.reload())
                    : Component.translatable("hud.remnants_ballistics.no_ammo");
            g.text(font, text, x + PANEL_WIDTH / 2 - font.width(text) / 2, y - 10, alpha | 0xFF6060, true);
        } else if (stack.isDamageableItem()) {
            float durability = 1f - stack.getDamageValue() / (float) stack.getMaxDamage();
            int color = durability < 0.15f ? 0xFF4040 : durability < 0.4f ? 0xFFB040 : 0x50D050;
            g.fill(x + 6, barY, x + PANEL_WIDTH - 6, barY + 1, alpha | 0x303030);
            g.fill(x + 6, barY, x + 6 + (int) ((PANEL_WIDTH - 12) * durability), barY + 1, alpha | color);
        }
        g.pose().popMatrix();
    }

    /** Small helper so the HUD shows the player's actual reload key. */
    static final class ClientKeyName {
        private ClientKeyName() {}

        static Component reload() {
            return com.nightbeam.remnants.ballistics.client.Keybinds.RELOAD.getTranslatedKeyMessage();
        }
    }
}
