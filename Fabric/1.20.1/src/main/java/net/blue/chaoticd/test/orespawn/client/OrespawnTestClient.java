package net.blue.chaoticd.test.orespawn.client;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.PigRenderer;

/** Client-only registration for the original reference proxy. */
public final class OrespawnTestClient {
    private OrespawnTestClient() {
    }

    public static void initialize() {
        // This must exist on every client because the entity type is a stable
        // registry ID even when the local test toggle is off.
        EntityRendererRegistry.register(OrespawnTestEntities.REFERENCE_PROXY, PigRenderer::new);
        ChaoticDimensions.LOGGER.info(
            "[Orespawn Test] Registered client renderer for the original vanilla-pig reference proxy."
        );
    }
}
