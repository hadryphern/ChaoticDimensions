package net.blue.chaoticd.client.visual;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Client-only identifiers and atmospheric tuning for the Shadow Dimension. */
public final class ShadowVisualConfig {
    public static final ResourceKey<Level> SHADOW_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        id("shadow_dimension")
    );

    public static final ResourceLocation DIMENSION_EFFECTS = id("shadow");

    private ShadowVisualConfig() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, path);
    }
}
