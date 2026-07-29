package net.blue.chaoticd.client.visual;

import java.util.List;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.client.visual.AuroraCloudRenderer.CloudLayer;
import net.blue.chaoticd.client.visual.AuroraCloudRenderer.Settings;
import net.blue.chaoticd.client.visual.AuroraRainbowRenderer.ArcSettings;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Single tuning map for Aurora's client-only atmosphere. */
public final class AuroraVisualConfig {
    public static final ResourceKey<Level> AURORA_DIMENSION = ResourceKey.create(
        Registries.DIMENSION, id("aurora_dimension"));
    public static final ResourceLocation DIMENSION_EFFECTS = id("aurora");

    /** These values are mirrored in aurora_biome.json and checked by AuroraVisualValidator. */
    public static final int SKY_COLOR = 0xEAB8D7;
    public static final int FOG_COLOR = 0xF3DDEA;

    public static final float FALLBACK_CLOUD_HEIGHT = 308.0F;

    private AuroraVisualConfig() {
    }

    public static Settings clouds() {
        return new Settings(
            new CloudLayer(
                308.0F, 1_280.0F, 4_096.0F,
                1.00F, 0.92F, 0.97F, 0.64F,
                0.18F, 0.04F
            ),
            new CloudLayer(
                356.0F, 920.0F, 4_096.0F,
                0.92F, 0.86F, 1.00F, 0.30F,
                -0.07F, 0.13F
            )
        );
    }

    public static AuroraRainbowRenderer.Settings rainbows() {
        return new AuroraRainbowRenderer.Settings(
            72,
            3,
            0.16F,
            14.0F,
            0.14F,
            0.38F,
            List.of(
                0xF3A1AE,
                0xF6C38E,
                0xF8E394,
                0xBDE3B0,
                0x9EDCE5,
                0xAEBBEA,
                0xD4A9E8
            ),
            List.of(
                new ArcSettings(-32.0F, 210.0F, -38.0F, 106.0F, 22.0F, 18.0F, 0.32F),
                new ArcSettings(152.0F, 250.0F, -26.0F, 82.0F, 15.0F, 22.0F, 0.14F)
            )
        );
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, path);
    }
}
