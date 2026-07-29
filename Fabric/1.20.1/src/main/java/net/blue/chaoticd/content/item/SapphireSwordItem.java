package net.blue.chaoticd.content.item;

import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModCombatEnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.AABB;

/** Sapphire sword with a 24-block-radius damage wave after a direct melee hit. */
public final class SapphireSwordItem extends SwordItem {
    public static final double AREA_RADIUS = 24.0D;

    public SapphireSwordItem(Item.Properties properties) {
        super(
            SapphireTier.INSTANCE,
            ProgressionMaterial.SAPPHIRE.swordAttackModifier(),
            -2.4F,
            properties
        );
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean used = super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide || !used) {
            return used;
        }

        AABB area = target.getBoundingBox().inflate(AREA_RADIUS);
        float areaDamage = ProgressionMaterial.SAPPHIRE.swordAttackDamage()
            * ModCombatEnchantments.damageMultiplier(stack);
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, area,
            candidate -> candidate != attacker && candidate != target && candidate.isAlive())) {
            // Sapphiric effects are applied once by ModGameplayEvents to the original melee hit.
            // The wave only deals damage, avoiding a second 24-block entity scan on every hit.
            nearby.hurt(attacker.damageSources().magic(), areaDamage);
        }
        return used;
    }

}
