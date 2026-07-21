package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.enchantment.DheathicEnchantment;
import net.blue.chaoticd.content.enchantment.BigBerthaEnchantment;
import net.blue.chaoticd.content.enchantment.RoyalEnchantment;
import net.blue.chaoticd.content.enchantment.SapphiricEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/** Registers the two Sapphire-era enchantments and the obtainable Sapphiric book. */
public final class ModEnchantments {
    public static final Enchantment SAPPHIRIC = register("sapphiric", new SapphiricEnchantment());
    public static final Enchantment DHEATHIC = register("dheathic", new DheathicEnchantment());
    public static final Enchantment BIG_BERTHA = register("big_bertha", new BigBerthaEnchantment());
    public static final Enchantment ROYAL = register("royal", new RoyalEnchantment());

    private ModEnchantments() {
    }

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id), enchantment);
    }

    public static void initialize() {
        // Books are placed in the dedicated Chaotic Dimensions Enchantments category.
    }
}
