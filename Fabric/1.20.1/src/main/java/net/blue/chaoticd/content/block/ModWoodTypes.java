package net.blue.chaoticd.content.block;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Shared block and wood types for every Rosalita wood-family block. */
public final class ModWoodTypes {
    public static final BlockSetType ROSALITA_SET = BlockSetTypeBuilder.copyOf(BlockSetType.OAK)
        .register(new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita"));
    public static final WoodType ROSALITA = WoodTypeBuilder.copyOf(WoodType.OAK)
        .register(new ResourceLocation(ChaoticDimensions.MOD_ID, "rosalita"), ROSALITA_SET);

    private ModWoodTypes() {
    }
}
