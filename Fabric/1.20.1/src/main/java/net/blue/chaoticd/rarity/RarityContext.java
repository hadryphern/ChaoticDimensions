package net.blue.chaoticd.rarity;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Runtime inputs available to item and enchantment rarity providers. */
public record RarityContext(ItemStack stack, Enchantment enchantment, int enchantmentLevel) {
    public RarityContext {
        Objects.requireNonNull(stack, "stack");
        if (enchantment == null && enchantmentLevel != 0) {
            throw new IllegalArgumentException("An item-only context cannot have an enchantment level");
        }
        if (enchantmentLevel < 0) {
            throw new IllegalArgumentException("Enchantment level cannot be negative");
        }
    }

    public static RarityContext forItem(ItemStack stack) {
        return new RarityContext(stack, null, 0);
    }

    public static RarityContext forEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        return new RarityContext(stack, Objects.requireNonNull(enchantment, "enchantment"), level);
    }

    public boolean hasEnchantment() {
        return enchantment != null;
    }
}
