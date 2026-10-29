package com.nightbeam.remnants.ballistics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.anim.PoseState;
import com.nightbeam.remnants.ballistics.client.anim.WeaponAnimator;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Item model type {@code remnants_ballistics:gun}: the animated 3D weapon outside first person (third person, on the
 * ground, in item frames). The {@code base} model supplies the display transforms; the parts are drawn in item model
 * space (0..1 = 16 pixels). GUIs show the flat icon instead (a {@code display_context} select in the item definition).
 * Each loader registers {@link Unbaked#MAP_CODEC} under {@link #TYPE}.
 */
public final class GunItemModel implements ItemModel {
    public static final Identifier TYPE = RemnantsBallistics.id("gun");
    private static final Vector3fc[] EXTENTS = {new Vector3f(0f, 0f, 0f), new Vector3f(1f, 1f, 1f)};
    private static final Supplier<Vector3fc[]> EXTENTS_SUPPLIER = () -> EXTENTS;

    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    private GunItemModel(ModelRenderProperties properties, Matrix4fc transformation) {
        this.properties = properties;
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext context,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        if (!(stack.getItem() instanceof GunItem gun)) return;
        // A held weapon shows its holder's animation, captured now: rendering happens after extraction.
        PoseState pose = null;
        LivingEntity holder = owner == null ? null : owner.asLivingEntity();
        if (holder != null && (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) && holder.getMainHandItem() == stack) {
            pose = WeaponAnimator.thirdPerson(holder.getId(), holder == Minecraft.getInstance().player).copy();
            output.setAnimated();
        }
        Args args = new Args(gun.definition(), GunData.attachments(stack), pose);
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(args);
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setExtents(EXTENTS_SUPPLIER);
        layer.setLocalTransform(transformation);
        layer.setupSpecialModel(Renderer.INSTANCE, args);
        properties.applyToLayer(layer, context);
    }

    /** What one weapon render needs, extracted from the stack and its holder. */
    public record Args(WeaponDefinition definition, GunAttachments attachments, @Nullable PoseState pose) {}

    /** Draws the parts and fitted attachments of {@link Args}. */
    public static final class Renderer implements SpecialModelRenderer<Args> {
        static final Renderer INSTANCE = new Renderer();

        @Override
        public void submit(@Nullable Args args, PoseStack ps, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
            if (args == null) return;
            WeaponDefinition def = args.definition();
            GunRenderer.render(def, GunVisualsLoader.INSTANCE.get(def), args.attachments(), args.pose(), ps, collector, light, overlay);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (Vector3fc corner : EXTENTS) output.accept(corner);
        }

        @Override
        public @Nullable Args extractArgument(ItemStack stack) {
            return null;
        }
    }

    public record Unbaked(Identifier base, Optional<Transformation> transformation) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation)
        ).apply(inst, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(base);
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, model.getTopTextureSlots());
            return new GunItemModel(properties, Transformation.compose(transformation, this.transformation));
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
