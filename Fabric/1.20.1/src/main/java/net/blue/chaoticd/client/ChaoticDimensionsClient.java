package net.blue.chaoticd.client;

import net.fabricmc.api.ClientModInitializer;
import net.blue.chaoticd.content.ModBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

/** Registers visual behavior that must never be loaded on a dedicated server. */
public final class ChaoticDimensionsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SapphiricVisuals.initialize();
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), ModBlocks.PASTEL_PINK_LEAVES,
            ModBlocks.PASTEL_PURPLE_LEAVES, ModBlocks.PASTEL_BLUE_LEAVES);
    }
}
