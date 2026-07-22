package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration;
import net.blue.chaoticd.worldgen.tree.AuroraTreeFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Registers code-backed world-generation features used by the Aurora datapack. */
public final class ModWorldgenFeatures {
    public static final Feature<AuroraTreeConfiguration> AURORA_TREE = Registry.register(
        BuiltInRegistries.FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_tree"),
        new AuroraTreeFeature(AuroraTreeConfiguration.CODEC)
    );

    private ModWorldgenFeatures() {
    }

    public static void initialize() {
        // Class loading performs the registry insertion before datapacks are decoded.
    }
}
