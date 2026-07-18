package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.worldgen.biome.RosalitaRegion;
import net.minecraft.resources.ResourceLocation;
import terrablender.api.Regions;
import terrablender.api.TerraBlenderApi;

/** TerraBlender entrypoint used only to add the low-frequency Overworld region. */
public final class ChaoticDimensionsTerraBlender implements TerraBlenderApi {
    @Override
    public void onTerraBlenderInitialized() {
        Regions.register(new RosalitaRegion(new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita_overworld"),
            RosalitaConstants.ROSALITA_REGION_WEIGHT));
    }
}
