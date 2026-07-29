package net.blue.chaoticd.content.menu;

import net.blue.chaoticd.content.ModBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

/**
 * Vanilla crafting menu whose validity is tied to the Crystal Crafting Table.
 *
 * <p>The vanilla {@link CraftingMenu} only accepts {@code Blocks.CRAFTING_TABLE},
 * which would close a menu opened by a custom crafting-table block on the next
 * server tick. Keeping the access here makes the custom block the authority for
 * the distance and block-state check.</p>
 */
public final class CrystalCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess crystalAccess;

    public CrystalCraftingMenu(
        int containerId,
        Inventory inventory,
        ContainerLevelAccess access
    ) {
        super(containerId, inventory, access);
        this.crystalAccess = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
            this.crystalAccess,
            player,
            ModBlocks.CRYSTAL_CRAFTING_TABLE
        );
    }
}
