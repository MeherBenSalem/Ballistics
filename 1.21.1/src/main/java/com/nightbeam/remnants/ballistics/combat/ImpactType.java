package com.nightbeam.remnants.ballistics.combat;

import com.nightbeam.remnants.ballistics.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** Surface classes that select impact particles, decals and sounds. */
public enum ImpactType {
    STONE(true), METAL(true), WOOD(false), GLASS(false), DIRT(false), SOFT(false), WATER(false), FLESH(false), ENERGY(false);

    private final boolean canRicochet;

    ImpactType(boolean canRicochet) {
        this.canRicochet = canRicochet;
    }

    public boolean canRicochet() {
        return canRicochet;
    }

    public static ImpactType byId(int id) {
        ImpactType[] values = values();
        return values[Math.floorMod(id, values.length)];
    }

    /** Tags take precedence so packs can reclassify blocks; otherwise the block's sound type decides. */
    public static ImpactType of(BlockState state, LevelReader level, BlockPos pos) {
        if (state.is(ModTags.Blocks.IMPACT_METAL)) return METAL;
        if (state.is(ModTags.Blocks.IMPACT_GLASS)) return GLASS;
        if (state.is(ModTags.Blocks.IMPACT_WOOD)) return WOOD;
        if (state.is(ModTags.Blocks.IMPACT_SOFT)) return SOFT;
        SoundType sound = state.getSoundType(level, pos, null);
        if (sound == SoundType.METAL || sound == SoundType.COPPER || sound == SoundType.ANVIL || sound == SoundType.CHAIN
                || sound == SoundType.NETHERITE_BLOCK || sound == SoundType.LANTERN) return METAL;
        if (sound == SoundType.GLASS || sound == SoundType.AMETHYST || sound == SoundType.AMETHYST_CLUSTER) return GLASS;
        if (sound == SoundType.WOOD || sound == SoundType.BAMBOO_WOOD || sound == SoundType.CHERRY_WOOD || sound == SoundType.NETHER_WOOD
                || sound == SoundType.BAMBOO || sound == SoundType.SCAFFOLDING) return WOOD;
        if (sound == SoundType.WOOL || sound == SoundType.GRASS || sound == SoundType.SNOW || sound == SoundType.POWDER_SNOW
                || sound == SoundType.SLIME_BLOCK || sound == SoundType.HONEY_BLOCK || sound == SoundType.MOSS || sound == SoundType.MOSS_CARPET
                || sound == SoundType.AZALEA_LEAVES || sound == SoundType.CHERRY_LEAVES) return SOFT;
        if (sound == SoundType.GRAVEL || sound == SoundType.SAND || sound == SoundType.ROOTED_DIRT || sound == SoundType.MUD
                || sound == SoundType.SOUL_SAND || sound == SoundType.SOUL_SOIL || sound == SoundType.PACKED_MUD) return DIRT;
        return STONE;
    }
}
