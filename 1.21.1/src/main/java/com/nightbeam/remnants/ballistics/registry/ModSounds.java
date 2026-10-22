package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.weapon.SoundProfile;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sound events. Every name here has an entry in {@code assets/remnants_ballistics/sounds.json}; the asset
 * manifest test ({@code tools/check_assets.py}) verifies that each referenced .ogg exists.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, RemnantsBallistics.MODID);
    private static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> BY_NAME = new LinkedHashMap<>();
    private static final Map<SoundProfile, DeferredHolder<SoundEvent, SoundEvent>> FIRE = new EnumMap<>(SoundProfile.class);

    static {
        for (SoundProfile profile : SoundProfile.values()) {
            FIRE.put(profile, register("gun." + profile.id() + ".fire"));
        }
    }

    public static final DeferredHolder<SoundEvent, SoundEvent> SUPPRESSED_FIRE = register("gun.suppressed.fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTANT_FIRE = register("gun.distant.fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> RAILGUN_CHARGE = register("gun.railgun.charge");

    public static final DeferredHolder<SoundEvent, SoundEvent> DRY_FIRE = register("gun.dry_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_MODE = register("gun.fire_mode");
    public static final DeferredHolder<SoundEvent, SoundEvent> EQUIP = register("gun.equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> AIM_IN = register("gun.aim_in");
    public static final DeferredHolder<SoundEvent, SoundEvent> AIM_OUT = register("gun.aim_out");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAG_OUT = register("gun.mag_out");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAG_IN = register("gun.mag_in");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDE_BACK = register("gun.slide_back");
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDE_RELEASE = register("gun.slide_release");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOLT_OPEN = register("gun.bolt_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOLT_CLOSE = register("gun.bolt_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP_BACK = register("gun.pump_back");
    public static final DeferredHolder<SoundEvent, SoundEvent> PUMP_FORWARD = register("gun.pump_forward");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELL_INSERT = register("gun.shell_insert");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYLINDER_OPEN = register("gun.cylinder_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> CYLINDER_CLOSE = register("gun.cylinder_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> CELL_OUT = register("gun.cell_out");
    public static final DeferredHolder<SoundEvent, SoundEvent> CELL_IN = register("gun.cell_in");
    public static final DeferredHolder<SoundEvent, SoundEvent> COVER_OPEN = register("gun.cover_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> COVER_CLOSE = register("gun.cover_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> CASING_DROP = register("gun.casing_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHELL_DROP = register("gun.shell_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> INSPECT = register("gun.inspect");
    public static final DeferredHolder<SoundEvent, SoundEvent> JAMMED = register("gun.jammed");

    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_STONE = register("impact.stone");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_METAL = register("impact.metal");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_WOOD = register("impact.wood");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_GLASS = register("impact.glass");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_DIRT = register("impact.dirt");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_WATER = register("impact.water");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_FLESH = register("impact.flesh");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMPACT_ENERGY = register("impact.energy");
    public static final DeferredHolder<SoundEvent, SoundEvent> RICOCHET = register("impact.ricochet");
    public static final DeferredHolder<SoundEvent, SoundEvent> WHIZ = register("impact.whiz");

    public static final DeferredHolder<SoundEvent, SoundEvent> HITMARKER = register("ui.hitmarker");
    public static final DeferredHolder<SoundEvent, SoundEvent> HEADSHOT = register("ui.headshot");
    public static final DeferredHolder<SoundEvent, SoundEvent> KILL = register("ui.kill");

    public static final DeferredHolder<SoundEvent, SoundEvent> GUNSMITH_USE = register("block.gunsmith.use");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMMO_PRESS_USE = register("block.ammo_press.use");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ORB_HUM = register("entity.void_orb.hum");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(RemnantsBallistics.id(name)));
        BY_NAME.put(name, holder);
        return holder;
    }

    public static SoundEvent fire(SoundProfile profile) {
        return FIRE.get(profile).get();
    }

    /** Looks up a sound by its event name (used by animation sound keyframes). */
    public static SoundEvent byName(String name) {
        DeferredHolder<SoundEvent, SoundEvent> holder = BY_NAME.get(name);
        return holder == null ? null : holder.get();
    }

    public static Map<String, DeferredHolder<SoundEvent, SoundEvent>> all() {
        return Collections.unmodifiableMap(BY_NAME);
    }
}
