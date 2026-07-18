package net.blue.chaoticd.worldgen.tag;

import net.blue.chaoticd.ChaoticDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Tags used by the Rosalita underground replacement feature. */
public final class ModBlockTags {
    public static final TagKey<Block> FORBIDDEN_IN_ROSALITA_UNDERGROUND = create("forbidden_in_rosalita_underground");
    public static final TagKey<Block> ALLOWED_ROSALITA_ORES = create("allowed_rosalita_ores");

    private ModBlockTags() {
    }

    private static TagKey<Block> create(String path) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(ChaoticDimensions.MOD_ID, path));
    }
}
