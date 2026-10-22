package com.nightbeam.remnants.ballistics;

import com.mojang.logging.LogUtils;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.registry.ModRegistries;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Common entry point, called by each loader's mod initialiser. Loader-specific wiring (configs, payloads, events) lives
 * in the {@code neoforge} and {@code fabric} modules; everything here is plain Minecraft.
 */
public final class RemnantsBallistics {
    public static final String MODID = "remnants_ballistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    private RemnantsBallistics() {}

    /** Creates every registry entry. Call once, from the loader entry point. */
    public static void init() {
        ModRegistries.init();
    }

    /** Validates the weapon and attachment tables; call once registration has finished. */
    public static void setup() {
        Weapons.validate(Attachments::byName);
        LOGGER.info("Remnants: Ballistics loaded {} weapons and {} attachments on {}", Weapons.all().size(), Attachments.all().size(),
                Services.PLATFORM.loaderName());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
