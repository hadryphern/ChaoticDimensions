package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.ModEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Converts a short real draw into a much stronger draw when Disparada is on the bow. */
@Mixin(net.minecraft.world.item.BowItem.class)
public abstract class BowItemMixin {
    private static final ThreadLocal<ItemStack> CHAOTICD_ACTIVE_BOW = new ThreadLocal<>();

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void chaoticd$rememberBow(ItemStack bow, Level level, LivingEntity user, int timeLeft,
                                      CallbackInfo callback) {
        CHAOTICD_ACTIVE_BOW.set(bow);
    }

    @ModifyArg(method = "releaseUsing", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"), index = 0)
    private int chaoticd$accelerateBowCharge(int drawTicks) {
        ItemStack bow = CHAOTICD_ACTIVE_BOW.get();
        if (bow == null) {
            return drawTicks;
        }
        int disparada = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DISPARADA, bow);
        int multiplier = switch (disparada) {
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 6;
            case 4 -> 10;
            case 5 -> 20;
            default -> 1;
        };
        return drawTicks * multiplier;
    }

    @Inject(method = "releaseUsing", at = @At("RETURN"))
    private void chaoticd$forgetBow(ItemStack bow, Level level, LivingEntity user, int timeLeft,
                                    CallbackInfo callback) {
        CHAOTICD_ACTIVE_BOW.remove();
    }
}
