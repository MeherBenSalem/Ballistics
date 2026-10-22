package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.block.AmmoPressBlock;
import com.nightbeam.remnants.ballistics.block.GunsmithWorkbenchBlock;
import com.nightbeam.remnants.ballistics.platform.RegistrationProvider;
import com.nightbeam.remnants.ballistics.platform.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, RemnantsBallistics.MODID);

    public static final RegistryObject<Block, GunsmithWorkbenchBlock> GUNSMITH_WORKBENCH = BLOCKS.register("gunsmith_workbench",
            () -> new GunsmithWorkbenchBlock(stationProperties()));
    public static final RegistryObject<Block, AmmoPressBlock> AMMO_PRESS = BLOCKS.register("ammo_press",
            () -> new AmmoPressBlock(stationProperties()));

    private ModBlocks() {}

    static void init() {}

    private static BlockBehaviour.Properties stationProperties() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }
}
