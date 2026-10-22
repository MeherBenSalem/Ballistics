package com.nightbeam.remnants.ballistics.block;

import com.mojang.serialization.MapCodec;
import com.nightbeam.remnants.ballistics.menu.AmmoPressMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Ammunition crafting station driven by {@code remnants_ballistics:ammo_press} recipes. */
public class AmmoPressBlock extends StationBlock {
    public static final MapCodec<AmmoPressBlock> CODEC = simpleCodec(AmmoPressBlock::new);
    private static final Component TITLE = Component.translatable("container.remnants_ballistics.ammo_press");
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 0, 0, 16, 6, 16),
            box(3, 6, 3, 13, 9, 13),
            box(6, 9, 6, 10, 16, 10));

    public AmmoPressBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new AmmoPressMenu(id, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
    }

    @Override
    protected VoxelShape shape() {
        return SHAPE;
    }
}
