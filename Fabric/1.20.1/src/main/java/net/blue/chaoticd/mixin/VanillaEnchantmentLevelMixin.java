package net.blue.chaoticd.mixin;

import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.FireAspectEnchantment;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.item.enchantment.LootBonusEnchantment;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.item.enchantment.SweepingEdgeEnchantment;
import net.minecraft.world.item.enchantment.ThornsEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Raises the requested vanilla enchantment caps without changing unrelated enchantments. */
@Mixin({DamageEnchantment.class, DiggingEnchantment.class, DigDurabilityEnchantment.class,
    FireAspectEnchantment.class, KnockbackEnchantment.class, LootBonusEnchantment.class,
    ProtectionEnchantment.class, SweepingEdgeEnchantment.class, ThornsEnchantment.class})
public abstract class VanillaEnchantmentLevelMixin {
    @Inject(method = "getMaxLevel", at = @At("HEAD"), cancellable = true)
    private void chaoticd$raiseVanillaCaps(CallbackInfoReturnable<Integer> callback) {
        Object self = this;
        if (self == Enchantments.SHARPNESS) callback.setReturnValue(15);
        else if (self == Enchantments.UNBREAKING) callback.setReturnValue(10);
        else if (self == Enchantments.ALL_DAMAGE_PROTECTION || self == Enchantments.FIRE_PROTECTION
            || self == Enchantments.BLAST_PROTECTION || self == Enchantments.PROJECTILE_PROTECTION
            || self == Enchantments.FALL_PROTECTION || self == Enchantments.THORNS) callback.setReturnValue(15);
        else if (self == Enchantments.BLOCK_EFFICIENCY || self == Enchantments.KNOCKBACK) callback.setReturnValue(self == Enchantments.KNOCKBACK ? 20 : 10);
        else if (self == Enchantments.MOB_LOOTING || self == Enchantments.BLOCK_FORTUNE
            || self == Enchantments.SMITE || self == Enchantments.BANE_OF_ARTHROPODS
            || self == Enchantments.SWEEPING_EDGE || self == Enchantments.FIRE_ASPECT) callback.setReturnValue(10);
    }
}
