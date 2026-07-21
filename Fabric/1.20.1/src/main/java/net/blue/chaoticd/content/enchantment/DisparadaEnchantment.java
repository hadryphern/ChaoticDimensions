package net.blue.chaoticd.content.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** Portuguese-named bow enchantment that greatly reduces draw time. */
public final class DisparadaEnchantment extends Enchantment {
    public DisparadaEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}
