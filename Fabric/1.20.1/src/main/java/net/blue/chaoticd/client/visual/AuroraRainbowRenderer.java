package net.blue.chaoticd.client.visual;

import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.material.FogType;
import org.joml.Matrix4f;

/**
 * Renders one or more soft, sky-locked rainbows for the Aurora dimension.
 *
 * <p>The owner is responsible for calling this renderer only from Aurora's sky pass. The mesh is
 * built lazily, uploaded once and then rendered with a single draw call. Because its vertices are
 * relative to the sky rather than the player position, changing altitude cannot make a rainbow
 * intersect the camera.</p>
 */
public final class AuroraRainbowRenderer implements AutoCloseable {
    private static final int FLOATS_PER_VERTEX = 7;

    private final Settings settings;
    private VertexBuffer vertexBuffer;
    private MeshMetrics meshMetrics;

    public AuroraRainbowRenderer(Settings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
    }

    /**
     * Draws the cached rainbow mesh after the regular sky and before terrain.
     *
     * @param poseStack current sky pose, including the camera rotation but no camera translation
     * @param projectionMatrix current world projection
     * @param camera active camera, used to hide the effect while submerged
     * @param tickDelta partial tick reserved for future subtle, time-continuous effects
     */
    public void render(PoseStack poseStack, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
        Objects.requireNonNull(poseStack, "poseStack");
        Objects.requireNonNull(projectionMatrix, "projectionMatrix");
        Objects.requireNonNull(camera, "camera");
        if (camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        RenderSystem.assertOnRenderThread();
        ensureBuffer();

        if (vertexBuffer == null) {
            return;
        }

        poseStack.pushPose();
        // Keep the same angular size while bringing the sky mesh inside Minecraft's 2-chunk
        // minimum far plane. Without this, decorative sky geometry can be clipped at low distance.
        poseStack.scale(settings.renderScale(), settings.renderScale(), settings.renderScale());
        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        try {
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                vertexBuffer.bind();
                vertexBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
            }
        } finally {
            VertexBuffer.unbind();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            poseStack.popPose();
        }
    }

    /** Returns immutable geometry statistics without requiring an OpenGL context. */
    public MeshMetrics inspectGeometry() {
        if (meshMetrics == null) {
            meshMetrics = buildMesh().metrics();
        }

        return meshMetrics;
    }

    public Settings settings() {
        return settings;
    }

    private void ensureBuffer() {
        if (vertexBuffer != null) {
            return;
        }

        CpuMesh mesh = buildMesh();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float[] vertices = mesh.vertices();

        for (int offset = 0; offset < vertices.length; offset += FLOATS_PER_VERTEX) {
            builder.vertex(vertices[offset], vertices[offset + 1], vertices[offset + 2])
                .color(vertices[offset + 3], vertices[offset + 4], vertices[offset + 5], vertices[offset + 6])
                .endVertex();
        }

        VertexBuffer uploaded = new VertexBuffer(VertexBuffer.Usage.STATIC);

        try {
            uploaded.bind();
            uploaded.upload(builder.end());
            vertexBuffer = uploaded;
            meshMetrics = mesh.metrics();
        } catch (RuntimeException exception) {
            uploaded.close();
            throw exception;
        } finally {
            VertexBuffer.unbind();
        }
    }

    private CpuMesh buildMesh() {
        int colorIntervals = settings.colors().size() - 1;
        int radialSteps = colorIntervals * settings.colorSubdivisions();
        int quadCount = settings.arcs().size() * settings.arcSegments() * radialSteps;
        float[] vertices = new float[quadCount * 4 * FLOATS_PER_VERTEX];
        MeshWriter writer = new MeshWriter(vertices);

        for (ArcSettings arc : settings.arcs()) {
            appendArc(writer, arc, radialSteps);
        }

        if (writer.offset() != vertices.length) {
            throw new IllegalStateException("Aurora rainbow mesh size mismatch: " + writer.offset() + " / " + vertices.length);
        }

        MeshMetrics metrics = new MeshMetrics(
            quadCount,
            quadCount * 4,
            1,
            writer.minX(),
            writer.maxX(),
            writer.minY(),
            writer.maxY(),
            writer.minZ(),
            writer.maxZ(),
            writer.minAlpha(),
            writer.maxAlpha(),
            writer.allFinite()
        );
        return new CpuMesh(vertices, metrics);
    }

