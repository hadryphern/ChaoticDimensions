package net.blue.chaoticd.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.blue.chaoticd.content.ModBlocks;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration;
import net.blue.chaoticd.worldgen.tree.AuroraTreePlanner;
import net.blue.chaoticd.worldgen.tree.AuroraTreePlanner.TreePlan;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * Headless statistical and structural acceptance test
 * for the pure Aurora tree planner.
 *
 * <p>The standalone validator temporarily reopens the Block, Item
 * and Fluid registries because loading ModBlocks also initializes
 * the custom Dream Fluid.</p>
 */
public final class AuroraTreeValidator {
    private static final int SAMPLE_COUNT = 6000;

    private static final Path CONFIGURED_FEATURE = Path.of(
        "build/resources/main/data/chaoticd/worldgen/configured_feature/pastel_aurora_trees.json"
    );

    private AuroraTreeValidator() {
    }

    public static void main(String[] args) throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        /*
         * Minecraft freezes the built-in registries after bootstrap.
         *
         * The validator loads ModBlocks manually outside Fabric Loader.
         * ModBlocks registers BlockItems and also initializes Dream Fluid,
         * so all three intrusive registries must be temporarily reopened.
         */
        reopenIntrusiveRegistry(BuiltInRegistries.BLOCK);
        reopenIntrusiveRegistry(BuiltInRegistries.ITEM);
        reopenIntrusiveRegistry(BuiltInRegistries.FLUID);

        require(
            ModBlocks.AURORA_PINKKO_LOG,
            "Aurora blocks were not initialized"
        );

        /*
         * Freeze them again after all mod content required by this
         * standalone validation has been registered.
         */
        BuiltInRegistries.FLUID.freeze();
        BuiltInRegistries.BLOCK.freeze();
        BuiltInRegistries.ITEM.freeze();

        AuroraTreeConfiguration configuration =
            loadConfiguration();

        Map<String, Integer> profiles =
            new HashMap<>();

        Set<String> foliageColors =
            new HashSet<>();

        Set<String> trunkColors =
            new HashSet<>();

        Set<String> trunkLeafPairs =
            new HashSet<>();

        Set<Long> normalizedSignatures =
            new HashSet<>();

        Set<Integer> widths =
            new HashSet<>();

        int straight = 0;
        int curved = 0;

        int minimumHeight =
            Integer.MAX_VALUE;

        int maximumHeight =
            Integer.MIN_VALUE;

        int minimumLogs =
            Integer.MAX_VALUE;

        int maximumLogs =
            Integer.MIN_VALUE;

        int minimumLeaves =
            Integer.MAX_VALUE;

        int maximumLeaves =
            Integer.MIN_VALUE;

        for (
            int sample = 0;
            sample < SAMPLE_COUNT;
            sample++
        ) {
            long seed = mix64(
                0x4155524F5241L
                    + sample * 0x9E3779B97F4A7C15L
            );

            BlockPos origin = new BlockPos(
                sample * 37 - 90_000,
                192,
                sample * -53 + 140_000
            );

            TreePlan plan =
                AuroraTreePlanner.plan(
                    configuration,
                    seed,
                    origin
                );

            TreePlan replay =
                AuroraTreePlanner.plan(
                    configuration,
                    seed,
                    origin
                );

            check(
                plan.equals(replay),
                "Same seed and position produced a different tree"
            );

            validatePlan(
                configuration,
                origin,
                plan
            );

            profiles.merge(
                plan.profileName(),
                1,
                Integer::sum
            );

            widths.add(
                plan.baseWidth()
            );

            minimumHeight = Math.min(
                minimumHeight,
                plan.height()
            );

            maximumHeight = Math.max(
                maximumHeight,
                plan.height()
            );

            minimumLogs = Math.min(
                minimumLogs,
                plan.logs().size()
            );

            maximumLogs = Math.max(
                maximumLogs,
                plan.logs().size()
            );

            minimumLeaves = Math.min(
                minimumLeaves,
                plan.leaves().size()
            );

            maximumLeaves = Math.max(
                maximumLeaves,
                plan.leaves().size()
            );

            normalizedSignatures.add(
                normalizedSignature(
                    plan,
                    origin
                )
            );

            String trunk = blockId(
                plan.logs()
                    .values()
                    .iterator()
                    .next()
            );

            String leaves = blockId(
                plan.leaves()
                    .values()
                    .iterator()
                    .next()
            );

            trunkColors.add(trunk);
            foliageColors.add(leaves);
            trunkLeafPairs.add(trunk + "|" + leaves);

            boolean isStraight =
                plan.trunkCenters()
                    .stream()
                    .allMatch(
                        position ->
                            position.getX()
                                == origin.getX()
                                && position.getZ()
                                == origin.getZ()
                    );

            if (isStraight) {
                straight++;
            } else {
                curved++;
            }
        }

