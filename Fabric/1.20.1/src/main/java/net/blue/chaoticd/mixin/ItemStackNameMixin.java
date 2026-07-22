package net.blue.chaoticd.mixin;

import net.blue.chaoticd.client.rarity.TooltipRarityRenderer;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client-only name styling; copies Components and retains custom-name typography/events. */
@Mixin(ItemStack.class)
public abstract class ItemStackNameMixin {
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void chaoticd$applyResolvedRarity(CallbackInfoReturnable<Component> callback) {
        ItemStack stack = (ItemStack) (Object) this;
        RarityDefinition rarity = RarityResolver.global().resolveItem(stack);
        callback.setReturnValue(TooltipRarityRenderer.global().style(callback.getReturnValue(), rarity));
    }
}
