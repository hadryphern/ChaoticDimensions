package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.entity.LegacyCowEntity;
import net.blue.chaoticd.content.entity.LegacyCreeperEntity;
import net.blue.chaoticd.content.entity.LegacyMobVariant;
import net.blue.chaoticd.content.entity.LegacyPigEntity;
import net.blue.chaoticd.content.entity.SirOrensEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.Villager;

/** Legacy entities are registered for spawn eggs only; no biome spawn is added. */
public final class ModEntities {
    /** The one-owner merchant that appears after a player's first Aurora visit. */
    public static final EntityType<SirOrensEntity> SIR_ORENS = register(
        "sir_orens",
        "villager",
        EntityType.Builder.<SirOrensEntity>of(SirOrensEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.95F)
    );

    public static final EntityType<LegacyPigEntity> DIMENSION_PIG = register(
        "dimension_pig",
        "pig",
        EntityType.Builder.<LegacyPigEntity>of(
            (type, level) -> new LegacyPigEntity(type, level, LegacyMobVariant.DIMENSION_PIG),
            MobCategory.CREATURE
        ).sized(0.9F, 0.9F)
    );

    public static final EntityType<LegacyPigEntity> GOLD_DIMENSION_PIG = register(
        "gold_dimension_pig",
        "pig",
        EntityType.Builder.<LegacyPigEntity>of(
            (type, level) -> new LegacyPigEntity(type, level, LegacyMobVariant.GOLD_DIMENSION_PIG),
            MobCategory.CREATURE
        ).sized(0.9F, 0.9F)
    );

    public static final EntityType<LegacyCowEntity> APPLE_COW = register(
        "apple_cow",
        "cow",
        EntityType.Builder.<LegacyCowEntity>of(
            (type, level) -> new LegacyCowEntity(type, level, LegacyMobVariant.APPLE_COW),
            MobCategory.CREATURE
        ).sized(0.9F, 1.4F)
    );

    public static final EntityType<LegacyCowEntity> GOLDEN_APPLE_COW = register(
        "golden_apple_cow",
        "cow",
        EntityType.Builder.<LegacyCowEntity>of(
            (type, level) -> new LegacyCowEntity(type, level, LegacyMobVariant.GOLDEN_APPLE_COW),
            MobCategory.CREATURE
        ).sized(0.9F, 1.4F)
    );

    public static final EntityType<LegacyCowEntity> CRYSTAL_APPLE_COW = register(
        "crystal_apple_cow",
        "cow",
        EntityType.Builder.<LegacyCowEntity>of(
            (type, level) -> new LegacyCowEntity(type, level, LegacyMobVariant.CRYSTAL_APPLE_COW),
            MobCategory.CREATURE
        ).sized(0.9F, 1.4F)
    );

    public static final EntityType<LegacyCowEntity> CRYSTAL_GOLDEN_APPLE = register(
        "crystal_golden_apple",
        "cow",
        EntityType.Builder.<LegacyCowEntity>of(
            (type, level) -> new LegacyCowEntity(type, level, LegacyMobVariant.CRYSTAL_GOLDEN_APPLE),
            MobCategory.CREATURE
        ).sized(0.9F, 1.4F)
    );

    public static final EntityType<LegacyCreeperEntity> CRYSTAL_CREEPER = register(
        "crystal_creeper",
        "creeper",
        EntityType.Builder.<LegacyCreeperEntity>of(
            (type, level) -> new LegacyCreeperEntity(type, level, LegacyMobVariant.CRYSTAL_CREEPER),
            MobCategory.MONSTER
        ).sized(0.6F, 1.7F)
    );

    private ModEntities() {
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
        String id,
        String vanillaDataFixerId,
        EntityType.Builder<T> builder
    ) {
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id),
            /*
             * These legacy entities inherit vanilla Pig, Cow or Creeper data
             * and do not define extra persistent fields.  Supplying that
             * vanilla choice id lets Minecraft resolve the DataFixer schema
             * instead of emitting a startup error for every custom registry
             * id.
             */
            builder.clientTrackingRange(8).build(vanillaDataFixerId)
        );
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(SIR_ORENS, Villager.createAttributes());
        FabricDefaultAttributeRegistry.register(DIMENSION_PIG, Pig.createAttributes());
        FabricDefaultAttributeRegistry.register(GOLD_DIMENSION_PIG, Pig.createAttributes());
        FabricDefaultAttributeRegistry.register(APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(GOLDEN_APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(CRYSTAL_APPLE_COW, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(CRYSTAL_GOLDEN_APPLE, Cow.createAttributes());
        FabricDefaultAttributeRegistry.register(CRYSTAL_CREEPER, Creeper.createAttributes());
    }
}