        int totalWeight =
            configuration.profiles()
                .stream()
                .mapToInt(
                    AuroraTreeConfiguration.TreeProfile::weight
                )
                .sum();

        for (
            AuroraTreeConfiguration.TreeProfile profile :
                configuration.profiles()
        ) {
            int observed =
                profiles.getOrDefault(
                    profile.name(),
                    0
                );

            double expectedRatio =
                profile.weight()
                    / (double) totalWeight;

            double observedRatio =
                observed
                    / (double) SAMPLE_COUNT;

            double tolerance =
                profile.name().equals("monumental")
                    ? 0.009D
                    : 0.025D;

            check(
                Math.abs(
                    observedRatio
                        - expectedRatio
                ) <= tolerance,
                "Weight drift for "
                    + profile.name()
                    + ": expected "
                    + expectedRatio
                    + ", got "
                    + observedRatio
            );
        }

        Set<String> configuredTrunks = new HashSet<>();
        Set<String> configuredLeaves = new HashSet<>();
        Set<String> configuredPairs = new HashSet<>();

        if (configuration.treePalette().isEmpty()) {
            if (configuration.trunkPalette().isEmpty()) {
                configuredTrunks.add(blockId(configuration.trunkState()));
            } else {
                configuration.trunkPalette().forEach(choice ->
                    configuredTrunks.add(blockId(choice.state()))
                );
            }

            configuration.foliagePalette().forEach(choice ->
                configuredLeaves.add(blockId(choice.state()))
            );

            for (String trunk : configuredTrunks) {
                for (String leaves : configuredLeaves) {
                    configuredPairs.add(trunk + "|" + leaves);
                }
            }
        } else {
            configuration.treePalette().forEach(choice -> {
                String trunk = blockId(choice.trunkState());
                String leaves = blockId(choice.leafState());
                configuredTrunks.add(trunk);
                configuredLeaves.add(leaves);
                configuredPairs.add(trunk + "|" + leaves);
            });
        }

        check(
            trunkColors.equals(configuredTrunks),
            "Not every configured trunk was selected: expected "
                + configuredTrunks + ", got " + trunkColors
        );

        check(
            foliageColors.equals(configuredLeaves),
            "Not every configured foliage color was selected: expected "
                + configuredLeaves + ", got " + foliageColors
        );

        check(
            trunkLeafPairs.equals(configuredPairs),
            "Tree trunk/foliage selection is not independent: expected "
                + configuredPairs + ", got " + trunkLeafPairs
        );

        check(
            widths.equals(
                Set.of(1, 2, 3)
            ),
            "The planner did not produce all trunk widths: "
                + widths
        );

        check(
            straight > SAMPLE_COUNT / 20
                && curved > SAMPLE_COUNT / 4,
            "Straight/curved silhouettes are not both represented"
        );

        check(
            normalizedSignatures.size()
                > SAMPLE_COUNT * 0.92D,
            "Tree silhouettes repeat too frequently: "
                + normalizedSignatures.size()
        );

