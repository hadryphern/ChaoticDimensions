package net.blue.chaoticd.worldgen.water;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.blue.chaoticd.content.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/** Generates contained lakes, tiny pond islands and edge waterfalls in the Aurora Dimension. */
public final class AuroraWaterFeature extends Feature<AuroraWaterConfiguration> {
    private static final int WORLDGEN_FLAGS = 19;
    private static final double POND_RADIUS_RATIO = 0.72D;

    public AuroraWaterFeature(Codec<AuroraWaterConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AuroraWaterConfiguration> context) {
        return switch (context.config().mode()) {
            case SURFACE_LAKE -> placeSurfaceLake(context);
            case POND_ISLAND -> placePondIsland(context);
            case WATERFALL -> placeWaterfall(context);
        };
    }

    private static boolean placeSurfaceLake(
        FeaturePlaceContext<AuroraWaterConfiguration> context
    ) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        AuroraWaterConfiguration config = context.config();

        BlockPos surfaceAir = surfaceAir(
            level,
            context.origin().getX(),
            context.origin().getZ()
        );

        if (surfaceAir == null
            || !isAuroraTerrain(level.getBlockState(surfaceAir.below()))) {
            return false;
        }

        int radiusX = between(
            random,
            config.minRadius(),
            config.maxRadius()
        );

        int radiusZ = Math.max(
            config.minRadius(),
            radiusX - random.nextInt(3)
        );

        int maximumDepth = between(
            random,
            config.minDepth(),
            config.maxDepth()
        );

        int waterY = surfaceAir.getY() - 1;

        if (!canCarveLake(
            level,
            surfaceAir,
            radiusX,
            radiusZ,
            maximumDepth
        )) {
            return false;
        }

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                double distance = normalizedDistance(
                    dx,
                    dz,
                    radiusX,
                    radiusZ
                );

                int x = surfaceAir.getX() + dx;
                int z = surfaceAir.getZ() + dz;

