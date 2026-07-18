package net.blue.chaoticd.content.block;

import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps vanilla barrel inventory behavior while using the Rosalita texture model. */
public final class RosalitaBarrelBlock extends BarrelBlock {
    public RosalitaBarrelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
