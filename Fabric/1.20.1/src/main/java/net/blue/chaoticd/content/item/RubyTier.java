package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for the centralized Ruby progression data. */
public enum RubyTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return ProgressionMaterial.RUBY.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.RUBY.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.RUBY.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.RUBY.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.RUBY.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.RUBY.getRepairIngredient();
    }
}
