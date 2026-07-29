package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Compatibility facade for the centralized Sapphire progression data. */
public final class SapphireTier implements Tier {
    public static final SapphireTier INSTANCE = new SapphireTier();

    private SapphireTier() {
    }

    @Override
    public int getUses() {
        return ProgressionMaterial.SAPPHIRE.getUses();
    }

    @Override
    public float getSpeed() {
        return ProgressionMaterial.SAPPHIRE.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return ProgressionMaterial.SAPPHIRE.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return ProgressionMaterial.SAPPHIRE.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return ProgressionMaterial.SAPPHIRE.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return ProgressionMaterial.SAPPHIRE.getRepairIngredient();
    }
}
