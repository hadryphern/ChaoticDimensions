package net.blue.chaoticd.mixin.client;

import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores an entity target beyond vanilla's three-block anti-reach client clamp. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "pick", at = @At("TAIL"))
    private void chaoticd$pickLongRangeSwordTarget(float partialTick, CallbackInfo callback) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity player = minecraft.getCameraEntity();
        if (player == null || minecraft.level == null || minecraft.player == null) {
            return;
        }

        float reach = ModCombatEnchantments.attackReach(minecraft.player.getMainHandItem());
        if (reach <= 3.0F) {
            return;
        }

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 direction = player.getViewVector(partialTick);
        double maximumReach = reach;
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
            maximumReach = Math.min(maximumReach, eye.distanceTo(minecraft.hitResult.getLocation()));
        }
        Vec3 end = eye.add(direction.scale(maximumReach));
        AABB searchBox = player.getBoundingBox().expandTowards(direction.scale(maximumReach)).inflate(1.0D);
        EntityHitResult target = ProjectileUtil.getEntityHitResult(player, eye, end, searchBox,
            candidate -> !candidate.isSpectator() && candidate.isPickable(), maximumReach * maximumReach);
        if (target != null) {
            minecraft.hitResult = target;
            minecraft.crosshairPickEntity = target.getEntity();
        }
    }
}
