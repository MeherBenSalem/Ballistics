package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.block.AmmoPressBlock;
import com.nightbeam.remnants.ballistics.block.GunsmithWorkbenchBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final RegistryHolder<GunsmithWorkbenchBlock> GUNSMITH_WORKBENCH = Registrar.register(Registries.BLOCK, "gunsmith_workbench",
            () -> new GunsmithWorkbenchBlock(stationProperties()));

    public static final RegistryHolder<AmmoPressBlock> AMMO_PRESS = Registrar.register(Registries.BLOCK, "ammo_press",
            () -> new AmmoPressBlock(stationProperties()));

    private ModBlocks() {}

    static void init() {}

    private static BlockBehaviour.Properties stationProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }
}
