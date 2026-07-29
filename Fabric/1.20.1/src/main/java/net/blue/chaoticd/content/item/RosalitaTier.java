package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for the centralized Rosalita progression data. */
public enum RosalitaTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return ProgressionMaterial.ROSALITA.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.ROSALITA.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.ROSALITA.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.ROSALITA.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.ROSALITA.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.ROSALITA.getRepairIngredient();
    }
}
