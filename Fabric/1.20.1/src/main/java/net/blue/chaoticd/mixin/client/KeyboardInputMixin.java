package net.blue.chaoticd.mixin.client;

import net.blue.chaoticd.content.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reorders local movement directions while Sapphiric is active. */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void chaoticd$shuffleDirections(boolean slowDown, float slowdownFactor, CallbackInfo callback) {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.hasEffect(ModEffects.SAPPHIRIC)) {
            return;
        }

        Input input = (Input) (Object) this;
        boolean up = input.up;
        boolean down = input.down;
        boolean left = input.left;
        boolean right = input.right;

        switch ((player.tickCount / 20) % 4) {
            case 0 -> {
                input.up = left;
                input.right = up;
                input.down = right;
                input.left = down;
            }
            case 1 -> {
                input.up = right;
                input.right = down;
                input.down = left;
                input.left = up;
            }
            case 2 -> {
                input.up = down;
                input.right = left;
                input.down = up;
                input.left = right;
            }
            default -> {
                input.up = left;
                input.right = down;
                input.down = right;
                input.left = up;
            }
        }
        input.forwardImpulse = (input.up ? 1.0F : 0.0F) - (input.down ? 1.0F : 0.0F);
        input.leftImpulse = (input.left ? 1.0F : 0.0F) - (input.right ? 1.0F : 0.0F);
    }
}
