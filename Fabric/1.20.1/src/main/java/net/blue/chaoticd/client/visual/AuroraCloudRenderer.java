package net.blue.chaoticd.client.visual;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

/**
 * Lightweight, Aurora-only cloud renderer intended for registration through
 * {@link DimensionRenderingRegistry#registerCloudRenderer}.
 *
 * <p>The renderer reuses Minecraft's tileable cloud mask, but draws two independently tinted,
 * scaled and moving layers. Geometry is uploaded once and then shifted relative to the camera;
 * no vertex data or temporary collections are rebuilt every frame.</p>
 */
public final class AuroraCloudRenderer implements DimensionRenderingRegistry.CloudRenderer, AutoCloseable {
    private static final ResourceLocation VANILLA_CLOUD_TEXTURE =
        new ResourceLocation("minecraft", "textures/environment/clouds.png");
    /** Small enough that at least one row remains inside the two-chunk minimum far plane. */
    public static final float GRID_CELL_SIZE = 128.0F;

    private final Settings settings;
    private VertexBuffer primaryBuffer;
    private VertexBuffer secondaryBuffer;
    private boolean closed;

    public AuroraCloudRenderer(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    public AuroraCloudRenderer() {
        this(Settings.defaults());
    }

    public Settings settings() {
        return settings;
    }

    @Override
    public void render(WorldRenderContext context) {
        Objects.requireNonNull(context, "context");
        if (closed || Minecraft.getInstance().options.getCloudsType() == CloudStatus.OFF) {
            return;
        }

        ensureBuffers();
        if (primaryBuffer == null || secondaryBuffer == null) {
            return;
        }

        context.profiler().push("chaoticd_aurora_clouds");
        try {
            prepareRenderState();

            Vec3 cameraPosition = context.camera().getPosition();
            double cameraX = cameraPosition.x;
            double cameraY = cameraPosition.y;
            double cameraZ = cameraPosition.z;
            double timeSeconds = (context.world().getGameTime() + context.tickDelta()) / 20.0D;
            CloudLayer primary = settings.primary();
            CloudLayer secondary = settings.secondary();

            // Translucent layers are rendered back-to-front from any camera altitude.
            if (Math.abs(primary.height() - cameraY) >= Math.abs(secondary.height() - cameraY)) {
                renderLayer(context, primary, primaryBuffer, timeSeconds, cameraX, cameraY, cameraZ);
                renderLayer(context, secondary, secondaryBuffer, timeSeconds, cameraX, cameraY, cameraZ);
            } else {
                renderLayer(context, secondary, secondaryBuffer, timeSeconds, cameraX, cameraY, cameraZ);
                renderLayer(context, primary, primaryBuffer, timeSeconds, cameraX, cameraY, cameraZ);
            }
        } finally {
            restoreRenderState();
            context.profiler().pop();
        }
    }

    private void ensureBuffers() {
        if (primaryBuffer == null) {
            primaryBuffer = createLayerBuffer(settings.primary());
        }
        if (secondaryBuffer == null) {
            secondaryBuffer = createLayerBuffer(settings.secondary());
        }
    }

    private static VertexBuffer createLayerBuffer(CloudLayer layer) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float extent = layer.halfExtent();
        int cellsPerSide = cellsPerSide(layer);
        float cellSize = extent * 2.0F / cellsPerSide;
        for (int zCell = 0; zCell < cellsPerSide; zCell++) {
            float z0 = -extent + zCell * cellSize;
            float z1 = z0 + cellSize;
            for (int xCell = 0; xCell < cellsPerSide; xCell++) {
                float x0 = -extent + xCell * cellSize;
                float x1 = x0 + cellSize;
                addVertex(builder, x0, z0, x0 / layer.texturePeriod(), z0 / layer.texturePeriod());
                addVertex(builder, x0, z1, x0 / layer.texturePeriod(), z1 / layer.texturePeriod());
                addVertex(builder, x1, z1, x1 / layer.texturePeriod(), z1 / layer.texturePeriod());
                addVertex(builder, x1, z0, x1 / layer.texturePeriod(), z0 / layer.texturePeriod());
            }
        }

        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(builder.end());
        VertexBuffer.unbind();
        return buffer;
    }

    public static int cellsPerSide(CloudLayer layer) {
        return (int)Math.ceil(layer.halfExtent() * 2.0F / GRID_CELL_SIZE);
    }

    public static int vertexCount(CloudLayer layer) {
        int cells = cellsPerSide(layer);
        return cells * cells * 4;
    }

