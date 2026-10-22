package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {}

    public static final class Blocks {
        /** Shattered by bullets when {@code breakGlass} is enabled. */
        public static final TagKey<Block> BULLET_BREAKABLE = tag("bullet_breakable");
        /** Bullets with enough penetration power pass through; resistance scales with block hardness. */
        public static final TagKey<Block> BULLET_PENETRABLE = tag("bullet_penetrable");
        public static final TagKey<Block> IMPACT_METAL = tag("impact/metal");
        public static final TagKey<Block> IMPACT_WOOD = tag("impact/wood");
        public static final TagKey<Block> IMPACT_GLASS = tag("impact/glass");
        public static final TagKey<Block> IMPACT_SOFT = tag("impact/soft");

        private static TagKey<Block> tag(String path) {
            return TagKey.create(Registries.BLOCK, RemnantsBallistics.id(path));
        }

        private Blocks() {}
    }

    public static final class Entities {
        /** Upright mobs with head/torso/leg hit zones. */
        public static final TagKey<EntityType<?>> HUMANOID_HIT_ZONES = TagKey.create(Registries.ENTITY_TYPE, RemnantsBallistics.id("humanoid_hit_zones"));

        private Entities() {}
    }
}
