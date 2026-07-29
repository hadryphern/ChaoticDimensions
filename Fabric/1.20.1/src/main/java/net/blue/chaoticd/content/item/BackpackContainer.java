package net.blue.chaoticd.content.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

/** A persistent 27-slot container backed by the backpack ItemStack NBT. */
public final class BackpackContainer extends SimpleContainer {
    private static final String ITEMS_TAG = "BackpackItems";
    private final ItemStack backpack;
    private boolean loading;

    public BackpackContainer(ItemStack backpack) {
        super(27);
        this.backpack = backpack;
        this.loading = true;
        load();
        this.loading = false;
    }

    private void load() {
        CompoundTag tag = backpack.getOrCreateTag();
        if (!tag.contains(ITEMS_TAG, Tag.TAG_LIST)) {
            return;
        }

        ListTag items = tag.getList(ITEMS_TAG, Tag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            CompoundTag itemTag = items.getCompound(index);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < getContainerSize()) {
                super.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !(stack.getItem() instanceof LeatherBackpackItem);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) {
            save();
        }
    }

    private void save() {
        ListTag items = new ListTag();

        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte)slot);
            stack.save(itemTag);
            items.add(itemTag);
        }

        backpack.getOrCreateTag().put(ITEMS_TAG, items);
    }
}
