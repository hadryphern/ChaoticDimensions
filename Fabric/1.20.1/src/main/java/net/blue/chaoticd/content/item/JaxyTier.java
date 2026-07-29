package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for the centralized Jaxy progression data. */
public enum JaxyTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return ProgressionMaterial.JAXY.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.JAXY.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.JAXY.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.JAXY.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.JAXY.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.JAXY.getRepairIngredient();
    }
}
