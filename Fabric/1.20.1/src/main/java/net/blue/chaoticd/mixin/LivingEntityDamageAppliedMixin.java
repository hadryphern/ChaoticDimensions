package net.blue.chaoticd.mixin;

import java.util.ArrayDeque;
import java.util.Deque;
import net.blue.chaoticd.content.ModGameplayEvents;
import net.blue.chaoticd.gameplay.ProgressionArmorProtection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reports damage only after vanilla armor, absorption, cancellation and
 * invulnerability processing have decided whether the hit actually landed.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageAppliedMixin {
    @Unique
    private final Deque<Float> chaoticd$damageSnapshots = new ArrayDeque<>();

    /**
     * Scale direct Sapphiric/Royal sword damage as part of the original hit.
     * Keeping that hit intact is important: Player.attack uses its return value
     * to apply durability, knockback, sweeping and weapon-specific callbacks.
     */
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float chaoticd$scaleDirectSwordDamage(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide) {
            return amount;
        }

        float scaledSwordDamage = ModGameplayEvents.scaleDirectSwordDamage(source, amount);
        return ProgressionArmorProtection.scaleIncomingDamage(entity, source, scaledSwordDamage);
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void chaoticd$captureDamageSnapshot(
        DamageSource source,
        float amount,
        CallbackInfoReturnable<Boolean> callback
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide) {
            chaoticd$damageSnapshots.addLast(entity.getHealth() + entity.getAbsorptionAmount());
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void chaoticd$reportAppliedDamage(
        DamageSource source,
        float amount,
        CallbackInfoReturnable<Boolean> callback
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide || chaoticd$damageSnapshots.isEmpty()) {
            return;
        }

        float before = chaoticd$damageSnapshots.removeLast();
        if (!callback.getReturnValue()) {
            return;
        }

        float appliedDamage = Math.max(
            0.0F,
            before - entity.getHealth() - entity.getAbsorptionAmount()
        );
        ModGameplayEvents.onDamageApplied(entity, source, appliedDamage);
    }
}
