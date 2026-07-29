package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.fluid.DreamFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;

/** Registers both source and flowing states of Dream Fluid. */
public final class ModFluids {
    public static final FlowingFluid FLOWING_DREAM_FLUID = register(
        "flowing_dream_fluid",
        new DreamFluid.Flowing()
    );

    public static final FlowingFluid DREAM_FLUID = register(
        "dream_fluid",
        new DreamFluid.Source()
    );

    private ModFluids() {
    }

    private static FlowingFluid register(String id, FlowingFluid fluid) {
        return Registry.register(
            BuiltInRegistries.FLUID,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id),
            fluid
        );
    }

    public static void initialize() {
        // Static fields perform registry insertion.
    }
}
