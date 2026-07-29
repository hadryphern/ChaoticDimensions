package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.block.CrystalFurnaceBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Block entity registrations used by restored functional legacy blocks. */
public final class ModBlockEntities {
    public static final BlockEntityType<CrystalFurnaceBlockEntity> CRYSTAL_FURNACE =
        Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            new ResourceLocation(ChaoticDimensions.MOD_ID, "crystal_furnace"),
            FabricBlockEntityTypeBuilder.create(
                CrystalFurnaceBlockEntity::new,
                ModBlocks.CRYSTAL_FURNACE
            ).build()
        );

    private ModBlockEntities() {
    }

    public static void initialize() {
        // Static field performs registry insertion.
    }
}
