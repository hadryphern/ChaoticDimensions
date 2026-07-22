package net.blue.chaoticd.worldgen.tree;

import com.mojang.serialization.Codec;
import java.util.Comparator;
import java.util.Map;
import net.blue.chaoticd.worldgen.tree.AuroraTreePlanner.TreePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/** Places an Aurora tree only after validating its complete deterministic geometry. */
public final class AuroraTreeFeature extends Feature<AuroraTreeConfiguration> {
    private static final int WORLDGEN_FLAGS = 19;

    public AuroraTreeFeature(Codec<AuroraTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<AuroraTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        AuroraTreeConfiguration configuration = context.config();
        TreePlan plan = AuroraTreePlanner.plan(configuration, level.getSeed(), context.origin());
        if (!hasValidBounds(level, plan) || !hasValidGround(level, configuration, plan)
            || !hasClearSpace(level, plan)) {
            return false;
        }

        plan.logs().entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getKey().getY()))
            .forEach(entry -> level.setBlock(entry.getKey(), entry.getValue(), WORLDGEN_FLAGS));
        plan.leaves().entrySet().stream()
            .sorted(Comparator.comparingInt(entry -> entry.getKey().getY()))
            .forEach(entry -> level.setBlock(entry.getKey(), entry.getValue(), WORLDGEN_FLAGS));
        return true;
    }

    private static boolean hasValidBounds(WorldGenLevel level, TreePlan plan) {
        for (BlockPos position : plan.allBlocks()) {
            if (position.getY() <= level.getMinBuildHeight() || position.getY() >= level.getMaxBuildHeight() - 1
                || !level.ensureCanWrite(position)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidGround(WorldGenLevel level, AuroraTreeConfiguration configuration,
                                          TreePlan plan) {
        for (BlockPos position : plan.requiredGround()) {
            BlockState ground = level.getBlockState(position);
            boolean valid = configuration.groundStates().stream()
                .anyMatch(configured -> ground.is(configured.getBlock()));
            if (!valid) return false;
        }
        return true;
    }

    private static boolean hasClearSpace(WorldGenLevel level, TreePlan plan) {
        for (Map.Entry<BlockPos, BlockState> entry : plan.logs().entrySet()) {
            if (!canReplace(level.getBlockState(entry.getKey()))) return false;
        }
        for (Map.Entry<BlockPos, BlockState> entry : plan.leaves().entrySet()) {
            if (!canReplace(level.getBlockState(entry.getKey()))) return false;
        }
        return true;
    }

    private static boolean canReplace(BlockState state) {
        if (!state.getFluidState().isEmpty() || state.is(BlockTags.FEATURES_CANNOT_REPLACE)
            || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
            return false;
        }
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES);
    }
}
