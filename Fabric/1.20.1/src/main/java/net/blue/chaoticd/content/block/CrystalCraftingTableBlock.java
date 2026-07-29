package net.blue.chaoticd.content.block;

import org.jetbrains.annotations.Nullable;

import net.blue.chaoticd.content.menu.CrystalCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** A functional Crafting Table variant with its own menu validity check. */
public final class CrystalCraftingTableBlock extends CraftingTableBlock {
    private static final Component CONTAINER_TITLE = Component.translatable(
        "container.chaoticd.crystal_crafting_table"
    );

    public CrystalCraftingTableBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(
        BlockState state,
        Level level,
        BlockPos pos
    ) {
        return new SimpleMenuProvider(
            (containerId, inventory, player) -> createMenu(
                containerId,
                inventory,
                level,
                pos
            ),
            CONTAINER_TITLE
        );
    }

    private CrystalCraftingMenu createMenu(
        int containerId,
        Inventory inventory,
        Level level,
        BlockPos pos
    ) {
        return new CrystalCraftingMenu(
            containerId,
            inventory,
            ContainerLevelAccess.create(level, pos)
        );
    }
}
