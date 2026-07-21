package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    public static final Block PASTEL_GRASS = register("pastel_grass", new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK)
        .mapColor(MapColor.COLOR_PINK).sound(SoundType.GRASS).isValidSpawn((state, level, pos, type) -> false)));
    public static final Block PASTEL_AURORA_LOG = register("pastel_aurora_log", new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
        .mapColor(MapColor.COLOR_PINK)));
    public static final Block PASTEL_AURORA_PLANKS = register("pastel_aurora_planks", new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
        .mapColor(MapColor.COLOR_PINK)));
    public static final Block PASTEL_PINK_LEAVES = register("pastel_pink_leaves", leaves(MapColor.COLOR_PINK));
    public static final Block PASTEL_PURPLE_LEAVES = register("pastel_purple_leaves", leaves(MapColor.COLOR_PURPLE));
    public static final Block PASTEL_BLUE_LEAVES = register("pastel_blue_leaves", leaves(MapColor.COLOR_LIGHT_BLUE));
    public static final Block SAPPHIRE_ORE = register("sapphire_ore", new Block(BlockBehaviour.Properties.copy(Blocks.DIAMOND_ORE)
        .mapColor(MapColor.SNOW).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false)));
    public static final Block ROSALITA_ORE = register("rosalita_ore", new Block(BlockBehaviour.Properties.copy(Blocks.EMERALD_ORE)
        .mapColor(MapColor.SNOW).requiresCorrectToolForDrops().isValidSpawn((state, level, pos, type) -> false)));

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
        // Static registration above.
    }

}
