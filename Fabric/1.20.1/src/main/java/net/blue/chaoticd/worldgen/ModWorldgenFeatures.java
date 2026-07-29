package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.worldgen.shadow.ShadowLavaConfiguration;
import net.blue.chaoticd.worldgen.shadow.ShadowLavaFeature;
import net.blue.chaoticd.worldgen.tree.AuroraTreeConfiguration;
import net.blue.chaoticd.worldgen.tree.AuroraTreeFeature;
import net.blue.chaoticd.worldgen.water.AuroraWaterConfiguration;
import net.blue.chaoticd.worldgen.water.AuroraWaterFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Registers code-backed world-generation features used by both custom dimensions. */
public final class ModWorldgenFeatures {
    public static final Feature<AuroraTreeConfiguration> AURORA_TREE = Registry.register(
        BuiltInRegistries.FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_tree"),
        new AuroraTreeFeature(AuroraTreeConfiguration.CODEC)
    );

    public static final Feature<AuroraWaterConfiguration> AURORA_WATER = Registry.register(
        BuiltInRegistries.FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_water"),
        new AuroraWaterFeature(AuroraWaterConfiguration.CODEC)
    );

    public static final Feature<ShadowLavaConfiguration> SHADOW_LAVA = Registry.register(
        BuiltInRegistries.FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "shadow_lava"),
        new ShadowLavaFeature(ShadowLavaConfiguration.CODEC)
    );

    private ModWorldgenFeatures() {
    }

    public static void initialize() {
        // Class loading performs registry insertion before datapacks are decoded.
    }
}
