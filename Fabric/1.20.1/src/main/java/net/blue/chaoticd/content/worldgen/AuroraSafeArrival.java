package net.blue.chaoticd.content.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/** Finds a safe landing area on ordinary Aurora terrain without creating or changing blocks. */
public final class AuroraSafeArrival {
    private static final int SEARCH_STEP = 24;
    /*
     * Arrival is invoked from an item-use/server-tick path, so it must never
     * synchronously generate an unbounded ring of Aurora chunks.  The hard
     * radius permits a broad search, while the budgets below cap expensive
     * generator and real-chunk checks on sparse seeds.
     */
    private static final int MAX_SEARCH_RADIUS = 768;
    private static final int MAX_CANDIDATE_INSPECTIONS = 512;
    private static final int MAX_CHUNK_VALIDATIONS = 128;
    private static final int SAFE_FOOTPRINT_RADIUS = 7;
    private static final int MAX_SURFACE_VARIATION = 5;
    private static final int MIN_ARRIVAL_Y = 80;

    private AuroraSafeArrival() {
    }

    public static Optional<BlockPos> find(ServerLevel level) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        Map<Long, Integer> heightCache = new HashMap<>();
        SearchBudget budget = new SearchBudget(MAX_CANDIDATE_INSPECTIONS, MAX_CHUNK_VALIDATIONS);

        Optional<BlockPos> origin = inspect(level, generator, randomState, heightCache, budget, 0, 0);
        if (origin.isPresent()) {
            return origin;
        }
        if (budget.exhausted()) {
            return Optional.empty();
        }

        for (int radius = SEARCH_STEP; radius <= MAX_SEARCH_RADIUS; radius += SEARCH_STEP) {
            for (int offset = -radius; offset <= radius; offset += SEARCH_STEP) {
                Optional<BlockPos> candidate = inspect(level, generator, randomState, heightCache, budget,
                    -radius, offset);
                if (candidate.isPresent()) return candidate;
                if (budget.exhausted()) return Optional.empty();

                candidate = inspect(level, generator, randomState, heightCache, budget, radius, offset);
                if (candidate.isPresent()) return candidate;
                if (budget.exhausted()) return Optional.empty();
            }
            for (int offset = -radius + SEARCH_STEP; offset < radius; offset += SEARCH_STEP) {
                Optional<BlockPos> candidate = inspect(level, generator, randomState, heightCache, budget,
                    offset, -radius);
                if (candidate.isPresent()) return candidate;
                if (budget.exhausted()) return Optional.empty();

                candidate = inspect(level, generator, randomState, heightCache, budget, offset, radius);
                if (candidate.isPresent()) return candidate;
                if (budget.exhausted()) return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static Optional<BlockPos> inspect(ServerLevel level, ChunkGenerator generator, RandomState randomState,
                                               Map<Long, Integer> heightCache, SearchBudget budget, int x, int z) {
        /*
         * getBaseHeight evaluates the noise router.  Bound those generator
         * samples too: restricting only getChunk would still allow thousands
         * of synchronous density evaluations on a hostile/sparse seed.
         */
        if (!budget.tryConsumeCandidateInspection()) {
            return Optional.empty();
        }
        int centerHeight = height(generator, level, randomState, heightCache, x, z);
        if (!isUsableHeight(level, centerHeight)) {
            return Optional.empty();
        }

        int minimum = centerHeight;
        int maximum = centerHeight;
        int radius = SAFE_FOOTPRINT_RADIUS;
        int[][] samples = {
            {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius},
            {radius, radius}, {radius, -radius}, {-radius, radius}, {-radius, -radius}
        };
        for (int[] sample : samples) {
            int sampledHeight = height(generator, level, randomState, heightCache, x + sample[0], z + sample[1]);
            if (!isUsableHeight(level, sampledHeight)) {
                return Optional.empty();
            }
            minimum = Math.min(minimum, sampledHeight);
            maximum = Math.max(maximum, sampledHeight);
        }
        if (maximum - minimum > MAX_SURFACE_VARIATION) {
            return Optional.empty();
        }

        if (!budget.tryConsumeChunkValidation()) {
            return Optional.empty();
        }
        level.getChunk(x >> 4, z >> 4);
        int feetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos feet = new BlockPos(x, feetY, z);
        BlockPos floor = feet.below();
        BlockState floorState = level.getBlockState(floor);
        if (feetY < MIN_ARRIVAL_Y
            || feetY >= level.getMaxBuildHeight() - 2
            || floorState.isAir()
            || !level.getFluidState(floor).isEmpty()
            || floorState.getCollisionShape(level, floor).isEmpty()
            || !level.getBlockState(feet).isAir()
            || !level.getBlockState(feet.above()).isAir()) {
            return Optional.empty();
        }
        return Optional.of(feet);
    }

    private static int height(ChunkGenerator generator, ServerLevel level, RandomState randomState,
                              Map<Long, Integer> cache, int x, int z) {
        long key = BlockPos.asLong(x, 0, z);
        return cache.computeIfAbsent(key, ignored -> generator.getBaseHeight(
            x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState));
    }

    private static boolean isUsableHeight(ServerLevel level, int height) {
        return height >= MIN_ARRIVAL_Y && height < level.getMaxBuildHeight() - 2;
    }

    /** Bounds both generator sampling and synchronous chunk materialization. */
    private static final class SearchBudget {
        private int remainingCandidateInspections;
        private int remainingChunkValidations;

        private SearchBudget(int maximumCandidateInspections, int maximumChunkValidations) {
            remainingCandidateInspections = maximumCandidateInspections;
            remainingChunkValidations = maximumChunkValidations;
        }

        private boolean tryConsumeCandidateInspection() {
            if (remainingCandidateInspections <= 0) {
                return false;
            }
            remainingCandidateInspections--;
            return true;
        }

        private boolean tryConsumeChunkValidation() {
            if (remainingChunkValidations <= 0) {
                return false;
            }
            remainingChunkValidations--;
            return true;
        }

        private boolean exhausted() {
            return remainingCandidateInspections <= 0 || remainingChunkValidations <= 0;
        }
    }
}
