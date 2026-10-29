package com.nightbeam.remnants.ballistics;

import com.mojang.logging.LogUtils;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.platform.Services;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Loader-independent constants and setup. The loader entry points ({@code RemnantsBallisticsFabric},
 * {@code RemnantsBallisticsNeoForge}) register content from {@code registry.Registrar}, the configs and the payloads
 * in {@code network.ModNetwork}, then call {@link #commonSetup()}.
 */
public final class RemnantsBallistics {
    public static final String MODID = "remnants_ballistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    private RemnantsBallistics() {}

    public static void commonSetup() {
        Weapons.validate(Attachments::byName);
        LOGGER.info("Remnants: Ballistics loaded {} weapons and {} attachments on {}", Weapons.all().size(), Attachments.all().size(),
                Services.PLATFORM.getPlatformName());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
