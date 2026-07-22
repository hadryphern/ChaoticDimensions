package net.blue.chaoticd.worldgen.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration.CanopySettings;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration.FoliageChoice;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration.ShapeSettings;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration.TreeProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Pure, deterministic geometry planner for Aurora trees. It never reads or writes a world. */
public final class AuroraTreePlanner {
    private static final long TREE_SALT = 0x4155524F52415F54L;
    private static final Direction[] NEIGHBORS = Direction.values();

    private AuroraTreePlanner() {
    }

    public static TreePlan plan(AuroraTreeConfiguration configuration, long worldSeed, BlockPos origin) {
        RandomSource random = RandomSource.create(mix64(worldSeed ^ origin.asLong() ^ TREE_SALT));
        TreeProfile profile = chooseWeighted(configuration.profiles(), TreeProfile::weight, random);
        FoliageChoice foliage = chooseWeighted(configuration.foliagePalette(), FoliageChoice::weight, random);
        int height = between(random, profile.size().minHeight(), profile.size().maxHeight());
        int baseWidth = between(random, profile.size().minTrunkWidth(), profile.size().maxTrunkWidth());
        int segments = Math.min(height, between(random, profile.size().minSegments(), profile.size().maxSegments()));

        LinkedHashMap<BlockPos, BlockState> logs = new LinkedHashMap<>();
        LinkedHashSet<BlockPos> requiredGround = new LinkedHashSet<>();
        ArrayList<BlockPos> trunkCenters = new ArrayList<>(height);
        ShapeSettings shape = profile.shape();
        growTrunk(configuration.trunkState(), origin, height, baseWidth, segments, shape,
            configuration.maxHorizontalReach(), random, logs, requiredGround, trunkCenters);

        ArrayList<BlockPos> branchTips = new ArrayList<>();
        growBranches(configuration.trunkState(), profile, origin, trunkCenters,
            configuration.maxHorizontalReach(), random, logs, branchTips);
        growRoots(configuration.trunkState(), profile, origin, baseWidth,
            configuration.maxHorizontalReach(), random, logs, requiredGround);
        branchTips.removeIf(tip -> isFullyEnclosedByLogs(tip, logs.keySet()));

        LinkedHashSet<BlockPos> leafCandidates = new LinkedHashSet<>();
        createCanopy(profile.canopy(), trunkCenters.get(trunkCenters.size() - 1), origin,
            configuration.maxHorizontalReach(), random, leafCandidates);
        createBranchFoliage(profile.canopy(), branchTips, origin, configuration.maxHorizontalReach(),
            random, leafCandidates);
        leafCandidates.removeAll(logs.keySet());
        LinkedHashMap<BlockPos, BlockState> leaves = connectLeavesToLogs(
            foliage.state(), logs.keySet(), leafCandidates);

        return new TreePlan(
            profile.name(),
            height,
            baseWidth,
            segments,
            immutableOrderedMap(logs),
            immutableOrderedMap(leaves),
            Collections.unmodifiableSet(new LinkedHashSet<>(requiredGround)),
            List.copyOf(trunkCenters),
            List.copyOf(branchTips)
        );
    }

