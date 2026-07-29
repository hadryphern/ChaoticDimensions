package net.blue.chaoticd.content.block;

import net.blue.chaoticd.content.ModBlockEntities;
import net.blue.chaoticd.content.menu.CrystalFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Block entity that runs the vanilla smelting recipe type and Furnace menu. */
public final class CrystalFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public CrystalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_FURNACE, pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.chaoticd.crystal_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new CrystalFurnaceMenu(
            id,
            inventory,
            this,
            this.dataAccess,
            ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }
}
