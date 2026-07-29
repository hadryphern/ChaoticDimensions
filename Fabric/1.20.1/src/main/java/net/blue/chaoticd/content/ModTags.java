package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

/** Central tag keys used by code-backed Chaotic Dimensions systems. */
public final class ModTags {
    public static final TagKey<Fluid> DREAM_FLUID =
        TagKey.create(
            Registries.FLUID,
            id("dream_fluid")
        );

    /** All blocks governed by Crystal's Silk Touch harvesting rule. */
    public static final TagKey<Block> CRYSTAL_SENSITIVE =
        TagKey.create(
            Registries.BLOCK,
            id("crystal_sensitive")
        );

    /**
     * Dream Fluid structures that can be located while inside Aurora.
     */
    public static final TagKey<Structure>
        DREAM_FLUID_AURORA_STRUCTURES =
        TagKey.create(
            Registries.STRUCTURE,
            id("dream_fluid_aurora")
        );

    /**
     * Underground and sky Dream Fluid structures in the Overworld.
     */
    public static final TagKey<Structure>
        DREAM_FLUID_OVERWORLD_STRUCTURES =
        TagKey.create(
            Registries.STRUCTURE,
            id("dream_fluid_overworld")
        );

    /** Entity classifications used by the dimension-specific soul drops. */
    public static final TagKey<EntityType<?>> AURORA_COMMON_MOBS =
        TagKey.create(Registries.ENTITY_TYPE, id("aurora_common_mobs"));
    public static final TagKey<EntityType<?>> AURORA_MINIBOSSES =
        TagKey.create(Registries.ENTITY_TYPE, id("aurora_minibosses"));
    public static final TagKey<EntityType<?>> SHADOW_COMMON_MOBS =
        TagKey.create(Registries.ENTITY_TYPE, id("shadow_common_mobs"));
    public static final TagKey<EntityType<?>> SHADOW_MINIBOSSES =
        TagKey.create(Registries.ENTITY_TYPE, id("shadow_minibosses"));

    private ModTags() {
    }

    private static ResourceLocation id(
        String path
    ) {
        return new ResourceLocation(
            ChaoticDimensions.MOD_ID,
            path
        );
    }
}
