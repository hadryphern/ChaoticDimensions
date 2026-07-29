package net.blue.chaoticd.mixin;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Raises only items whose own vanilla limit is the normal 64-item default.
 * Explicitly limited items (one, sixteen, or another value) keep their limit.
 */
@Mixin(Item.class)
public abstract class ItemMaxStackSizeMixin {
    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void chaoticd$raiseOnlyDefaultStackables(CallbackInfoReturnable<Integer> callback) {
        if (callback.getReturnValue() == ExtendedStackSize.VANILLA_DEFAULT) {
            callback.setReturnValue(ExtendedStackSize.MAXIMUM);
        }
    }
}
