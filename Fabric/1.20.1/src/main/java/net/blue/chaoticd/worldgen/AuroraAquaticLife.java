package net.blue.chaoticd.worldgen;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Mantém uma pequena população de peixes tropicais vanilla
 * nos lagos calmos da dimensão Aurora.
 *
 * <p>O sistema procura apenas água-fonte com profundidade suficiente,
 * evitando que peixes apareçam em cachoeiras ou pequenos fluxos.</p>
 */
public final class AuroraAquaticLife {
    private static final ResourceKey<Level> AURORA_DIMENSION =
        ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(
                ChaoticDimensions.MOD_ID,
                "aurora_dimension"
            )
        );

    /**
     * Intervalo entre verificações.
     *
     * <p>100 ticks equivalem aproximadamente a 5 segundos.</p>
     */
    private static final int CHECK_INTERVAL_TICKS = 100;

    /**
     * Distância horizontal em que o sistema procura lagos
     * ao redor de cada jogador.
     */
    private static final int SEARCH_RADIUS = 28;

    /**
     * Quantidade máxima de peixes tropicais perto de um jogador.
     */
    private static final int MAX_FISH_NEAR_PLAYER = 10;

    /**
     * Número de posições aleatórias testadas em cada verificação.
     */
    private static final int WATER_SEARCH_ATTEMPTS = 18;

    /**
     * Quantos blocos abaixo do topo da coluna podem ser examinados
     * para encontrar a superfície de um lago.
     */
    private static final int WATER_SURFACE_SCAN_DEPTH = 7;

    private AuroraAquaticLife() {
    }

    /**
     * Registra a verificação no final de cada tick dos mundos do servidor.
     */
    public static void initialize() {
        ServerTickEvents.END_WORLD_TICK.register(
            AuroraAquaticLife::tickWorld
        );
    }

    /**
     * Executa o controle populacional apenas na dimensão Aurora.
     */
    private static void tickWorld(ServerLevel level) {
        if (!level.dimension().equals(AURORA_DIMENSION)
            || level.getGameTime() % CHECK_INTERVAL_TICKS != 0L
            || level.players().isEmpty()) {
            return;
        }

        RandomSource random = level.getRandom();

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }

            AABB populationArea =
                player.getBoundingBox().inflate(
                    SEARCH_RADIUS,
                    18.0D,
                    SEARCH_RADIUS
                );

            int nearbyFish =
                level.getEntitiesOfClass(
                    TropicalFish.class,
                    populationArea,
                    TropicalFish::isAlive
                ).size();

            if (nearbyFish >= MAX_FISH_NEAR_PLAYER) {
                continue;
            }

            for (
                int attempt = 0;
                attempt < WATER_SEARCH_ATTEMPTS;
                attempt++
            ) {
                int x =
                    player.getBlockX()
                        + random.nextInt(
                            SEARCH_RADIUS * 2 + 1
                        )
                        - SEARCH_RADIUS;

                int z =
                    player.getBlockZ()
                        + random.nextInt(
                            SEARCH_RADIUS * 2 + 1
                        )
                        - SEARCH_RADIUS;

                BlockPos spawnPos = findCalmWater(
                    level,
                    random,
                    x,
                    z,
                    player.getBlockY()
                );

                if (spawnPos != null) {
                    spawnFish(
                        level,
                        random,
                        spawnPos
                    );

                    break;
                }
            }
        }
    }

    /**
     * Procura uma posição submersa dentro de um lago estável.
     *
     * @return posição válida dentro da água ou {@code null}
     */
    private static BlockPos findCalmWater(
        ServerLevel level,
        RandomSource random,
        int x,
        int z,
        int playerY
    ) {
        int topY = level.getHeight(
            Heightmap.Types.WORLD_SURFACE,
            x,
            z
        );

        if (topY <= level.getMinBuildHeight() + 1) {
            return null;
        }

        BlockPos surface = null;

        int minimumScanY = Math.max(
            level.getMinBuildHeight(),
            topY - WATER_SURFACE_SCAN_DEPTH
        );

        for (int y = topY; y >= minimumScanY; y--) {
            BlockPos candidate =
                new BlockPos(x, y, z);

            if (isSourceWater(level, candidate)) {
                surface = candidate;
                break;
            }
        }

        if (surface == null
            || Math.abs(surface.getY() - playerY) > 28) {
            return null;
        }

        /*
         * Exige várias fontes de água vizinhas.
         * Uma cachoeira ou corrente estreita normalmente não passa
         * nesta verificação.
         */
        int calmNeighbours = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (isSourceWater(
                    level,
                    surface.offset(dx, 0, dz)
                )) {
                    calmNeighbours++;
                }
            }
        }

        if (calmNeighbours < 6) {
            return null;
        }

        /*
         * Mede quantos blocos de água existem abaixo da superfície.
         */
        int waterDepth = 0;

        while (
            waterDepth < 5
                && level.getFluidState(
                    surface.below(waterDepth)
                ).is(FluidTags.WATER)
        ) {
            waterDepth++;
        }

        if (waterDepth < 2) {
            return null;
        }

        /*
         * Escolhe aleatoriamente uma altura dentro da coluna de água.
         */
        return surface.below(
            random.nextInt(waterDepth)
        );
    }

    /**
     * Verifica se o bloco contém uma fonte de água completa.
     */
    private static boolean isSourceWater(
        ServerLevel level,
        BlockPos pos
    ) {
        return level.getFluidState(pos).is(FluidTags.WATER)
            && level.getFluidState(pos).isSource();
    }

    /**
     * Cria um peixe tropical vanilla com variante aleatória.
     */
    private static void spawnFish(
        ServerLevel level,
        RandomSource random,
        BlockPos pos
    ) {
        TropicalFish fish =
            EntityType.TROPICAL_FISH.create(level);

        if (fish == null) {
            return;
        }

        fish.moveTo(
            pos.getX() + 0.5D,
            pos.getY() + 0.35D,
            pos.getZ() + 0.5D,
            random.nextFloat() * 360.0F,
            0.0F
        );

        /*
         * No Minecraft 1.20.1 com mappings oficiais da Mojang,
         * finalizeSpawn exige cinco argumentos.
         *
         * O quarto argumento é SpawnGroupData.
         * O quinto argumento é CompoundTag.
         *
         * Ambos podem ser null para um spawn natural comum.
         */
        fish.finalizeSpawn(
            level,
            level.getCurrentDifficultyAt(pos),
            MobSpawnType.NATURAL,
            null,
            null
        );

        level.addFreshEntity(fish);
    }
}