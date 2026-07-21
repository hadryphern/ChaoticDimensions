package net.blue.chaoticd.content.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** Applies the disorienting Sapphiric effect to direct sword melee hits. */
public final class SapphiricEnchantment extends Enchantment {
    public SapphiricEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof SwordItem;
    }

    @Override
    public int getMinCost(int level) {
        return 12 + level * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 18;
    }
}
