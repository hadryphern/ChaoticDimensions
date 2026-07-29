package net.blue.chaoticd.mixin;

import net.blue.chaoticd.stack.ExtendedStackSize;
import net.minecraft.world.entity.player.StackedContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Lets the recipe book and recipe-placement solver account for all items in a
 * 999 stack instead of silently considering at most the vanilla 64.
 */
@Mixin(StackedContents.class)
public abstract class StackedContentsMixin {
    @ModifyConstant(
        method = "accountStack(Lnet/minecraft/world/item/ItemStack;)V",
        constant = @Constant(intValue = ExtendedStackSize.VANILLA_DEFAULT)
    )
    private int chaoticd$accountExtendedDefaultStacks(int original) {
        return ExtendedStackSize.MAXIMUM;
    }
}