    private static void addVertex(BufferBuilder builder, float x, float z, float u, float v) {
        builder.vertex(x, 0.0F, z)
            .uv(u, v)
            .color(1.0F, 1.0F, 1.0F, 1.0F)
            .normal(0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    private static void prepareRenderState() {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        RenderSystem.setShaderTexture(0, VANILLA_CLOUD_TEXTURE);
        FogRenderer.levelFogColor();
    }

    private static void renderLayer(
        WorldRenderContext context,
        CloudLayer layer,
        VertexBuffer buffer,
        double timeSeconds,
        double cameraX,
        double cameraY,
        double cameraZ
    ) {
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            return;
        }

        // Anchors the repeating texture in world space while applying a subtle continuous drift.
        double phaseX = centeredModulo(cameraX - timeSeconds * layer.speedX(), layer.texturePeriod());
        double phaseZ = centeredModulo(cameraZ - timeSeconds * layer.speedZ(), layer.texturePeriod());

        PoseStack matrices = context.matrixStack();
        matrices.pushPose();
        try {
            matrices.translate(-phaseX, layer.height() - cameraY, -phaseZ);
            RenderSystem.setShaderColor(layer.red(), layer.green(), layer.blue(), layer.opacity());
            buffer.bind();
            buffer.drawWithShader(matrices.last().pose(), context.projectionMatrix(), shader);
            VertexBuffer.unbind();
        } finally {
            matrices.popPose();
        }
    }

    static double positiveModulo(double value, double modulus) {
        double result = value % modulus;
        return result < 0.0D ? result + modulus : result;
    }

    static double centeredModulo(double value, double modulus) {
        return positiveModulo(value + modulus * 0.5D, modulus) - modulus * 0.5D;
    }

    private static void restoreRenderState() {
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    /** Releases the two GPU buffers. Call from the render thread or during client shutdown. */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::closeBuffers);
        } else {
            closeBuffers();
        }
    }

    private void closeBuffers() {
        if (primaryBuffer != null) {
            primaryBuffer.close();
            primaryBuffer = null;
        }
        if (secondaryBuffer != null) {
            secondaryBuffer.close();
            secondaryBuffer = null;
        }
    }

    /** Drops GPU state after a renderer reload and recreates it lazily on the next frame. */
    public void invalidate() {
        if (closed) return;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::closeBuffers);
        } else {
            closeBuffers();
        }
    }

    /** Two-layer cloud settings kept immutable so they are safe to share from a visual config. */
    public record Settings(CloudLayer primary, CloudLayer secondary) {
        public Settings {
            Objects.requireNonNull(primary, "primary");
            Objects.requireNonNull(secondary, "secondary");
        }

        public static Settings defaults() {
            return new Settings(
                new CloudLayer(326.0F, 1_440.0F, 4_096.0F,
                    1.00F, 0.90F, 0.96F, 0.62F, 0.28F, 0.05F),
                new CloudLayer(364.0F, 1_040.0F, 4_096.0F,
                    0.94F, 0.84F, 1.00F, 0.28F, -0.12F, 0.20F)
            );
        }
    }

    /**
     * @param height absolute world height of this layer
     * @param texturePeriod world-space blocks occupied by one repeat of the cloud mask
     * @param halfExtent half-width of the rendered plane; keep comfortably beyond view distance
     * @param red red tint multiplier from 0 to 1
     * @param green green tint multiplier from 0 to 1
     * @param blue blue tint multiplier from 0 to 1
     * @param opacity layer opacity from 0 to 1
     * @param speedX horizontal drift in blocks per second
     * @param speedZ horizontal drift in blocks per second
     */
    public record CloudLayer(
        float height,
        float texturePeriod,
        float halfExtent,
        float red,
        float green,
        float blue,
        float opacity,
        float speedX,
        float speedZ
    ) {
        public CloudLayer {
            requireFinite(height, "height");
            requireFinite(speedX, "speedX");
            requireFinite(speedZ, "speedZ");
            if (!Float.isFinite(texturePeriod) || texturePeriod < 128.0F) {
                throw new IllegalArgumentException("texturePeriod must be finite and at least 128 blocks");
            }
            if (!Float.isFinite(halfExtent) || halfExtent < texturePeriod * 1.5F) {
                throw new IllegalArgumentException("halfExtent must cover at least 1.5 texture periods");
            }
            requireUnit(red, "red");
            requireUnit(green, "green");
            requireUnit(blue, "blue");
            requireUnit(opacity, "opacity");
        }

        private static void requireFinite(float value, String name) {
            if (!Float.isFinite(value)) {
                throw new IllegalArgumentException(name + " must be finite");
            }
        }

        private static void requireUnit(float value, String name) {
            if (!Float.isFinite(value) || value < 0.0F || value > 1.0F) {
                throw new IllegalArgumentException(name + " must be between 0 and 1");
            }
        }
    }
}
