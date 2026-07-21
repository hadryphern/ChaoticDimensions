package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * The single, ordered creative tab for this mod.
 *
 * Keep the section methods and their call order intact. New content must be added only to its matching
 * section so the tab never becomes a random collection of items.
 */
public final class ModItemGroups {
    public static final CreativeModeTab CHAOTIC_DIMENSIONS = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "chaotic_dimensions"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.chaoticd.chaotic_dimensions"))
            .icon(ModItems::createSapphireSword)
            .displayItems((parameters, entries) -> {
                addBlocks(entries);
                addToolsAndWeapons(entries);
                addMaterialsAndOres(entries);
                addArmor(entries);
                addEnchantments(entries);
                addPotions(entries);
                addNature(entries);
                addFood(entries);
                addSpawnEggs(entries);
                addUsefulItems(entries);
            })
            .build());

    private ModItemGroups() {
    }

    /** First: all block items, ordered by their block family. */
    private static void addBlocks(CreativeModeTab.Output entries) {
        // Future blocks belong here.
    }

    /** Second: tools, then weapons, each family kept together. */
    private static void addToolsAndWeapons(CreativeModeTab.Output entries) {
        entries.accept(ModItems.createSapphireTool(ModItems.SapphireToolType.PICKAXE));
        entries.accept(ModItems.createSapphireTool(ModItems.SapphireToolType.AXE));
        entries.accept(ModItems.createSapphireTool(ModItems.SapphireToolType.SHOVEL));
        entries.accept(ModItems.createSapphireTool(ModItems.SapphireToolType.HOE));
        entries.accept(ModItems.createSapphireSword());
    }

    /** Third: gems, ingots, raw materials, then ore blocks. */
    private static void addMaterialsAndOres(CreativeModeTab.Output entries) {
        entries.accept(ModItems.SAPPHIRE_GEM);
    }

    /** Fourth: all armor pieces, grouped by material. */
    private static void addArmor(CreativeModeTab.Output entries) {
        // Future armor belongs here.
    }

    /** Fifth: enchanted books. Dheathic remains an Ender Dragon reward in survival. */
    private static void addEnchantments(CreativeModeTab.Output entries) {
        for (int level = 1; level <= ModEnchantments.SAPPHIRIC.getMaxLevel(); level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.SAPPHIRIC, level)));
        }
        for (int level = 1; level <= ModEnchantments.BIG_BERTHA.getMaxLevel(); level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.BIG_BERTHA, level)));
        }
        for (int level = 1; level <= ModEnchantments.ROYAL.getMaxLevel(); level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.ROYAL, level)));
        }
        for (int level = 1; level <= ModEnchantments.DISPARADA.getMaxLevel(); level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DISPARADA, level)));
        }
        entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DHEATHIC, 1)));
    }

    /** Sixth: drinkable potion, splash potion, lingering potion, then tipped arrow. */
    private static void addPotions(CreativeModeTab.Output entries) {
        entries.accept(ModPotions.potion(Items.POTION));
        entries.accept(ModPotions.potion(Items.SPLASH_POTION));
        entries.accept(ModPotions.potion(Items.LINGERING_POTION));
        entries.accept(ModPotions.potion(Items.TIPPED_ARROW));
    }

    private static void addNature(CreativeModeTab.Output entries) {
        // Future natural content belongs here.
    }

    private static void addFood(CreativeModeTab.Output entries) {
        // Future food belongs here.
    }

    private static void addSpawnEggs(CreativeModeTab.Output entries) {
        // Future spawn eggs belong here.
    }

    private static void addUsefulItems(CreativeModeTab.Output entries) {
        // Future utility items belong here.
    }

    public static void initialize() {
        // Loading this class performs the registry entry above.
    }
}
