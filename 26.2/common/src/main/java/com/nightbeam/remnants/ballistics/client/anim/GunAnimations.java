package com.nightbeam.remnants.ballistics.client.anim;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads animation sets from {@code assets/<ns>/animations/gun/<set>.json}. The format is the Bedrock/Blockbench
 * animation JSON subset: {@code animations → <name> → bones → <bone> → position|rotation|scale}, keyframes keyed by
 * time in seconds, values as arrays or {@code {"post": [...], "lerp_mode": "catmullrom"}}, plus
 * {@code sound_effects}. Missing sets are reported in the log, never silently replaced.
 */
public final class GunAnimations extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final GunAnimations INSTANCE = new GunAnimations();

    private volatile Map<Identifier, Map<String, GunAnimation>> sets = Map.of();

    private GunAnimations() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("animations/gun"));
    }

    /** @return the clip or null if the set or clip does not exist */
    public GunAnimation get(Identifier set, String name) {
        Map<String, GunAnimation> clips = sets.get(set);
        return clips == null ? null : clips.get(name);
    }

    public boolean hasSet(Identifier set) {
        return sets.containsKey(set);
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, Map<String, GunAnimation>> loaded = new HashMap<>();
        for (Map.Entry<Identifier, JsonElement> file : files.entrySet()) {
            try {
                loaded.put(file.getKey(), parseSet(file.getValue().getAsJsonObject()));
            } catch (RuntimeException e) {
                RemnantsBallistics.LOGGER.error("Failed to load gun animation set {}", file.getKey(), e);
            }
        }
        sets = Map.copyOf(loaded);
        RemnantsBallistics.LOGGER.info("Loaded {} gun animation sets", loaded.size());
    }

    static Map<String, GunAnimation> parseSet(JsonObject root) {
        Map<String, GunAnimation> clips = new HashMap<>();
        JsonObject animations = root.getAsJsonObject("animations");
        for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
            String name = entry.getKey();
            int dot = name.lastIndexOf('.');
            if (dot >= 0) name = name.substring(dot + 1);
            clips.put(name, parseClip(name, entry.getValue().getAsJsonObject()));
        }
        return clips;
    }

    private static GunAnimation parseClip(String name, JsonObject json) {
        boolean loop = json.has("loop") && json.get("loop").isJsonPrimitive() && json.get("loop").getAsBoolean();
        float length = json.has("animation_length") ? json.get("animation_length").getAsFloat() : 0f;
        Map<String, GunAnimation.Bone> bones = new HashMap<>();
        if (json.has("bones")) {
            for (Map.Entry<String, JsonElement> bone : json.getAsJsonObject("bones").entrySet()) {
                JsonObject b = bone.getValue().getAsJsonObject();
                Channel pos = b.has("position") ? parseChannel(b.get("position")) : null;
                Channel rot = b.has("rotation") ? parseChannel(b.get("rotation")) : null;
                Channel scale = b.has("scale") ? parseChannel(b.get("scale")) : null;
                bones.put(bone.getKey(), new GunAnimation.Bone(pos, rot, scale));
                length = Math.max(length, maxTime(b));
            }
        }
        List<GunAnimation.SoundCue> sounds = new ArrayList<>();
        if (json.has("sound_effects")) {
            for (Map.Entry<String, JsonElement> cue : json.getAsJsonObject("sound_effects").entrySet()) {
                float time = Float.parseFloat(cue.getKey());
                for (JsonElement effect : asArray(cue.getValue())) {
                    JsonObject o = effect.getAsJsonObject();
                    String sound = o.get("effect").getAsString();
                    int colon = sound.indexOf(':');
                    if (colon >= 0) sound = sound.substring(colon + 1);
                    float volume = o.has("volume") ? o.get("volume").getAsFloat() : 1f;
                    float pitch = o.has("pitch") ? o.get("pitch").getAsFloat() : 1f;
                    sounds.add(new GunAnimation.SoundCue(time, sound, volume, pitch));
                }
            }
            sounds.sort(Comparator.comparingDouble(GunAnimation.SoundCue::time));
        }
        return new GunAnimation(name, length, loop, Map.copyOf(bones), List.copyOf(sounds));
    }

    private static List<JsonElement> asArray(JsonElement e) {
        List<JsonElement> list = new ArrayList<>();
        if (e.isJsonArray()) e.getAsJsonArray().forEach(list::add);
        else list.add(e);
        return list;
    }

    private static float maxTime(JsonObject bone) {
        float max = 0f;
        for (String key : new String[]{"position", "rotation", "scale"}) {
            JsonElement e = bone.get(key);
            if (e != null && e.isJsonObject()) {
                for (String t : e.getAsJsonObject().keySet()) max = Math.max(max, Float.parseFloat(t));
            }
        }
        return max;
    }

    private static Channel parseChannel(JsonElement json) {
        if (json.isJsonArray()) return Channel.constant(vec(json.getAsJsonArray()));
        TreeMap<Float, JsonElement> sorted = new TreeMap<>();
        for (Map.Entry<String, JsonElement> key : json.getAsJsonObject().entrySet()) sorted.put(Float.parseFloat(key.getKey()), key.getValue());
        float[] times = new float[sorted.size()];
        Vector3f[] values = new Vector3f[sorted.size()];
        Channel.Lerp[] lerps = new Channel.Lerp[sorted.size()];
        int i = 0;
        for (Map.Entry<Float, JsonElement> key : sorted.entrySet()) {
            times[i] = key.getKey();
            JsonElement v = key.getValue();
            Channel.Lerp lerp = Channel.Lerp.LINEAR;
            if (v.isJsonObject()) {
                JsonObject o = v.getAsJsonObject();
                if (o.has("lerp_mode")) {
                    lerp = switch (o.get("lerp_mode").getAsString()) {
                        case "catmullrom" -> Channel.Lerp.CATMULLROM;
                        case "step" -> Channel.Lerp.STEP;
                        default -> Channel.Lerp.LINEAR;
                    };
                }
                v = o.has("post") ? o.get("post") : o.get("pre");
            }
            values[i] = vec(v.getAsJsonArray());
            lerps[i] = lerp;
            i++;
        }
        return new Channel(times, values, lerps);
    }

    private static Vector3f vec(JsonArray a) {
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }
}
