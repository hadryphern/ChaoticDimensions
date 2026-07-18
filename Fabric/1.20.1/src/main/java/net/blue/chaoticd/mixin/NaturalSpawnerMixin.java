package net.blue.chaoticd.mixin;

import net.blue.chaoticd.worldgen.RosalitaSpawnPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels only the vanilla NaturalSpawner path. Commands, spawn eggs, pets and
 * entities that walk into the biome never enter this method and remain valid.
 */
@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {
    @Inject(method = "isValidSpawnPostitionForType", at = @At("HEAD"), cancellable = true)
    private static void chaoticd$blockNaturalSpawnsInRosalita(ServerLevel level, MobCategory category,
            StructureManager structureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnData,
            BlockPos.MutableBlockPos position, double distanceToPlayer,
            CallbackInfoReturnable<Boolean> callback) {
        if (!RosalitaSpawnPolicy.isNaturalSpawnAllowed(level, position, spawnData.type)) {
            callback.setReturnValue(false);
        }
    }
}