    private static void growTrunk(BlockState trunkState, BlockPos origin, int height, int baseWidth,
                                  int segments, ShapeSettings shape, int maxHorizontalReach, RandomSource random,
                                  Map<BlockPos, BlockState> logs, Set<BlockPos> requiredGround,
                                  List<BlockPos> centers) {
        boolean inclined = random.nextFloat() < shape.inclinationChance();
        boolean curved = random.nextFloat() < shape.bendChance();
        double angle = random.nextDouble() * Math.PI * 2.0;
        double driftX = 0.0;
        double driftZ = 0.0;
        double velocityX = inclined ? Math.cos(angle) * shape.bendStrength() * 0.55 : 0.0;
        double velocityZ = inclined ? Math.sin(angle) * shape.bendStrength() * 0.55 : 0.0;
        double targetX = velocityX;
        double targetZ = velocityZ;
        int segmentLength = Math.max(1, height / segments);
        BlockPos previousCenter = origin;

        for (int y = 0; y < height; y++) {
            if (curved && y > 0 && y % segmentLength == 0) {
                angle += (random.nextDouble() - 0.5) * 1.45;
                double strength = shape.bendStrength() * (0.35 + random.nextDouble() * 0.65);
                targetX = clamp(velocityX * 0.55 + Math.cos(angle) * strength, -0.44, 0.44);
                targetZ = clamp(velocityZ * 0.55 + Math.sin(angle) * strength, -0.44, 0.44);
            }
            velocityX += (targetX - velocityX) * 0.28;
            velocityZ += (targetZ - velocityZ) * 0.28;
            driftX += velocityX;
            driftZ += velocityZ;
            double maximumDrift = Math.max(1.0, maxHorizontalReach * 0.48);
            double driftRadius = Math.sqrt(driftX * driftX + driftZ * driftZ);
            if (driftRadius > maximumDrift) {
                double scale = maximumDrift / driftRadius;
                driftX *= scale;
                driftZ *= scale;
                velocityX *= 0.45;
                velocityZ *= 0.45;
                targetX *= -0.25;
                targetZ *= -0.25;
            }
            BlockPos center = origin.offset((int)Math.round(driftX), y, (int)Math.round(driftZ));

            int width = taperedWidth(baseWidth, y, height);
            if (y > 0 && (center.getX() != previousCenter.getX() || center.getZ() != previousCenter.getZ())) {
                addConnectedLine(trunkState, previousCenter.above(), center, logs);
            }
            fillTrunkFootprint(trunkState, center, width, y, logs, y == 0 ? requiredGround : null);
            centers.add(center);
            previousCenter = center;
        }
    }

    private static void fillTrunkFootprint(BlockState trunkState, BlockPos center, int width, int level,
                                           Map<BlockPos, BlockState> logs, Set<BlockPos> ground) {
        int minimum = width == 2 ? 0 : -(width / 2);
        int maximum = width == 2 ? 1 : width / 2;
        for (int dx = minimum; dx <= maximum; dx++) {
            for (int dz = minimum; dz <= maximum; dz++) {
                if (width == 3 && Math.abs(dx) == 1 && Math.abs(dz) == 1
                    && (stableHash(center.getX() + dx, level, center.getZ() + dz) & 3L) == 0L) {
                    continue;
                }
                BlockPos position = center.offset(dx, 0, dz);
                logs.putIfAbsent(position, withAxis(trunkState, Direction.Axis.Y));
                if (ground != null) ground.add(position.below());
            }
        }
    }

    private static int taperedWidth(int baseWidth, int y, int height) {
        double progress = y / (double)Math.max(1, height - 1);
        if (baseWidth == 3) {
            if (progress < 0.52) return 3;
            if (progress < 0.80) return 2;
            return 1;
        }
        if (baseWidth == 2) return progress < 0.67 ? 2 : 1;
        return 1;
    }

    private static void growBranches(BlockState trunkState, TreeProfile profile, BlockPos origin,
                                     List<BlockPos> trunkCenters, int maxReach,
                                     RandomSource random, Map<BlockPos, BlockState> logs,
                                     List<BlockPos> branchTips) {
        ShapeSettings shape = profile.shape();
        int branchCount = between(random, shape.minBranches(), shape.maxBranches());
        if (branchCount == 0) return;
        int firstLevel = Math.min(trunkCenters.size() - 2,
            Math.max(1, (int)Math.floor(trunkCenters.size() * shape.branchStartFraction())));
        double phase = random.nextDouble() * Math.PI * 2.0;

        for (int index = 0; index < branchCount; index++) {
            int available = Math.max(1, trunkCenters.size() - 1 - firstLevel);
            int level = firstLevel + Math.floorMod(
                (index * available / Math.max(1, branchCount)) + random.nextInt(3) - 1, available);
            BlockPos anchor = trunkCenters.get(Math.min(level, trunkCenters.size() - 2));
            double angle = phase + (Math.PI * 2.0 * index / branchCount) + (random.nextDouble() - 0.5) * 0.72;
            int length = between(random, shape.minBranchLength(), shape.maxBranchLength());
            length = Math.min(length, Math.max(2, maxReach - 2));
            double rise = -0.12 + random.nextDouble() * 0.47;
            BlockPos endpoint = branchEndpoint(anchor, origin, angle, length, rise, maxReach - 2);
            addConnectedLine(trunkState, anchor, endpoint, logs);

            if (shape.maxBranchWidth() > 1 && length >= 5 && random.nextFloat() < 0.48F) {
                Direction offsetDirection = perpendicularDirection(angle);
                BlockPos thickEnd = interpolate(anchor, endpoint, 0.52).relative(offsetDirection);
                addConnectedLine(trunkState, anchor.relative(offsetDirection), thickEnd, logs);
            }
            branchTips.add(endpoint);

            if (length >= 4 && random.nextFloat() < shape.secondaryBranchChance()) {
                BlockPos secondaryStart = interpolate(anchor, endpoint, 0.55 + random.nextDouble() * 0.18);
                double splitAngle = angle + (random.nextBoolean() ? 1.0 : -1.0) * (0.58 + random.nextDouble() * 0.62);
                int secondaryLength = Math.max(2, (int)Math.round(length * (0.42 + random.nextDouble() * 0.23)));
                BlockPos secondaryEnd = branchEndpoint(secondaryStart, origin, splitAngle,
                    secondaryLength, 0.08 + random.nextDouble() * 0.33, maxReach - 2);
                addConnectedLine(trunkState, secondaryStart, secondaryEnd, logs);
                branchTips.add(secondaryEnd);
            }
        }
    }

