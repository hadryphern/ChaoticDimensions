package net.blue.chaoticd.content.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Compatibility facade for the centralized post-Netherite progression data.
 * Vylam and Vortex are present so their intended tiers are available as soon
 * as their supplied tool assets are registered.
 */
public enum FutureProgressionTier implements Tier {
    CHLOROPHYTE(ProgressionMaterial.CHLOROPHYTE),
    VYLAM(ProgressionMaterial.VYLAM),
    HERO(ProgressionMaterial.HERO),
    SHADOW(ProgressionMaterial.SHADOW),
    VORTEX(ProgressionMaterial.VORTEX);

    private final ProgressionMaterial material;

    FutureProgressionTier(ProgressionMaterial material) {
        this.material = material;
    }

    @Override
    public int getUses() {
        return material.getUses();
    }

    @Override
    public float getSpeed() {
        return material.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return material.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return material.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return material.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return material.getRepairIngredient();
    }
}
