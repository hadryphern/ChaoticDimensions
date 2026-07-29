package net.blue.chaoticd.client;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.minecraft.resources.ResourceLocation;

/**
 * Uses animated, interpolated Aurora-colored sprites for Dream Fluid.
 *
 * <p>The PNG metadata performs the smooth pink-purple-blue fade without
 * forcing chunk rebuilds every frame.</p>
 */
public final class DreamFluidRenderHandler
    extends SimpleFluidRenderHandler {

    private static final ResourceLocation STILL =
        new ResourceLocation(
            ChaoticDimensions.MOD_ID,
            "block/dream_fluid_still"
        );

    private static final ResourceLocation FLOW =
        new ResourceLocation(
            ChaoticDimensions.MOD_ID,
            "block/dream_fluid_flow"
        );

    public DreamFluidRenderHandler() {
        super(STILL, FLOW);
    }
}
