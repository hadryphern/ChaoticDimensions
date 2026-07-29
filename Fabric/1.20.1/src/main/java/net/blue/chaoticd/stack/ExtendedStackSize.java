package net.blue.chaoticd.stack;

import net.minecraft.world.item.ItemStack;

/**
 * Shared limits for the 1.20.1 extended-stack compatibility layer.
 *
 * <p>This helper deliberately lives outside the mixin package.  Mixin packages
 * may only contain transformed mixin classes; loading a normal helper from one
 * of them can crash the server while saving an {@link ItemStack}.</p>
 */
public final class ExtendedStackSize {
    public static final int VANILLA_DEFAULT = 64;
    public static final int MAXIMUM = 999;
    public static final int NETWORK_EXTENDED_MARKER = Byte.MIN_VALUE;
    public static final String SERIALIZED_COUNT_KEY = "chaoticd:stack_count";

    private ExtendedStackSize() {
    }

    public static boolean isSupportedCount(ItemStack stack, int count) {
        return count > 0 && count <= MAXIMUM && count <= stack.getMaxStackSize();
    }
}
