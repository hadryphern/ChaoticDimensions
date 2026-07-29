package net.blue.chaoticd.gameplay;

import net.minecraft.world.item.ItemStack;

/** Snapshot used while the player's inventory is temporarily dropped on death. */
public record DeathTotemInventorySlot(int slot, ItemStack stack) {
}
