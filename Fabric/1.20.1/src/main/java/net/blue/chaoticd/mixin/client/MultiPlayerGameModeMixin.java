package net.blue.chaoticd.mixin.client;

import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets the local crosshair select entities at Big Bertha or Royal range. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "getPickRange", at = @At("RETURN"), cancellable = true)
    private void chaoticd$extendSwordPickRange(CallbackInfoReturnable<Float> callback) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            callback.setReturnValue(Math.max(callback.getReturnValue(),
                ModCombatEnchantments.attackReach(player.getMainHandItem())));
        }
    }
}
