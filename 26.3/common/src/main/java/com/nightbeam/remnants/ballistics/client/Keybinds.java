package com.nightbeam.remnants.ballistics.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.client.KeyMapping;

import java.util.List;

/**
 * Weapon key bindings. Firing and aiming reuse the vanilla attack and use keys so players keep their existing
 * mouse layout; these are the additional actions. The loaders register {@link #ALL} (NeoForge also limits them to
 * in-game use, so they do not conflict with GUI keys).
 */
public final class Keybinds {
    /** Registered by each loader (label: {@code key.category.remnants_ballistics.weapons}). */
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(RemnantsBallistics.id("weapons"));

    public static final KeyMapping RELOAD = new KeyMapping("key.remnants_ballistics.reload", InputConstants.Type.KEYBOARD, InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping FIRE_MODE = new KeyMapping("key.remnants_ballistics.fire_mode", InputConstants.Type.KEYBOARD, InputConstants.KEY_V, CATEGORY);
    public static final KeyMapping INSPECT = new KeyMapping("key.remnants_ballistics.inspect", InputConstants.Type.KEYBOARD, InputConstants.KEY_I, CATEGORY);

    public static final List<KeyMapping> ALL = List.of(RELOAD, FIRE_MODE, INSPECT);

    private Keybinds() {}
}
