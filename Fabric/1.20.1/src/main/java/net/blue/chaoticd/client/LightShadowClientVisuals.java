package net.blue.chaoticd.client;

import net.blue.chaoticd.content.block.ModBlocks;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.RenderType;

/** Client-only foliage treatment: no vanilla-green fallback in Light or Shadow assets. */
public final class LightShadowClientVisuals {
    private static final int LIGHT = 0xFFFFFF;
    private static final int SHADOW = 0x030303;

    private LightShadowClientVisuals() {
    }

    public static void initialize() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), ModBlocks.get("bloco_folha_branca"),
            ModBlocks.get("folha_sombra"), ModBlocks.get("light_leaves"), ModBlocks.get("shadow_leaves"),
            ModBlocks.get("light_sapling"), ModBlocks.get("shadow_sapling"), ModBlocks.get("light_lattice"), ModBlocks.get("shadow_lattice"));
        register(LIGHT, "bloco_folha_branca", "light_leaves");
        register(SHADOW, "folha_sombra", "shadow_leaves");
    }

    private static void register(int color, String... ids) {
        net.minecraft.world.level.block.Block[] blocks = new net.minecraft.world.level.block.Block[ids.length];
        for (int index = 0; index < ids.length; index++) {
            blocks[index] = ModBlocks.get(ids[index]);
        }
        ColorProviderRegistry.BLOCK.register((state, world, position, tintIndex) -> color, blocks);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> color, blocks);
    }
}
