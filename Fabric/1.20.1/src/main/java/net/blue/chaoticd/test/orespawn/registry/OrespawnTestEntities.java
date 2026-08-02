package net.blue.chaoticd.test.orespawn.registry;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.entity.OrespawnTestReferenceEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Pig;

/** Registries for original test-only entities, loaded only when explicitly enabled. */
public final class OrespawnTestEntities {
    public static final EntityType<OrespawnTestReferenceEntity> REFERENCE_PROXY = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "orespawn_test_reference_proxy"),
        EntityType.Builder.<OrespawnTestReferenceEntity>of(
            OrespawnTestReferenceEntity::new,
            MobCategory.CREATURE
        ).sized(0.9F, 0.9F).clientTrackingRange(8).build("pig")
    );

    private OrespawnTestEntities() {
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(REFERENCE_PROXY, Pig.createAttributes());
    }
}
