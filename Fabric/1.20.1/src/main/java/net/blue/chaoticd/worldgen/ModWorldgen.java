package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.worldgen.feature.RosalitaUndergroundFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;

/** Registers custom world-generation codecs before datapack biomes are decoded. */
public final class ModWorldgen {
    public static final Feature<?> ROSALITA_UNDERGROUND = Registry.register(BuiltInRegistries.FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita_underground"), new RosalitaUndergroundFeature());

    private ModWorldgen() {
    }

    public static void initialize() {
        // Forces class initialization during the common setup phase.
    }
}
