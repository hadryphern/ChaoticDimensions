package net.blue.chaoticd.content.block;

import net.blue.chaoticd.worldgen.RosalitaTreeGrower;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Public Rosalita sapling wrapper around Minecraft's native sapling behaviour. */
public final class RosalitaSaplingBlock extends SaplingBlock {
    public RosalitaSaplingBlock(BlockBehaviour.Properties properties) {
        super(new RosalitaTreeGrower(), properties);
    }
}
