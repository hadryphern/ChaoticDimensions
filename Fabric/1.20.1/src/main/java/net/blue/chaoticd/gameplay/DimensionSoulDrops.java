package net.blue.chaoticd.gameplay;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Server-side soul drops restricted to explicitly classified dimension mobs.
 *
 * <p>The tag files intentionally begin empty: Aurora and Shadow currently
 * have no natural common mobs or minibosses. This avoids treating testing
 * spawn eggs as an unintended farm. Once a real entity is added to one of the
 * tags, its configured drop becomes active immediately and only in its named
 * dimension.</p>
 */
public final class DimensionSoulDrops {
    private static final ResourceKey<Level> AURORA_DIMENSION = dimension("aurora_dimension");
    private static final ResourceKey<Level> SHADOW_DIMENSION = dimension("shadow_dimension");

    private static final float COMMON_SOUL_CHANCE = 0.05F;
    private static final float DEMONIC_SOUL_CHANCE = 0.15F;
    private static final float VOID_SOUL_CHANCE = 0.55F;

    private DimensionSoulDrops() {
    }

    public static void onLivingEntityDeath(LivingEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }

        RandomSource random = entity.getRandom();

        if (entity.level().dimension().equals(AURORA_DIMENSION)) {
            if (entity.getType().is(ModTags.AURORA_COMMON_MOBS)) {
                rollDrop(entity, random, ModItems.AURORA_SOUL, COMMON_SOUL_CHANCE);
                rollDrop(entity, random, ModItems.CRYSTALINE_SOUL, COMMON_SOUL_CHANCE);
            }
            if (entity.getType().is(ModTags.AURORA_MINIBOSSES)) {
                rollDrop(entity, random, ModItems.DEMONIC_SOULD, DEMONIC_SOUL_CHANCE);
            }
            return;
        }

        if (entity.level().dimension().equals(SHADOW_DIMENSION)) {
            if (entity.getType().is(ModTags.SHADOW_COMMON_MOBS)) {
                rollDrop(entity, random, ModItems.SHADOW_SOUL, COMMON_SOUL_CHANCE);
            }
            if (entity.getType().is(ModTags.SHADOW_MINIBOSSES)) {
                rollDrop(entity, random, ModItems.VOID_SOUL, VOID_SOUL_CHANCE);
            }
        }
    }

    private static void rollDrop(
        LivingEntity entity,
        RandomSource random,
        Item item,
        float chance
    ) {
        if (random.nextFloat() < chance) {
            entity.spawnAtLocation(item);
        }
    }

    private static ResourceKey<Level> dimension(String path) {
        return ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(ChaoticDimensions.MOD_ID, path)
        );
    }
}