        System.out.printf(
            "AURORA TREE VALIDATION PASSED: "
                + "samples=%d profiles=%s trunks=%s leaves=%s combinations=%d "
                + "straight=%d curved=%d "
                + "height=%d..%d logs=%d..%d "
                + "leaves=%d..%d unique=%.1f%%%n",
            SAMPLE_COUNT,
            profiles,
            trunkColors,
            foliageColors,
            trunkLeafPairs.size(),
            straight,
            curved,
            minimumHeight,
            maximumHeight,
            minimumLogs,
            maximumLogs,
            minimumLeaves,
            maximumLeaves,
            normalizedSignatures.size()
                * 100.0D
                / SAMPLE_COUNT
        );
    }

    private static AuroraTreeConfiguration loadConfiguration()
        throws IOException {
        try (
            Reader reader =
                Files.newBufferedReader(
                    CONFIGURED_FEATURE
                )
        ) {
            JsonObject root =
                JsonParser.parseReader(
                    reader
                ).getAsJsonObject();

            return AuroraTreeConfiguration.CODEC
                .parse(
                    JsonOps.INSTANCE,
                    root.get("config")
                )
                .getOrThrow(
                    false,
                    message -> {
                        throw new IllegalStateException(
                            "Could not decode Aurora tree config: "
                                + message
                        );
                    }
                );
        }
    }

    private static String blockId(
        net.minecraft.world.level.block.state.BlockState state
    ) {
        return BuiltInRegistries.BLOCK
            .getKey(state.getBlock())
            .toString();
    }

    private static void validatePlan(
        AuroraTreeConfiguration configuration,
        BlockPos origin,
        TreePlan plan
    ) {
        check(
            plan.logs().containsKey(origin),
            "Trunk is not rooted at the feature origin"
        );

        check(
            !plan.leaves().isEmpty(),
            "Tree has no leaves"
        );

        check(
            plan.requiredGround()
                .stream()
                .allMatch(
                    position ->
                        plan.logs()
                            .containsKey(
                                position.above()
                            )
                ),
            "Ground requirement exists without a supported log"
        );

        checkConnected(
            plan.logs().keySet(),
            "Disconnected trunk or branch"
        );

        Map<BlockPos, Integer> leafDistances =
            leafDistances(plan);

        for (
            Map.Entry<
                BlockPos,
                net.minecraft.world.level.block.state.BlockState
            > entry :
                plan.leaves().entrySet()
        ) {
            Integer distance =
                leafDistances.get(
                    entry.getKey()
                );

            check(
                distance != null
                    && distance >= 1
                    && distance
                    <= LeavesBlock.DECAY_DISTANCE - 1,
                "Leaf is disconnected or would immediately decay at "
                    + entry.getKey()
            );

            check(
                !entry.getValue()
                    .hasProperty(
                        LeavesBlock.DISTANCE
                    )
                    || entry.getValue()
                        .getValue(
                            LeavesBlock.DISTANCE
                        )
                        .equals(distance),
                "Encoded leaf distance differs from geometry at "
                    + entry.getKey()
            );

            check(
                !entry.getValue()
                    .hasProperty(
                        LeavesBlock.PERSISTENT
                    )
                    || !entry.getValue()
                        .getValue(
                            LeavesBlock.PERSISTENT
                        ),
                "Generated leaf was marked persistent"
            );
        }

        for (
            BlockPos position :
                plan.allBlocks()
        ) {
            int dx =
                position.getX()
                    - origin.getX();

            int dz =
                position.getZ()
                    - origin.getZ();

            check(
                Math.abs(dx)
                    <= configuration
                        .maxHorizontalReach()
                    && Math.abs(dz)
                    <= configuration
                        .maxHorizontalReach(),
                "Tree escaped the configured horizontal safety radius: "
                    + "profile="
                    + plan.profileName()
                    + " origin="
                    + origin
                    + " position="
                    + position
                    + " delta="
                    + dx
                    + ","
                    + dz
                    + " kind="
                    + (
                        plan.logs().containsKey(position)
                            ? "log"
                            : "leaf"
                    )
                    + " trunkCenter="
                    + plan.trunkCenters()
                        .contains(position)
            );
        }

        for (
            int index = 1;
            index < plan.trunkCenters().size();
            index++
        ) {
            BlockPos previous =
                plan.trunkCenters()
                    .get(index - 1);

            BlockPos current =
                plan.trunkCenters()
                    .get(index);

            check(
                current.getY()
                    == previous.getY() + 1,
                "Trunk layers are not vertically continuous"
            );

            check(
                Math.abs(
                    current.getX()
                        - previous.getX()
                ) <= 1
                    && Math.abs(
                        current.getZ()
                            - previous.getZ()
                    ) <= 1,
                "Trunk curve changes direction too abruptly"
            );
        }

        for (
            BlockPos tip :
                plan.branchTips()
        ) {
            check(
                plan.logs().containsKey(tip),
                "Branch tip is not connected to a log"
            );

            boolean attachedFoliage =
                false;

            for (
                Direction direction :
                    Direction.values()
            ) {
                if (
                    plan.leaves()
                        .containsKey(
                            tip.relative(direction)
                        )
                ) {
                    attachedFoliage =
                        true;

                    break;
                }
            }

            check(
                attachedFoliage,
                "Branch tip has no attached foliage"
            );
        }

        if (!plan.branchTips().isEmpty()) {
            boolean hasHorizontalBranchLog =
                plan.logs()
                    .values()
                    .stream()
                    .anyMatch(
                        state ->
                            state.hasProperty(
                                RotatedPillarBlock.AXIS
                            )
                                && state.getValue(
                                    RotatedPillarBlock.AXIS
                                )
                                != Direction.Axis.Y
                    );

            check(
                hasHorizontalBranchLog,
                "Connected branches were not assigned a horizontal log axis"
            );
        }
    }

    private static void checkConnected(
        Set<BlockPos> positions,
        String message
    ) {
        Queue<BlockPos> queue =
            new ArrayDeque<>();

        Set<BlockPos> visited =
            new HashSet<>();

        BlockPos first =
            positions.iterator().next();

        queue.add(first);
        visited.add(first);

        while (!queue.isEmpty()) {
            BlockPos current =
                queue.remove();

            for (
                Direction direction :
                    Direction.values()
            ) {
                BlockPos neighbor =
                    current.relative(direction);

                if (
                    positions.contains(neighbor)
                        && visited.add(neighbor)
                ) {
                    queue.add(neighbor);
                }
            }
        }

        check(
            visited.size()
                == positions.size(),
            message
                + ": "
                + visited.size()
                + "/"
                + positions.size()
        );
    }

    private static Map<BlockPos, Integer> leafDistances(
        TreePlan plan
    ) {
        Map<BlockPos, Integer> distances =
            new HashMap<>();

        Queue<BlockPos> queue =
            new ArrayDeque<>();

        for (
            BlockPos log :
                plan.logs().keySet()
        ) {
            distances.put(log, 0);
            queue.add(log);
        }

        while (!queue.isEmpty()) {
            BlockPos current =
                queue.remove();

            int nextDistance =
                distances.get(current) + 1;

            if (
                nextDistance
                    >= LeavesBlock.DECAY_DISTANCE
            ) {
                continue;
            }

            for (
                Direction direction :
                    Direction.values()
            ) {
                BlockPos neighbor =
                    current.relative(direction);

                if (
                    plan.leaves()
                        .containsKey(neighbor)
                        && !distances
                            .containsKey(neighbor)
                ) {
                    distances.put(
                        neighbor,
                        nextDistance
                    );

                    queue.add(neighbor);
                }
            }
        }

        return distances;
    }

    private static long normalizedSignature(
        TreePlan plan,
        BlockPos origin
    ) {
        List<BlockPos> logs =
            new ArrayList<>(
                plan.logs().keySet()
            );

        List<BlockPos> leaves =
            new ArrayList<>(
                plan.leaves().keySet()
            );

        logs.sort(BlockPos::compareTo);
        leaves.sort(BlockPos::compareTo);

        long signature =
            0xcbf29ce484222325L;

        for (BlockPos position : logs) {
            signature = hashPosition(
                signature,
                position,
                origin,
                0x4CL
            );
        }

        for (BlockPos position : leaves) {
            signature = hashPosition(
                signature,
                position,
                origin,
                0x46L
            );
        }

        return signature;
    }

    private static long hashPosition(
        long signature,
        BlockPos position,
        BlockPos origin,
        long marker
    ) {
        signature ^= marker;
        signature *= 0x100000001b3L;

        signature ^= BlockPos.asLong(
            position.getX()
                - origin.getX(),
            position.getY()
                - origin.getY(),
            position.getZ()
                - origin.getZ()
        );

        return signature
            * 0x100000001b3L;
    }

    private static long mix64(long value) {
        value =
            (
                value
                    ^ (value >>> 30)
            )
                * 0xBF58476D1CE4E5B9L;

        value =
            (
                value
                    ^ (value >>> 27)
            )
                * 0x94D049BB133111EBL;

        return value
            ^ (value >>> 31);
    }

    /**
     * Temporarily makes a frozen intrusive registry writable
     * for the standalone JavaExec validation process.
     */
    private static void reopenIntrusiveRegistry(
        Registry<?> registry
    ) {
        try {
            MappedRegistry<?> mapped =
                (MappedRegistry<?>) registry;

            Field frozen =
                MappedRegistry.class
                    .getDeclaredField("frozen");

            frozen.setAccessible(true);
            frozen.setBoolean(mapped, false);

            Field intrusive =
                MappedRegistry.class
                    .getDeclaredField(
                        "unregisteredIntrusiveHolders"
                    );

            intrusive.setAccessible(true);

            intrusive.set(
                mapped,
                new IdentityHashMap<>()
            );
        } catch (
            ReflectiveOperationException exception
        ) {
            throw new IllegalStateException(
                "Could not prepare standalone registries",
                exception
            );
        }
    }

    private static void check(
        boolean condition,
        String message
    ) {
        if (!condition) {
            throw new IllegalStateException(
                message
            );
        }
    }

    private static <T> T require(
        T value,
        String message
    ) {
        check(
            value != null,
            message
        );

        return value;
    }
}
