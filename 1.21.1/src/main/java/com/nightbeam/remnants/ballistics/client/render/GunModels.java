package com.nightbeam.remnants.ballistics.client.render;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.IQuadTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone baked models for weapon parts, attachments and inventory icons, plus skin palette remapping.
 * <p>
 * Every weapon has six part models ({@code models/gun/<weapon>/<part>.json}); parts a weapon does not have are
 * shipped as empty models so registration is uniform and nothing is silently substituted.
 */
public final class GunModels {
    public static final String[] PARTS = {"body", "action", "magazine", "stock", "sights", "muzzle"};

    private static final Map<ModelResourceLocation, List<BakedQuad>> QUADS = new HashMap<>();
    private static final Map<String, List<BakedQuad>> SKINNED = new HashMap<>();

    private GunModels() {}

    public static ModelResourceLocation part(WeaponDefinition def, String part) {
        return ModelResourceLocation.standalone(RemnantsBallistics.id("gun/" + def.name() + "/" + part));
    }

    public static ModelResourceLocation icon(WeaponDefinition def) {
        return ModelResourceLocation.standalone(RemnantsBallistics.id("item/icon/" + def.name()));
    }

    public static ModelResourceLocation attachment(String name) {
        return ModelResourceLocation.standalone(RemnantsBallistics.id("attachment/" + name));
    }

    public static void register(ModelEvent.RegisterAdditional event) {
        for (WeaponDefinition def : Weapons.all()) {
            for (String part : PARTS) event.register(part(def, part));
            event.register(icon(def));
        }
        for (AttachmentDefinition att : Attachments.all()) {
            if (att.slot() != AttachmentSlot.SKIN) event.register(attachment(att.name()));
        }
    }

    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        synchronized (QUADS) {
            QUADS.clear();
            SKINNED.clear();
        }
    }

    public static BakedModel model(ModelResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getModel(id);
    }

    /** All quads of a standalone model (general and face-culled lists), cached until the next model reload. */
    public static List<BakedQuad> quads(ModelResourceLocation id) {
        synchronized (QUADS) {
            List<BakedQuad> cached = QUADS.get(id);
            if (cached != null) return cached;
            BakedModel model = model(id);
            RandomSource random = RandomSource.create(42L);
            List<BakedQuad> all = new ArrayList<>(model.getQuads(null, null, random));
            for (Direction dir : Direction.values()) all.addAll(model.getQuads(null, dir, random));
            List<BakedQuad> result = List.copyOf(all);
            QUADS.put(id, result);
            return result;
        }
    }

    /**
     * Quads re-pointed from each weapon texture ({@code item/gun/<name>}) to its skin variant
     * ({@code item/gun/<name>_<skin>}, same UV layout). Quads using any other texture are kept as-is.
     */
    public static List<BakedQuad> skinned(ModelResourceLocation id, String skin) {
        if (skin == null || skin.isEmpty()) return quads(id);
        String key = id + "|" + skin;
        synchronized (QUADS) {
            List<BakedQuad> cached = SKINNED.get(key);
            if (cached != null) return cached;
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
            SKINNED.put(key, result);
        }
        return result;
    }

    private static BakedQuad remap(BakedQuad quad, TextureAtlasSprite from, TextureAtlasSprite to) {
        int[] data = quad.getVertices().clone();
        float fromW = from.getU1() - from.getU0();
        float fromH = from.getV1() - from.getV0();
        for (int v = 0; v < 4; v++) {
            int offset = v * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = Float.intBitsToFloat(data[offset]);
            float w = Float.intBitsToFloat(data[offset + 1]);
            float fu = fromW == 0f ? 0f : (u - from.getU0()) / fromW;
            float fv = fromH == 0f ? 0f : (w - from.getV0()) / fromH;
            data[offset] = Float.floatToRawIntBits(to.getU0() + fu * (to.getU1() - to.getU0()));
            data[offset + 1] = Float.floatToRawIntBits(to.getV0() + fv * (to.getV1() - to.getV0()));
        }
        return new BakedQuad(data, quad.getTintIndex(), quad.getDirection(), to, quad.isShade(), quad.hasAmbientOcclusion());
    }
}
