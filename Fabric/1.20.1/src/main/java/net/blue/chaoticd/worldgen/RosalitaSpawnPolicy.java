package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.worldgen.tag.ModEntityTypeTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;

/** Central policy for natural mob spawning above and below Rosalita columns. */
public final class RosalitaSpawnPolicy {
    private RosalitaSpawnPolicy() {
    }

    public static boolean isNaturalSpawnAllowed(ServerLevel level, BlockPos position, EntityType<?> entityType) {
        return !RosalitaBiomeArea.isInsideRosalitaSurfaceColumn(level, position.getX(), position.getZ())
            || entityType.is(ModEntityTypeTags.ALLOWED_IN_ROSALITA_BIOME);
    }
}
