package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Adds the explicit Overworld and Nether ore palettes used by the progression. */
public final class LegacyWorldgen {
    private static final ResourceKey<PlacedFeature> RUBY_ORE = ResourceKey.create(
        Registries.PLACED_FEATURE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "ruby_ore")
    );

    private static final ResourceKey<PlacedFeature> JAX_ORE = feature("jax_ore");
    private static final ResourceKey<PlacedFeature> ROSALITA_ORE = feature("rosalita_ore");
    private static final ResourceKey<PlacedFeature> TITANIUM_ORE = feature("titanium_ore");
    private static final ResourceKey<PlacedFeature> NETHER_RUBY_ORE = feature("nether_ruby_ore");
    private static final ResourceKey<PlacedFeature> NETHER_JAX_ORE = feature("nether_jax_ore");
    private static final ResourceKey<PlacedFeature> NETHER_ROSALITA_ORE = feature("nether_rosalita_ore");

    private LegacyWorldgen() {
    }

    public static void initialize() {
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            RUBY_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            JAX_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            ROSALITA_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            TITANIUM_ORE
        );

        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheNether(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            NETHER_RUBY_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheNether(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            NETHER_JAX_ORE
        );
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheNether(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            NETHER_ROSALITA_ORE
        );
    }

    private static ResourceKey<PlacedFeature> feature(String id) {
        return ResourceKey.create(
            Registries.PLACED_FEATURE,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id)
        );
    }
}
