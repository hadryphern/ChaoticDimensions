package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.worldgen.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

/** Resolves Rosalita membership from the surface column rather than cave biomes. */
public final class RosalitaBiomeArea {
    private RosalitaBiomeArea() {
    }

    public static boolean isInsideRosalitaSurfaceColumn(LevelReader level, int x, int z) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        LevelHeightAccessor heights = level;
        int checkedY = Math.max(heights.getMinBuildHeight(), surfaceY);
        return level.getBiome(new BlockPos(x, checkedY, z)).is(ModBiomes.ROSALITA_BIOME);
    }
}
