package net.blue.chaoticd.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.blue.chaoticd.client.visual.AuroraCloudRenderer;
import net.blue.chaoticd.client.visual.AuroraDimensionEffects;
import net.blue.chaoticd.client.visual.AuroraRainbowRenderer;
import net.blue.chaoticd.client.visual.AuroraVisualConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;

/** Headless acceptance checks for Aurora's client-only sky configuration and cached geometry. */
public final class AuroraVisualValidator {
    private static final Path DIMENSION_TYPE = Path.of(
        "build/resources/main/data/chaoticd/dimension_type/aurora_dimension_type.json");
    private static final Path BIOME = Path.of(
        "build/resources/main/data/chaoticd/worldgen/biome/aurora_biome.json");

    private AuroraVisualValidator() {
    }

    public static void main(String[] args) throws IOException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        validateRegistryConfiguration();
        validateAtmosphere();
        validateCloudLayers();
        validateRainbowMesh();
        System.out.println("AURORA VISUAL VALIDATION PASSED: exclusive effects, pastel atmosphere, "
            + "two cached cloud layers and finite rainbow geometry.");
    }

    private static void validateRegistryConfiguration() throws IOException {
        JsonObject dimensionType = readObject(DIMENSION_TYPE);
        check(dimensionType.get("effects").getAsString().equals(AuroraVisualConfig.DIMENSION_EFFECTS.toString()),
            "Dimension type does not reference Aurora's registered visual effects");
        check(dimensionType.get("ambient_light").getAsFloat() == 0.25F,
            "Visual work must not silently change Aurora gameplay lighting");
        check(dimensionType.get("fixed_time").getAsLong() == 6_000L,
            "Aurora's stable daylight was unexpectedly changed");
        check(AuroraVisualConfig.AURORA_DIMENSION.location().toString().equals("chaoticd:aurora_dimension"),
            "Visual renderers are not scoped to the Aurora dimension key");
    }

    private static void validateAtmosphere() throws IOException {
        JsonObject biomeEffects = readObject(BIOME).getAsJsonObject("effects");
        check(biomeEffects.get("sky_color").getAsInt() == AuroraVisualConfig.SKY_COLOR,
            "Biome sky color and client visual config diverged");
        check(biomeEffects.get("fog_color").getAsInt() == AuroraVisualConfig.FOG_COLOR,
            "Biome fog/clear color and client visual config diverged");

        Rgb sky = unpack(AuroraVisualConfig.SKY_COLOR);
        Rgb fog = unpack(AuroraVisualConfig.FOG_COLOR);
        check(sky.red > sky.blue && sky.blue > sky.green,
            "Aurora sky is not a soft pink-lilac color");
        check(fog.red > fog.blue && fog.blue > fog.green,
            "Aurora fog is not a pink-pearl color");
        check(sky.minimum() >= 0.68F && fog.minimum() >= 0.80F,
            "Aurora atmosphere is too dark for the requested pastel direction");
        check(sky.maximum() - sky.minimum() <= 0.25F && fog.maximum() - fog.minimum() <= 0.16F,
            "Aurora atmosphere is too saturated");

        AuroraDimensionEffects effects = new AuroraDimensionEffects(AuroraVisualConfig.FALLBACK_CLOUD_HEIGHT);
        Vec3 baseFog = new Vec3(fog.red, fog.green, fog.blue);
        Vec3 daylightFog = effects.getBrightnessDependentFogColor(baseFog, 1.0F);
        Vec3 dimFog = effects.getBrightnessDependentFogColor(baseFog, 0.35F);
        check(close(daylightFog.x, baseFog.x) && close(daylightFog.y, baseFog.y)
                && close(daylightFog.z, baseFog.z),
            "Full daylight unexpectedly shifts Aurora fog color");
        check(dimFog.x > 0.0D && dimFog.y > 0.0D && dimFog.z > 0.0D
                && dimFog.x < baseFog.x && dimFog.y < baseFog.y && dimFog.z < baseFog.z,
            "Brightness-aware fog response is invalid");
        check(!effects.isFoggyAt(0, 0),
            "Short vanilla world fog would hide Aurora's distant floating islands");

        System.out.printf("AURORA atmosphere sky=#%06X fog=#%06X daylight=%s dim=%s%n",
            AuroraVisualConfig.SKY_COLOR, AuroraVisualConfig.FOG_COLOR, daylightFog, dimFog);
    }

    private static void validateCloudLayers() {
        AuroraCloudRenderer.Settings settings = AuroraVisualConfig.clouds();
        AuroraCloudRenderer.CloudLayer primary = settings.primary();
        AuroraCloudRenderer.CloudLayer secondary = settings.secondary();

        check(Math.abs(primary.height() - secondary.height()) >= 24.0F,
            "Cloud layers are too close to create visible depth");
        check(primary.texturePeriod() != secondary.texturePeriod(),
            "Cloud masks would repeat in lockstep");
        check(primary.speedX() != secondary.speedX() || primary.speedZ() != secondary.speedZ(),
            "Cloud layers would drift in lockstep");
        check(primary.halfExtent() >= 2_048.0F && secondary.halfExtent() >= 2_048.0F,
            "Cloud planes do not safely cover long view distances");
        check(primary.opacity() > secondary.opacity(),
            "Secondary cloud layer must remain the subtler depth layer");
        check(Math.abs(primary.speedX()) <= 0.25F && Math.abs(primary.speedZ()) <= 0.25F
                && Math.abs(secondary.speedX()) <= 0.25F && Math.abs(secondary.speedZ()) <= 0.25F,
            "Cloud movement is too fast for the calm Aurora atmosphere");

        float combinedOpacity = 1.0F - (1.0F - primary.opacity()) * (1.0F - secondary.opacity());
        check(combinedOpacity <= 0.80F,
            "Cloud layers are opaque enough to overwhelm the sky");
        check(primary.red() >= 0.9F && primary.blue() >= 0.9F
                && secondary.red() >= 0.85F && secondary.blue() >= 0.9F,
            "Cloud tint is too dark or heavy");
        check(AuroraCloudRenderer.GRID_CELL_SIZE <= 128.0F,
            "Cloud grid can be entirely clipped by Minecraft's minimum far plane");

        int cachedVertices = AuroraCloudRenderer.vertexCount(primary)
            + AuroraCloudRenderer.vertexCount(secondary);
        check(cachedVertices <= 40_000,
            "Cloud grid is too dense for a lightweight persistent renderer");

        System.out.printf("AURORA clouds layers=2 heights=(%.0f,%.0f) periods=(%.0f,%.0f) "
                + "combinedAlpha=%.3f gridCell=%.0f cachedVertices=%d drawCalls=2%n",
            primary.height(), secondary.height(), primary.texturePeriod(), secondary.texturePeriod(), combinedOpacity,
            AuroraCloudRenderer.GRID_CELL_SIZE, cachedVertices);
    }

    private static void validateRainbowMesh() {
        AuroraRainbowRenderer renderer = new AuroraRainbowRenderer(AuroraVisualConfig.rainbows());
        AuroraRainbowRenderer.Settings settings = renderer.settings();
        AuroraRainbowRenderer.MeshMetrics metrics = renderer.inspectGeometry();

        int expectedQuads = settings.arcs().size() * settings.arcSegments()
            * (settings.colors().size() - 1) * settings.colorSubdivisions();
        check(settings.arcs().size() == 2, "Aurora should have one principal and one subtle distant rainbow");
        check(metrics.quadCount() == expectedQuads, "Rainbow quad count does not match its tuning data");
        check(metrics.vertexCount() == expectedQuads * 4, "Rainbow vertex count is inconsistent");
        check(metrics.drawCalls() == 1, "Rainbow arcs must share one cached draw call");
        check(metrics.allFinite(), "Rainbow mesh contains NaN or infinite coordinates/colors");
        check(metrics.minAlpha() == 0.0F, "Rainbow edges are not completely feathered");
        check(metrics.maxAlpha() <= 0.35F && metrics.maxAlpha() >= 0.25F,
            "Principal rainbow is either invisible or visually overpowering");
        check(metrics.minY() < 0.0F && metrics.maxY() > 0.0F,
            "Rainbows are not crossing the distant horizon naturally");
        check(settings.radialFeatherFraction() >= 0.10F && settings.endpointFeatherDegrees() >= 8.0F,
            "Rainbow edge feathering is too abrupt");

        float conservativeDistance = settings.arcs().stream()
            .map(arc -> {
                double horizontal = Math.hypot(arc.distance() + arc.depthCurve(), arc.outerRadius());
                double vertical = Math.abs(arc.horizonOffset()) + arc.outerRadius();
                return (float)Math.hypot(horizontal, vertical);
            })
            .max(Float::compare)
            .orElseThrow();
        check(conservativeDistance * settings.renderScale() < 120.0F,
            "Rainbow can cross Minecraft's 128-block far plane at the minimum render distance");

        System.out.printf("AURORA rainbows arcs=%d quads=%d vertices=%d drawCalls=%d "
                + "alpha=%.3f..%.3f renderScale=%.2f maxRenderedDistance<%.1f "
                + "bounds=[%.1f..%.1f, %.1f..%.1f, %.1f..%.1f]%n",
            settings.arcs().size(), metrics.quadCount(), metrics.vertexCount(), metrics.drawCalls(),
            metrics.minAlpha(), metrics.maxAlpha(), settings.renderScale(), conservativeDistance * settings.renderScale(),
            metrics.minX(), metrics.maxX(), metrics.minY(), metrics.maxY(), metrics.minZ(), metrics.maxZ());
    }

    private static JsonObject readObject(Path path) throws IOException {
        check(Files.isRegularFile(path), "Missing processed resource: " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static Rgb unpack(int rgb) {
        return new Rgb((rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F);
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) <= 1.0E-6D;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private record Rgb(float red, float green, float blue) {
        private float minimum() {
            return Math.min(red, Math.min(green, blue));
        }

        private float maximum() {
            return Math.max(red, Math.max(green, blue));
        }
    }
}
