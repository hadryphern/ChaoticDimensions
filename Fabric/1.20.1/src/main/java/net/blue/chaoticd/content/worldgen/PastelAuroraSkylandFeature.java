package net.blue.chaoticd.content.worldgen;

import com.mojang.serialization.Codec;
import net.blue.chaoticd.content.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Generates one large, silent floating island near the upper edge of the Overworld. */
public final class PastelAuroraSkylandFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockState SOIL = ModBlocks.PASTEL_SOIL.defaultBlockState();
    private static final BlockState GRASS = ModBlocks.PASTEL_GRASS.defaultBlockState();

    public PastelAuroraSkylandFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = 44 + random.nextInt(25);
        int top = origin.getY();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) continue;
                int surface = top - (int) Math.round(distance / radius * 7.0D);
                int depth = 7 + (int) Math.round((1.0D - distance / radius) * 20.0D);
                for (int y = surface - depth; y <= surface; y++) {
                    BlockPos pos = origin.offset(dx, y - top, dz);
                    level.setBlock(pos, y == surface ? GRASS : islandBlock(random), Block.UPDATE_CLIENTS);
                }
            }
        }

        int trees = 13 + random.nextInt(10);
        for (int index = 0; index < trees; index++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = random.nextInt(Math.max(8, radius - 9));
            int dx = (int) Math.round(Math.cos(angle) * distance);
            int dz = (int) Math.round(Math.sin(angle) * distance);
            int surface = top - (int) Math.round(Math.sqrt(dx * dx + dz * dz) / radius * 7.0D);
            placeTree(level, origin.offset(dx, surface - top + 1, dz), random);
        }

        // A source at the island edge becomes the visible waterfall/route from the world below.
        int waterfallX = random.nextBoolean() ? radius / 2 : -radius / 2;
        int waterfallZ = random.nextBoolean() ? radius / 3 : -radius / 3;
        int waterfallSurface = top - (int) Math.round(Math.sqrt(waterfallX * waterfallX + waterfallZ * waterfallZ) / radius * 7.0D);
        level.setBlock(origin.offset(waterfallX, waterfallSurface - top + 1, waterfallZ), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
        return true;
    }

    private static BlockState islandBlock(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 3) return ModBlocks.SAPPHIRE_ORE.defaultBlockState();
        if (roll < 6) return ModBlocks.ROSALITA_ORE.defaultBlockState();
        return SOIL;
    }

    private static void placeTree(WorldGenLevel level, BlockPos base, RandomSource random) {
        int trunkHeight = 5 + random.nextInt(4);
        for (int y = 0; y < trunkHeight; y++) {
            level.setBlock(base.above(y), ModBlocks.PASTEL_AURORA_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        BlockState leaves = switch (random.nextInt(3)) {
            case 0 -> ModBlocks.PASTEL_PINK_LEAVES.defaultBlockState();
            case 1 -> ModBlocks.PASTEL_PURPLE_LEAVES.defaultBlockState();
            default -> ModBlocks.PASTEL_BLUE_LEAVES.defaultBlockState();
        };
        BlockPos crown = base.above(trunkHeight - 2);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -2; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (dx * dx + dz * dz + dy * dy > 11 || random.nextFloat() < 0.12F) continue;
                    BlockPos pos = crown.offset(dx, dy, dz);
                    if (level.isEmptyBlock(pos)) level.setBlock(pos, leaves, Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
