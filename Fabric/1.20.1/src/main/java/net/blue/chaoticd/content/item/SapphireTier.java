package net.blue.chaoticd.content.item;

import net.blue.chaoticd.content.ModItems;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Shared material rules for every Sapphire tool and weapon. */
public final class SapphireTier implements Tier {
    public static final SapphireTier INSTANCE = new SapphireTier();

    private SapphireTier() {
    }

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
}