    private static BlockPos branchEndpoint(BlockPos start, BlockPos origin, double angle, int length,
                                           double rise, int maximumRadius) {
        int endX = start.getX() + (int)Math.round(Math.cos(angle) * length);
        int endZ = start.getZ() + (int)Math.round(Math.sin(angle) * length);
        int endY = start.getY() + (int)Math.round(rise * length);
        int dx = endX - origin.getX();
        int dz = endZ - origin.getZ();
        double radius = Math.sqrt(dx * (double)dx + dz * (double)dz);
        if (radius > maximumRadius) {
            double scale = maximumRadius / radius;
            endX = origin.getX() + (int)Math.round(dx * scale);
            endZ = origin.getZ() + (int)Math.round(dz * scale);
        }
        return new BlockPos(endX, Math.max(origin.getY() + 2, endY), endZ);
    }

    private static void addConnectedLine(BlockState trunkState, BlockPos start, BlockPos end,
                                         Map<BlockPos, BlockState> logs) {
        int steps = Math.max(Math.abs(end.getX() - start.getX()),
            Math.max(Math.abs(end.getY() - start.getY()), Math.abs(end.getZ() - start.getZ())));
        BlockPos cursor = start;
        logs.putIfAbsent(cursor, withAxis(trunkState, Direction.Axis.Y));
        for (int step = 1; step <= Math.max(1, steps); step++) {
            double progress = step / (double)Math.max(1, steps);
            BlockPos target = new BlockPos(
                (int)Math.round(start.getX() + (end.getX() - start.getX()) * progress),
                (int)Math.round(start.getY() + (end.getY() - start.getY()) * progress),
                (int)Math.round(start.getZ() + (end.getZ() - start.getZ()) * progress));
            cursor = walkAxis(trunkState, cursor, target.getX(), Direction.Axis.X, logs);
            cursor = walkAxis(trunkState, cursor, target.getZ(), Direction.Axis.Z, logs);
            cursor = walkAxis(trunkState, cursor, target.getY(), Direction.Axis.Y, logs);
        }
    }

    private static BlockPos walkAxis(BlockState state, BlockPos cursor, int target,
                                     Direction.Axis axis, Map<BlockPos, BlockState> logs) {
        int current = axis.choose(cursor.getX(), cursor.getY(), cursor.getZ());
        while (current != target) {
            int delta = Integer.compare(target, current);
            cursor = switch (axis) {
                case X -> cursor.offset(delta, 0, 0);
                case Y -> cursor.offset(0, delta, 0);
                case Z -> cursor.offset(0, 0, delta);
            };
            logs.putIfAbsent(cursor, withAxis(state, axis));
            current += delta;
        }
        return cursor;
    }

