package net.blue.chaoticd.mixin;

import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * The base Container interface otherwise caps otherwise-valid 999 stacks at
 * 64 when they enter player inventories, chests, hoppers, furnaces, and other
 * standard storage. Special slots that explicitly return one or another value
 * continue to use their own limits.
 */
@Mixin(Container.class)
public interface ContainerMaxStackSizeMixin {
    /**
     * @author Chaotic Dimensions
     * @reason Keep the default container capacity in sync with default item capacity.
     */
    @Overwrite
    default int getMaxStackSize() {
        return ExtendedStackSize.MAXIMUM;
    }
}
