package net.blue.chaoticd.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Shared, deterministic rules for custom sword enchantment damage and reach. */
public final class ModCombatEnchantments {
    private static final float[] BIG_BERTHA_REACH = {0.0F, 8.0F, 16.0F, 24.0F, 32.0F};
    private static final float[] ROYAL_REACH = {0.0F, 16.0F, 32.0F, 48.0F, 64.0F};

    private ModCombatEnchantments() {
    }

    /** Sapphiric I–V means 2x, 4x, 8x, 16x, and 32x base sword damage respectively. */
    public static float damageMultiplier(ItemStack sword) {
        int sapphiric = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SAPPHIRIC, sword);
        int royal = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ROYAL, sword);
        float multiplier = sapphiric > 0 ? 1 << sapphiric : 1.0F;
        return royal > 0 ? multiplier * 10.0F : multiplier;
    }

    /** Big Bertha and Royal extend attack reach while leaving the item model untouched. */
    public static float attackReach(ItemStack sword) {
        int royal = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ROYAL, sword);
        if (royal > 0) {
            return ROYAL_REACH[royal];
        }
        int bigBertha = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BIG_BERTHA, sword);
        return bigBertha > 0 ? BIG_BERTHA_REACH[bigBertha] : 0.0F;
    }

    /** Sapphiric area radii: direct target, then 6/12/18/24 blocks. */
    public static double sapphiricEffectRadius(int level) {
        return switch (level) {
            case 2 -> 6.0D;
            case 3 -> 12.0D;
            case 4 -> 18.0D;
            case 5 -> 24.0D;
            default -> 0.0D;
        };
    }
}
