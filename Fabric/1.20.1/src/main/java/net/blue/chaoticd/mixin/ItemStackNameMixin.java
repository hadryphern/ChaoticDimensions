package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.ModItemRarities;
import net.blue.chaoticd.content.RarityText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the visual rank to stock hover names, with a permanent RGB Sapphire gradient. */
@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void chaoticd$applyRankedName(CallbackInfoReturnable<Component> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.hasCustomHoverName()) {
            return;
        }
        Component original = callback.getReturnValue();
        ModItemRarities.Rank rank = ModItemRarities.rank(stack);
        callback.setReturnValue(rank == ModItemRarities.Rank.LEGENDARY
            ? RarityText.rainbow(original.getString())
            : Component.literal(original.getString()).setStyle(Style.EMPTY.withColor(rank.color())));
    }
}
