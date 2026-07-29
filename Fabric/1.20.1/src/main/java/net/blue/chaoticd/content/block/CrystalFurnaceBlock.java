package net.blue.chaoticd.content.block;

import org.jetbrains.annotations.Nullable;

import net.blue.chaoticd.content.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalFurnaceBlock extends AbstractFurnaceBlock {

    public CrystalFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalFurnaceBlockEntity(pos, state);
    }

    @Override
    protected void openContainer(Level level, BlockPos pos, Player player) {

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof MenuProvider provider) {

            player.openMenu(provider);

            player.awardStat(Stats.INTERACT_WITH_FURNACE);

        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {

        return createFurnaceTicker(
                level,
                type,
                ModBlockEntities.CRYSTAL_FURNACE
        );

    }

}