package net.blue.chaoticd.content.block;

import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.blue.chaoticd.content.block.entity.RosalitaTrappedChestBlockEntity;

/** Native trapped chest behaviour backed by a Rosalita-owned block-entity type. */
public final class RosalitaTrappedChestBlock extends TrappedChestBlock {
    public RosalitaTrappedChestBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos position, BlockState state) {
        return new RosalitaTrappedChestBlockEntity(position, state);
    }

    @Override
    public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
        return ModBlockEntities.ROSALITA_TRAPPED_CHEST;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        return level.isClientSide
            ? createTickerHelper(type, ModBlockEntities.ROSALITA_TRAPPED_CHEST, ChestBlockEntity::lidAnimateTick)
            : null;
    }
}
