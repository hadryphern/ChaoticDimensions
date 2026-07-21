package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.enchantment.DheathicEnchantment;
import net.blue.chaoticd.content.enchantment.SapphiricEnchantment;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** Registers the two Sapphire-era enchantments and the obtainable Sapphiric book. */
public final class ModEnchantments {
    public static final Enchantment SAPPHIRIC = register("sapphiric", new SapphiricEnchantment());
    public static final Enchantment DHEATHIC = register("dheathic", new DheathicEnchantment());

    private ModEnchantments() {
    }

    private static Enchantment register(String id, Enchantment enchantment) {
        return Registry.register(BuiltInRegistries.ENCHANTMENT,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id), enchantment);
    }

    public static void initialize() {
        // Dheathic deliberately is not placed in Creative: the Ender Dragon is its sole normal source.
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries ->
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(SAPPHIRIC, 1))));
    }
}
