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
import net.minecraft.world.level.block.Block;

/**
 * Ordered creative tabs for released content and explicitly unfinished content.
 *
 * <p>The main tab deliberately follows one stable inventory order: blocks,
 * tools grouped by their concrete type, armor grouped by slot, progression
 * materials, useful items and finally consumables.  This prevents a new item
 * from being inserted beside an unrelated item just because both share the
 * same material.</p>
 */
public final class ModItemGroups {
    public static final CreativeModeTab CHAOTIC_DIMENSIONS = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "chaotic_dimensions"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.chaoticd.chaotic_dimensions"))
            .icon(() -> new ItemStack(ModItems.SAPPHIRE_SWORD))
            .displayItems((parameters, entries) -> {
                addBlocks(entries);
                addReleasedSwords(entries);
                addReleasedPickaxes(entries);
                addReleasedAxes(entries);
                addReleasedShovels(entries);
                addReleasedHoes(entries);
                addArmorByPiece(entries);
                addMaterialsAndOres(entries);
                addUsefulItems(entries);
                addEnchantments(entries);
                addPotions(entries);
                addSpawnEggs(entries);
                addFood(entries);
            })
            .build()
    );

    /**
     * Separate laboratory tab for registered content that is not ready for
     * survival progression yet.  An item belongs here only while it is missing
     * a real source, a complete progression specification or is retained as a
     * legacy compatibility block.
     */
    public static final CreativeModeTab CHAOTIC_TEST = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "chaotic_test"),
        FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.chaoticd.chaotic_test"))
            .icon(() -> new ItemStack(ModItems.CHLOROPHYTE_PICKAXE))
            .displayItems((parameters, entries) -> addTestItems(entries))
            .build()
    );

    private ModItemGroups() {
    }

    private static void addBlocks(CreativeModeTab.Output entries) {
        // Terrain bases.
        entries.accept(ModBlocks.AURORA_GRASS_BLOCK);
        entries.accept(ModBlocks.AURORA_DIRT);
        entries.accept(ModBlocks.AURORA_STONE);
        entries.accept(ModBlocks.SHADOW_GRASS);
        entries.accept(ModBlocks.SHADOW_SOIL);
        entries.accept(ModBlocks.SHADOW_STONE);
        entries.accept(ModBlocks.CRYSTAL_GRASS_BLOCK);
        entries.accept(ModBlocks.CRYSTAL_DIRT);

        // Wood families stay together so every variant is easy to find.
        addWoodFamily(entries,
            ModBlocks.AURORA_PINKKO_LOG,
            ModBlocks.AURORA_PINKKO_WOOD,
            ModBlocks.STRIPPED_AURORA_PINKKO_LOG,
            ModBlocks.STRIPPED_AURORA_PINKKO_WOOD,
            ModBlocks.AURORA_PINKKO_PLANKS,
            ModBlocks.AURORA_PINKKO_LEAVES,
            ModBlocks.AURORA_PINKKO_SAPLING);
        addWoodFamily(entries,
            ModBlocks.AURORA_SOULESS_LOG,
            ModBlocks.AURORA_SOULESS_WOOD,
            ModBlocks.STRIPPED_AURORA_SOULESS_LOG,
            ModBlocks.STRIPPED_AURORA_SOULESS_WOOD,
            ModBlocks.AURORA_SOULESS_PLANKS,
            ModBlocks.AURORA_SOULESS_LEAVES,
            ModBlocks.AURORA_SOULESS_SAPLING);
        addWoodFamily(entries,
            ModBlocks.AURORA_SKY_LOG,
            ModBlocks.AURORA_SKY_WOOD,
            ModBlocks.STRIPPED_AURORA_SKY_LOG,
            ModBlocks.STRIPPED_AURORA_SKY_WOOD,
            ModBlocks.AURORA_SKY_PLANKS,
            ModBlocks.AURORA_SKY_LEAVES,
            ModBlocks.AURORA_SKY_SAPLING);
        addWoodFamily(entries,
            ModBlocks.SHADOW_LOG,
            ModBlocks.SHADOW_WOOD,
            ModBlocks.STRIPPED_SHADOW_LOG,
            ModBlocks.STRIPPED_SHADOW_WOOD,
            ModBlocks.SHADOW_PLANKS,
            ModBlocks.SHADOW_LEAVES,
            ModBlocks.SHADOW_SAPLING);
        addCrystalWood(entries);

        // Ore blocks, followed by their compact storage blocks.
        entries.accept(ModBlocks.TITANIUM_ORE);
        entries.accept(ModBlocks.DEEPSLATE_TITANIUM_ORE);
        entries.accept(ModBlocks.RUBY_ORE);
        entries.accept(ModBlocks.DEEPSLATE_RUBY_ORE);
        entries.accept(ModBlocks.JAX_ORE);
        entries.accept(ModBlocks.DEEPSLATE_JAXY_ORE);
        entries.accept(ModBlocks.ROSALITA_ORE);
        entries.accept(ModBlocks.DEEPSLATE_ROSALITA_ORE);
        entries.accept(ModBlocks.SAPPHIRE_ORE);
        entries.accept(ModBlocks.NETHER_RUBY_ORE);
        entries.accept(ModBlocks.NETHER_JAX_ORE);
        entries.accept(ModBlocks.NETHER_ROSALITA_ORE);
        entries.accept(ModBlocks.AURORA_RUBY_ORE);
        entries.accept(ModBlocks.AURORA_JAX_ORE);
        entries.accept(ModBlocks.AURORA_ROSALITA_ORE);
        entries.accept(ModBlocks.AURORA_SAPPHIRE_ORE);
        entries.accept(ModBlocks.TITANIUM_BLOCK);
        entries.accept(ModBlocks.RUBY_BLOCK);
        entries.accept(ModBlocks.JAXY_BLOCK);
        entries.accept(ModBlocks.ROSALITA_BLOCK);

        // Crafted and decorative blocks are last inside the block section.
        entries.accept(ModBlocks.CRYSTAL_RED_PLANT);
        entries.accept(ModBlocks.CRYSTAL_YELLOW_PLANT);
        entries.accept(ModBlocks.CRYSTAL_BLUE_PLANT);
        entries.accept(ModBlocks.CRYSTAL_GREEN_PLANT);
        entries.accept(ModBlocks.CRYSTAL_FURNACE);
        entries.accept(ModBlocks.CRYSTAL_CRAFTING_TABLE);
    }

    private static void addReleasedSwords(CreativeModeTab.Output entries) {
        entries.accept(ModItems.EMERALD_SWORD);
        entries.accept(ModItems.RUBY_SWORD);
        entries.accept(ModItems.JAXY_SWORD);
        entries.accept(ModItems.TITANIUM_SWORD);
        entries.accept(ModItems.ROSALITA_SWORD);
        entries.accept(ModItems.SAPPHIRE_SWORD);
    }

    private static void addReleasedPickaxes(CreativeModeTab.Output entries) {
        entries.accept(ModItems.EMERALD_PICKAXE);
        entries.accept(ModItems.RUBY_PICKAXE);
        entries.accept(ModItems.JAXY_PICKAXE);
        entries.accept(ModItems.TITANIUM_PICKAXE);
        entries.accept(ModItems.ROSALITA_PICKAXE);
        entries.accept(ModItems.SAPPHIRE_PICKAXE);
    }

    private static void addReleasedAxes(CreativeModeTab.Output entries) {
        entries.accept(ModItems.EMERALD_AXE);
        entries.accept(ModItems.RUBY_AXE);
        entries.accept(ModItems.JAXY_AXE);
        entries.accept(ModItems.TITANIUM_AXE);
        entries.accept(ModItems.ROSALITA_AXE);
        entries.accept(ModItems.SAPPHIRE_AXE);
    }

    private static void addReleasedShovels(CreativeModeTab.Output entries) {
        entries.accept(ModItems.EMERALD_SHOVEL);
        entries.accept(ModItems.RUBY_SHOVEL);
        entries.accept(ModItems.JAXY_SHOVEL);
        entries.accept(ModItems.TITANIUM_SHOVEL);
        entries.accept(ModItems.ROSALITA_SHOVEL);
        entries.accept(ModItems.SAPPHIRE_SHOVEL);
    }

    private static void addReleasedHoes(CreativeModeTab.Output entries) {
        entries.accept(ModItems.EMERALD_HOE);
        entries.accept(ModItems.RUBY_HOE);
        entries.accept(ModItems.JAXY_HOE);
        entries.accept(ModItems.TITANIUM_HOE);
        entries.accept(ModItems.ROSALITA_HOE);
        entries.accept(ModItems.SAPPHIRE_HOE);
    }

    private static void addArmorByPiece(CreativeModeTab.Output entries) {
        // Helmets.
        entries.accept(ModItems.EMERALD_HELMET);
        entries.accept(ModItems.RUBY_HELMET);
        entries.accept(ModItems.JAXY_HELMET);
        entries.accept(ModItems.TITANIUM_HELMET);
        entries.accept(ModItems.ROSALITA_HELMET);

        // Chestplates.
        entries.accept(ModItems.EMERALD_CHESTPLATE);
        entries.accept(ModItems.RUBY_CHESTPLATE);
        entries.accept(ModItems.JAXY_CHESTPLATE);
        entries.accept(ModItems.TITANIUM_CHESTPLATE);
        entries.accept(ModItems.ROSALITA_CHESTPLATE);

        // Leggings.
        entries.accept(ModItems.EMERALD_LEGGINGS);
        entries.accept(ModItems.RUBY_LEGGINGS);
        entries.accept(ModItems.JAXY_LEGGINGS);
        entries.accept(ModItems.TITANIUM_LEGGINGS);
        entries.accept(ModItems.ROSALITA_LEGGINGS);

        // Boots.
        entries.accept(ModItems.EMERALD_BOOTS);
        entries.accept(ModItems.RUBY_BOOTS);
        entries.accept(ModItems.JAXY_BOOTS);
        entries.accept(ModItems.TITANIUM_BOOTS);
        entries.accept(ModItems.ROSALITA_BOOTS);
    }

    private static void addMaterialsAndOres(CreativeModeTab.Output entries) {
        // Main progression materials.
        entries.accept(ModItems.EMERALD_INGOT);
        entries.accept(ModItems.RUBY_NUGGET);
        entries.accept(ModItems.RUBY);
        entries.accept(ModItems.RUBY_PLATE);
        entries.accept(ModItems.JAXY_GEM);
        entries.accept(ModItems.SOLAR_OBSIDIAN);
        entries.accept(ModItems.TITANIUM_INGOT);
        entries.accept(ModItems.TITAN_SOULD);
        entries.accept(ModItems.ROSALITA_GEM);
        entries.accept(ModItems.SAPPHIRE_GEM);

        // Souls intentionally remain consecutive instead of being scattered
        // among every material used by Sir. Orens.
        entries.accept(ModItems.AURORA_SOUL);
        entries.accept(ModItems.CRYSTALINE_SOUL);
        entries.accept(ModItems.DEMONIC_SOULD);
        entries.accept(ModItems.SHADOW_SOUL);
        entries.accept(ModItems.VOID_SOUL);
        entries.accept(ModItems.VORTEX_SOUL);

        // Existing quest and merchant materials.
        entries.accept(ModItems.WATER_INGOT);
        entries.accept(ModItems.LAVA_INGOT);
        entries.accept(ModItems.DEMONITH);
        entries.accept(ModItems.THE_CRYSTALINE);
        entries.accept(ModItems.THE_UNDERGUER);
        entries.accept(ModItems.THE_CALICE_QUEEN);
        entries.accept(ModItems.MONTHRA_SCALE);
        entries.accept(ModItems.CRYSTALINE_SIGIL);
        entries.accept(ModItems.UNDERGUER_SIGIL);
        entries.accept(ModItems.VOID);
    }

    private static void addUsefulItems(CreativeModeTab.Output entries) {
        entries.accept(ModItems.DEATH_TOTEM);
        entries.accept(ModItems.DREAM_FLUID_BUCKET);
        entries.accept(ModItems.CRYSTALINE_SEE);
        entries.accept(ModItems.CRYSTALINE_EYE);
        entries.accept(ModItems.AURORA_PEARL);
        entries.accept(ModItems.LEATHER_BACKPACK);
        entries.accept(ModItems.BEDROCK_STICK);
    }

    private static void addEnchantments(CreativeModeTab.Output entries) {
        addBooks(entries, ModEnchantments.SAPPHIRIC);
        addBooks(entries, ModEnchantments.BIG_BERTHA);
        addBooks(entries, ModEnchantments.ROYAL);
        addBooks(entries, ModEnchantments.DISPARADA);
        entries.accept(EnchantedBookItem.createForEnchantment(
            new EnchantmentInstance(ModEnchantments.DHEATHIC, 1)
        ));
    }

    private static void addBooks(
        CreativeModeTab.Output entries,
        net.minecraft.world.item.enchantment.Enchantment enchantment
    ) {
        for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
            entries.accept(EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(enchantment, level)
            ));
        }
    }

    private static void addPotions(CreativeModeTab.Output entries) {
        entries.accept(ModPotions.potion(Items.POTION));
        entries.accept(ModPotions.potion(Items.SPLASH_POTION));
        entries.accept(ModPotions.potion(Items.LINGERING_POTION));
        entries.accept(ModPotions.potion(Items.TIPPED_ARROW));
    }

    private static void addSpawnEggs(CreativeModeTab.Output entries) {
        entries.accept(ModItems.DIMENSION_PIG_SPAWN_EGG);
        entries.accept(ModItems.GOLD_DIMENSION_PIG_SPAWN_EGG);
        entries.accept(ModItems.APPLE_COW_SPAWN_EGG);
        entries.accept(ModItems.GOLDEN_APPLE_COW_SPAWN_EGG);
        entries.accept(ModItems.CRYSTAL_APPLE_COW_SPAWN_EGG);
        entries.accept(ModItems.CRYSTAL_GOLDEN_APPLE_SPAWN_EGG);
        entries.accept(ModItems.CRYSTAL_CREEPER_SPAWN_EGG);
    }

    private static void addFood(CreativeModeTab.Output entries) {
        entries.accept(ModItems.CHAOTIC_APPLE);
        entries.accept(ModItems.GOLD_SPECIAL_APPLE);
        entries.accept(ModItems.DIMENSION_APPLE);
    }

    private static void addTestItems(CreativeModeTab.Output entries) {
        // Legacy Aurora blocks stay accessible for old development worlds but
        // are not part of the current Aurora generation palette.
        entries.accept(ModBlocks.PASTEL_GRASS);
        entries.accept(ModBlocks.PASTEL_SOIL);
        entries.accept(ModBlocks.PASTEL_AURORA_STONE);
        entries.accept(ModBlocks.PASTEL_AURORA_LOG);
        entries.accept(ModBlocks.PASTEL_AURORA_WOOD);
        entries.accept(ModBlocks.STRIPPED_PASTEL_AURORA_LOG);
        entries.accept(ModBlocks.STRIPPED_PASTEL_AURORA_WOOD);
        entries.accept(ModBlocks.PASTEL_AURORA_PLANKS);
        entries.accept(ModBlocks.PASTEL_PINK_LEAVES);
        entries.accept(ModBlocks.PASTEL_PURPLE_LEAVES);
        entries.accept(ModBlocks.PASTEL_BLUE_LEAVES);

        // Registered future materials without a released survival source.
        entries.accept(ModItems.SHADOW_GEM);
        entries.accept(ModItems.SHADOW_NUGGET);
        entries.accept(ModItems.VYLAM_GEM);
        entries.accept(ModItems.CHLOROPHYTE_INGOT);
        entries.accept(ModItems.HERO_GEM);
        entries.accept(ModItems.DERMAN_GEM);
        entries.accept(ModItems.VORTEX_GEM);
        entries.accept(ModItems.SUN_TEAR);
        entries.accept(ModItems.SUN_PEAK);

        // Tools whose material chain is still not released in survival.
        entries.accept(ModItems.SHADOW_SWORD);
        entries.accept(ModItems.HERO_SWORD);
        entries.accept(ModItems.CHLOROPHYTE_PICKAXE);
        entries.accept(ModItems.SHADOW_PICKAXE);
        entries.accept(ModItems.HERO_PICKAXE);
        entries.accept(ModItems.HERO_AXE);
        entries.accept(ModItems.HERO_SHOVEL);
        entries.accept(ModItems.HERO_HOE);
    }

    private static void addWoodFamily(
        CreativeModeTab.Output entries,
        Block log,
        Block wood,
        Block strippedLog,
        Block strippedWood,
        Block planks,
        Block leaves,
        Block sapling
    ) {
        entries.accept(log);
        entries.accept(wood);
        entries.accept(strippedLog);
        entries.accept(strippedWood);
        entries.accept(planks);
        entries.accept(leaves);
        entries.accept(sapling);
    }

    private static void addCrystalWood(CreativeModeTab.Output entries) {
        entries.accept(ModBlocks.CRYSTAL_LOG);
        entries.accept(ModBlocks.CRYSTAL_PLANKS);
        entries.accept(ModBlocks.CRYSTAL_LEAVES_1);
        entries.accept(ModBlocks.CRYSTAL_LEAVES_2);
        entries.accept(ModBlocks.CRYSTAL_LEAVES_3);
    }

    public static void initialize() {
        // Static fields perform registry insertion.
    }
}
