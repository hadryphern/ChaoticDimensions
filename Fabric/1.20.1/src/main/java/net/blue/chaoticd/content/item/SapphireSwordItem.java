package net.blue.chaoticd.content.item;

import net.blue.chaoticd.content.ModEffects;
import net.blue.chaoticd.content.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;

/** 589-damage sword with a 24-block-radius damage wave after a direct melee hit. */
public final class SapphireSwordItem extends SwordItem {
    public static final int DIRECT_DAMAGE = 589;
    public static final float AREA_DAMAGE = 589.0F;
    public static final double AREA_RADIUS = 24.0D;

    private static final Tier SAPPHIRE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 3000;
        }

        @Override
        public float getSpeed() {
            return 12.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getLevel() {
            return 4;
        }

        @Override
        public int getEnchantmentValue() {
            return 25;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.SAPPHIRE_GEM);
        }
    };

    public SapphireSwordItem(Item.Properties properties) {
        super(SAPPHIRE_TIER, DIRECT_DAMAGE, -2.4F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean used = super.hurtEnemy(stack, target, attacker);
        if (attacker.level().isClientSide || !used) {
            return used;
        }

        // Only the entity actually struck in melee receives the Sapphiric effect.
        target.addEffect(new MobEffectInstance(ModEffects.SAPPHIRIC, 20 * 45), attacker);

        AABB area = target.getBoundingBox().inflate(AREA_RADIUS);
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, area,
            candidate -> candidate != attacker && candidate != target && candidate.isAlive())) {
            nearby.hurt(attacker instanceof Player player
                ? attacker.damageSources().playerAttack(player)
                : attacker.damageSources().mobAttack(attacker), AREA_DAMAGE);
        }
        return used;
    }
}
