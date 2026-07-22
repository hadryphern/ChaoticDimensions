package net.blue.chaoticd.validation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/** Standalone packaging validator for the Aurora block family and its authored textures. */
public final class AuroraAssetsValidator {
    private static final Path ROOT = Path.of("build/resources/main");
    private static final Path ASSETS = ROOT.resolve("assets/chaoticd");
    private static final Path DATA = ROOT.resolve("data/chaoticd");
    private static final Set<String> BLOCKS = Set.of(
        "pastel_soil",
        "pastel_grass",
        "pastel_aurora_stone",
        "pastel_aurora_log",
        "pastel_aurora_wood",
        "stripped_pastel_aurora_log",
        "stripped_pastel_aurora_wood",
        "pastel_aurora_planks",
        "pastel_pink_leaves",
        "pastel_purple_leaves",
        "pastel_blue_leaves",
        "sapphire_ore",
        "rosalita_ore"
    );
    private static final Set<String> BLOCK_TEXTURES = Set.of(
        "pastel_soil",
        "pastel_grass",
        "pastel_grass_side",
        "pastel_aurora_stone",
        "pastel_aurora_log",
        "pastel_aurora_log_top",
        "stripped_pastel_aurora_log",
        "stripped_pastel_aurora_log_top",
        "pastel_aurora_planks",
        "pastel_pink_leaves",
        "pastel_purple_leaves",
        "pastel_blue_leaves",
        "sapphire_ore",
        "rosalita_ore"
    );
    private static final Set<String> LEAVES = Set.of(
        "pastel_pink_leaves", "pastel_purple_leaves", "pastel_blue_leaves");
    private static final List<String> LANGUAGES = List.of("pt_br", "en_us", "es_co", "es_mx");

    private AuroraAssetsValidator() {
    }

    public static void main(String[] args) throws IOException {
        check(Files.isRegularFile(ROOT.resolve("pack.mcmeta")), "Missing pack.mcmeta");
        validateEveryJson();
        validateBlockCoverage();
        validateTextures();
        validateModelReferences();
        validateLanguages();
        validateRequiredTags();
        System.out.println("AURORA ASSET VALIDATION PASSED: 13 blocks, 14 authored 128x128 textures, alpha, JSON and model references.");
    }

    private static void validateEveryJson() throws IOException {
        try (Stream<Path> files = Files.walk(ROOT)) {
            for (Path file : files.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json") || path.getFileName().toString().equals("pack.mcmeta"))
                .toList()) {
                readJson(file);
            }
        }
    }

    private static void validateBlockCoverage() {
        for (String id : BLOCKS) {
            requireFile(ASSETS.resolve("blockstates/" + id + ".json"));
            requireFile(ASSETS.resolve("models/item/" + id + ".json"));
            requireFile(DATA.resolve("loot_tables/blocks/" + id + ".json"));
        }
    }

    private static void validateTextures() throws IOException {
        Path textureRoot = ASSETS.resolve("textures/block");
        Set<String> actual = new LinkedHashSet<>();
        try (Stream<Path> files = Files.list(textureRoot)) {
            files.filter(path -> path.toString().endsWith(".png"))
                .map(path -> path.getFileName().toString().replaceFirst("\\.png$", ""))
                .sorted()
                .forEach(actual::add);
        }
        check(actual.equals(BLOCK_TEXTURES), "Unexpected active block texture set: " + actual);

        for (String id : BLOCK_TEXTURES) {
            Path file = textureRoot.resolve(id + ".png");
            BufferedImage image = ImageIO.read(file.toFile());
            check(image != null, "Unreadable PNG: " + file);
            check(image.getWidth() == 128 && image.getHeight() == 128,
                "Texture is not 128x128: " + file + " is " + image.getWidth() + "x" + image.getHeight());

            int transparent = 0;
            int brightGreen = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int argb = image.getRGB(x, y);
                    int alpha = argb >>> 24;
                    int red = argb >>> 16 & 0xff;
                    int green = argb >>> 8 & 0xff;
                    int blue = argb & 0xff;
                    if (alpha == 0) transparent++;
                    if (alpha > 16 && green > 210 && red < 80 && blue < 80) brightGreen++;
                }
            }