                if (distance <= 0.76D) {
                    double centerFactor =
                        1.0D - distance / 0.76D;

                    int localDepth =
                        config.minDepth()
                            + (int) Math.round(
                                centerFactor
                                    * (maximumDepth - config.minDepth())
                            );

                    localDepth = Math.max(1, localDepth);
                    int floorY = waterY - localDepth;

                    setBlock(
                        level,
                        new BlockPos(x, floorY, z),
                        ModBlocks.AURORA_STONE.defaultBlockState()
                    );

                    setBlock(
                        level,
                        new BlockPos(x, floorY - 1, z),
                        ModBlocks.AURORA_STONE.defaultBlockState()
                    );

                    for (int y = floorY + 1; y <= waterY; y++) {
                        setWater(
                            level,
                            new BlockPos(x, y, z),
                            false
                        );
                    }

                    maybePlaceSeagrass(
                        level,
                        random,
                        new BlockPos(x, floorY, z),
                        waterY
                    );

                    for (int y = waterY + 1; y <= waterY + 2; y++) {
                        BlockPos clear = new BlockPos(x, y, z);

                        if (!level.getBlockState(clear).isAir()) {
                            setBlock(
                                level,
                                clear,
                                Blocks.AIR.defaultBlockState()
                            );
                        }
                    }
                } else if (distance <= 1.0D) {
                    BlockPos rim = new BlockPos(x, waterY, z);

                    setBlock(
                        level,
                        rim,
                        ModBlocks.AURORA_GRASS_BLOCK.defaultBlockState()
                    );

                    setBlock(
                        level,
                        rim.below(),
                        ModBlocks.AURORA_DIRT.defaultBlockState()
                    );
                }
            }
        }

        addLilyPads(
            level,
            random,
            surfaceAir,
            radiusX,
            radiusZ,
            waterY
        );

        return true;
    }

    private static boolean canCarveLake(
        WorldGenLevel level,
        BlockPos center,
        int radiusX,
        int radiusZ,
        int maximumDepth
    ) {
        int centerGroundY = center.getY() - 1;

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

                int localGroundY =
                    level.getHeight(
                        Heightmap.Types.WORLD_SURFACE_WG,
                        x,
                        z
                    ) - 1;

                if (Math.abs(localGroundY - centerGroundY) > 2) {
                    return false;
                }

                for (
                    int y = centerGroundY - maximumDepth - 2;
                    y <= centerGroundY + 2;
                    y++
                ) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!level.ensureCanWrite(pos)) {
                        return false;
                    }
                }

                if (distance <= 0.80D) {
                    BlockState deepSupport = level.getBlockState(
                        new BlockPos(
                            x,
                            centerGroundY - maximumDepth - 2,
                            z
                        )
                    );

                    if (!isAuroraTerrain(deepSupport)) {
                        return false;
                    }

                    for (
                        int y = centerGroundY - maximumDepth;
                        y <= centerGroundY + 1;
                        y++
                    ) {
                        if (!level.getFluidState(
                            new BlockPos(x, y, z)
                        ).isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private static boolean placePondIsland(
        FeaturePlaceContext<AuroraWaterConfiguration> context
    ) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        AuroraWaterConfiguration config = context.config();
        BlockPos center = context.origin();

        int radiusX = between(
            random,
            config.minRadius(),
            config.maxRadius()
        );

        int radiusZ = Math.max(
            config.minRadius(),
            radiusX - random.nextInt(3)
        );

        int maximumDepth = between(
            random,
            config.minDepth(),
            config.maxDepth()
        );

        if (!isClearForPondIsland(
            level,
            center,
            radiusX,
            radiusZ,
            maximumDepth
        )) {
            return false;
        }

        int waterY = center.getY();

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                double distance = normalizedDistance(
                    dx,
                    dz,
                    radiusX,
                    radiusZ
                );

                if (distance > 1.0D) {
                    continue;
                }

                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                int undersideDepth =
                    2 + (int) Math.round(
                        (1.0D - distance) * (maximumDepth + 2)
                    );

                int bottomY = waterY - undersideDepth;
                boolean pond = distance <= POND_RADIUS_RATIO;

                if (pond) {
                    double pondFactor =
                        1.0D - distance / POND_RADIUS_RATIO;

                    int pondDepth =
                        config.minDepth()
                            + (int) Math.round(
                                pondFactor
                                    * (maximumDepth - config.minDepth())
                            );

                    pondDepth = Math.max(1, pondDepth);
                    int floorY = waterY - pondDepth;

                    for (int y = bottomY; y <= floorY; y++) {
                        setBlock(
                            level,
                            new BlockPos(x, y, z),
                            ModBlocks.AURORA_STONE
                                .defaultBlockState()
                        );
                    }

                    for (int y = floorY + 1; y <= waterY; y++) {
                        setWater(
                            level,
                            new BlockPos(x, y, z),
                            false
                        );
                    }

                    maybePlaceSeagrass(
                        level,
                        random,
                        new BlockPos(x, floorY, z),
                        waterY
                    );
                } else {
                    for (
                        int y = bottomY;
                        y < waterY - 1;
                        y++
                    ) {
                        setBlock(
                            level,
                            new BlockPos(x, y, z),
                            ModBlocks.AURORA_STONE
                                .defaultBlockState()
                        );
                    }

                    setBlock(
                        level,
                        new BlockPos(x, waterY - 1, z),
                        ModBlocks.AURORA_DIRT.defaultBlockState()
                    );

                    setBlock(
                        level,
                        new BlockPos(x, waterY, z),
                        ModBlocks.AURORA_GRASS_BLOCK.defaultBlockState()
                    );
                }
            }
        }

        addLilyPads(
            level,
            random,
            center.above(),
            radiusX,
            radiusZ,
            waterY
        );

        return true;
    }

    private static boolean isClearForPondIsland(
        WorldGenLevel level,
        BlockPos center,
        int radiusX,
        int radiusZ,
        int maximumDepth
    ) {
        for (int dx = -radiusX - 2; dx <= radiusX + 2; dx++) {
            for (int dz = -radiusZ - 2; dz <= radiusZ + 2; dz++) {
                double distance = normalizedDistance(
                    dx,
                    dz,
                    radiusX + 2,
                    radiusZ + 2
                );

                if (distance > 1.0D) {
                    continue;
                }

                for (
                    int y = center.getY() - maximumDepth - 5;
                    y <= center.getY() + 3;
                    y++
                ) {
                    BlockPos pos = new BlockPos(
                        center.getX() + dx,
                        y,
                        center.getZ() + dz
                    );

                    if (!level.ensureCanWrite(pos)
                        || !level.getBlockState(pos).isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean placeWaterfall(
        FeaturePlaceContext<AuroraWaterConfiguration> context
    ) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        AuroraWaterConfiguration config = context.config();

        List<WaterfallCandidate> candidates = new ArrayList<>();

        int searchRadius = config.maxRadius();

        int requiredDrop = between(
            random,
            config.minDepth(),
            config.maxDepth()
        );

        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                int x = context.origin().getX() + dx;
                int z = context.origin().getZ() + dz;

                BlockPos surfaceAir = surfaceAir(level, x, z);

                if (surfaceAir == null
                    || !level.getFluidState(surfaceAir).isEmpty()) {
                    continue;
                }

                BlockPos basin = surfaceAir.below();

                if (!isAuroraTerrain(level.getBlockState(basin))) {
                    continue;
                }

                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos outflow = basin.relative(direction);

                    if (hasOpenDrop(
                        level,
                        outflow,
                        requiredDrop
                    ) && hasSolidWaterfallBanks(
                        level,
                        basin,
                        direction
                    )) {
                        candidates.add(
                            new WaterfallCandidate(
                                basin,
                                direction
                            )
                        );
                    }
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        WaterfallCandidate candidate =
            candidates.get(random.nextInt(candidates.size()));

        BlockPos optionalWideSource = null;

        Direction tangent =
            candidate.direction().getAxis() == Direction.Axis.X
                ? Direction.NORTH
                : Direction.EAST;

        if (random.nextFloat() < 0.35F) {
            Direction side = random.nextBoolean()
                ? tangent
                : tangent.getOpposite();

            BlockPos wideSource =
                candidate.source().relative(side);

            if (isAuroraTerrain(level.getBlockState(wideSource))
                && level.getBlockState(wideSource.above()).isAir()
                && hasOpenDrop(
                    level,
                    wideSource.relative(candidate.direction()),
                    requiredDrop
                )
                && hasSolidWaterfallBanks(
                    level,
                    wideSource,
                    candidate.direction()
                )) {
                optionalWideSource = wideSource;
            }
        }

        setWater(
            level,
            candidate.source(),
            true
        );

        if (optionalWideSource != null) {
            setWater(
                level,
                optionalWideSource,
                true
            );
        }

        return true;
    }

    private static boolean hasSolidWaterfallBanks(
        WorldGenLevel level,
        BlockPos source,
        Direction outflowDirection
    ) {
        Direction tangent =
            outflowDirection.getAxis() == Direction.Axis.X
                ? Direction.NORTH
                : Direction.EAST;

        return isAuroraTerrain(
            level.getBlockState(
                source.relative(outflowDirection.getOpposite())
            )
        )
            && isAuroraTerrain(
                level.getBlockState(source.relative(tangent))
            )
            && isAuroraTerrain(
                level.getBlockState(
                    source.relative(tangent.getOpposite())
                )
            )
            && level.getBlockState(source.above()).isAir();
    }

    private static boolean hasOpenDrop(
        WorldGenLevel level,
        BlockPos outside,
        int minimumDrop
    ) {
        for (int depth = 0; depth <= minimumDrop; depth++) {
            BlockPos pos = outside.below(depth);

            if (!level.ensureCanWrite(pos)
                || !level.getBlockState(pos).isAir()) {
                return false;
            }
        }

        return true;
    }

    private static void maybePlaceSeagrass(
        WorldGenLevel level,
        RandomSource random,
        BlockPos floor,
        int waterY
    ) {
        BlockPos plant = floor.above();

        if (waterY - floor.getY() >= 2
            && random.nextFloat() < 0.10F
            && level.getBlockState(plant).is(Blocks.WATER)
            && level.getFluidState(plant).isSource()) {
            setBlock(
                level,
                plant,
                Blocks.SEAGRASS.defaultBlockState()
            );
        }
    }

    private static void addLilyPads(
        WorldGenLevel level,
        RandomSource random,
        BlockPos center,
        int radiusX,
        int radiusZ,
        int waterY
    ) {
        int attempts = Math.max(
            2,
            (radiusX + radiusZ) / 2
        );

        for (int attempt = 0; attempt < attempts; attempt++) {
            int dx =
                random.nextInt(radiusX * 2 + 1) - radiusX;

            int dz =
                random.nextInt(radiusZ * 2 + 1) - radiusZ;

            if (normalizedDistance(
                dx,
                dz,
                radiusX,
                radiusZ
            ) > 0.50D) {
                continue;
            }

            BlockPos water = new BlockPos(
                center.getX() + dx,
                waterY,
                center.getZ() + dz
            );

            BlockPos pad = water.above();

            if (level.getFluidState(water).is(FluidTags.WATER)
                && level.getFluidState(water).isSource()
                && level.getBlockState(pad).isAir()
                && level.ensureCanWrite(pad)) {
                setBlock(
                    level,
                    pad,
                    Blocks.LILY_PAD.defaultBlockState()
                );
            }
        }
    }

    private static BlockPos surfaceAir(
        WorldGenLevel level,
        int x,
        int z
    ) {
        int y = level.getHeight(
            Heightmap.Types.WORLD_SURFACE_WG,
            x,
            z
        );

        if (y <= level.getMinBuildHeight() + 2
            || y >= level.getMaxBuildHeight() - 2) {
            return null;
        }

        return new BlockPos(x, y, z);
    }

    private static boolean isAuroraTerrain(BlockState state) {
        return state.is(ModBlocks.AURORA_GRASS_BLOCK)
            || state.is(ModBlocks.AURORA_DIRT)
            || state.is(ModBlocks.AURORA_STONE);
    }

    private static void setWater(
        WorldGenLevel level,
        BlockPos pos,
        boolean scheduleFlow
    ) {
        setBlock(
            level,
            pos,
            Blocks.WATER.defaultBlockState()
        );

        if (scheduleFlow) {
            level.scheduleTick(
                pos,
                Fluids.WATER,
                1
            );
        }
    }

    private static void setBlock(
        WorldGenLevel level,
        BlockPos pos,
        BlockState state
    ) {
        level.setBlock(
            pos,
            state,
            WORLDGEN_FLAGS
        );
    }

    private static int between(
        RandomSource random,
        int minimum,
        int maximum
    ) {
        return minimum
            + random.nextInt(maximum - minimum + 1);
    }

    private static double normalizedDistance(
        int dx,
        int dz,
        int radiusX,
        int radiusZ
    ) {
        double normalizedX =
            dx / (double) Math.max(1, radiusX);

        double normalizedZ =
            dz / (double) Math.max(1, radiusZ);

        return Math.sqrt(
            normalizedX * normalizedX
                + normalizedZ * normalizedZ
        );
    }

    private record WaterfallCandidate(
        BlockPos source,
        Direction direction
    ) {
    }
}
