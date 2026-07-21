package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Keeps the server-side interaction validation in step with the custom sword reach. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @ModifyConstant(method = "handleInteract", constant = @Constant(doubleValue = 36.0D), require = 0)
    private double chaoticd$allowExtendedSwordReach(double vanillaSquaredReach) {
        float reach = ModCombatEnchantments.attackReach(player.getMainHandItem());
        return reach > 0.0F ? Math.max(vanillaSquaredReach, reach * reach) : vanillaSquaredReach;
    }
}
