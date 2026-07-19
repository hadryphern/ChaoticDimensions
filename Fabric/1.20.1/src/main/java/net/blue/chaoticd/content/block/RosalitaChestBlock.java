package net.blue.chaoticd.content.block;

import java.util.function.Supplier;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** A native chest block that delegates to the Rosalita chest block-entity type. */
public final class RosalitaChestBlock extends ChestBlock {
    public RosalitaChestBlock(BlockBehaviour.Properties properties,
                              Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
    }
}