    private void appendArc(MeshWriter writer, ArcSettings arc, int radialSteps) {
        float azimuth = (float)Math.toRadians(arc.azimuthDegrees());
        float tangentX = (float)Math.cos(azimuth);
        float tangentZ = -(float)Math.sin(azimuth);
        float forwardX = (float)Math.sin(azimuth);
        float forwardZ = (float)Math.cos(azimuth);

        for (int segment = 0; segment < settings.arcSegments(); segment++) {
            float along0 = segment / (float)settings.arcSegments();
            float along1 = (segment + 1) / (float)settings.arcSegments();
            float angle0 = (float)Math.PI * along0;
            float angle1 = (float)Math.PI * along1;
            float endFade0 = endpointFade(along0);
            float endFade1 = endpointFade(along1);

            for (int radial = 0; radial < radialSteps; radial++) {
                float across0 = radial / (float)radialSteps;
                float across1 = (radial + 1) / (float)radialSteps;
                float radius0 = arc.outerRadius() - arc.bandWidth() * across0;
                float radius1 = arc.outerRadius() - arc.bandWidth() * across1;
                float radialFade0 = radialFade(across0);
                float radialFade1 = radialFade(across1);
                Rgb color0 = gradientColor(across0);
                Rgb color1 = gradientColor(across1);

                appendVertex(writer, arc, tangentX, tangentZ, forwardX, forwardZ, angle0, radius0,
                    color0, arc.opacity() * endFade0 * radialFade0);
                appendVertex(writer, arc, tangentX, tangentZ, forwardX, forwardZ, angle1, radius0,
                    color0, arc.opacity() * endFade1 * radialFade0);
                appendVertex(writer, arc, tangentX, tangentZ, forwardX, forwardZ, angle1, radius1,
                    color1, arc.opacity() * endFade1 * radialFade1);
                appendVertex(writer, arc, tangentX, tangentZ, forwardX, forwardZ, angle0, radius1,
                    color1, arc.opacity() * endFade0 * radialFade1);
            }
        }
    }

    private void appendVertex(MeshWriter writer, ArcSettings arc, float tangentX, float tangentZ,
                              float forwardX, float forwardZ, float angle, float radius,
                              Rgb color, float alpha) {
        float horizontal = (float)Math.cos(angle) * radius;
        float vertical = (float)Math.sin(angle) * radius;
        float endpointDepth = 1.0F - (float)Math.sin(angle);
        float distance = arc.distance() + arc.depthCurve() * endpointDepth;
        float x = forwardX * distance + tangentX * horizontal;
        float y = arc.horizonOffset() + vertical;
        float z = forwardZ * distance + tangentZ * horizontal;
        writer.vertex(x, y, z, color.red(), color.green(), color.blue(), alpha);
    }

    private float endpointFade(float along) {
        float featherFraction = settings.endpointFeatherDegrees() / 180.0F;
        float edgeDistance = Math.min(along, 1.0F - along);
        return smoothstep(0.0F, featherFraction, edgeDistance);
    }

    private float radialFade(float across) {
        float edgeDistance = Math.min(across, 1.0F - across);
        return smoothstep(0.0F, settings.radialFeatherFraction(), edgeDistance);
    }

    private Rgb gradientColor(float across) {
        List<Integer> colors = settings.colors();
        float scaled = across * (colors.size() - 1);
        int lower = Math.min((int)Math.floor(scaled), colors.size() - 2);
        int upper = lower + 1;
        float amount = scaled - lower;
        Rgb first = unpack(colors.get(lower));
        Rgb second = unpack(colors.get(upper));
        float red = lerp(amount, first.red(), second.red());
        float green = lerp(amount, first.green(), second.green());
        float blue = lerp(amount, first.blue(), second.blue());
        float pearl = settings.pearlBlend();
        return new Rgb(lerp(pearl, red, 1.0F), lerp(pearl, green, 1.0F), lerp(pearl, blue, 1.0F));
    }

