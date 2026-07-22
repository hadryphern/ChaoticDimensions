package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.block.AuroraGrassBlock;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/** Blocks exclusive to the high-altitude Pastel Aurora Skylands. */
public final class ModBlocks {
    public static final Block PASTEL_SOIL = register("pastel_soil", new Block(BlockBehaviour.Properties.copy(Blocks.DIRT)
        .mapColor(MapColor.SNOW).sound(SoundType.GRAVEL).isValidSpawn((state, level, pos, type) -> false)));
    public static final Block PASTEL_GRASS = register("pastel_grass", new AuroraGrassBlock(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
        .mapColor(MapColor.COLOR_PINK).sound(SoundType.GRASS).isValidSpawn((state, level, pos, type) -> false)));
    public static final Block PASTEL_AURORA_STONE = register("pastel_aurora_stone", new Block(BlockBehaviour.Properties.copy(Blocks.STONE)
        .mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false)));
    public static final Block PASTEL_AURORA_LOG = register("pastel_aurora_log", new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
        .mapColor(MapColor.COLOR_PINK)));
    public static final Block PASTEL_AURORA_WOOD = register("pastel_aurora_wood", new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD)
        .mapColor(MapColor.COLOR_PINK)));
    public static final Block STRIPPED_PASTEL_AURORA_LOG = register("stripped_pastel_aurora_log", new RotatedPillarBlock(
        BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).mapColor(MapColor.COLOR_PINK)));
    public static final Block STRIPPED_PASTEL_AURORA_WOOD = register("stripped_pastel_aurora_wood", new RotatedPillarBlock(
        BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD).mapColor(MapColor.COLOR_PINK)));
    public static final Block PASTEL_AURORA_PLANKS = register("pastel_aurora_planks", new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
        .mapColor(MapColor.COLOR_PINK)));
    public static final Block PASTEL_PINK_LEAVES = register("pastel_pink_leaves", leaves(MapColor.COLOR_PINK));
    public static final Block PASTEL_PURPLE_LEAVES = register("pastel_purple_leaves", leaves(MapColor.COLOR_PURPLE));
    public static final Block PASTEL_BLUE_LEAVES = register("pastel_blue_leaves", leaves(MapColor.COLOR_LIGHT_BLUE));
    public static final Block SAPPHIRE_ORE = register("sapphire_ore", new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
        .mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false), UniformInt.of(3, 7)));
    public static final Block ROSALITA_ORE = register("rosalita_ore", new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.EMERALD_ORE)
        .mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false), UniformInt.of(3, 7)));

    private ModBlocks() {
    }

    private static LeavesBlock leaves(MapColor color) {
        return new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).mapColor(color));
    }

    private static Block register(String id, Block block) {
        ResourceLocation key = new ResourceLocation(ChaoticDimensions.MOD_ID, id);
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        Registry.register(BuiltInRegistries.ITEM, key, new BlockItem(block, new Item.Properties()));
        return block;
    }

    public static void initialize() {
        StrippableBlockRegistry.register(PASTEL_AURORA_LOG, STRIPPED_PASTEL_AURORA_LOG);
        StrippableBlockRegistry.register(PASTEL_AURORA_WOOD, STRIPPED_PASTEL_AURORA_WOOD);

        FlammableBlockRegistry flammables = FlammableBlockRegistry.getDefaultInstance();
        flammables.add(PASTEL_AURORA_LOG, 5, 5);
        flammables.add(PASTEL_AURORA_WOOD, 5, 5);
        flammables.add(STRIPPED_PASTEL_AURORA_LOG, 5, 5);
        flammables.add(STRIPPED_PASTEL_AURORA_WOOD, 5, 5);
        flammables.add(PASTEL_AURORA_PLANKS, 5, 20);
        flammables.add(PASTEL_PINK_LEAVES, 30, 60);
        flammables.add(PASTEL_PURPLE_LEAVES, 30, 60);
        flammables.add(PASTEL_BLUE_LEAVES, 30, 60);

        CompostingChanceRegistry.INSTANCE.add(PASTEL_PINK_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(PASTEL_PURPLE_LEAVES, 0.3F);
        CompostingChanceRegistry.INSTANCE.add(PASTEL_BLUE_LEAVES, 0.3F);
    }

}
