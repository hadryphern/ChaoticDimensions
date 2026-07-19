package net.blue.chaoticd.content.block.entity;

import net.blue.chaoticd.content.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Native chest inventory with a Rosalita-specific block-entity type. */
public final class RosalitaChestBlockEntity extends ChestBlockEntity {
    public RosalitaChestBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ROSALITA_CHEST, position, state);
    }
}