    private static Rgb unpack(int rgb) {
        return new Rgb((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        if (edge1 <= edge0) {
            return value >= edge1 ? 1.0F : 0.0F;
        }

        float normalized = clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return normalized * normalized * (3.0F - 2.0F * normalized);
    }

    private static float lerp(float amount, float start, float end) {
        return start + amount * (end - start);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    /** All tunable values used by the rainbow renderer. */
    public record Settings(int arcSegments, int colorSubdivisions, float radialFeatherFraction,
                           float endpointFeatherDegrees, float pearlBlend, float renderScale,
                           List<Integer> colors, List<ArcSettings> arcs) {
        public Settings {
            colors = List.copyOf(colors);
            arcs = List.copyOf(arcs);
            requireRange(arcSegments, 8, 512, "arcSegments");
            requireRange(colorSubdivisions, 1, 16, "colorSubdivisions");
            requireRange(radialFeatherFraction, 0.0F, 0.5F, "radialFeatherFraction");
            requireRange(endpointFeatherDegrees, 0.0F, 45.0F, "endpointFeatherDegrees");
            requireRange(pearlBlend, 0.0F, 1.0F, "pearlBlend");
            requireRange(renderScale, 0.05F, 1.0F, "renderScale");

            if (colors.size() < 2 || colors.size() > 32) {
                throw new IllegalArgumentException("colors must contain between 2 and 32 entries");
            }

            for (int rgb : colors) {
                if (rgb < 0 || rgb > 0xFFFFFF) {
                    throw new IllegalArgumentException("Rainbow colors must be RGB values: " + rgb);
                }
            }

            if (arcs.isEmpty() || arcs.size() > 8) {
                throw new IllegalArgumentException("arcs must contain between 1 and 8 entries");
            }
        }
    }

    /** Geometry and opacity parameters for one distant arch. */
    public record ArcSettings(float azimuthDegrees, float distance, float horizonOffset,
                              float outerRadius, float bandWidth, float depthCurve, float opacity) {
        public ArcSettings {
            requireFinite(azimuthDegrees, "azimuthDegrees");
            requireRange(distance, 8.0F, 512.0F, "distance");
            requireRange(horizonOffset, -256.0F, 256.0F, "horizonOffset");
            requireRange(outerRadius, 4.0F, 256.0F, "outerRadius");
            requireRange(bandWidth, 0.5F, outerRadius - 0.01F, "bandWidth");
            requireRange(depthCurve, 0.0F, 128.0F, "depthCurve");
            requireRange(opacity, 0.0F, 1.0F, "opacity");
        }
    }

    /** Small immutable report used by tests and performance diagnostics. */
    public record MeshMetrics(int quadCount, int vertexCount, int drawCalls,
                              float minX, float maxX, float minY, float maxY,
                              float minZ, float maxZ, float minAlpha, float maxAlpha,
                              boolean allFinite) {
    }

    private record CpuMesh(float[] vertices, MeshMetrics metrics) {
    }

    private record Rgb(float red, float green, float blue) {
    }

    private static final class MeshWriter {
        private final float[] vertices;
        private int offset;
        private float minX = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;
        private float minAlpha = Float.POSITIVE_INFINITY;
        private float maxAlpha = Float.NEGATIVE_INFINITY;
        private boolean allFinite = true;

        private MeshWriter(float[] vertices) {
            this.vertices = vertices;
        }

        private void vertex(float x, float y, float z, float red, float green, float blue, float alpha) {
            allFinite &= Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z)
                && Float.isFinite(red) && Float.isFinite(green) && Float.isFinite(blue) && Float.isFinite(alpha);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
            minAlpha = Math.min(minAlpha, alpha);
            maxAlpha = Math.max(maxAlpha, alpha);
            vertices[offset++] = x;
            vertices[offset++] = y;
            vertices[offset++] = z;
            vertices[offset++] = red;
            vertices[offset++] = green;
            vertices[offset++] = blue;
            vertices[offset++] = alpha;
        }

        private int offset() {
            return offset;
        }

        private float minX() {
            return minX;
        }

        private float maxX() {
            return maxX;
        }

        private float minY() {
            return minY;
        }

        private float maxY() {
            return maxY;
        }

        private float minZ() {
            return minZ;
        }

        private float maxZ() {
            return maxZ;
        }

        private float minAlpha() {
            return minAlpha;
        }

        private float maxAlpha() {
            return maxAlpha;
        }

        private boolean allFinite() {
            return allFinite;
        }
    }

    private static void requireRange(int value, int minimum, int maximum, String name) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum + ": " + value);
        }
    }

    private static void requireRange(float value, float minimum, float maximum, String name) {
        requireFinite(value, name);
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum + ": " + value);
        }
    }

    private static void requireFinite(float value, String name) {
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite: " + value);
        }
    }
}
