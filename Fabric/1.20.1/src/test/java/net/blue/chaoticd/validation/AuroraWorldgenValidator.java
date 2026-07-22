package net.blue.chaoticd.validation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.imageio.ImageIO;
import net.blue.chaoticd.content.ModBlocks;
import net.blue.chaoticd.worldgen.ModWorldgenFeatures;
import net.minecraft.SharedConstants;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

/** Headless acceptance test for the Aurora dimension's registries and terrain distribution. */
public final class AuroraWorldgenValidator {
    private static final ResourceLocation AURORA_ID = new ResourceLocation("chaoticd", "aurora_dimension");
    private static final ResourceLocation SETTINGS_ID = new ResourceLocation("chaoticd", "aurora_archipelago");
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);
    private static final long[] SEEDS = {0x4A617661L, 0x4175726F7261L, -0x4368616F746963L};
    private static final int REGION_HALF_SIZE = 1024;
    private static final int SAMPLE_STEP = 32;
    private static final int WINDOW_RADIUS = 4;
    private static final int WINDOW_STRIDE = 4;
    private static final int ALMOST_CONNECTED_GAP = 48;
    private static final int NEARBY_ISLAND_GAP = 128;
    private static final int ISOLATED_ISLAND_GAP = 224;
    private static final int SMALL_ISLAND_MAX_SAMPLES = 12;
    private static final SampleRegion[] SAMPLE_REGIONS = {
        new SampleRegion("spawn", 0, 0),
        new SampleRegion("+1000", 1000, 1000),
        new SampleRegion("-1000", -1000, -1000),
        new SampleRegion("+5000", 5000, 5000),
        new SampleRegion("-5000", -5000, -5000)
    };

    private AuroraWorldgenValidator() {
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        reopenIntrusiveRegistry(BuiltInRegistries.BLOCK);
        reopenIntrusiveRegistry(BuiltInRegistries.ITEM);
        reopenRegistry(BuiltInRegistries.FEATURE);
        require(ModBlocks.PASTEL_AURORA_STONE, "Aurora blocks were not initialized");
        ModWorldgenFeatures.initialize();
        BuiltInRegistries.BLOCK.freeze();
        BuiltInRegistries.ITEM.freeze();
        BuiltInRegistries.FEATURE.freeze();

        VanillaPackResources vanilla = new VanillaPackResourcesBuilder()
            .exposeNamespace("minecraft")
            .pushJarResources()
            .build();
        PackResources mod = new PathPackResources("chaoticd-validation", Path.of("build/resources/main"), false);

        try (MultiPackResourceManager resources = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(vanilla, mod))) {
            LayeredRegistryAccess<RegistryLayer> layers = RegistryLayer.createRegistryAccess();
            RegistryAccess.Frozen worldgen = RegistryDataLoader.load(
                resources, layers.getAccessForLoading(RegistryLayer.WORLDGEN), RegistryDataLoader.WORLDGEN_REGISTRIES);
            layers = layers.replaceFrom(RegistryLayer.WORLDGEN, worldgen);
            RegistryAccess.Frozen dimensions = RegistryDataLoader.load(
                resources, layers.getAccessForLoading(RegistryLayer.DIMENSIONS), RegistryDataLoader.DIMENSION_REGISTRIES);

            Registry<LevelStem> stemRegistry = dimensions.registryOrThrow(Registries.LEVEL_STEM);
            LevelStem stem = require(stemRegistry.get(AURORA_ID), "Aurora LevelStem was not loaded");
            check(stem.generator() instanceof NoiseBasedChunkGenerator, "Aurora is not using minecraft:noise");
            NoiseBasedChunkGenerator generator = (NoiseBasedChunkGenerator)stem.generator();

            Registry<NoiseGeneratorSettings> settingsRegistry = worldgen.registryOrThrow(Registries.NOISE_SETTINGS);
            NoiseGeneratorSettings settings = require(settingsRegistry.get(SETTINGS_ID), "Aurora noise settings were not loaded");
            check(settings.defaultBlock().is(ModBlocks.PASTEL_AURORA_STONE), "Unexpected default terrain block");
            check(settings.defaultFluid().isAir(), "Aurora default fluid must be air");
            check(settings.noiseSettings().minY() == -64 && settings.noiseSettings().height() == 384,
                "Noise height does not match the dimension type");

            List<Long> signatures = new ArrayList<>();
            boolean render = List.of(args).contains("--render");
            for (int seedIndex = 0; seedIndex < SEEDS.length; seedIndex++) {
                long seed = SEEDS[seedIndex];
                RandomState randomState = RandomState.create(settings, worldgen.lookupOrThrow(Registries.NOISE), seed);
                long determinismProbe = probe(generator, randomState);
                RandomState reloadedState = RandomState.create(settings, worldgen.lookupOrThrow(Registries.NOISE), seed);
                check(determinismProbe == probe(generator, reloadedState),
                    "Reloading the same seed changed terrain for seed " + seed);
                List<RegionStats> regions = new ArrayList<>();
                long signature = 0x6a09e667f3bcc909L;
                for (SampleRegion sample : SAMPLE_REGIONS) {
                    RegionStats stats = scan(generator, randomState, sample.centerX, sample.centerZ);
                    validateRegion(sample.name, seed, stats);
                    regions.add(stats);
                    signature = Long.rotateLeft(signature ^ stats.signature, 13) * 0x9e3779b97f4a7c15L;
                    System.out.printf("AURORA seed=%d region=%s center=(%d,%d) %s%n",
                        seed, sample.name, sample.centerX, sample.centerZ, stats);
                }
                SafeSite safeSite = findSafeSite(generator, randomState);

                validateDistribution(seed, regions);
                check(safeSite != null, "No wide safe arrival surface within 2048 blocks for seed " + seed);
                check(regions.stream().allMatch(region -> region.floorBlocks == 0),
                    "A continuous lower floor was detected for seed " + seed);

                signatures.add(signature);
                System.out.printf(
                    "AURORA seed=%d safe=(%d,%d,y=%d) distribution=%s signature=%016x%n",
                    seed, safeSite.x, safeSite.z, safeSite.height, summarize(regions), signature);
                if (render && seedIndex == 0) render(generator, randomState, seed);
            }

            check(signatures.stream().distinct().count() == SEEDS.length, "Different seeds produced identical terrain signatures");
            System.out.println("AURORA VALIDATION PASSED: registries, deterministic multi-scale clusters, proximity, isolation, voids and safe arrival.");
        }
    }

    private static RegionStats scan(NoiseBasedChunkGenerator generator, RandomState randomState, int centerX, int centerZ) {
        int side = REGION_HALF_SIZE * 2 / SAMPLE_STEP + 1;
        boolean[][] land = new boolean[side][side];
        int landColumns = 0;
        int minimumHeight = Integer.MAX_VALUE;
        int maximumHeight = Integer.MIN_VALUE;
        int floorBlocks = 0;
        int thicknessSamples = 0;
        int thicknessSum = 0;
        int maximumThickness = 0;
        long signature = 0xcbf29ce484222325L;

        for (int zi = 0; zi < side; zi++) {
            int z = centerZ - REGION_HALF_SIZE + zi * SAMPLE_STEP;
            for (int xi = 0; xi < side; xi++) {
                int x = centerX - REGION_HALF_SIZE + xi * SAMPLE_STEP;
                int height = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, HEIGHT, randomState);
                land[zi][xi] = height > HEIGHT.getMinBuildHeight();
                signature ^= Integer.toUnsignedLong(height * 31 + x * 17 + z);
                signature *= 0x100000001b3L;
                if (land[zi][xi]) {
                    landColumns++;
                    minimumHeight = Math.min(minimumHeight, height);
                    maximumHeight = Math.max(maximumHeight, height);
                    if ((xi + zi) % 23 == 0) {
                        NoiseColumn column = generator.getBaseColumn(x, z, HEIGHT, randomState);
                        if (!column.getBlock(-64).isAir() || !column.getBlock(0).isAir()) floorBlocks++;
                        int thickness = 0;
                        int currentRun = 0;
                        int longestRun = 0;
                        for (int y = 64; y < 312; y++) {
                            if (!column.getBlock(y).isAir()) {
                                thickness++;
                                currentRun++;
                                longestRun = Math.max(longestRun, currentRun);
                            } else {
                                currentRun = 0;
                            }
                        }
                        thicknessSamples++;
                        thicknessSum += thickness;
                        maximumThickness = Math.max(maximumThickness, longestRun);
                    }
                }
            }
        }

        ComponentStats components = analyzeComponents(land);
        WindowStats windows = analyzeWindows(land);
        double landRatio = landColumns / (double)(side * side);
        return new RegionStats(landRatio, components.count, components.almostConnectedPairs,
            components.nearbyPairs, components.isolatedComponents, components.smallIntermediateIslands,
            components.averageNearestGap, components.largestShare, windows.denseWindows, windows.emptyWindows,
            windows.totalWindows, largestEmptySpan(land), landColumns == 0 ? 0 : minimumHeight,
            landColumns == 0 ? 0 : maximumHeight, floorBlocks,
            thicknessSamples == 0 ? 0.0 : thicknessSum / (double)thicknessSamples, maximumThickness, signature);
    }

    private static long probe(NoiseBasedChunkGenerator generator, RandomState randomState) {
        int[][] points = {
            {0, 0}, {1000, 0}, {-1000, 0}, {0, 1000}, {0, -1000},
            {5000, 1000}, {-5000, -1000}, {8192, -4096}, {-8192, 4096}
        };
        long signature = 0x9e3779b97f4a7c15L;
        for (int[] point : points) {
            int height = generator.getBaseHeight(point[0], point[1], Heightmap.Types.WORLD_SURFACE_WG, HEIGHT, randomState);
            signature = Long.rotateLeft(signature ^ Integer.toUnsignedLong(height), 11) * 0xbf58476d1ce4e5b9L;
        }
        return signature;
    }

    private static ComponentStats analyzeComponents(boolean[][] land) {
        boolean[][] visited = new boolean[land.length][land[0].length];
        List<List<GridPoint>> components = new ArrayList<>();
        for (int z = 0; z < land.length; z++) {
            for (int x = 0; x < land[z].length; x++) {
                if (!land[z][x] || visited[z][x]) continue;
                List<GridPoint> component = new ArrayList<>();
                Queue<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{x, z});
                visited[z][x] = true;
                while (!queue.isEmpty()) {
                    int[] point = queue.remove();
                    component.add(new GridPoint(point[0], point[1]));
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx != 0 || dz != 0) visit(land, visited, queue, point[0] + dx, point[1] + dz);
                        }
                    }
                }
                components.add(component);
            }
        }

        int[] nearestSquared = new int[components.size()];
        java.util.Arrays.fill(nearestSquared, Integer.MAX_VALUE);
        int almostConnectedPairs = 0;
        int nearbyPairs = 0;
        for (int first = 0; first < components.size(); first++) {
            for (int second = first + 1; second < components.size(); second++) {
                int squared = minimumDistanceSquared(components.get(first), components.get(second));
                nearestSquared[first] = Math.min(nearestSquared[first], squared);
                nearestSquared[second] = Math.min(nearestSquared[second], squared);
                double gap = sampleGap(squared);
                if (gap <= ALMOST_CONNECTED_GAP) almostConnectedPairs++;
                if (gap <= NEARBY_ISLAND_GAP) nearbyPairs++;
            }
        }

        int isolatedComponents = 0;
        int smallIntermediateIslands = 0;
        double nearestGapSum = 0.0;
        int nearestGapSamples = 0;
        int largestSize = 0;
        int totalSize = 0;
        for (int index = 0; index < components.size(); index++) {
            int size = components.get(index).size();
            totalSize += size;
            largestSize = Math.max(largestSize, size);
            if (nearestSquared[index] == Integer.MAX_VALUE) continue;
            double gap = sampleGap(nearestSquared[index]);
            nearestGapSum += gap;
            nearestGapSamples++;
            if (gap >= ISOLATED_ISLAND_GAP) isolatedComponents++;
            if (size <= SMALL_ISLAND_MAX_SAMPLES && gap <= NEARBY_ISLAND_GAP) smallIntermediateIslands++;
        }
        return new ComponentStats(components.size(), almostConnectedPairs, nearbyPairs, isolatedComponents,
            smallIntermediateIslands, nearestGapSamples == 0 ? 0.0 : nearestGapSum / nearestGapSamples,
            totalSize == 0 ? 0.0 : largestSize / (double)totalSize);
    }

    private static int minimumDistanceSquared(List<GridPoint> first, List<GridPoint> second) {
        int minimum = Integer.MAX_VALUE;
        for (GridPoint a : first) {
            for (GridPoint b : second) {
                int dx = a.x - b.x;
                int dz = a.z - b.z;
                minimum = Math.min(minimum, dx * dx + dz * dz);
                if (minimum == 4) return minimum;
            }
        }
        return minimum;
    }

    private static double sampleGap(int squaredSampleDistance) {
        return Math.max(0.0, Math.sqrt(squaredSampleDistance) * SAMPLE_STEP - SAMPLE_STEP);
    }

    private static WindowStats analyzeWindows(boolean[][] land) {
        int dense = 0;
        int empty = 0;
        int total = 0;
        int diameter = WINDOW_RADIUS * 2 + 1;
        int area = diameter * diameter;
        for (int centerZ = WINDOW_RADIUS; centerZ < land.length - WINDOW_RADIUS; centerZ += WINDOW_STRIDE) {
            for (int centerX = WINDOW_RADIUS; centerX < land[centerZ].length - WINDOW_RADIUS; centerX += WINDOW_STRIDE) {
                int landSamples = 0;
                for (int z = centerZ - WINDOW_RADIUS; z <= centerZ + WINDOW_RADIUS; z++) {
                    for (int x = centerX - WINDOW_RADIUS; x <= centerX + WINDOW_RADIUS; x++) {
                        if (land[z][x]) landSamples++;
                    }
                }
                double ratio = landSamples / (double)area;
                if (ratio >= 0.42) dense++;
                if (ratio <= 0.02) empty++;
                total++;
            }
        }
        return new WindowStats(dense, empty, total);
    }

    private static int largestEmptySpan(boolean[][] land) {
        int[][] squares = new int[land.length][land[0].length];
        int largest = 0;
        for (int z = 0; z < land.length; z++) {
            for (int x = 0; x < land[z].length; x++) {
                if (land[z][x]) continue;
                if (x == 0 || z == 0) {
                    squares[z][x] = 1;
                } else {
                    squares[z][x] = 1 + Math.min(squares[z - 1][x - 1],
                        Math.min(squares[z - 1][x], squares[z][x - 1]));
                }
                largest = Math.max(largest, squares[z][x]);
            }
        }
        return largest * SAMPLE_STEP;
    }

    private static void render(NoiseBasedChunkGenerator generator, RandomState randomState, long seed) {
        int half = 1024;
        int step = 16;
        int scale = 3;
        int side = half * 2 / step + 1;
        BufferedImage image = new BufferedImage(side * scale, side * scale, BufferedImage.TYPE_INT_RGB);
        for (int zi = 0; zi < side; zi++) {
            int z = -half + zi * step;
            for (int xi = 0; xi < side; xi++) {
                int x = -half + xi * step;
                int height = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, HEIGHT, randomState);
                int rgb = height <= HEIGHT.getMinBuildHeight()
                    ? new Color(7, 5, 20).getRGB()
                    : Color.HSBtoRGB(0.88F - Math.min(1.0F, Math.max(0.0F, (height - 170) / 90.0F)) * 0.35F,
                        0.62F, 0.95F);
                for (int dz = 0; dz < scale; dz++) {
                    for (int dx = 0; dx < scale; dx++) image.setRGB(xi * scale + dx, zi * scale + dz, rgb);
                }
            }
        }
        Path output = Path.of("/tmp/aurora-archipelago-" + seed + ".png");
        try {
            ImageIO.write(image, "png", output.toFile());
            System.out.println("AURORA render=" + output);
            renderCrossSection(generator, randomState, seed);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write validation render", exception);
        }
    }

    private static void renderCrossSection(NoiseBasedChunkGenerator generator, RandomState randomState, long seed) throws IOException {
        int minimumY = 64;
        int maximumY = 311;
        int xStep = 4;
        int width = 2048 / xStep + 1;
        int yScale = 2;
        BufferedImage image = new BufferedImage(width, (maximumY - minimumY + 1) * yScale, BufferedImage.TYPE_INT_RGB);
        for (int xi = 0; xi < width; xi++) {
            int x = -1024 + xi * xStep;
            NoiseColumn column = generator.getBaseColumn(x, 0, HEIGHT, randomState);
            for (int y = minimumY; y <= maximumY; y++) {
                int rgb = column.getBlock(y).isAir() ? new Color(7, 5, 20).getRGB() : new Color(222, 112, 229).getRGB();
                int imageY = (maximumY - y) * yScale;
                image.setRGB(xi, imageY, rgb);
                image.setRGB(xi, imageY + 1, rgb);
            }
        }
        Path output = Path.of("/tmp/aurora-cross-section-" + seed + ".png");
        ImageIO.write(image, "png", output.toFile());
        System.out.println("AURORA cross-section=" + output);
    }

    private static void visit(boolean[][] land, boolean[][] visited, Queue<int[]> queue, int x, int z) {
        if (z < 0 || z >= land.length || x < 0 || x >= land[z].length || visited[z][x] || !land[z][x]) return;
        visited[z][x] = true;
        queue.add(new int[]{x, z});
    }

    private static SafeSite findSafeSite(NoiseBasedChunkGenerator generator, RandomState randomState) {
        Map<Long, Integer> cache = new HashMap<>();
        SafeSite origin = inspectSafe(generator, randomState, cache, 0, 0);
        if (origin != null) return origin;
        for (int radius = 24; radius <= 2048; radius += 24) {
            for (int offset = -radius; offset <= radius; offset += 24) {
                SafeSite site = inspectSafe(generator, randomState, cache, -radius, offset);
                if (site != null) return site;
                site = inspectSafe(generator, randomState, cache, radius, offset);
                if (site != null) return site;
            }
            for (int offset = -radius + 24; offset < radius; offset += 24) {
                SafeSite site = inspectSafe(generator, randomState, cache, offset, -radius);
                if (site != null) return site;
                site = inspectSafe(generator, randomState, cache, offset, radius);
                if (site != null) return site;
            }
        }
        return null;
    }

    private static SafeSite inspectSafe(NoiseBasedChunkGenerator generator, RandomState randomState,
                                        Map<Long, Integer> cache, int x, int z) {
        int[] offsets = {0, 0, 7, 0, -7, 0, 0, 7, 0, -7, 7, 7, 7, -7, -7, 7, -7, -7};
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;
        for (int i = 0; i < offsets.length; i += 2) {
            int sampleX = x + offsets[i];
            int sampleZ = z + offsets[i + 1];
            long key = ((long)sampleX << 32) ^ Integer.toUnsignedLong(sampleZ);
            int height = cache.computeIfAbsent(key, ignored -> generator.getBaseHeight(
                sampleX, sampleZ, Heightmap.Types.WORLD_SURFACE_WG, HEIGHT, randomState));
            if (height < 80) return null;
            minimum = Math.min(minimum, height);
            maximum = Math.max(maximum, height);
        }
        return maximum - minimum <= 5 ? new SafeSite(x, z, generator.getBaseHeight(
            x, z, Heightmap.Types.WORLD_SURFACE_WG, HEIGHT, randomState)) : null;
    }

    private static void validateRegion(String name, long seed, RegionStats stats) {
        check(stats.landRatio < 0.82, "Terrain became a suspended supercontinent near " + name + " for seed " + seed);
        if (stats.landRatio > 0.0) {
            check(stats.minimumHeight >= 65 && stats.maximumHeight < 312,
                "Terrain escaped its vertical band near " + name + " for seed " + seed);
            check(stats.averageThickness >= 8.0 && stats.maximumThickness >= 20,
                "Islands are too thin near " + name + " for seed " + seed);
            check(stats.maximumThickness <= 128,
                "Island footprint lost its tapered underside near " + name + " for seed " + seed);
        }
    }

    private static void validateDistribution(long seed, List<RegionStats> regions) {
        double minimumLand = regions.stream().mapToDouble(region -> region.landRatio).min().orElseThrow();
        double maximumLand = regions.stream().mapToDouble(region -> region.landRatio).max().orElseThrow();
        int almostConnected = regions.stream().mapToInt(region -> region.almostConnectedPairs).sum();
        int nearby = regions.stream().mapToInt(region -> region.nearbyPairs).sum();
        int isolated = regions.stream().mapToInt(region -> region.isolatedComponents).sum();
        int intermediates = regions.stream().mapToInt(region -> region.smallIntermediateIslands).sum();
        int denseWindows = regions.stream().mapToInt(region -> region.denseWindows).sum();
        int emptyWindows = regions.stream().mapToInt(region -> region.emptyWindows).sum();
        int widestVoid = regions.stream().mapToInt(region -> region.largestEmptySpan).max().orElse(0);

        check(maximumLand >= 0.20, "No dense or moderate archipelago region was sampled for seed " + seed);
        check(maximumLand - minimumLand >= 0.10,
            "Island coverage does not vary enough between ±1000 and ±5000 for seed " + seed);
        check(denseWindows > 0, "No compact archipelago window was sampled for seed " + seed);
        check(emptyWindows > 0 && widestVoid >= 256,
            "No extensive empty region was sampled for seed " + seed);
        check(almostConnected > 0 && nearby >= 8,
            "Islands never become nearly connected or bridgeable for seed " + seed);
        check(intermediates >= 4, "Too few small intermediate islands for seed " + seed);
        check(isolated > 0, "No isolated island was sampled for seed " + seed);
    }

    private static String summarize(List<RegionStats> regions) {
        double minimumLand = regions.stream().mapToDouble(region -> region.landRatio).min().orElse(0.0);
        double maximumLand = regions.stream().mapToDouble(region -> region.landRatio).max().orElse(0.0);
        int almostConnected = regions.stream().mapToInt(region -> region.almostConnectedPairs).sum();
        int nearby = regions.stream().mapToInt(region -> region.nearbyPairs).sum();
        int isolated = regions.stream().mapToInt(region -> region.isolatedComponents).sum();
        int intermediates = regions.stream().mapToInt(region -> region.smallIntermediateIslands).sum();
        int widestVoid = regions.stream().mapToInt(region -> region.largestEmptySpan).max().orElse(0);
        return String.format("coverage=%.1f..%.1f%% near=%d/almost=%d small=%d isolated=%d void=%d",
            minimumLand * 100.0, maximumLand * 100.0, nearby, almostConnected, intermediates, isolated, widestVoid);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void reopenIntrusiveRegistry(Registry<?> registry) {
        try {
            reopenRegistry(registry);
            MappedRegistry<?> mapped = (MappedRegistry<?>)registry;
            Field intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            intrusive.setAccessible(true);
            intrusive.set(mapped, new IdentityHashMap<>());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not prepare standalone registries", exception);
        }
    }

    private static void reopenRegistry(Registry<?> registry) {
        try {
            MappedRegistry<?> mapped = (MappedRegistry<?>)registry;
            Field frozen = MappedRegistry.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            frozen.setBoolean(mapped, false);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not reopen standalone registry", exception);
        }
    }

    private static <T> T require(T value, String message) {
        check(value != null, message);
        return value;
    }

    private record SafeSite(int x, int z, int height) {
    }

    private record SampleRegion(String name, int centerX, int centerZ) {
    }

    private record GridPoint(int x, int z) {
    }

    private record ComponentStats(int count, int almostConnectedPairs, int nearbyPairs, int isolatedComponents,
                                  int smallIntermediateIslands, double averageNearestGap, double largestShare) {
    }

    private record WindowStats(int denseWindows, int emptyWindows, int totalWindows) {
    }

    private record RegionStats(double landRatio, int components, int almostConnectedPairs, int nearbyPairs,
                               int isolatedComponents, int smallIntermediateIslands, double averageNearestGap,
                               double largestComponentShare, int denseWindows, int emptyWindows, int totalWindows,
                               int largestEmptySpan, int minimumHeight, int maximumHeight, int floorBlocks,
                               double averageThickness, int maximumThickness, long signature) {
        @Override
        public String toString() {
            return String.format(
                "land=%.1f%% groups=%d near=%d/%d isolated=%d small=%d gap=%.0f largest=%.0f%% "
                    + "windows=%d/%d/%d void=%d y=%d..%d thickness=%.1f/%d",
                landRatio * 100.0, components, nearbyPairs, almostConnectedPairs, isolatedComponents,
                smallIntermediateIslands, averageNearestGap, largestComponentShare * 100.0,
                denseWindows, emptyWindows, totalWindows, largestEmptySpan,
                minimumHeight, maximumHeight, averageThickness, maximumThickness);
        }
    }
}
