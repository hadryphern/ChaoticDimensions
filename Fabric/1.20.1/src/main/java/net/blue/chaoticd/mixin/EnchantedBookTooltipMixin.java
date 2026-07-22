package net.blue.chaoticd.mixin;

import java.util.List;
import net.blue.chaoticd.client.rarity.AnimatedColorProvider;
import net.blue.chaoticd.client.rarity.TooltipEnchantmentStyler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Styles StoredEnchantments at their creation site; normal item enchantments are handled separately. */
@Mixin(EnchantedBookItem.class)
public abstract class EnchantedBookTooltipMixin {
    @Unique
    private int chaoticd$firstBookEnchantmentLine;

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    private void chaoticd$captureFirstBookLine(ItemStack stack, Level level, List<Component> lines,
                                                TooltipFlag flag, CallbackInfo callback) {
        chaoticd$firstBookEnchantmentLine = lines.size();
    }

    @Inject(method = "appendHoverText", at = @At("RETURN"))
    private void chaoticd$styleBookEnchantments(ItemStack stack, Level level, List<Component> lines,
                                                 TooltipFlag flag, CallbackInfo callback) {
        TooltipEnchantmentStyler.styleExisting(stack, lines, EnchantedBookItem.getEnchantments(stack),
            chaoticd$firstBookEnchantmentLine, AnimatedColorProvider.global().nowNanos());
    }
}
