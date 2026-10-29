package com.nightbeam.remnants.ballistics.client.render;

import com.google.gson.JsonElement;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Loads {@link GunVisuals} for every weapon. Missing or broken files are logged and use a visible fallback. */
public final class GunVisualsLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final GunVisualsLoader INSTANCE = new GunVisualsLoader();

    private volatile Map<String, GunVisuals> visuals = Map.of();
    private final Set<String> warned = new HashSet<>();

    private GunVisualsLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("gun_visuals"));
    }

    /** Whether the weapon has a loaded visuals file (rather than the fallback). */
    public boolean has(WeaponDefinition def) {
        return visuals.containsKey(def.name());
    }

    public GunVisuals get(WeaponDefinition def) {
        GunVisuals v = visuals.get(def.name());
        if (v != null) return v;
        synchronized (warned) {
            if (warned.add(def.name())) {
                RemnantsBallistics.LOGGER.warn("No gun_visuals/{}.json found; using the generic fallback pose", def.name());
            }
        }
        return GunVisuals.fallback(RemnantsBallistics.id("rifle"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, GunVisuals> loaded = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> e : files.entrySet()) {
            if (!e.getKey().getNamespace().equals(RemnantsBallistics.MODID)) continue;
            try {
                loaded.put(e.getKey().getPath(), GunVisuals.parse(e.getValue().getAsJsonObject()));
            } catch (RuntimeException ex) {
                RemnantsBallistics.LOGGER.error("Invalid gun visuals {}", e.getKey(), ex);
            }
        }
        for (WeaponDefinition def : Weapons.all()) {
            if (!loaded.containsKey(def.name())) RemnantsBallistics.LOGGER.warn("Weapon {} has no gun_visuals entry", def.name());
        }
        visuals = Map.copyOf(loaded);
        synchronized (warned) {
            warned.clear();
        }
    }
}
