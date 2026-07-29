package net.blue.chaoticd.content.menu;

import net.blue.chaoticd.content.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceMenu;

/**
 * Furnace menu whose proximity check accepts the Crystal Furnace block.
 *
 * <p>The vanilla {@link FurnaceMenu} is hard-coded to a vanilla furnace for
 * its {@code stillValid} check. Without this override the server opens the
 * menu and immediately closes it on the next tick.</p>
 */
public final class CrystalFurnaceMenu extends FurnaceMenu {
    private final ContainerLevelAccess crystalAccess;

    public CrystalFurnaceMenu(
        int containerId,
        Inventory inventory,
        Container furnace,
        ContainerData data,
        ContainerLevelAccess access
    ) {
        super(containerId, inventory, furnace, data);
        this.crystalAccess = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
            this.crystalAccess,
            player,
            ModBlocks.CRYSTAL_FURNACE
        );
    }
}
