package net.blue.chaoticd.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

/** Grass that spreads only across its paired soil and immediately becomes soil when covered. */
public final class AuroraGrassBlock extends GrassBlock {
    private final Block soil;

    public AuroraGrassBlock(BlockBehaviour.Properties properties, Block soil) {
        super(properties);
        this.soil = soil;
    }

    private static boolean canRemainGrass(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);

        if (above.is(Blocks.SNOW) && above.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        }

        if (above.getFluidState().getAmount() == 8) {
            return false;
        }

        int blockedLight = LightEngine.getLightBlockInto(
            level,
            state,
            pos,
            above,
            abovePos,
            Direction.UP,
            above.getLightBlock(level, abovePos)
        );

        return blockedLight < level.getMaxLightLevel();
    }

    private static boolean canSpreadTo(BlockState state, LevelReader level, BlockPos pos) {
        return canRemainGrass(state, level, pos)
            && !level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    /**
     * Unlike randomTick, updateShape runs as soon as the block above changes.
     * This removes the long delay when a solid block is placed over Aurora grass.
     */
    @Override
    public BlockState updateShape(
        BlockState state,
        Direction direction,
        BlockState neighbourState,
        LevelAccessor level,
        BlockPos pos,
        BlockPos neighbourPos
    ) {
        if (direction == Direction.UP && !canRemainGrass(state, level, pos)) {
            return soil.defaultBlockState();
        }

        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canRemainGrass(state, level, pos)) {
            level.setBlockAndUpdate(pos, soil.defaultBlockState());
            return;
        }

        if (level.getMaxLocalRawBrightness(pos.above()) < 9) {
            return;
        }

        BlockState spreadingState = defaultBlockState();

        for (int attempt = 0; attempt < 4; attempt++) {
            BlockPos targetPos = pos.offset(
                random.nextInt(3) - 1,
                random.nextInt(5) - 3,
                random.nextInt(3) - 1
            );

            if (level.getBlockState(targetPos).is(soil)
                && canSpreadTo(spreadingState, level, targetPos)) {
                boolean snowy = level.getBlockState(targetPos.above()).is(Blocks.SNOW);
                level.setBlockAndUpdate(targetPos, spreadingState.setValue(SNOWY, snowy));
            }
        }
    }
}
