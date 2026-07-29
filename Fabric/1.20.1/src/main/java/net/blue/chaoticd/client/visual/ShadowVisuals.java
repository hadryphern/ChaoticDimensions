package net.blue.chaoticd.client.visual;

import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;

/** Registers the client-only atmosphere used exclusively by the Shadow Dimension. */
public final class ShadowVisuals {
    private static boolean initialized;

    private ShadowVisuals() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        DimensionRenderingRegistry.registerDimensionEffects(
            ShadowVisualConfig.DIMENSION_EFFECTS,
            new ShadowDimensionEffects()
        );

        initialized = true;
    }
}
