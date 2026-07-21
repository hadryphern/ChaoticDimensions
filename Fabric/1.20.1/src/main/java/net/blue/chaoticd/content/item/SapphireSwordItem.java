package net.blue.chaoticd.content.item;

import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModCombatEnchantments;
import net.blue.chaoticd.content.ModEnchantments;
import net.blue.chaoticd.content.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;

/** 589-damage sword with a 24-block-radius damage wave after a direct melee hit. */
public final class SapphireSwordItem extends SwordItem {
    public static final int DIRECT_DAMAGE = 589;
    public static final float AREA_DAMAGE = 589.0F;
    public static final double AREA_RADIUS = 24.0D;

    public SapphireSwordItem(Item.Properties properties) {
        super(SapphireTier.INSTANCE, DIRECT_DAMAGE, -2.4F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean used = super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide || !used) {
            return used;
        }

        AABB area = target.getBoundingBox().inflate(AREA_RADIUS);
        int sapphiricLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SAPPHIRIC, stack);
        float areaDamage = AREA_DAMAGE * ModCombatEnchantments.damageMultiplier(stack);
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, area,
            candidate -> candidate != attacker && candidate != target && candidate.isAlive())) {
            // A magic source has no melee attacker, so Sapphiric never propagates via the area wave.
            nearby.hurt(attacker.damageSources().magic(), areaDamage);
            if (sapphiricLevel >= 5) {
                nearby.addEffect(new MobEffectInstance(ModEffects.SAPPHIRIC, 20 * 45), attacker);
            }
        }
        return used;
    }

}
