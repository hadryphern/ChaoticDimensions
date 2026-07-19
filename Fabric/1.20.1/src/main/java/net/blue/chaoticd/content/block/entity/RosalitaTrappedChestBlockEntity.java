package net.blue.chaoticd.content.block.entity;

import net.blue.chaoticd.content.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Rosalita trapped chest inventory, including vanilla-compatible redstone updates. */
public final class RosalitaTrappedChestBlockEntity extends ChestBlockEntity {
    public RosalitaTrappedChestBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ROSALITA_TRAPPED_CHEST, position, state);
    }

    @Override
    protected void signalOpenCount(Level level, BlockPos position, BlockState state, int oldCount, int newCount) {
        super.signalOpenCount(level, position, state, oldCount, newCount);
        if (oldCount != newCount) {
            level.updateNeighborsAt(position, state.getBlock());
            level.updateNeighborsAt(position.below(), state.getBlock());
        }
    }
}
