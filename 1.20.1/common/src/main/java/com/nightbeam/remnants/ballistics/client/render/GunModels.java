package com.nightbeam.remnants.ballistics.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Weapon part, attachment and icon geometry, loaded from the mod's Java model JSON ({@code models/gun/<weapon>/<part>},
 * {@code models/attachment/<name>}) and drawn directly with entity render types.
 * <p>
 * The models are plain cuboids with single-axis rotations, so instead of going through each loader's (and each Minecraft
 * version's) standalone baked-model API this reproduces what vanilla's {@code FaceBakery} does: the same face vertex
 * order, UV mapping and element rotation, with normals snapped to the nearest axis as baked quads have. Skins swap the
 * weapon texture ({@code item/gun/<weapon>} → {@code item/gun/<weapon>_<skin>}) since they share its UV layout.
 * <p>
 * Every weapon has six part models; parts a weapon does not have are shipped as empty models.
 */
public final class GunModels extends SimplePreparableReloadListener<GunModels.Loaded> {
    public static final GunModels INSTANCE = new GunModels();
    public static final String[] PARTS = {"body", "action", "magazine", "stock", "sights", "muzzle"};
    private static final String[] MODEL_DIRS = {"models/gun", "models/attachment"};
    private static final String GUN_TEXTURES = "textures/item/gun";

    /** One textured layer of a model: 4 vertices per quad, each {x, y, z (pixels), u, v (0..1), nx, ny, nz}. */
    public record Layer(ResourceLocation texture, float[] vertices) {
        public int quads() {
            return vertices.length / (4 * 8);
        }
    }

    public record Model(List<Layer> layers) {
        public static final Model EMPTY = new Model(List.of());

        public int quads() {
            return layers.stream().mapToInt(Layer::quads).sum();
        }
    }

    record Loaded(Map<ResourceLocation, Model> models, Set<ResourceLocation> textures) {}

    private volatile Loaded loaded = new Loaded(Map.of(), Set.of());
    private final Map<String, ResourceLocation> skinCache = new java.util.concurrent.ConcurrentHashMap<>();

    private GunModels() {}

    public static ResourceLocation part(WeaponDefinition def, String part) {
        return RemnantsBallistics.id("gun/" + def.name() + "/" + part);
    }

    public static ResourceLocation attachment(String name) {
        return RemnantsBallistics.id("attachment/" + name);
    }

    /** The inventory icon texture of a weapon. */
    public static ResourceLocation icon(WeaponDefinition def) {
        return RemnantsBallistics.id("textures/item/icon/" + def.name() + ".png");
    }

    public boolean has(ResourceLocation model) {
        return loaded.models().containsKey(model);
    }

    public Model model(ResourceLocation id) {
        return loaded.models().getOrDefault(id, Model.EMPTY);
    }

    /** Whether a texture file exists (only weapon textures are indexed; used for skins). */
    public boolean hasTexture(ResourceLocation texture) {
        return loaded.textures().contains(texture);
    }

