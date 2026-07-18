package net.blue.chaoticd.worldgen.biome;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.worldgen.RosalitaConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

/** Registry keys for biomes supplied by Chaotic Dimensions data resources. */
public final class ModBiomes {
    public static final ResourceKey<Biome> ROSALITA_BIOME = ResourceKey.create(
        Registries.BIOME, new ResourceLocation(ChaoticDimensions.MOD_ID, RosalitaConstants.BIOME_ID));

    private ModBiomes() {
    }
}
