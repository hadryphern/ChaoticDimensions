package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.worldgen.PastelAuroraSkylandFeature;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.biome.Biome;

/** Registers the rare Overworld Skylands feature. It uses height placement data near the build ceiling. */
public final class ModWorldgen {
    public static final ResourceLocation PASTEL_AURORA_SKYLAND_ID = new ResourceLocation(ChaoticDimensions.MOD_ID, "pastel_aurora_skyland");
    public static final ResourceKey<Biome> AURORA_BIOME = ResourceKey.create(Registries.BIOME,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_biome"));
    public static final Feature<NoneFeatureConfiguration> PASTEL_AURORA_SKYLAND = Registry.register(
        BuiltInRegistries.FEATURE, PASTEL_AURORA_SKYLAND_ID, new PastelAuroraSkylandFeature(NoneFeatureConfiguration.CODEC));
    private static final ResourceKey<PlacedFeature> PASTEL_AURORA_SKYLAND_PLACED = ResourceKey.create(
        Registries.PLACED_FEATURE, PASTEL_AURORA_SKYLAND_ID);

    private ModWorldgen() {
    }

    public static void initialize() {
        BiomeModifications.addFeature(BiomeSelectors.includeByKey(AURORA_BIOME), GenerationStep.Decoration.RAW_GENERATION,
            PASTEL_AURORA_SKYLAND_PLACED);
    }
}
