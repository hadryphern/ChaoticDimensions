package net.blue.chaoticd.content.block;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.block.entity.RosalitaChestBlockEntity;
import net.blue.chaoticd.content.block.entity.RosalitaTrappedChestBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Block entity types owned by the Fabric port. */
public final class ModBlockEntities {
    public static BlockEntityType<RosalitaChestBlockEntity> ROSALITA_CHEST;
    public static BlockEntityType<RosalitaTrappedChestBlockEntity> ROSALITA_TRAPPED_CHEST;
    private static boolean initialized;

    private ModBlockEntities() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        ROSALITA_CHEST = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita_chest"),
            BlockEntityType.Builder.of(RosalitaChestBlockEntity::new, ModBlocks.get("rosalita_chest")).build(null));
        ROSALITA_TRAPPED_CHEST = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita_trapped_chest"),
            BlockEntityType.Builder.of(RosalitaTrappedChestBlockEntity::new,
                ModBlocks.get("rosalita_trapped_chest")).build(null));
    }
}
