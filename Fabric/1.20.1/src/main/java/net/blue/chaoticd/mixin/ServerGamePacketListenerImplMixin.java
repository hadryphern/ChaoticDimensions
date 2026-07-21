package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the server-side interaction validation in step with the custom sword reach. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Redirect(method = "handleInteract", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double chaoticd$scaleInteractionDistanceForSwordReach(AABB targetBounds, Vec3 eyePosition) {
        double actualSquaredDistance = targetBounds.distanceToSqr(eyePosition);
        float reach = ModCombatEnchantments.attackReach(player.getMainHandItem());
        // Vanilla compares this value to a fixed 36.0 (six blocks); normalize our custom reach to it.
        return reach > 0.0F ? actualSquaredDistance * 36.0D / (reach * reach) : actualSquaredDistance;
    }
}
