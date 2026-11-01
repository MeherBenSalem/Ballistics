package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.entity.VoidOrbEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final RegistryHolder<EntityType<VoidOrbEntity>> VOID_ORB = Registrar.register(Registries.ENTITY_TYPE, "void_orb",
            () -> EntityType.Builder.<VoidOrbEntity>of(VoidOrbEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("void_orb"));

    private ModEntities() {}

    static void init() {}
}
