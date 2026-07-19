package net.blue.chaoticd.client;

import net.blue.chaoticd.content.block.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

/** Client-only Fabric entrypoint. Renderers and screens are registered here. */
public final class ChaoticDimensionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RosalitaClientVisuals.initialize();
        LightShadowClientVisuals.initialize();
        BlockEntityRendererRegistry.register(ModBlockEntities.ROSALITA_CHEST, RosalitaChestRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntities.ROSALITA_TRAPPED_CHEST, RosalitaChestRenderer::new);
    }
}
