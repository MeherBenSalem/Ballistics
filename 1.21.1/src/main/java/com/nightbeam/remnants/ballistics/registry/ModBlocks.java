package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.block.AmmoPressBlock;
import com.nightbeam.remnants.ballistics.block.GunsmithWorkbenchBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RemnantsBallistics.MODID);

    public static final DeferredBlock<GunsmithWorkbenchBlock> GUNSMITH_WORKBENCH = BLOCKS.registerBlock("gunsmith_workbench",
            GunsmithWorkbenchBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<AmmoPressBlock> AMMO_PRESS = BLOCKS.registerBlock("ammo_press",
            AmmoPressBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f, 6f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

    private ModBlocks() {}
}
