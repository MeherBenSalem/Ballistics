package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.client.platform.ClientServices;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone baked models for weapon parts, attachments and inventory icons, plus skin palette remapping.
 * <p>
 * Every weapon has six part models ({@code models/gun/<weapon>/<part>.json}); parts a weapon does not have are
 * shipped as empty models so registration is uniform and nothing is silently substituted. Each loader registers
 * {@link #standaloneModels()} with its extra-model hook.
 */
public final class GunModels {
    public static final String[] PARTS = {"body", "action", "magazine", "stock", "sights", "muzzle"};

    /** Vertex layout of block/item quads ({@link DefaultVertexFormat#BLOCK}), in ints. */
    private static final int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    private static final int UV0 = DefaultVertexFormat.BLOCK.getOffset(VertexFormatElement.UV0) / 4;

    private record Cached(BakedModel source, List<BakedQuad> quads) {}

    private static final Map<ResourceLocation, Cached> QUADS = new HashMap<>();
    private static final Map<String, Cached> SKINNED = new HashMap<>();

    private GunModels() {}

    public static ResourceLocation part(WeaponDefinition def, String part) {
        return RemnantsBallistics.id("gun/" + def.name() + "/" + part);
    }

    public static ResourceLocation icon(WeaponDefinition def) {
        return RemnantsBallistics.id("item/icon/" + def.name());
    }

    public static ResourceLocation attachment(String name) {
        return RemnantsBallistics.id("attachment/" + name);
    }

    /** Every standalone model the renderers draw. */
    public static List<ResourceLocation> standaloneModels() {
        List<ResourceLocation> ids = new ArrayList<>();
        for (WeaponDefinition def : Weapons.all()) {
            for (String part : PARTS) ids.add(part(def, part));
            ids.add(icon(def));
        }
        for (AttachmentDefinition att : Attachments.all()) {
            if (att.slot() != AttachmentSlot.SKIN) ids.add(attachment(att.name()));
        }
        return ids;
    }

    public static BakedModel model(ResourceLocation id) {
        return ClientServices.PLATFORM.getStandaloneModel(id);
    }

    /**
     * All quads of a standalone model (general and face-culled lists). Cached per baked model, so a resource reload
     * (which bakes new models) refreshes the cache by itself.
     */
    public static List<BakedQuad> quads(ResourceLocation id) {
        BakedModel model = model(id);
        synchronized (QUADS) {
            Cached cached = QUADS.get(id);
            if (cached != null && cached.source() == model) return cached.quads();
        }
        RandomSource random = RandomSource.create(42L);
        List<BakedQuad> all = new ArrayList<>(model.getQuads(null, null, random));
        for (Direction dir : Direction.values()) all.addAll(model.getQuads(null, dir, random));
        List<BakedQuad> result = List.copyOf(all);
        synchronized (QUADS) {
            QUADS.put(id, new Cached(model, result));
        }
        return result;
    }

    /**
     * Quads re-pointed from each weapon texture ({@code item/gun/<name>}) to its skin variant
     * ({@code item/gun/<name>_<skin>}, same UV layout). Quads using any other texture are kept as-is.
     */
    public static List<BakedQuad> skinned(ResourceLocation id, String skin) {
        if (skin == null || skin.isEmpty()) return quads(id);
        BakedModel model = model(id);
        String key = id + "|" + skin;
        synchronized (QUADS) {
            Cached cached = SKINNED.get(key);
            if (cached != null && cached.source() == model) return cached.quads();
        }
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        Map<TextureAtlasSprite, TextureAtlasSprite> swaps = new HashMap<>();
        List<BakedQuad> out = new ArrayList<>();
        for (BakedQuad quad : quads(id)) {
            TextureAtlasSprite base = quad.getSprite();
            ResourceLocation name = base.contents().name();
            if (!name.getNamespace().equals(RemnantsBallistics.MODID) || !name.getPath().startsWith("item/gun/")) {
                out.add(quad);
                continue;
            }
            TextureAtlasSprite skinned = swaps.computeIfAbsent(base, b -> atlas.apply(name.withSuffix("_" + skin)));
            // A missing skin texture resolves to the atlas' missing sprite; keep the original rather than show it.
            if (skinned.contents().name().equals(MissingTextureAtlasSprite.getLocation())) out.add(quad);
            else out.add(remap(quad, base, skinned));
        }
        List<BakedQuad> result = List.copyOf(out);
        synchronized (QUADS) {
            SKINNED.put(key, new Cached(model, result));
        }
        return result;
    }

    private static BakedQuad remap(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
        int[] data = quad.getVertices().clone();
        float fromW = from.getU1() - from.getU0();
        float fromH = from.getV1() - from.getV0();
        for (int v = 0; v < 4; v++) {
            int offset = v * STRIDE + UV0;
            float u = Float.intBitsToFloat(data[offset]);
            float w = Float.intBitsToFloat(data[offset + 1]);
            float fu = fromW == 0f ? 0f : (u - from.getU0()) / fromW;
            float fv = fromH == 0f ? 0f : (w - from.getV0()) / fromH;
            data[offset] = Float.floatToRawIntBits(to.getU0() + fu * (to.getU1() - to.getU0()));
            data[offset + 1] = Float.floatToRawIntBits(to.getV0() + fv * (to.getV1() - to.getV0()));
        }
        return new BakedQuad(data, quad.getTintIndex(), quad.getDirection(), to, quad.isShade());
    }
}