    private static void growRoots(BlockState trunkState, TreeProfile profile, BlockPos origin,
                                  int baseWidth, int maxReach, RandomSource random,
                                  Map<BlockPos, BlockState> logs, Set<BlockPos> ground) {
        ShapeSettings shape = profile.shape();
        if (shape.maxRootLength() == 0 || random.nextFloat() >= shape.rootChance()) return;
        int roots = Math.min(7, 2 + baseWidth + random.nextInt(3));
        double phase = random.nextDouble() * Math.PI * 2.0;
        for (int root = 0; root < roots; root++) {
            double angle = phase + Math.PI * 2.0 * root / roots + (random.nextDouble() - 0.5) * 0.5;
            int length = Math.min(maxReach - 1, between(random, 1, shape.maxRootLength()));
            BlockPos end = origin.offset((int)Math.round(Math.cos(angle) * length), 0,
                (int)Math.round(Math.sin(angle) * length));
            HashSet<BlockPos> before = new HashSet<>(logs.keySet());
            addConnectedLine(trunkState, origin, end, logs);
            for (BlockPos position : logs.keySet()) {
                if (!before.contains(position) && position.getY() == origin.getY()) ground.add(position.below());
            }
        }
    }

    private static void createCanopy(CanopySettings canopy, BlockPos crown, BlockPos origin,
                                     int maxReach, RandomSource random, Set<BlockPos> leaves) {
        int radius = between(random, canopy.minRadius(), canopy.maxRadius());
        int blobs = between(random, canopy.minBlobs(), canopy.maxBlobs());
        addLeafBlob(crown, origin, radius, Math.max(2, Math.round(radius * canopy.verticalScale())),
            canopy.density(), maxReach, random, leaves);

        for (int blob = 1; blob < blobs; blob++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radial = radius * (0.15 + random.nextDouble() * 0.56);
            int offsetY = (int)Math.round((random.nextDouble() - 0.52) * radius * canopy.verticalScale());
            BlockPos center = crown.offset((int)Math.round(Math.cos(angle) * radial), offsetY,
                (int)Math.round(Math.sin(angle) * radial));
            int blobRadius = Math.max(1, (int)Math.round(radius * (0.34 + random.nextDouble() * 0.34)));
            addLeafBlob(center, origin, blobRadius,
                Math.max(1, Math.round(blobRadius * canopy.verticalScale())),
                canopy.density() * (0.88F + random.nextFloat() * 0.15F), maxReach, random, leaves);
        }
    }

    private static void createBranchFoliage(CanopySettings canopy, List<BlockPos> branchTips,
                                            BlockPos origin, int maxReach, RandomSource random,
                                            Set<BlockPos> leaves) {
        for (BlockPos tip : branchTips) {
            int radius = between(random, canopy.minTipRadius(), canopy.maxTipRadius());
            addLeafBlob(tip, origin, radius, Math.max(1, Math.round(radius * canopy.verticalScale())),
                Math.min(0.92F, canopy.density() + 0.08F), maxReach, random, leaves);
        }
    }

    private static boolean isFullyEnclosedByLogs(BlockPos position, Set<BlockPos> logs) {
        for (Direction direction : NEIGHBORS) {
            if (!logs.contains(position.relative(direction))) return false;
        }
        return true;
    }

