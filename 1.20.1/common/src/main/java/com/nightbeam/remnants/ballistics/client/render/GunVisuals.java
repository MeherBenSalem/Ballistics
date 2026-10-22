package com.nightbeam.remnants.ballistics.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Client-side presentation data for one weapon, loaded from {@code assets/<ns>/gun_visuals/<weapon>.json}.
 * Points are in model pixels (Blockbench coordinates, barrel toward -Z); view-space values are in blocks.
 */
public record GunVisuals(
        ResourceLocation animations,
        float scale,
        Vector3f pivot,
        Vector3f hip,
        Vector3f hipRotation,
        float adsDistance,
        Vector3f sight,
        Vector3f muzzle,
        Vector3f eject,
        Vector3f leftHand,
        Map<AttachmentSlot, Vector3f> mounts,
        Map<String, Vector3f> pivots,
        Vector3f sprintPosition,
        Vector3f sprintRotation,
        String shell,
        float flashSize) {

    public Vector3f mount(AttachmentSlot slot) {
        return mounts.getOrDefault(slot, muzzle);
    }

    public Vector3f pivot(String bone) {
        return pivots.getOrDefault(bone, pivot);
    }

    public static GunVisuals parse(JsonObject json) {
        Map<AttachmentSlot, Vector3f> mounts = new EnumMap<>(AttachmentSlot.class);
        if (json.has("mounts")) {
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("mounts").entrySet()) {
                for (AttachmentSlot slot : AttachmentSlot.values()) {
                    if (slot.getSerializedName().equals(e.getKey())) mounts.put(slot, vec(e.getValue()));
                }
            }
        }
        Map<String, Vector3f> pivots = new HashMap<>();
        if (json.has("pivots")) {
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("pivots").entrySet()) pivots.put(e.getKey(), vec(e.getValue()));
        }
        JsonObject sprint = json.has("sprint") ? json.getAsJsonObject("sprint") : new JsonObject();
        Vector3f pivot = vec(json.get("pivot"));
        Vector3f muzzle = vec(json.get("muzzle"));
        return new GunVisuals(
                new ResourceLocation(json.get("animations").getAsString()),
                json.has("scale") ? json.get("scale").getAsFloat() : 0.55f,
                pivot,
                vec(json.get("hip")),
                json.has("hip_rotation") ? vec(json.get("hip_rotation")) : new Vector3f(),
                json.has("ads_distance") ? json.get("ads_distance").getAsFloat() : 0.3f,
                vec(json.get("sight")),
                muzzle,
                json.has("eject") ? vec(json.get("eject")) : new Vector3f(pivot),
                json.has("left_hand") ? vec(json.get("left_hand")) : new Vector3f(pivot),
                Map.copyOf(mounts),
                Map.copyOf(pivots),
                sprint.has("position") ? vec(sprint.get("position")) : new Vector3f(0.02f, -0.08f, 0.05f),
                sprint.has("rotation") ? vec(sprint.get("rotation")) : new Vector3f(-15f, 35f, 10f),
                json.has("shell") ? json.get("shell").getAsString() : "casing",
                json.has("flash_size") ? json.get("flash_size").getAsFloat() : 1f);
    }

    private static Vector3f vec(JsonElement e) {
        JsonArray a = e.getAsJsonArray();
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    /** Neutral fallback used (and logged) when a weapon ships without a visuals file. */
    public static GunVisuals fallback(ResourceLocation animations) {
        Vector3f pivot = new Vector3f(8, 4, 10);
        return new GunVisuals(animations, 0.55f, pivot, new Vector3f(0.28f, -0.34f, -0.62f), new Vector3f(), 0.35f,
                new Vector3f(8, 9, 12), new Vector3f(8, 8, -4), new Vector3f(9, 8, 8), new Vector3f(8, 5, 2),
                Map.of(), Map.of(), new Vector3f(0.02f, -0.08f, 0.05f), new Vector3f(-15f, 35f, 10f), "casing", 1f);
    }
}
