package net.blue.chaoticd.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Preserves extended stack counts in every legacy ItemStack NBT save path.
 * Vanilla's Count byte remains present and is capped at 127 for safe fallback,
 * while the full 128..999 count is stored in a namespaced Int companion key.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackCountSerializationMixin {
    @Inject(method = "save", at = @At("RETURN"))
    private void chaoticd$saveExtendedCount(
        CompoundTag serialized,
        CallbackInfoReturnable<CompoundTag> callback
    ) {
        ItemStack stack = (ItemStack) (Object) this;
        int count = stack.getCount();
        if (count > Byte.MAX_VALUE && ExtendedStackSize.isSupportedCount(stack, count)) {
            serialized.putByte("Count", Byte.MAX_VALUE);
            serialized.putInt(ExtendedStackSize.SERIALIZED_COUNT_KEY, count);
        } else {
            serialized.remove(ExtendedStackSize.SERIALIZED_COUNT_KEY);
        }
    }

    @Inject(method = "of", at = @At("RETURN"), cancellable = true)
    private static void chaoticd$restoreExtendedCount(
        CompoundTag serialized,
        CallbackInfoReturnable<ItemStack> callback
    ) {
        if (!serialized.contains(ExtendedStackSize.SERIALIZED_COUNT_KEY, Tag.TAG_INT)) {
            return;
        }

        ItemStack stack = callback.getReturnValue();
        int count = serialized.getInt(ExtendedStackSize.SERIALIZED_COUNT_KEY);
        if (!stack.isEmpty() && count > Byte.MAX_VALUE && ExtendedStackSize.isSupportedCount(stack, count)) {
            stack.setCount(count);
        }
    }
}