    private static void addLeafBlob(BlockPos center, BlockPos origin, int radius, int verticalRadius,
                                    float density, int maxReach, RandomSource random, Set<BlockPos> leaves) {
        for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos position = center.offset(dx, dy, dz);
                    if (position.getY() <= origin.getY()) continue;
                    int originX = position.getX() - origin.getX();
                    int originZ = position.getZ() - origin.getZ();
                    if (originX * originX + originZ * originZ > maxReach * maxReach) continue;
                    double normalized = dx * (double)dx / (radius * radius)
                        + dz * (double)dz / (radius * radius)
                        + dy * (double)dy / (verticalRadius * verticalRadius);
                    double raggedBoundary = 0.78 + random.nextDouble() * 0.42;
                    if (normalized > raggedBoundary) continue;
                    float localDensity = normalized < 0.28 ? Math.min(0.96F, density + 0.20F) : density;
                    if (random.nextFloat() <= localDensity) leaves.add(position);
                }
            }
        }
        if (center.getY() > origin.getY()) {
            int centerDx = center.getX() - origin.getX();
            int centerDz = center.getZ() - origin.getZ();
            if (centerDx * centerDx + centerDz * centerDz <= maxReach * maxReach) leaves.add(center);
            for (Direction direction : NEIGHBORS) {
                BlockPos neighbor = center.relative(direction);
                int dx = neighbor.getX() - origin.getX();
                int dz = neighbor.getZ() - origin.getZ();
                if (neighbor.getY() > origin.getY() && dx * dx + dz * dz <= maxReach * maxReach) {
                    leaves.add(neighbor);
                }
            }
        }
    }

    private static LinkedHashMap<BlockPos, BlockState> connectLeavesToLogs(BlockState foliage,
                                                                            Set<BlockPos> logs,
                                                                            Set<BlockPos> candidates) {
        Map<BlockPos, Integer> distance = new HashMap<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos log : logs) {
            distance.put(log, 0);
            queue.add(log);
        }
        while (!queue.isEmpty()) {
            BlockPos current = queue.remove();
            int nextDistance = distance.get(current) + 1;
            if (nextDistance > LeavesBlock.DECAY_DISTANCE - 1) continue;
            for (Direction direction : NEIGHBORS) {
                BlockPos neighbor = current.relative(direction);
                if (!candidates.contains(neighbor) || distance.containsKey(neighbor)) continue;
                distance.put(neighbor, nextDistance);
                queue.add(neighbor);
            }
        }

        LinkedHashMap<BlockPos, BlockState> result = new LinkedHashMap<>();
        candidates.stream()
            .filter(distance::containsKey)
            .sorted(Comparator.<BlockPos>comparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ))
            .forEach(position -> {
                BlockState state = foliage;
                if (state.hasProperty(LeavesBlock.DISTANCE)) {
                    state = state.setValue(LeavesBlock.DISTANCE, distance.get(position));
                }
                if (state.hasProperty(LeavesBlock.PERSISTENT)) state = state.setValue(LeavesBlock.PERSISTENT, false);
                if (state.hasProperty(LeavesBlock.WATERLOGGED)) state = state.setValue(LeavesBlock.WATERLOGGED, false);
                result.put(position, state);
            });
        return result;
    }

    private static BlockState withAxis(BlockState state, Direction.Axis axis) {
        return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, axis) : state;
    }

    private static Direction perpendicularDirection(double angle) {
        double x = -Math.sin(angle);
        double z = Math.cos(angle);
        if (Math.abs(x) > Math.abs(z)) return x >= 0 ? Direction.EAST : Direction.WEST;
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static BlockPos interpolate(BlockPos start, BlockPos end, double progress) {
        return new BlockPos(
            (int)Math.round(start.getX() + (end.getX() - start.getX()) * progress),
            (int)Math.round(start.getY() + (end.getY() - start.getY()) * progress),
            (int)Math.round(start.getZ() + (end.getZ() - start.getZ()) * progress));
    }

    private static int between(RandomSource random, int minimum, int maximum) {
        return minimum == maximum ? minimum : minimum + random.nextInt(maximum - minimum + 1);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static long stableHash(int x, int y, int z) {
        return mix64(x * 0x9E3779B97F4A7C15L ^ y * 0xC2B2AE3D27D4EB4FL ^ z * 0x165667B19E3779F9L);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static <T> T chooseWeighted(List<T> entries, java.util.function.ToIntFunction<T> weight,
                                        RandomSource random) {
        int total = entries.stream().mapToInt(weight).sum();
        int choice = random.nextInt(total);
        for (T entry : entries) {
            choice -= weight.applyAsInt(entry);
            if (choice < 0) return entry;
        }
        return entries.get(entries.size() - 1);
    }

    private static <K, V> Map<K, V> immutableOrderedMap(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    public record TreePlan(
        String profileName,
        int height,
        int baseWidth,
        int segments,
        Map<BlockPos, BlockState> logs,
        Map<BlockPos, BlockState> leaves,
        Set<BlockPos> requiredGround,
        List<BlockPos> trunkCenters,
        List<BlockPos> branchTips
    ) {
        public Set<BlockPos> allBlocks() {
            HashSet<BlockPos> result = new HashSet<>(logs.keySet());
            result.addAll(leaves.keySet());
            return Collections.unmodifiableSet(result);
        }
    }
}
