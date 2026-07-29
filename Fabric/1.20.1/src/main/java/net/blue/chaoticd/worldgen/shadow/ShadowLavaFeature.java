package net.blue.chaoticd.worldgen.shadow;

import com.mojang.serialization.Codec;
import net.blue.chaoticd.content.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/** Carves rare, contained lava pools into sufficiently thick Shadow terrain. */
public final class ShadowLavaFeature extends Feature<ShadowLavaConfiguration> {
    private static final int WORLDGEN_FLAGS = 19;

    public ShadowLavaFeature(Codec<ShadowLavaConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ShadowLavaConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ShadowLavaConfiguration config = context.config();

        int centerX = context.origin().getX();
        int centerZ = context.origin().getZ();
        int surfaceY = level.getHeight(
            Heightmap.Types.WORLD_SURFACE_WG,
            centerX,
            centerZ
        ) - 1;

        BlockPos surface = new BlockPos(centerX, surfaceY, centerZ);

        if (!isShadowTerrain(level.getBlockState(surface))) {
            return false;
        }

        int radiusX = between(random, config.minRadius(), config.maxRadius());
        int radiusZ = Math.max(config.minRadius(), radiusX - random.nextInt(3));
        int maximumDepth = between(random, config.minDepth(), config.maxDepth());

        if (!canCarve(level, surface, radiusX, radiusZ, maximumDepth)) {
            return false;
        }

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                double distance = normalizedDistance(dx, dz, radiusX, radiusZ);

                if (distance > 1.0D) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

                if (distance <= 0.72D) {
                    double centerFactor = 1.0D - distance / 0.72D;
                    int localDepth = config.minDepth()
                        + (int)Math.round(
                            centerFactor * (maximumDepth - config.minDepth())
                        );

                    localDepth = Math.max(1, localDepth);
                    int floorY = surfaceY - localDepth;

                    setBlock(
                        level,
                        new BlockPos(x, floorY, z),
                        ModBlocks.SHADOW_STONE.defaultBlockState()
                    );

                    for (int y = floorY + 1; y <= surfaceY; y++) {
                        BlockPos lavaPos = new BlockPos(x, y, z);
                        setBlock(level, lavaPos, Blocks.LAVA.defaultBlockState());
                        level.scheduleTick(lavaPos, Fluids.LAVA, 1);
                    }

                    BlockPos above = new BlockPos(x, surfaceY + 1, z);
                    if (!level.getBlockState(above).isAir()) {
                        setBlock(level, above, Blocks.AIR.defaultBlockState());
                    }
                } else {
                    setBlock(
                        level,
                        new BlockPos(x, surfaceY, z),
                        ModBlocks.SHADOW_STONE.defaultBlockState()
                    );
                }
            }
        }

        return true;
    }

    private static boolean canCarve(
        WorldGenLevel level,
        BlockPos center,
        int radiusX,
        int radiusZ,
        int depth
    ) {
        for (int dx = -radiusX - 1; dx <= radiusX + 1; dx++) {
            for (int dz = -radiusZ - 1; dz <= radiusZ + 1; dz++) {
                double distance = normalizedDistance(
                    dx,
                    dz,
                    radiusX + 1,
                    radiusZ + 1
                );

                if (distance > 1.0D) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int localSurfaceY = level.getHeight(
                    Heightmap.Types.WORLD_SURFACE_WG,
                    x,
                    z
                ) - 1;

                if (Math.abs(localSurfaceY - center.getY()) > 2) {
                    return false;
                }

                for (int y = center.getY() - depth - 2; y <= center.getY(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!level.ensureCanWrite(pos)
                        || !isShadowTerrain(level.getBlockState(pos))
                        || !level.getFluidState(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean isShadowTerrain(BlockState state) {
        return state.is(ModBlocks.SHADOW_GRASS)
            || state.is(ModBlocks.SHADOW_SOIL)
            || state.is(ModBlocks.SHADOW_STONE);
    }

    private static void setBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, WORLDGEN_FLAGS);
    }

    private static int between(RandomSource random, int minimum, int maximum) {
        return minimum + random.nextInt(maximum - minimum + 1);
    }

    private static double normalizedDistance(
        int dx,
        int dz,
        int radiusX,
        int radiusZ
    ) {
        double normalizedX = dx / (double)Math.max(1, radiusX);
        double normalizedZ = dz / (double)Math.max(1, radiusZ);
        return Math.sqrt(
            normalizedX * normalizedX
                + normalizedZ * normalizedZ
        );
    }
}
