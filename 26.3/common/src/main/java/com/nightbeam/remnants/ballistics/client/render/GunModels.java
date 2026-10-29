package com.nightbeam.remnants.ballistics.client.render;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.client.platform.ClientServices;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone baked models for weapon parts and attachments, plus skin palette remapping.
 * <p>
 * Every weapon has six part models ({@code models/gun/<weapon>/<part>.json}); parts a weapon does not have are
 * shipped as empty models so registration is uniform and nothing is silently substituted. Each loader registers
 * {@link #standaloneModels()} with its extra-model hook and bakes them to quad lists.
 */
public final class GunModels {
    public static final String[] PARTS = {"body", "action", "magazine", "stock", "sights", "muzzle"};

    private record Cached(List<BakedQuad> source, List<BakedQuad> quads) {}

    private static final Map<String, Cached> SKINNED = new HashMap<>();

    private GunModels() {}

    public static Identifier part(WeaponDefinition def, String part) {
        return RemnantsBallistics.id("gun/" + def.name() + "/" + part);
    }

    public static Identifier attachment(String name) {
        return RemnantsBallistics.id("attachment/" + name);
    }

    /** Every standalone model the renderers draw. */
    public static List<Identifier> standaloneModels() {
        List<Identifier> ids = new ArrayList<>();
        for (WeaponDefinition def : Weapons.all()) {
            for (String part : PARTS) ids.add(part(def, part));
        }
        for (AttachmentDefinition att : Attachments.all()) {
            if (att.slot() != AttachmentSlot.SKIN) ids.add(attachment(att.name()));
        }
        return ids;
    }

    /** Whether the loader baked this standalone model. */
    public static boolean isLoaded(Identifier id) {
        return ClientServices.PLATFORM.getStandaloneQuads(id) != null;
    }

    /** All quads of a standalone model, or none if it is missing. A resource reload bakes new lists. */
    public static List<BakedQuad> quads(Identifier id) {
        List<BakedQuad> quads = ClientServices.PLATFORM.getStandaloneQuads(id);
        return quads == null ? List.of() : quads;
    }

    /**
     * Quads re-pointed from each weapon texture ({@code item/gun/<name>}) to its skin variant
     * ({@code item/gun/<name>_<skin>}, same UV layout). Quads using any other texture are kept as-is. Cached per
     * baked quad list, so a resource reload refreshes the cache by itself.
     */
    public static List<BakedQuad> skinned(Identifier id, String skin) {
        List<BakedQuad> source = quads(id);
        if (skin == null || skin.isEmpty() || source.isEmpty()) return source;
        String key = id + "|" + skin;
        synchronized (SKINNED) {
            Cached cached = SKINNED.get(key);
            if (cached != null && cached.source() == source) return cached.quads();
        }
        Map<TextureAtlasSprite, TextureAtlasSprite> swaps = new HashMap<>();
        List<BakedQuad> out = new ArrayList<>(source.size());
        for (BakedQuad quad : source) {
            TextureAtlasSprite base = quad.materialInfo().sprite();
            Identifier name = base.contents().name();
            if (!name.getNamespace().equals(RemnantsBallistics.MODID) || !name.getPath().startsWith("item/gun/")) {
                out.add(quad);
                continue;
            }
            TextureAtlasSprite skinned = swaps.computeIfAbsent(base, b -> sprite(b.atlasLocation(), name.withSuffix("_" + skin)));
            // A missing skin texture resolves to the atlas' missing sprite; keep the original rather than show it.
            if (skinned.contents().name().equals(MissingTextureAtlasSprite.getLocation())) out.add(quad);
            else out.add(remap(quad, base, skinned));
        }
        List<BakedQuad> result = List.copyOf(out);
        synchronized (SKINNED) {
            SKINNED.put(key, new Cached(source, result));
        }
        return result;
    }

    private static TextureAtlasSprite sprite(Identifier atlas, Identifier texture) {
        return Minecraft.getInstance().getAtlasManager().get(new SpriteId(atlas, texture));
    }

    private static BakedQuad remap(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
        long[] uv = new long[4];
        float fromW = from.getU1() - from.getU0();
        float fromH = from.getV1() - from.getV0();
        for (int v = 0; v < 4; v++) {
            long packed = quad.packedUV(v);
            float fu = fromW == 0f ? 0f : (UVPair.unpackU(packed) - from.getU0()) / fromW;
            float fv = fromH == 0f ? 0f : (UVPair.unpackV(packed) - from.getV0()) / fromH;
            uv[v] = UVPair.pack(to.getU0() + fu * (to.getU1() - to.getU0()), to.getV0() + fv * (to.getV1() - to.getV0()));
        }
        BakedQuad.MaterialInfo m = quad.materialInfo();
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), uv[0], uv[1], uv[2], uv[3],
                quad.direction(), new BakedQuad.MaterialInfo(to, m.layer(), m.itemRenderType(), m.itemGlintRenderType(),
                m.itemGlintSpecialRenderType(), m.tintIndex(), m.shadeDirectionOverride(), m.lightEmission()));
    }
}
