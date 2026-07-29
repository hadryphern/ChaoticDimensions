package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for the centralized Titanium progression data. */
public enum TitaniumTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return ProgressionMaterial.TITANIUM.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.TITANIUM.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.TITANIUM.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.TITANIUM.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.TITANIUM.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.TITANIUM.getRepairIngredient();
    }
}
