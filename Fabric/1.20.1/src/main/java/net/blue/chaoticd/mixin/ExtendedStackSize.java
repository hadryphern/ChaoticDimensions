package net.blue.chaoticd.mixin;

import net.minecraft.world.item.ItemStack;

/**
 * Shared limits for the 1.20.1 extended-stack compatibility layer.
 *
 * <p>Vanilla stores an item count in a signed byte in both the wire protocol
 * and the legacy ItemStack NBT representation. The mixins using this class
 * keep the normal byte representation untouched for ordinary stacks and use
 * a reserved byte marker plus a VarInt only when a count is above 127.</p>
 */
final class ExtendedStackSize {
    static final int VANILLA_DEFAULT = 64;
    static final int MAXIMUM = 999;
    static final int NETWORK_EXTENDED_MARKER = Byte.MIN_VALUE;
    static final String SERIALIZED_COUNT_KEY = "chaoticd:stack_count";

    private ExtendedStackSize() {
    }

    static boolean isSupportedCount(ItemStack stack, int count) {
        return count > 0 && count <= MAXIMUM && count <= stack.getMaxStackSize();
    }
}
