package net.blue.chaoticd.worldgen.tag;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/** Future Rosalita mobs opt into natural spawning through this empty-by-default tag. */
public final class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> ALLOWED_IN_ROSALITA_BIOME = TagKey.create(Registries.ENTITY_TYPE,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "allowed_in_rosalita_biome"));

    private ModEntityTypeTags() {
    }
}
