package net.blue.chaoticd.mixin;

import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Vanilla item-entity merging has an independent hard-coded 64-item cap. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityStackMergeMixin {
    @ModifyConstant(
        method = "merge(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
        constant = @Constant(intValue = ExtendedStackSize.VANILLA_DEFAULT)
    )
    private static int chaoticd$raiseDroppedItemMergeCap(int original) {
        return ExtendedStackSize.MAXIMUM;
    }
}
