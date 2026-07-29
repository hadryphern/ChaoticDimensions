package net.blue.chaoticd.mixin;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.blue.chaoticd.stack.ExtendedStackSize;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends the vanilla item-stack packet format without changing its byte layout
 * for every normal stack. Counts up to 127 remain the exact vanilla byte;
 * larger counts reserve -128 as an escape marker followed by a VarInt.
 *
 * <p>This runs on both the client and dedicated server. A connection that can
 * receive a stack above 127 must therefore run this same mod version on both
 * sides, which is required to avoid a protocol desynchronization.</p>
 */
@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufItemStackMixin {
    @Inject(method = "writeItem", at = @At("HEAD"), cancellable = true)
    private void chaoticd$writeExtendedItemStack(
        ItemStack stack,
        CallbackInfoReturnable<FriendlyByteBuf> callback
    ) {
        FriendlyByteBuf buffer = (FriendlyByteBuf) (Object) this;
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
            callback.setReturnValue(buffer);
            return;
        }

        int count = stack.getCount();
        if (!ExtendedStackSize.isSupportedCount(stack, count)) {
            throw new EncoderException("Unsupported Chaotic Dimensions stack count: " + count);
        }

        buffer.writeBoolean(true);
        Item item = stack.getItem();
        buffer.writeId(BuiltInRegistries.ITEM, item);
        if (count > Byte.MAX_VALUE) {
            buffer.writeByte(ExtendedStackSize.NETWORK_EXTENDED_MARKER);
            buffer.writeVarInt(count);
        } else {
            buffer.writeByte(count);
        }

        CompoundTag tag = null;
        if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
            tag = stack.getTag();
        }
        buffer.writeNbt(tag);
        callback.setReturnValue(buffer);
    }

    @Inject(method = "readItem", at = @At("HEAD"), cancellable = true)
    private void chaoticd$readExtendedItemStack(CallbackInfoReturnable<ItemStack> callback) {
        FriendlyByteBuf buffer = (FriendlyByteBuf) (Object) this;
        if (!buffer.readBoolean()) {
            callback.setReturnValue(ItemStack.EMPTY);
            return;
        }

        Item item = buffer.readById(BuiltInRegistries.ITEM);
        int encodedCount = buffer.readByte();
        int count = encodedCount == ExtendedStackSize.NETWORK_EXTENDED_MARKER
            ? buffer.readVarInt()
            : encodedCount;
        ItemStack stack = new ItemStack(item, count);
        if (!ExtendedStackSize.isSupportedCount(stack, count)) {
            throw new DecoderException("Unsupported Chaotic Dimensions stack count: " + count);
        }

        stack.setTag(buffer.readNbt());
        callback.setReturnValue(stack);
    }
}
