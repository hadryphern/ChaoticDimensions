package net.blue.chaoticd.worldgen.feature;

import net.blue.chaoticd.content.block.ModBlocks;
import net.blue.chaoticd.worldgen.RosalitaBiomeArea;
import net.blue.chaoticd.worldgen.RosalitaConstants;
import net.blue.chaoticd.worldgen.tag.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A one-pass chunk-generation feature that recolors naturally generated
 * underground material under Rosalita surface columns. It never runs during
 * normal gameplay, so existing chunks and player-built blocks are untouched.
 */
public final class RosalitaUndergroundFeature extends Feature<NoneFeatureConfiguration> {
    public RosalitaUndergroundFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        boolean replacedAny = false;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int x = minX + localX;
                int z = minZ + localZ;
                if (!RosalitaBiomeArea.isInsideRosalitaSurfaceColumn(level, x, z)) {
                    continue;
                }

                int topY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                int maxY = Math.min(topY, level.getMaxBuildHeight() - 1);
                for (int y = level.getMinBuildHeight(); y <= maxY; y++) {
                    BlockPos position = new BlockPos(x, y, z);
                    BlockState current = level.getBlockState(position);
                    BlockState replacement = replacementFor(current, x, y, z);
                    if (replacement != null) {
                        level.setBlock(position, replacement, Block.UPDATE_CLIENTS);
                        replacedAny = true;
                    }
                }
            }
        }

        return replacedAny;
    }

    private static BlockState replacementFor(BlockState state, int x, int y, int z) {
        if (state.is(ModBlockTags.ALLOWED_ROSALITA_ORES)) {
            return null;
        }
        if (state.is(ModBlockTags.FORBIDDEN_IN_ROSALITA_UNDERGROUND)) {
            return baseStone(y);
        }
        if (state.is(Blocks.GRANITE)) {
            return ModBlocks.get("rosalita_granite").defaultBlockState();
        }
        if (state.is(Blocks.DIORITE)) {
            return ModBlocks.get("rosalita_diorite").defaultBlockState();
        }
        if (state.is(Blocks.ANDESITE)) {
            return ModBlocks.get("rosalita_andesite").defaultBlockState();
        }
        if (state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE)) {
            return ModBlocks.get("rosalita_sandstone").defaultBlockState();
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)) {
            return distributedStone(x, y, z);
        }
        return null;
    }

    private static BlockState distributedStone(int x, int y, int z) {
        int roll = Math.floorMod(mix(x, y, z), 100);
        if (roll < RosalitaConstants.ROSALINE_PERCENT) {
            return ModBlocks.get("rosaline_stone").defaultBlockState();
        }
        if (roll < RosalitaConstants.ROSALINE_PERCENT + RosalitaConstants.GRANITE_PERCENT) {
            return ModBlocks.get("rosalita_granite").defaultBlockState();
        }
        if (roll < RosalitaConstants.ROSALINE_PERCENT + RosalitaConstants.GRANITE_PERCENT
            + RosalitaConstants.DIORITE_PERCENT) {
            return ModBlocks.get("rosalita_diorite").defaultBlockState();
        }
        if (roll < RosalitaConstants.ROSALINE_PERCENT + RosalitaConstants.GRANITE_PERCENT
            + RosalitaConstants.DIORITE_PERCENT + RosalitaConstants.ANDESITE_PERCENT) {
            return ModBlocks.get("rosalita_andesite").defaultBlockState();
        }
        return baseStone(y);
    }

    private static BlockState baseStone(int y) {
        return ModBlocks.get(y <= RosalitaConstants.DEEP_STONE_Y ? "deep_rosalita_stone" : "rosalita_stone")
            .defaultBlockState();
    }

    private static int mix(int x, int y, int z) {
        int value = x * 734_287 ^ y * 912_931 ^ z * 438_289;
        value ^= value >>> 13;
        return value * 1_274_126_177;
    }
}
