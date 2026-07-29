package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.block.AuroraGrassBlock;
import net.blue.chaoticd.content.block.CrystalCraftingTableBlock;
import net.blue.chaoticd.content.block.CrystalFurnaceBlock;
import net.blue.chaoticd.content.block.DreamFluidBlock;
import net.blue.chaoticd.content.block.ShadowGrassBlock;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Blocks belonging to the Aurora and Shadow dimensions. */
public final class ModBlocks {
    public static final Block PASTEL_SOIL = register(
        "pastel_soil",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
            .mapColor(MapColor.SNOW)
            .sound(SoundType.GRAVEL)
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block PASTEL_GRASS = register(
        "pastel_grass",
        new AuroraGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
            .mapColor(MapColor.COLOR_PINK)
            .sound(SoundType.GRASS)
            .isValidSpawn((state, level, pos, type) -> false), PASTEL_SOIL)
    );

    public static final Block PASTEL_AURORA_STONE = register(
        "pastel_aurora_stone",
        new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
            .mapColor(MapColor.QUARTZ)
            .requiresCorrectToolForDrops()
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block PASTEL_AURORA_LOG = register(
        "pastel_aurora_log",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
            .mapColor(MapColor.COLOR_PINK))
    );

    public static final Block PASTEL_AURORA_WOOD = register(
        "pastel_aurora_wood",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)
            .mapColor(MapColor.COLOR_PINK))
    );

    public static final Block STRIPPED_PASTEL_AURORA_LOG = register(
        "stripped_pastel_aurora_log",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG)
            .mapColor(MapColor.COLOR_PINK))
    );

    public static final Block STRIPPED_PASTEL_AURORA_WOOD = register(
        "stripped_pastel_aurora_wood",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD)
            .mapColor(MapColor.COLOR_PINK))
    );

    public static final Block PASTEL_AURORA_PLANKS = register(
        "pastel_aurora_planks",
        new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
            .mapColor(MapColor.COLOR_PINK))
    );

    public static final Block PASTEL_PINK_LEAVES = register(
        "pastel_pink_leaves",
        leaves(MapColor.COLOR_PINK)
    );

    public static final Block PASTEL_PURPLE_LEAVES = register(
        "pastel_purple_leaves",
        leaves(MapColor.COLOR_PURPLE)
    );

    public static final Block PASTEL_BLUE_LEAVES = register(
        "pastel_blue_leaves",
        leaves(MapColor.COLOR_LIGHT_BLUE)
    );

    /*
     * Aurora's current wood palette.  The older pastel blocks stay registered
     * only so existing development worlds can still be opened; all new terrain
     * and trees use the explicit Aurora blocks below.
     */
    public static final Block AURORA_DIRT = register(
        "aurora_dirt",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
            .mapColor(MapColor.SNOW)
            .sound(SoundType.GRAVEL)
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block AURORA_GRASS_BLOCK = register(
        "aurora_grass_block",
        new AuroraGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
            .mapColor(MapColor.COLOR_PINK)
            .sound(SoundType.GRASS)
            .isValidSpawn((state, level, pos, type) -> false), AURORA_DIRT)
    );

    public static final Block AURORA_STONE = register(
        "aurora_stone",
        new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
            .mapColor(MapColor.QUARTZ)
            .requiresCorrectToolForDrops()
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block AURORA_PINKKO_LOG = auroraLog("aurora_pinkko_log", MapColor.COLOR_PINK);
    public static final Block AURORA_PINKKO_WOOD = auroraWood("aurora_pinkko_wood", MapColor.COLOR_PINK);
    public static final Block STRIPPED_AURORA_PINKKO_LOG = auroraLog(
        "stripped_aurora_pinkko_log", MapColor.COLOR_PINK);
    public static final Block STRIPPED_AURORA_PINKKO_WOOD = auroraWood(
        "stripped_aurora_pinkko_wood", MapColor.COLOR_PINK);
    public static final Block AURORA_PINKKO_PLANKS = auroraPlanks(
        "aurora_pinkko_planks", MapColor.COLOR_PINK);
    public static final Block AURORA_PINKKO_LEAVES = register(
        "aurora_pinkko_leaves", leaves(MapColor.COLOR_PINK));
    public static final Block AURORA_PINKKO_SAPLING = auroraSapling(
        "aurora_pinkko_sapling", MapColor.COLOR_PINK);

    public static final Block AURORA_SOULESS_LOG = auroraLog("aurora_souless_log", MapColor.COLOR_PURPLE);
    public static final Block AURORA_SOULESS_WOOD = auroraWood("aurora_souless_wood", MapColor.COLOR_PURPLE);
    public static final Block STRIPPED_AURORA_SOULESS_LOG = auroraLog(
        "stripped_aurora_souless_log", MapColor.COLOR_PURPLE);
    public static final Block STRIPPED_AURORA_SOULESS_WOOD = auroraWood(
        "stripped_aurora_souless_wood", MapColor.COLOR_PURPLE);
    public static final Block AURORA_SOULESS_PLANKS = auroraPlanks(
        "aurora_souless_planks", MapColor.COLOR_PURPLE);
    public static final Block AURORA_SOULESS_LEAVES = register(
        "aurora_souless_leaves", leaves(MapColor.COLOR_PURPLE));
    public static final Block AURORA_SOULESS_SAPLING = auroraSapling(
        "aurora_souless_sapling", MapColor.COLOR_PURPLE);

    public static final Block AURORA_SKY_LOG = auroraLog("aurora_sky_log", MapColor.COLOR_LIGHT_BLUE);
    public static final Block AURORA_SKY_WOOD = auroraWood("aurora_sky_wood", MapColor.COLOR_LIGHT_BLUE);
    public static final Block STRIPPED_AURORA_SKY_LOG = auroraLog(
        "stripped_aurora_sky_log", MapColor.COLOR_LIGHT_BLUE);
    public static final Block STRIPPED_AURORA_SKY_WOOD = auroraWood(
        "stripped_aurora_sky_wood", MapColor.COLOR_LIGHT_BLUE);
    public static final Block AURORA_SKY_PLANKS = auroraPlanks(
        "aurora_sky_planks", MapColor.COLOR_LIGHT_BLUE);
    public static final Block AURORA_SKY_LEAVES = register(
        "aurora_sky_leaves", leaves(MapColor.COLOR_LIGHT_BLUE));
    public static final Block AURORA_SKY_SAPLING = auroraSapling(
        "aurora_sky_sapling", MapColor.COLOR_LIGHT_BLUE);

    public static final Block SAPPHIRE_ORE = register(
        "sapphire_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                .mapColor(MapColor.QUARTZ)
                .requiresCorrectToolForDrops()
                .isValidSpawn((state, level, pos, type) -> false),
            UniformInt.of(3, 7)
        )
    );

    public static final Block ROSALITA_ORE = register(
        "rosalita_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.EMERALD_ORE)
                .mapColor(MapColor.QUARTZ)
                .requiresCorrectToolForDrops()
                .isValidSpawn((state, level, pos, type) -> false),
            UniformInt.of(3, 7)
        )
    );

    /** Jax has no supplied item texture yet, so its ores deliberately drop themselves. */
    public static final Block JAX_ORE = register(
        "jax_ore",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
            .mapColor(MapColor.COLOR_PURPLE)
            .requiresCorrectToolForDrops())
    );

    public static final Block NETHER_RUBY_ORE = register(
        "nether_ruby_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops(),
            UniformInt.of(4, 8)
        )
    );

    public static final Block NETHER_JAX_ORE = register(
        "nether_jax_ore",
        new Block(BlockBehaviour.Properties.copy(Blocks.NETHER_QUARTZ_ORE)
            .mapColor(MapColor.COLOR_PURPLE)
            .requiresCorrectToolForDrops())
    );

    public static final Block NETHER_ROSALITA_ORE = register(
        "nether_rosalita_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.NETHER_GOLD_ORE)
                .mapColor(MapColor.COLOR_PINK)
                .requiresCorrectToolForDrops(),
            UniformInt.of(5, 9)
        )
    );

    public static final Block AURORA_RUBY_ORE = register(
        "aurora_ruby_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops(),
            UniformInt.of(5, 10)
        )
    );

    public static final Block AURORA_JAX_ORE = register(
        "aurora_jax_ore",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
            .mapColor(MapColor.COLOR_PURPLE)
            .requiresCorrectToolForDrops())
    );

    public static final Block AURORA_ROSALITA_ORE = register(
        "aurora_rosalita_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                .mapColor(MapColor.COLOR_PINK)
                .requiresCorrectToolForDrops(),
            UniformInt.of(6, 11)
        )
    );

    public static final Block AURORA_SAPPHIRE_ORE = register(
        "aurora_sapphire_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .requiresCorrectToolForDrops(),
            UniformInt.of(6, 12)
        )
    );

    public static final Block SHADOW_SOIL = register(
        "shadow_soil",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
            .mapColor(MapColor.COLOR_BLACK)
            .sound(SoundType.GRAVEL)
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block SHADOW_GRASS = register(
        "shadow_grass",
        new ShadowGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
            .mapColor(MapColor.COLOR_BLACK)
            .sound(SoundType.GRASS)
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block SHADOW_STONE = register(
        "shadow_stone",
        new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
            .mapColor(MapColor.COLOR_BLACK)
            .requiresCorrectToolForDrops()
            .isValidSpawn((state, level, pos, type) -> false))
    );

    public static final Block SHADOW_LOG = register(
        "shadow_log",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_LOG)
            .mapColor(MapColor.COLOR_BLACK))
    );

    public static final Block SHADOW_WOOD = register(
        "shadow_wood",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_WOOD)
            .mapColor(MapColor.COLOR_BLACK))
    );

    public static final Block STRIPPED_SHADOW_LOG = register(
        "stripped_shadow_log",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_DARK_OAK_LOG)
            .mapColor(MapColor.COLOR_BLACK))
    );

    public static final Block STRIPPED_SHADOW_WOOD = register(
        "stripped_shadow_wood",
        new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_DARK_OAK_WOOD)
            .mapColor(MapColor.COLOR_BLACK))
    );

    public static final Block SHADOW_PLANKS = register(
        "shadow_planks",
        new Block(BlockBehaviour.Properties.copy(Blocks.DARK_OAK_PLANKS)
            .mapColor(MapColor.COLOR_BLACK))
    );

    public static final Block SHADOW_LEAVES = register(
        "shadow_leaves",
        leaves(MapColor.COLOR_BLACK)
    );

    /** Decorative Shadow sapling supplied with the current Shadow wood assets. */
    public static final Block SHADOW_SAPLING = register(
        "shadow_sapling",
        new Block(
            BlockBehaviour.Properties.copy(Blocks.DARK_OAK_SAPLING)
                .mapColor(MapColor.COLOR_BLACK)
                .noCollission()
                .instabreak()
        )
    );



    public static final Block RUBY_ORE = register(
        "ruby_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops(),
            UniformInt.of(7, 12)
        )
    );

    public static final Block RUBY_BLOCK = register(
        "ruby_block",
        new Block(
            BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
        )
    );

    public static final Block DEEPSLATE_RUBY_ORE = register(
        "deepslate_ruby_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_DIAMOND_ORE)
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops(),
            UniformInt.of(7, 12)
        )
    );

    public static final Block TITANIUM_ORE = register(
        "titanium_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops(),
            UniformInt.of(2, 5)
        )
    );

    public static final Block TITANIUM_BLOCK = register(
        "titanium_block",
        new Block(
            BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
        )
    );

    public static final Block DEEPSLATE_TITANIUM_ORE = register(
        "deepslate_titanium_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops(),
            UniformInt.of(2, 5)
        )
    );

    public static final Block DEEPSLATE_JAXY_ORE = register(
        "deepslate_jaxy_ore",
        new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_DIAMOND_ORE)
            .mapColor(MapColor.COLOR_PURPLE)
            .requiresCorrectToolForDrops())
    );

    public static final Block DEEPSLATE_ROSALITA_ORE = register(
        "deepslate_rosalita_ore",
        new DropExperienceBlock(
            BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_DIAMOND_ORE)
                .mapColor(MapColor.COLOR_PINK)
                .requiresCorrectToolForDrops(),
            UniformInt.of(6, 11)
        )
    );

    public static final Block JAXY_BLOCK = register(
        "jaxy_block",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
            .mapColor(MapColor.COLOR_PURPLE)
            .requiresCorrectToolForDrops())
    );

    public static final Block ROSALITA_BLOCK = register(
        "rosalita_block",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_BLOCK)
            .mapColor(MapColor.COLOR_PINK)
            .requiresCorrectToolForDrops())
    );

    public static final Block CRYSTAL_DIRT = register(
        "crystal_dirt",
        new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
            .mapColor(MapColor.COLOR_PURPLE))
    );

    public static final Block CRYSTAL_GRASS_BLOCK = register(
        "crystal_grass_block",
        new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
            .mapColor(MapColor.COLOR_PURPLE))
    );

    public static final Block CRYSTAL_LOG = register(
        "crystal_log",
        new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
            .mapColor(MapColor.COLOR_PURPLE))
    );

    public static final Block CRYSTAL_PLANKS = register(
        "crystal_planks",
        new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
            .mapColor(MapColor.COLOR_PURPLE))
    );

    public static final Block CRYSTAL_LEAVES_1 = register(
        "crystal_leaves_1",
        leaves(MapColor.COLOR_PURPLE)
    );

    public static final Block CRYSTAL_LEAVES_2 = register(
        "crystal_leaves_2",
        leaves(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final Block CRYSTAL_LEAVES_3 = register(
        "crystal_leaves_3",
        leaves(MapColor.COLOR_PINK)
    );

    public static final Block CRYSTAL_RED_PLANT = register(
        "crystal_red_plant",
        crystalFlower(MapColor.COLOR_RED)
    );

    public static final Block CRYSTAL_YELLOW_PLANT = register(
        "crystal_yellow_plant",
        crystalFlower(MapColor.COLOR_YELLOW)
    );

    public static final Block CRYSTAL_BLUE_PLANT = register(
        "crystal_blue_plant",
        crystalFlower(MapColor.COLOR_LIGHT_BLUE)
    );

    public static final Block CRYSTAL_GREEN_PLANT = register(
        "crystal_green_plant",
        crystalFlower(MapColor.COLOR_GREEN)
    );

    public static final Block CRYSTAL_FURNACE = register(
        "crystal_furnace",
        new CrystalFurnaceBlock(crystalFurnaceProperties())
    );

    public static final Block CRYSTAL_CRAFTING_TABLE = register(
        "crystal_crafting_table",
        new CrystalCraftingTableBlock(
            BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE)
                .mapColor(MapColor.COLOR_PURPLE)
        )
    );

    public static final Block DREAM_FLUID = registerBlockOnly(
        "dream_fluid",
        new DreamFluidBlock(
            ModFluids.DREAM_FLUID,
            BlockBehaviour.Properties.copy(Blocks.LAVA)
                .lightLevel(state -> 12)
        )
    );

    private ModBlocks() {
    }

    private static FlowerBlock crystalFlower(MapColor color) {
        return new FlowerBlock(
            MobEffects.GLOWING,
            5,
            BlockBehaviour.Properties.copy(Blocks.DANDELION).mapColor(color)
        );
    }

    /**
     * Furnace-equivalent behavior without vanilla's pickaxe-only drop gate.
     * Crystal harvesting is enforced centrally by {@code CrystalHarvestRules}.
     */
    private static BlockBehaviour.Properties crystalFurnaceProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(3.5F)
            .sound(SoundType.STONE)
            .lightLevel(state -> state.getValue(AbstractFurnaceBlock.LIT) ? 13 : 0);
    }

    private static LeavesBlock leaves(MapColor color) {
        return new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).mapColor(color));
    }

    private static Block auroraLog(String id, MapColor color) {
        return register(id, new RotatedPillarBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_LOG).mapColor(color)));
    }

    private static Block auroraWood(String id, MapColor color) {
        return register(id, new RotatedPillarBlock(
            BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).mapColor(color)));
    }

    private static Block auroraPlanks(String id, MapColor color) {
        return register(id, new Block(
            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(color)));
    }

    private static Block auroraSapling(String id, MapColor color) {
        /*
         * These are decorative saplings for now.  Aurora trees are generated
         * by the custom worldgen feature; a growable sapling will be added
         * together with its dedicated growth rules rather than pointing to a
         * vanilla tree by mistake.
         */
        return register(id, new Block(
            BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
                .mapColor(color)
                .noCollission()
                .instabreak()));
    }

    private static Block register(String id, Block block) {
        ResourceLocation key = new ResourceLocation(ChaoticDimensions.MOD_ID, id);
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties()));
        return block;
    }

    private static Block registerBlockOnly(String id, Block block) {
        ResourceLocation key = new ResourceLocation(ChaoticDimensions.MOD_ID, id);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void initialize() {
        StrippableBlockRegistry.register(PASTEL_AURORA_LOG, STRIPPED_PASTEL_AURORA_LOG);
        StrippableBlockRegistry.register(PASTEL_AURORA_WOOD, STRIPPED_PASTEL_AURORA_WOOD);
        StrippableBlockRegistry.register(AURORA_PINKKO_LOG, STRIPPED_AURORA_PINKKO_LOG);
        StrippableBlockRegistry.register(AURORA_PINKKO_WOOD, STRIPPED_AURORA_PINKKO_WOOD);
        StrippableBlockRegistry.register(AURORA_SOULESS_LOG, STRIPPED_AURORA_SOULESS_LOG);
        StrippableBlockRegistry.register(AURORA_SOULESS_WOOD, STRIPPED_AURORA_SOULESS_WOOD);
        StrippableBlockRegistry.register(AURORA_SKY_LOG, STRIPPED_AURORA_SKY_LOG);
        StrippableBlockRegistry.register(AURORA_SKY_WOOD, STRIPPED_AURORA_SKY_WOOD);
        StrippableBlockRegistry.register(SHADOW_LOG, STRIPPED_SHADOW_LOG);
        StrippableBlockRegistry.register(SHADOW_WOOD, STRIPPED_SHADOW_WOOD);

        FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();

        flammables.add(PASTEL_AURORA_LOG, 5, 5);
        flammables.add(PASTEL_AURORA_WOOD, 5, 5);
        flammables.add(STRIPPED_PASTEL_AURORA_LOG, 5, 5);
        flammables.add(STRIPPED_PASTEL_AURORA_WOOD, 5, 5);
        flammables.add(PASTEL_AURORA_PLANKS, 5, 20);
        flammables.add(PASTEL_PINK_LEAVES, 30, 60);
        flammables.add(PASTEL_PURPLE_LEAVES, 30, 60);
        flammables.add(PASTEL_BLUE_LEAVES, 30, 60);

        addAuroraWoodFlammability(flammables,
            AURORA_PINKKO_LOG, AURORA_PINKKO_WOOD, STRIPPED_AURORA_PINKKO_LOG, STRIPPED_AURORA_PINKKO_WOOD,
            AURORA_PINKKO_PLANKS,
            AURORA_PINKKO_LEAVES, AURORA_PINKKO_SAPLING);
        addAuroraWoodFlammability(flammables,
            AURORA_SOULESS_LOG, AURORA_SOULESS_WOOD, STRIPPED_AURORA_SOULESS_LOG, STRIPPED_AURORA_SOULESS_WOOD,
            AURORA_SOULESS_PLANKS,
            AURORA_SOULESS_LEAVES, AURORA_SOULESS_SAPLING);
        addAuroraWoodFlammability(flammables,
            AURORA_SKY_LOG, AURORA_SKY_WOOD, STRIPPED_AURORA_SKY_LOG, STRIPPED_AURORA_SKY_WOOD,
            AURORA_SKY_PLANKS,
            AURORA_SKY_LEAVES, AURORA_SKY_SAPLING);

        flammables.add(SHADOW_LOG, 5, 5);
        flammables.add(SHADOW_WOOD, 5, 5);
        flammables.add(STRIPPED_SHADOW_LOG, 5, 5);
        flammables.add(STRIPPED_SHADOW_WOOD, 5, 5);
        flammables.add(SHADOW_PLANKS, 5, 20);
        flammables.add(SHADOW_LEAVES, 30, 60);
        flammables.add(SHADOW_SAPLING, 60, 100);

        flammables.add(CRYSTAL_LOG, 5, 5);
        flammables.add(CRYSTAL_PLANKS, 5, 20);
        flammables.add(CRYSTAL_LEAVES_1, 30, 60);
        flammables.add(CRYSTAL_LEAVES_2, 30, 60);
        flammables.add(CRYSTAL_LEAVES_3, 30, 60);

        CompostingChanceRegistry.INSTANCE.add(PASTEL_PINK_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(PASTEL_PURPLE_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(PASTEL_BLUE_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_PINKKO_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_SOULESS_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_SKY_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_PINKKO_SAPLING, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_SOULESS_SAPLING, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(AURORA_SKY_SAPLING, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(SHADOW_SAPLING, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_LEAVES_1, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_LEAVES_2, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_LEAVES_3, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_RED_PLANT, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_YELLOW_PLANT, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_BLUE_PLANT, 0.65F);
        CompostingChanceRegistry.INSTANCE.add(CRYSTAL_GREEN_PLANT, 0.65F);
    }

    private static void addAuroraWoodFlammability(
        FlammableBlockRegistry flammables,
        Block log,
        Block wood,
        Block strippedLog,
        Block strippedWood,
        Block planks,
        Block leaves,
        Block sapling
    ) {
        flammables.add(log, 5, 5);
        flammables.add(wood, 5, 5);
        flammables.add(strippedLog, 5, 5);
        flammables.add(strippedWood, 5, 5);
        flammables.add(planks, 5, 20);
        flammables.add(leaves, 30, 60);
        flammables.add(sapling, 60, 100);
    }
}
