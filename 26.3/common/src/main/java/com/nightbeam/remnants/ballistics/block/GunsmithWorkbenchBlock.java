package com.nightbeam.remnants.ballistics.block;

import com.nightbeam.remnants.ballistics.menu.GunsmithMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Fits and removes attachments, previews stats and repairs weapons. */
public class GunsmithWorkbenchBlock extends StationBlock {
    private static final Component TITLE = Component.translatable("container.remnants_ballistics.gunsmith");
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 12, 0, 16, 16, 16),
            box(1, 0, 1, 4, 12, 4), box(12, 0, 1, 15, 12, 4),
            box(1, 0, 12, 4, 12, 15), box(12, 0, 12, 15, 12, 15),
            box(2, 4, 2, 14, 6, 14));

    public GunsmithWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) -> new GunsmithMenu(id, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
    }

    @Override
    protected VoxelShape shape() {
        return SHAPE;
    }
}