            if (LEAVES.contains(id)) {
                double ratio = transparent / (double)(image.getWidth() * image.getHeight());
                check(ratio >= 0.15 && ratio <= 0.55,
                    "Leaf alpha ratio outside 15%-55%: " + id + "=" + ratio);
                check(brightGreen == 0, "Chroma-key green remained visible in " + id + ": " + brightGreen + " pixels");
            } else {
                check(transparent == 0, "Opaque block texture contains transparent pixels: " + id);
            }
        }
    }

    private static void validateModelReferences() throws IOException {
        Path blockstates = ASSETS.resolve("blockstates");
        try (Stream<Path> files = Files.list(blockstates)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonElement root = readJson(file);
                Set<String> models = new LinkedHashSet<>();
                collectNamedStrings(root, "model", models);
                for (String model : models) {
                    requireModModel(model, file);
                }
            }
        }

        Path models = ASSETS.resolve("models");
        try (Stream<Path> files = Files.walk(models)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject model = readJson(file).getAsJsonObject();
                if (model.has("parent")) {
                    requireModModel(model.get("parent").getAsString(), file);
                }
                if (!model.has("textures")) continue;
                for (var entry : model.getAsJsonObject("textures").entrySet()) {
                    String texture = entry.getValue().getAsString();
                    if (texture.startsWith("#") || !texture.startsWith("chaoticd:")) continue;
                    String local = texture.substring("chaoticd:".length());
                    requireFile(ASSETS.resolve("textures/" + local + ".png"), "Missing texture " + texture + " referenced by " + file);
                }
            }
        }
    }

    private static void validateLanguages() throws IOException {
        for (String language : LANGUAGES) {
            Path file = ASSETS.resolve("lang/" + language + ".json");
            JsonObject translations = readJson(file).getAsJsonObject();
            for (String id : BLOCKS) {
                check(translations.has("block.chaoticd." + id), "Missing " + language + " translation for " + id);
            }
            check(translations.has("biome.chaoticd.aurora_biome"), "Missing Aurora biome translation in " + language);
            check(translations.has("dimension.chaoticd.aurora_dimension"), "Missing Aurora dimension translation in " + language);
        }
    }

    private static void validateRequiredTags() {
        requireFile(DATA.resolve("tags/blocks/pastel_aurora_logs.json"));
        requireFile(DATA.resolve("tags/items/pastel_aurora_logs.json"));
        requireFile(DATA.resolve("tags/blocks/aurora_ore_replaceables.json"));
        requireFile(ROOT.resolve("data/minecraft/tags/blocks/logs_that_burn.json"));
        requireFile(ROOT.resolve("data/minecraft/tags/items/logs_that_burn.json"));
        requireFile(ROOT.resolve("data/minecraft/tags/blocks/leaves.json"));
        requireFile(ROOT.resolve("data/minecraft/tags/items/leaves.json"));
        requireFile(ROOT.resolve("data/fabric/tags/blocks/needs_tool_level_4.json"));
    }

    private static JsonElement readJson(Path file) throws IOException {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement json = JsonParser.parseReader(reader);
            check(json != null && !json.isJsonNull(), "Empty JSON: " + file);
            return json;
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Invalid JSON: " + file, exception);
        }
    }

    private static void collectNamedStrings(JsonElement element, String field, Set<String> values) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> collectNamedStrings(child, field, values));
            return;
        }
        if (!element.isJsonObject()) return;
        for (var entry : element.getAsJsonObject().entrySet()) {
            if (entry.getKey().equals(field) && entry.getValue().isJsonPrimitive()) {
                values.add(entry.getValue().getAsString());
            } else {
                collectNamedStrings(entry.getValue(), field, values);
            }
        }
    }

    private static void requireModModel(String id, Path source) {
        if (!id.startsWith("chaoticd:")) return;
        String local = id.substring("chaoticd:".length());
        requireFile(ASSETS.resolve("models/" + local + ".json"), "Missing model " + id + " referenced by " + source);
    }

    private static void requireFile(Path file) {
        requireFile(file, "Missing required file: " + file);
    }

    private static void requireFile(Path file, String message) {
        check(Files.isRegularFile(file), message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