    /** Draws a model in model space (the pose maps 16 model pixels to one unit). */
    public void draw(MultiBufferSource buffers, PoseStack ps, ResourceLocation model, String skin, int light, int overlay) {
        PoseStack.Pose pose = ps.last();
        for (Layer layer : model(model).layers()) {
            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutout(skinned(layer.texture(), skin)));
            float[] v = layer.vertices();
            for (int i = 0; i < v.length; i += 8) {
                vc.vertex(pose.pose(), v[i] / 16f, v[i + 1] / 16f, v[i + 2] / 16f).color(255, 255, 255, 255).uv(v[i + 3], v[i + 4])
                        .overlayCoords(overlay).uv2(light).normal(pose.normal(), v[i + 5], v[i + 6], v[i + 7]).endVertex();
            }
        }
    }

    /** The flat inventory icon, as {@code item/generated} would draw its front face (z = 8.5 px, facing south). */
    public void drawIcon(MultiBufferSource buffers, PoseStack ps, WeaponDefinition def, int light, int overlay) {
        PoseStack.Pose pose = ps.last();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutout(icon(def)));
        float z = 8.5f / 16f;
        float[][] corners = {{0f, 1f, 0f, 0f}, {0f, 0f, 0f, 1f}, {1f, 0f, 1f, 1f}, {1f, 1f, 1f, 0f}};
        for (float[] c : corners) {
            vc.vertex(pose.pose(), c[0], c[1], z).color(255, 255, 255, 255).uv(c[2], c[3]).overlayCoords(overlay).uv2(light)
                    .normal(pose.normal(), 0f, 0f, 1f).endVertex();
        }
    }

    private ResourceLocation skinned(ResourceLocation texture, String skin) {
        if (skin == null || skin.isEmpty() || !texture.getPath().startsWith(GUN_TEXTURES + "/")) return texture;
        return skinCache.computeIfAbsent(texture + "|" + skin, k -> {
            ResourceLocation candidate = texture.withPath(p -> p.substring(0, p.length() - 4) + "_" + skin + ".png");
            // A missing skin texture would render as the purple checkerboard; keep the base texture instead.
            return hasTexture(candidate) ? candidate : texture;
        });
    }

    // ------------------------------------------------------------------------------------------------------------
    // Loading
    // ------------------------------------------------------------------------------------------------------------

    @Override
    protected Loaded prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, Model> models = new HashMap<>();
        for (String dir : MODEL_DIRS) {
            for (Map.Entry<ResourceLocation, Resource> e : manager.listResources(dir, p -> p.getPath().endsWith(".json")).entrySet()) {
                ResourceLocation file = e.getKey();
                if (!file.getNamespace().equals(RemnantsBallistics.MODID)) continue;
                ResourceLocation id = file.withPath(p -> p.substring("models/".length(), p.length() - ".json".length()));
                try (Reader reader = e.getValue().openAsReader()) {
                    models.put(id, parse(JsonParser.parseReader(reader).getAsJsonObject()));
                } catch (Exception ex) {
                    RemnantsBallistics.LOGGER.error("Could not load weapon model {}", file, ex);
                }
            }
        }
        Set<ResourceLocation> textures = new HashSet<>(manager.listResources(GUN_TEXTURES, p -> p.getPath().endsWith(".png")).keySet());
        return new Loaded(Map.copyOf(models), Set.copyOf(textures));
    }

    @Override
    protected void apply(Loaded result, ResourceManager manager, ProfilerFiller profiler) {
        loaded = result;
        skinCache.clear();
        RemnantsBallistics.LOGGER.info("Loaded {} weapon and attachment models", result.models().size());
    }

    static Model parse(JsonObject root) {
        Map<String, String> textures = new HashMap<>();
        if (root.has("textures")) {
            for (Map.Entry<String, JsonElement> t : root.getAsJsonObject("textures").entrySet()) textures.put(t.getKey(), t.getValue().getAsString());
        }
        Map<ResourceLocation, List<Float>> byTexture = new HashMap<>();
        if (root.has("elements")) {
            for (JsonElement el : root.getAsJsonArray("elements")) bakeElement(el.getAsJsonObject(), textures, byTexture);
        }
        List<Layer> layers = new ArrayList<>();
        for (Map.Entry<ResourceLocation, List<Float>> e : byTexture.entrySet()) {
            float[] v = new float[e.getValue().size()];
            for (int i = 0; i < v.length; i++) v[i] = e.getValue().get(i);
            layers.add(new Layer(e.getKey(), v));
        }
        return new Model(List.copyOf(layers));
    }

    private static void bakeElement(JsonObject el, Map<String, String> textures, Map<ResourceLocation, List<Float>> out) {
        Vector3f from = vec(el.getAsJsonArray("from"));
        Vector3f to = vec(el.getAsJsonArray("to"));
        Matrix4f rotation = null;
        Vector3f origin = null;
        if (el.has("rotation")) {
            JsonObject r = el.getAsJsonObject("rotation");
            float angle = r.get("angle").getAsFloat();
            if (angle != 0f) {
                Vector3f axis = switch (r.get("axis").getAsString()) {
                    case "x" -> new Vector3f(1, 0, 0);
                    case "y" -> new Vector3f(0, 1, 0);
                    default -> new Vector3f(0, 0, 1);
                };
                rotation = new Matrix4f().rotation(new Quaternionf().rotationAxis((float) Math.toRadians(angle), axis));
                origin = vec(r.getAsJsonArray("origin"));
            }
        }
        for (Map.Entry<String, JsonElement> f : el.getAsJsonObject("faces").entrySet()) {
            Direction dir = Direction.byName(f.getKey());
            if (dir == null) continue;
            JsonObject face = f.getValue().getAsJsonObject();
            ResourceLocation texture = resolveTexture(face.get("texture").getAsString(), textures);
            if (texture == null) continue;
            float[] uv = face.has("uv") ? floats(face.getAsJsonArray("uv")) : defaultUv(dir, from, to);
            Vector3f[] corners = corners(dir, from, to);
            if (rotation != null) {
                for (Vector3f c : corners) {
                    Vector4f v = rotation.transform(new Vector4f(c.x - origin.x, c.y - origin.y, c.z - origin.z, 1f));
                    c.set(v.x + origin.x, v.y + origin.y, v.z + origin.z);
                }
            }
            // Baked quads light every vertex with the nearest axis direction of the (rotated) face.
            Vector3f normal = new Vector3f(corners[2]).sub(corners[1]).cross(new Vector3f(corners[0]).sub(corners[1])).normalize();
            Direction facing = Float.isFinite(normal.x) ? Direction.getNearest(normal.x, normal.y, normal.z) : dir;
            List<Float> verts = out.computeIfAbsent(texture, k -> new ArrayList<>());
            for (int i = 0; i < 4; i++) {
                // BlockFaceUV with rotation 0: vertex 0 → (u0, v0), 1 → (u0, v1), 2 → (u1, v1), 3 → (u1, v0).
                float u = (i == 0 || i == 1) ? uv[0] : uv[2];
                float v = (i == 0 || i == 3) ? uv[1] : uv[3];
                verts.add(corners[i].x);
                verts.add(corners[i].y);
                verts.add(corners[i].z);
                verts.add(u / 16f);
                verts.add(v / 16f);
                verts.add((float) facing.getStepX());
                verts.add((float) facing.getStepY());
                verts.add((float) facing.getStepZ());
            }
        }
    }

    /** Face corners in vanilla's FaceInfo order. */
    private static Vector3f[] corners(Direction dir, Vector3f a, Vector3f b) {
        return switch (dir) {
            case DOWN -> new Vector3f[] {v(a.x, a.y, b.z), v(a.x, a.y, a.z), v(b.x, a.y, a.z), v(b.x, a.y, b.z)};
            case UP -> new Vector3f[] {v(a.x, b.y, a.z), v(a.x, b.y, b.z), v(b.x, b.y, b.z), v(b.x, b.y, a.z)};
            case NORTH -> new Vector3f[] {v(b.x, b.y, a.z), v(b.x, a.y, a.z), v(a.x, a.y, a.z), v(a.x, b.y, a.z)};
            case SOUTH -> new Vector3f[] {v(a.x, b.y, b.z), v(a.x, a.y, b.z), v(b.x, a.y, b.z), v(b.x, b.y, b.z)};
            case WEST -> new Vector3f[] {v(a.x, b.y, a.z), v(a.x, a.y, a.z), v(a.x, a.y, b.z), v(a.x, b.y, b.z)};
            case EAST -> new Vector3f[] {v(b.x, b.y, b.z), v(b.x, a.y, b.z), v(b.x, a.y, a.z), v(b.x, b.y, a.z)};
        };
    }

    /** Vanilla's default UVs when a face omits them (the element's projection onto the face). */
    private static float[] defaultUv(Direction dir, Vector3f a, Vector3f b) {
        return switch (dir) {
            case DOWN -> new float[] {a.x, 16f - b.z, b.x, 16f - a.z};
            case UP -> new float[] {a.x, a.z, b.x, b.z};
            case NORTH -> new float[] {16f - b.x, 16f - b.y, 16f - a.x, 16f - a.y};
            case SOUTH -> new float[] {a.x, 16f - b.y, b.x, 16f - a.y};
            case WEST -> new float[] {a.z, 16f - b.y, b.z, 16f - a.y};
            case EAST -> new float[] {16f - b.z, 16f - b.y, 16f - a.z, 16f - a.y};
        };
    }

    private static ResourceLocation resolveTexture(String ref, Map<String, String> textures) {
        String value = ref;
        for (int depth = 0; value.startsWith("#") && depth < 8; depth++) value = textures.get(value.substring(1));
        if (value == null || value.startsWith("#")) return null;
        ResourceLocation id = new ResourceLocation(value);
        return id.withPath(p -> "textures/" + p + ".png");
    }

    private static Vector3f v(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private static Vector3f vec(JsonArray a) {
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private static float[] floats(JsonArray a) {
        float[] f = new float[a.size()];
        for (int i = 0; i < f.length; i++) f[i] = a.get(i).getAsFloat();
        return f;
    }
}
