package net.blue.chaoticd.content.block;

import java.util.function.Supplier;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** A fully functional chest that renders its Rosalita block texture instead of the vanilla chest atlas. */
public final class RosalitaChestBlock extends ChestBlock {
    public RosalitaChestBlock(BlockBehaviour.Properties properties,
                              Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
