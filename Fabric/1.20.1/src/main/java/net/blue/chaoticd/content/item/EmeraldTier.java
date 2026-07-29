package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Compatibility facade for the centralized Emerald progression data.
 */
public enum EmeraldTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return ProgressionMaterial.EMERALD.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.EMERALD.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.EMERALD.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.EMERALD.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.EMERALD.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.EMERALD.getRepairIngredient();
    }
}
