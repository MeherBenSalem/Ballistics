package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.block.AmmoPressBlock;
import com.nightbeam.remnants.ballistics.block.GunsmithWorkbenchBlock;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final RegistryHolder<GunsmithWorkbenchBlock> GUNSMITH_WORKBENCH = Registrar.register(Registries.BLOCK, "gunsmith_workbench",
            () -> new GunsmithWorkbenchBlock(stationProperties("gunsmith_workbench")));

    public static final RegistryHolder<AmmoPressBlock> AMMO_PRESS = Registrar.register(Registries.BLOCK, "ammo_press",
            () -> new AmmoPressBlock(stationProperties("ammo_press")));

    private ModBlocks() {}

    static void init() {}

    private static BlockBehaviour.Properties stationProperties(String name) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, RemnantsBallistics.id(name))).mapColor(MapColor.METAL).strength(3.5f, 6f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }
}
