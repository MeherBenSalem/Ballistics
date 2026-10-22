package com.nightbeam.remnants.ballistics.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Weapon key bindings. Firing and aiming reuse the vanilla attack and use keys so players keep their existing
 * mouse layout; these are the additional actions.
 */
public final class Keybinds {
    public static final String CATEGORY = "key.categories.remnants_ballistics";

    public static final KeyMapping RELOAD = new KeyMapping("key.remnants_ballistics.reload",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping FIRE_MODE = new KeyMapping("key.remnants_ballistics.fire_mode",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping INSPECT = new KeyMapping("key.remnants_ballistics.inspect",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, CATEGORY);

    /** Registered by each loader (NeoForge also limits them to the in-game key conflict context). */
    public static final KeyMapping[] ALL = {RELOAD, FIRE_MODE, INSPECT};

    private Keybinds() {}
}
