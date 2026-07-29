package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Extremely rare protection required to survive the Shadow Dimension normally. */
public final class DeathTotemItem extends Item {
    public DeathTotemItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
