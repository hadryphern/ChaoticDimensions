package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.enchantment.DheathicEnchantment;
import net.blue.chaoticd.content.enchantment.DisparadaEnchantment;
import net.blue.chaoticd.content.enchantment.BigBerthaEnchantment;
import net.blue.chaoticd.content.enchantment.RoyalEnchantment;
import net.blue.chaoticd.content.enchantment.SapphiricEnchantment;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

/** Registers the two Sapphire-era enchantments and the obtainable Sapphiric book. */
public final class ModEnchantments {
    public static final Enchantment SAPPHIRIC = register("sapphiric", new SapphiricEnchantment());
    public static final Enchantment DHEATHIC = register("dheathic", new DheathicEnchantment());
    public static final Enchantment BIG_BERTHA = register("big_bertha", new BigBerthaEnchantment());
    public static final Enchantment ROYAL = register("royal", new RoyalEnchantment());
    public static final Enchantment DISPARADA = register("disparada", new DisparadaEnchantment());

    private ModEnchantments() {
    }

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id), enchantment);
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            addBooks(entries, Enchantments.SHARPNESS, 15);
            addBooks(entries, Enchantments.UNBREAKING, 10);
            addBooks(entries, Enchantments.ALL_DAMAGE_PROTECTION, 15);
            addBooks(entries, Enchantments.FIRE_PROTECTION, 15);
            addBooks(entries, Enchantments.BLAST_PROTECTION, 15);
            addBooks(entries, Enchantments.PROJECTILE_PROTECTION, 15);
            addBooks(entries, Enchantments.FALL_PROTECTION, 15);
            addBooks(entries, Enchantments.THORNS, 15);
            addBooks(entries, Enchantments.BLOCK_EFFICIENCY, 10);
            addBooks(entries, Enchantments.KNOCKBACK, 20);
            addBooks(entries, Enchantments.SMITE, 10);
            addBooks(entries, Enchantments.BANE_OF_ARTHROPODS, 10);
            addBooks(entries, Enchantments.SWEEPING_EDGE, 10);
            addBooks(entries, Enchantments.FIRE_ASPECT, 10);
            addBooks(entries, Enchantments.MOB_LOOTING, 10);
            addBooks(entries, Enchantments.BLOCK_FORTUNE, 10);
            addBooks(entries, DISPARADA, 5);
        });
    }

    private static void addBooks(FabricItemGroupEntries entries, Enchantment enchantment, int maxLevel) {
        for (int level = 1; level <= maxLevel; level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
        }
    }
}
