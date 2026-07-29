package net.blue.chaoticd.mixin;

import net.blue.chaoticd.client.SpecialItemNameStyler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-only styling for a small, explicit set of built-in item names. */
@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void chaoticd$styleSpecialBuiltInName(CallbackInfoReturnable<Component> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        callback.setReturnValue(SpecialItemNameStyler.styleBuiltInName(stack, callback.getReturnValue()));
    }
}
