package net.blue.chaoticd.client.visual;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Registers and owns all visual resources exclusive to the Aurora Dimension. */
public final class AuroraVisuals {
    private static AuroraCloudRenderer clouds;
    private static AuroraRainbowRenderer rainbows;
    private static boolean initialized;

    private AuroraVisuals() {
    }

    public static synchronized void initialize() {
        if (initialized) return;

        clouds = new AuroraCloudRenderer(AuroraVisualConfig.clouds());
        rainbows = new AuroraRainbowRenderer(AuroraVisualConfig.rainbows());

        DimensionRenderingRegistry.registerDimensionEffects(
            AuroraVisualConfig.DIMENSION_EFFECTS,
            new AuroraDimensionEffects(AuroraVisualConfig.FALLBACK_CLOUD_HEIGHT)
        );
        DimensionRenderingRegistry.registerCloudRenderer(AuroraVisualConfig.AURORA_DIMENSION, clouds);
        WorldRenderEvents.AFTER_SETUP.register(AuroraVisuals::renderSkyDecorations);
        InvalidateRenderStateCallback.EVENT.register(AuroraVisuals::invalidateRenderState);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> close());
        initialized = true;
    }

    private static void renderSkyDecorations(WorldRenderContext context) {
        if (rainbows == null || !context.world().dimension().equals(AuroraVisualConfig.AURORA_DIMENSION)
            || Minecraft.getInstance().gui.getBossOverlay().shouldCreateWorldFog()) {
            return;
        }

        if (context.camera().getEntity() instanceof LivingEntity living
            && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }

        context.profiler().push("chaoticd_aurora_rainbows");
        try {
            rainbows.render(context.matrixStack(), context.projectionMatrix(), context.camera(), context.tickDelta());
        } finally {
            context.profiler().pop();
        }
    }

    private static void invalidateRenderState() {
        if (clouds != null) clouds.invalidate();
        if (rainbows != null) rainbows.close();
    }

    private static void close() {
        if (clouds != null) clouds.close();
        if (rainbows != null) rainbows.close();
    }
}
