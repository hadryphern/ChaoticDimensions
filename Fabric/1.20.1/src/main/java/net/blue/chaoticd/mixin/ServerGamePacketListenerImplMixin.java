package net.blue.chaoticd.mixin;

import net.blue.chaoticd.content.ModCombatEnchantments;
import net.blue.chaoticd.network.InteractionPacketInspector;
import net.blue.chaoticd.stack.ExtendedStackSize;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps the server-side interaction validation in step with the custom sword reach. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Redirect(method = "handleInteract", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/phys/AABB;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double chaoticd$scaleInteractionDistanceForSwordReach(
        AABB targetBounds,
        Vec3 eyePosition,
        ServerboundInteractPacket packet
    ) {
        double actualSquaredDistance = targetBounds.distanceToSqr(eyePosition);
        if (!InteractionPacketInspector.isAttack(packet)) {
            return actualSquaredDistance;
        }
        float reach = ModCombatEnchantments.attackReach(player.getMainHandItem());
        // Vanilla compares this value to a fixed 36.0 (six blocks); normalize our custom reach to it.
        return reach > 0.0F ? actualSquaredDistance * 36.0D / (reach * reach) : actualSquaredDistance;
    }

    /**
     * Vanilla rejects any creative-inventory packet above 64 before it reaches
     * the normal slot validation. Raise that transport guard in lockstep with
     * the extended default item limit.
     */
    @ModifyConstant(
        method = "handleSetCreativeModeSlot",
        constant = @Constant(intValue = ExtendedStackSize.VANILLA_DEFAULT)
    )
    private int chaoticd$raiseCreativePacketStackGuard(int original) {
        return ExtendedStackSize.MAXIMUM;
    }

    /**
     * Keep the creative packet guard strict for items which deliberately retain
     * a lower limit, such as unstackables and 16-item consumables. Returning a
     * value above the guard rejects an invalid client packet without changing
     * the rest of vanilla's handler.
     */
    @Redirect(
        method = "handleSetCreativeModeSlot",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getCount()I"
        )
    )
    private int chaoticd$validateCreativePacketItemLimit(ItemStack stack) {
        int count = stack.getCount();
        return count <= stack.getMaxStackSize() ? count : ExtendedStackSize.MAXIMUM + 1;
    }
}
