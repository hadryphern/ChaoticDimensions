package net.blue.chaoticd.content.block;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Trapped-chest redstone behavior with a Rosalita-specific baked texture. */
public final class RosalitaTrappedChestBlock extends TrappedChestBlock {
    public RosalitaTrappedChestBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
