package net.blue.chaoticd.client;

import net.blue.chaoticd.content.block.ModBlocks;
import net.blue.chaoticd.worldgen.RosalitaConstants;
import net.blue.chaoticd.worldgen.biome.ModBiomes;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;

/** Client-only render and tint registrations for Rosalita foliage. */
public final class RosalitaClientVisuals {
    private RosalitaClientVisuals() {
    }

    public static void initialize() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.get("rosalita_leaves"), RenderType.cutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.get("rosalita_sapling"), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.get("potted_rosalita_sapling"), RenderType.cutout());
        ColorProviderRegistry.BLOCK.register((state, world, position, tintIndex) -> {
            if (world instanceof ClientLevel level && position != null && level.getBiome(position).is(ModBiomes.ROSALITA_BIOME)) {
                return BiomeColors.getAverageFoliageColor(world, position);
            }
            return RosalitaConstants.FOLIAGE;
        }, ModBlocks.get("rosalita_leaves"));
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> RosalitaConstants.FOLIAGE,
            ModBlocks.get("rosalita_leaves"));
    }
}
