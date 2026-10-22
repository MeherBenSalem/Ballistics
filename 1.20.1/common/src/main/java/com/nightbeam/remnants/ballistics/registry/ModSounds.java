package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import com.nightbeam.remnants.ballistics.weapon.SoundProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sound events. Every name here has an entry in {@code assets/remnants_ballistics/sounds.json}; the asset
 * manifest test ({@code tools/check_assets.py}) verifies that each referenced .ogg exists.
 */
public final class ModSounds {
    public static final RegistrationProvider<SoundEvent> SOUNDS = RegistrationProvider.get(Registries.SOUND_EVENT, RemnantsBallistics.MODID);
    private static final Map<String, RegistryObject<SoundEvent, SoundEvent>> BY_NAME = new LinkedHashMap<>();
    private static final Map<SoundProfile, RegistryObject<SoundEvent, SoundEvent>> FIRE = new EnumMap<>(SoundProfile.class);

    static {
        for (SoundProfile profile : SoundProfile.values()) {
            FIRE.put(profile, register("gun." + profile.id() + ".fire"));
        }
    }

    public static final RegistryObject<SoundEvent, SoundEvent> SUPPRESSED_FIRE = register("gun.suppressed.fire");
    public static final RegistryObject<SoundEvent, SoundEvent> DISTANT_FIRE = register("gun.distant.fire");
    public static final RegistryObject<SoundEvent, SoundEvent> RAILGUN_CHARGE = register("gun.railgun.charge");

    public static final RegistryObject<SoundEvent, SoundEvent> DRY_FIRE = register("gun.dry_fire");
    public static final RegistryObject<SoundEvent, SoundEvent> FIRE_MODE = register("gun.fire_mode");
    public static final RegistryObject<SoundEvent, SoundEvent> EQUIP = register("gun.equip");
    public static final RegistryObject<SoundEvent, SoundEvent> AIM_IN = register("gun.aim_in");
    public static final RegistryObject<SoundEvent, SoundEvent> AIM_OUT = register("gun.aim_out");
    public static final RegistryObject<SoundEvent, SoundEvent> MAG_OUT = register("gun.mag_out");
    public static final RegistryObject<SoundEvent, SoundEvent> MAG_IN = register("gun.mag_in");
    public static final RegistryObject<SoundEvent, SoundEvent> SLIDE_BACK = register("gun.slide_back");
    public static final RegistryObject<SoundEvent, SoundEvent> SLIDE_RELEASE = register("gun.slide_release");
    public static final RegistryObject<SoundEvent, SoundEvent> BOLT_OPEN = register("gun.bolt_open");
    public static final RegistryObject<SoundEvent, SoundEvent> BOLT_CLOSE = register("gun.bolt_close");
    public static final RegistryObject<SoundEvent, SoundEvent> PUMP_BACK = register("gun.pump_back");
    public static final RegistryObject<SoundEvent, SoundEvent> PUMP_FORWARD = register("gun.pump_forward");
    public static final RegistryObject<SoundEvent, SoundEvent> SHELL_INSERT = register("gun.shell_insert");
    public static final RegistryObject<SoundEvent, SoundEvent> CYLINDER_OPEN = register("gun.cylinder_open");
    public static final RegistryObject<SoundEvent, SoundEvent> CYLINDER_CLOSE = register("gun.cylinder_close");
    public static final RegistryObject<SoundEvent, SoundEvent> CELL_OUT = register("gun.cell_out");
    public static final RegistryObject<SoundEvent, SoundEvent> CELL_IN = register("gun.cell_in");
    public static final RegistryObject<SoundEvent, SoundEvent> COVER_OPEN = register("gun.cover_open");
    public static final RegistryObject<SoundEvent, SoundEvent> COVER_CLOSE = register("gun.cover_close");
    public static final RegistryObject<SoundEvent, SoundEvent> CASING_DROP = register("gun.casing_drop");
    public static final RegistryObject<SoundEvent, SoundEvent> SHELL_DROP = register("gun.shell_drop");
    public static final RegistryObject<SoundEvent, SoundEvent> INSPECT = register("gun.inspect");
    public static final RegistryObject<SoundEvent, SoundEvent> JAMMED = register("gun.jammed");

    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_STONE = register("impact.stone");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_METAL = register("impact.metal");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_WOOD = register("impact.wood");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_GLASS = register("impact.glass");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_DIRT = register("impact.dirt");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_WATER = register("impact.water");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_FLESH = register("impact.flesh");
    public static final RegistryObject<SoundEvent, SoundEvent> IMPACT_ENERGY = register("impact.energy");
    public static final RegistryObject<SoundEvent, SoundEvent> RICOCHET = register("impact.ricochet");
    public static final RegistryObject<SoundEvent, SoundEvent> WHIZ = register("impact.whiz");

    public static final RegistryObject<SoundEvent, SoundEvent> HITMARKER = register("ui.hitmarker");
    public static final RegistryObject<SoundEvent, SoundEvent> HEADSHOT = register("ui.headshot");
    public static final RegistryObject<SoundEvent, SoundEvent> KILL = register("ui.kill");

    public static final RegistryObject<SoundEvent, SoundEvent> GUNSMITH_USE = register("block.gunsmith.use");
    public static final RegistryObject<SoundEvent, SoundEvent> AMMO_PRESS_USE = register("block.ammo_press.use");
    public static final RegistryObject<SoundEvent, SoundEvent> VOID_ORB_HUM = register("entity.void_orb.hum");

    private ModSounds() {}

    static void init() {}

    private static RegistryObject<SoundEvent, SoundEvent> register(String name) {
        RegistryObject<SoundEvent, SoundEvent> holder = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RemnantsBallistics.id(name)));
        BY_NAME.put(name, holder);
        return holder;
    }

    public static SoundEvent fire(SoundProfile profile) {
        return FIRE.get(profile).get();
    }

    /** Looks up a sound by its event name (used by animation sound keyframes). */
    public static SoundEvent byName(String name) {
        RegistryObject<SoundEvent, SoundEvent> holder = BY_NAME.get(name);
        return holder == null ? null : holder.get();
    }

    public static Map<String, RegistryObject<SoundEvent, SoundEvent>> all() {
        return Collections.unmodifiableMap(BY_NAME);
    }
}
