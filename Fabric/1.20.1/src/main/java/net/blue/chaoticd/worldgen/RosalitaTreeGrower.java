package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/** Grows the existing Rosalita oak-style configured feature from saplings. */
public final class RosalitaTreeGrower extends AbstractTreeGrower {
    private static final ResourceKey<ConfiguredFeature<?, ?>> ROSALITA_TREE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE, new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita_oak_tree"));

    @Override
    protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
        return ROSALITA_TREE;
    }
}
