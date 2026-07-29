package net.blue.chaoticd.validation;

import com.google.gson.JsonArray;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 * Packaging validator for Aurora, Shadow, Dream Fluid and Emerald assets.
 *
 * <p>Required assets are checked, while future extra textures are allowed.</p>
 */
public final class AuroraAssetsValidator {
    private static final Path ROOT =
        Path.of("build/resources/main");

    private static final Path ASSETS =
        ROOT.resolve("assets/chaoticd");

    private static final Path DATA =
        ROOT.resolve("data/chaoticd");

    private static final List<String> SUPPORTED_LANGUAGES = List.of(
        "en_us",
        "pt_br",
        "es_co",
        "es_mx"
    );

    private static final Set<String> REQUIRED_BLOCKS = Set.of(
        "aurora_dirt",
        "aurora_grass_block",
        "aurora_stone",
        "aurora_pinkko_log",
        "aurora_pinkko_wood",
        "stripped_aurora_pinkko_log",
        "stripped_aurora_pinkko_wood",
        "aurora_pinkko_planks",
        "aurora_pinkko_leaves",
        "aurora_pinkko_sapling",
        "aurora_souless_log",
        "aurora_souless_wood",
        "stripped_aurora_souless_log",
        "stripped_aurora_souless_wood",
        "aurora_souless_planks",
        "aurora_souless_leaves",
        "aurora_souless_sapling",
        "aurora_sky_log",
        "aurora_sky_wood",
        "stripped_aurora_sky_log",
        "stripped_aurora_sky_wood",
        "aurora_sky_planks",
        "aurora_sky_leaves",
        "aurora_sky_sapling",
        "ruby_ore",
        "ruby_block",
        "titanium_ore",
        "titanium_block",
        "deepslate_ruby_ore",
        "deepslate_titanium_ore",
        "deepslate_jaxy_ore",
        "deepslate_rosalita_ore",
        "jaxy_block",
        "rosalita_block",
        "jax_ore",
        "rosalita_ore",
        "nether_ruby_ore",
        "nether_jax_ore",
        "nether_rosalita_ore",
        "aurora_ruby_ore",
        "aurora_jax_ore",
        "aurora_rosalita_ore",
        "aurora_sapphire_ore",
        "shadow_soil",
        "shadow_grass",
        "shadow_stone",
        "shadow_log",
        "shadow_wood",
        "stripped_shadow_log",
        "stripped_shadow_wood",
        "shadow_planks",
        "shadow_leaves"
    );

    private static final Set<String> REQUIRED_BLOCK_TEXTURES = Set.of(
        "aurora_dirt",
        "aurora_grass_block",
        "aurora_grass_block_top",
        "aurora_stone",
        "aurora_pinkko_log",
        "aurora_pinkko_log_top",
        "stripped_aurora_pinkko_log",
        "stripped_aurora_pinkko_log_top",
        "aurora_pinkko_planks",
        "aurora_pinkko_sappling",
        "aurora_souless_log",
        "aurora_souless_log_top",
        "stripped_aurora_souless_log",
        "stripped_aurora_souless_log_top",
        "aurora_souless_planks",
        "aurora_souless_sappling",
        "aurora_sky_log",
        "aurora_sky_log_top",
        "stripped_aurora_sky_log",
        "stripped_aurora_sky_log_top",
        "aurora_sky_planks",
        "aurora_sky_sappling",
        "aurora_leaves_1",
        "aurora_leaves_2",
        "aurora_leaves_3",
        "ruby_ore",
        "ruby_block",
        "titanium_ore",
        "titanium_block",
        "deepslate_ruby_ore",
        "deepslate_titanium_ore",
        "deepslate_jaxy_ore",
        "deepslate_rosalita_ore",
        "jaxy_block",
        "rosalita_block",
        "jaxy_ore",
        "rosalita_ore",
        "nether_ruby_ore",
        "nether_jax_ore",
        "nether_rosalita",
        "aurora_ruby_ore",
        "aurora_jaxy_ore",
        "aurora_rosalita_ore",
        "aurora_sapphire_ore",
        "shadow_soil",
        "shadow_grass",
        "shadow_grass_side",
        "shadow_stone",
        "shadow_log",
        "shadow_log_top",
        "stripped_shadow_log",
        "stripped_shadow_log_top",
        "shadow_planks",
        "shadow_leaves"
    );

    private static final Set<String> LEAF_TEXTURES = Set.of(
        "aurora_leaves_1",
        "aurora_leaves_2",
        "aurora_leaves_3",
        "shadow_leaves"
    );

    private static final Set<String> SAPLING_TEXTURES = Set.of(
        "aurora_pinkko_sappling",
        "aurora_souless_sappling",
        "aurora_sky_sappling"
    );

    private static final List<String> EMERALD_ITEMS = List.of(
        "emerald_ingot",
        "emerald_sword",
        "emerald_pickaxe",
        "emerald_axe",
        "emerald_shovel",
        "emerald_hoe",
        "emerald_helmet",
        "emerald_chestplate",
        "emerald_leggings",
        "emerald_boots",
        "titanium_helmet",
        "titanium_chestplate",
        "titanium_leggings",
        "titanium_boots",
        "aurora_pearl",
        "dream_fluid_bucket",
        "crystaline_see"
    );

    /**
     * Items introduced by the Ruby -> Jaxy -> Titanium route and the
     * intentionally future-gated Rosalita route. Their models may use either
     * item or block textures, so model-reference validation verifies the
     * actual texture target.
     */
    private static final List<String> PROGRESSION_ITEM_MODELS = List.of(
        "ruby_plate",
        "rosalita_gem",
        "titan_sould",
        "jaxy_gem",
        "solar_obsidian",
        "jaxy_sword",
        "jaxy_pickaxe",
        "jaxy_axe",
        "jaxy_shovel",
        "jaxy_hoe",
        "jaxy_helmet",
        "jaxy_chestplate",
        "jaxy_leggings",
        "jaxy_boots",
        "rosalita_sword",
        "rosalita_pickaxe",
        "rosalita_axe",
        "rosalita_shovel",
        "rosalita_hoe",
        "rosalita_helmet",
        "rosalita_chestplate",
        "rosalita_leggings",
        "rosalita_boots",
        "shadow_gem",
        "shadow_nugget",
        "vylam_gem",
        "chlorophyte_ingot",
        "chlorophyte_pickaxe",
        "hero_gem",
        "hero_sword",
        "hero_axe",
        "hero_pickaxe",
        "hero_shovel",
        "hero_hoe",
        "derman_gem",
        "sun_tear",
        "sun_peak",
        "shadow_sword",
        "shadow_pickaxe",
        "aurora_soul",
        "crystaline_soul",
        "demonic_sould",
        "shadow_soul",
        "void_soul"
    );

    private static final List<String> ANIMATED_SOUL_TEXTURES = List.of(
        "aurora_soul",
        "crystaline_soul",
        "demonic_sould",
        "shadow_soul",
        "void_soul"
    );

    /**
     * Normal stone must receive the normal ore and deepslate must receive its
     * matching deepslate variant. Keeping this explicit prevents a silent
     * inversion in a configured feature from reaching a generated world.
     */
    private static final Map<String, List<String>> OVERWORLD_ORE_TARGETS = Map.of(
        "ruby_ore", List.of(
            "chaoticd:ruby_ore",
            "chaoticd:deepslate_ruby_ore"
        ),
        "jax_ore", List.of(
            "chaoticd:jax_ore",
            "chaoticd:deepslate_jaxy_ore"
        ),
        "titanium_ore", List.of(
            "chaoticd:titanium_ore",
            "chaoticd:deepslate_titanium_ore"
        ),
        "rosalita_ore", List.of(
            "chaoticd:rosalita_ore",
            "chaoticd:deepslate_rosalita_ore"
        )
    );

    private static final Set<String> PROGRESSION_PICKAXE_BLOCKS = Set.of(
        "chaoticd:ruby_ore",
        "chaoticd:deepslate_ruby_ore",
        "chaoticd:ruby_block",
        "chaoticd:jax_ore",
        "chaoticd:deepslate_jaxy_ore",
        "chaoticd:jaxy_block",
        "chaoticd:titanium_ore",
        "chaoticd:deepslate_titanium_ore",
        "chaoticd:titanium_block",
        "chaoticd:rosalita_ore",
        "chaoticd:deepslate_rosalita_ore",
        "chaoticd:rosalita_block"
    );

    private static final List<String> SMITHING_GEAR = List.of(
        "sword",
        "pickaxe",
        "axe",
        "shovel",
        "hoe",
        "helmet",
        "chestplate",
        "leggings",
        "boots"
    );

    private static final List<SmithingStep> SMITHING_STEPS = List.of(
        new SmithingStep(
            "emerald",
            "minecraft:netherite",
            "chaoticd:emerald_ingot",
            "minecraft:emerald",
            ""
        ),
        new SmithingStep(
            "ruby",
            "chaoticd:emerald",
            "chaoticd:ruby_plate",
            "chaoticd:ruby",
            "_smithing"
        ),
        new SmithingStep(
            "jaxy",
            "chaoticd:ruby",
            "chaoticd:solar_obsidian",
            "chaoticd:jaxy_gem",
            "_smithing"
        ),
        new SmithingStep(
            "titanium",
            "chaoticd:jaxy",
            "chaoticd:titan_sould",
            "chaoticd:titanium_ingot",
            "_smithing"
        )
    );

    /**
     * These keys are required in English and then parity guarantees them in
     * pt_br, es_co and es_mx as well.
     */
    private static final Set<String> REQUIRED_LANGUAGE_KEYS = Set.of(
        "block.chaoticd.aurora_pinkko_wood",
        "block.chaoticd.stripped_aurora_pinkko_wood",
        "block.chaoticd.aurora_souless_wood",
        "block.chaoticd.stripped_aurora_souless_wood",
        "block.chaoticd.aurora_sky_wood",
        "block.chaoticd.stripped_aurora_sky_wood",
        "block.chaoticd.deepslate_ruby_ore",
        "block.chaoticd.deepslate_titanium_ore",
        "block.chaoticd.deepslate_jaxy_ore",
        "block.chaoticd.deepslate_rosalita_ore",
        "block.chaoticd.jaxy_block",
        "block.chaoticd.rosalita_block",
        "item.chaoticd.titan_sould",
        "item.chaoticd.jaxy_gem",
        "item.chaoticd.solar_obsidian",
        "item.chaoticd.jaxy_sword",
        "item.chaoticd.jaxy_pickaxe",
        "item.chaoticd.jaxy_axe",
        "item.chaoticd.jaxy_shovel",
        "item.chaoticd.jaxy_hoe",
        "item.chaoticd.jaxy_helmet",
        "item.chaoticd.jaxy_chestplate",
        "item.chaoticd.jaxy_leggings",
        "item.chaoticd.jaxy_boots",
        "item.chaoticd.rosalita_sword",
        "item.chaoticd.rosalita_pickaxe",
        "item.chaoticd.rosalita_axe",
        "item.chaoticd.rosalita_shovel",
        "item.chaoticd.rosalita_hoe",
        "item.chaoticd.rosalita_helmet",
        "item.chaoticd.rosalita_chestplate",
        "item.chaoticd.rosalita_leggings",
        "item.chaoticd.rosalita_boots",
        "item.chaoticd.shadow_gem",
        "item.chaoticd.shadow_nugget",
        "item.chaoticd.vylam_gem",
        "item.chaoticd.chlorophyte_ingot",
        "item.chaoticd.chlorophyte_pickaxe",
        "item.chaoticd.hero_gem",
        "item.chaoticd.hero_sword",
        "item.chaoticd.hero_axe",
        "item.chaoticd.hero_pickaxe",
        "item.chaoticd.hero_shovel",
        "item.chaoticd.hero_hoe",
        "item.chaoticd.derman_gem",
        "item.chaoticd.sun_tear",
        "item.chaoticd.sun_peak",
        "item.chaoticd.shadow_sword",
        "item.chaoticd.shadow_pickaxe",
        "disconnect.chaoticd.stack_protocol_required",
        "disconnect.chaoticd.stack_protocol_version"
    );

    private AuroraAssetsValidator() {
    }

    public static void main(String[] args)
        throws IOException {

        requireFile(ROOT.resolve("pack.mcmeta"));
        validateEveryJson();
        validateLanguageParity();
        validateBlockCoverage();
        validateBlockTextures();
        validateModelReferences();
        validateEmeraldAndDreamAssets();
        validateProgressionAssets();
        validateAnimatedSoulAssets();
        validateRequiredTags();
        validateProgressionWorldgen();
        validateSmithingProgression();

        System.out.println(
            "AURORA/SHADOW/DREAM ASSET VALIDATION PASSED: "
                + REQUIRED_BLOCKS.size()
                + " required blocks, "
                + REQUIRED_BLOCK_TEXTURES.size()
                + " required block textures and "
                + EMERALD_ITEMS.size()
                + " Emerald/Dream item assets, "
                + PROGRESSION_ITEM_MODELS.size()
                + " progression item models."
        );
    }

    private static void validateEveryJson()
        throws IOException {

        try (Stream<Path> files = Files.walk(ROOT)) {
            for (
                Path file : files
                    .filter(Files::isRegularFile)
                    .filter(path ->
                        path.toString().endsWith(".json")
                            || path.getFileName()
                                .toString()
                                .equals("pack.mcmeta")
                    )
                    .toList()
            ) {
                readJson(file);
            }
        }
    }

    private static void validateBlockCoverage() {
        for (String id : REQUIRED_BLOCKS) {
            requireFile(
                ASSETS.resolve(
                    "blockstates/" + id + ".json"
                )
            );

            requireFile(
                ASSETS.resolve(
                    "models/item/" + id + ".json"
                )
            );

            requireFile(
                DATA.resolve(
                    "loot_tables/blocks/" + id + ".json"
                )
            );
        }
    }

    private static void validateLanguageParity()
        throws IOException {

        Path languageRoot = ASSETS.resolve("lang");
        Path english = languageRoot.resolve("en_us.json");

        Set<String> expectedKeys = languageKeys(english);

        check(
            !expectedKeys.isEmpty(),
            "Language file must not be empty: " + english
        );

        Set<String> missingRequired = new LinkedHashSet<>(
            REQUIRED_LANGUAGE_KEYS
        );
        missingRequired.removeAll(expectedKeys);

        check(
            missingRequired.isEmpty(),
            "English language file is missing required progression keys: "
                + missingRequired
        );

        for (String language : SUPPORTED_LANGUAGES) {
            Path file = languageRoot.resolve(language + ".json");
            Set<String> actualKeys = languageKeys(file);

            Set<String> missing = new LinkedHashSet<>(expectedKeys);
            missing.removeAll(actualKeys);

            Set<String> unexpected = new LinkedHashSet<>(actualKeys);
            unexpected.removeAll(expectedKeys);

            check(
                missing.isEmpty() && unexpected.isEmpty(),
                "Language keys diverge in "
                    + file
                    + "; missing="
                    + missing
                    + ", unexpected="
                    + unexpected
            );
        }
    }

    private static Set<String> languageKeys(Path file)
        throws IOException {

        requireFile(file);

        JsonElement root = readJson(file);

        check(
            root.isJsonObject(),
            "Language file must contain a JSON object: " + file
        );

        Set<String> keys = new LinkedHashSet<>();

        for (var entry : root.getAsJsonObject().entrySet()) {
            check(
                entry.getValue().isJsonPrimitive()
                    && entry.getValue()
                        .getAsJsonPrimitive()
                        .isString(),
                "Language value must be a string in "
                    + file
                    + ": "
                    + entry.getKey()
            );

            keys.add(entry.getKey());
        }

        return keys;
    }

    private static void validateBlockTextures()
        throws IOException {

        Path textureRoot =
            ASSETS.resolve("textures/block");

        Set<String> actual =
            new LinkedHashSet<>();

        try (Stream<Path> files = Files.list(textureRoot)) {
            files
                .filter(path ->
                    path.toString().endsWith(".png")
                )
                .map(path ->
                    path.getFileName()
                        .toString()
                        .replaceFirst("\\.png$", "")
                )
                .sorted()
                .forEach(actual::add);
        }

        Set<String> missing =
            new LinkedHashSet<>(
                REQUIRED_BLOCK_TEXTURES
            );

        missing.removeAll(actual);

        check(
            missing.isEmpty(),
            "Missing required block textures: "
                + missing
        );

        for (String id : REQUIRED_BLOCK_TEXTURES) {
            Path file =
                textureRoot.resolve(id + ".png");

            BufferedImage image =
                ImageIO.read(file.toFile());

            check(
                image != null,
                "Unreadable PNG: " + file
            );

            check(
                image.getWidth() == image.getHeight()
                    && (image.getWidth() == 16 || image.getWidth() == 128),
                "Block texture must be square 16x16 or 128x128: "
                    + file
            );

            int transparent = 0;

            for (int y = 0; y < image.getHeight(); y++) {
                for (
                    int x = 0;
                    x < image.getWidth();
                    x++
                ) {
                    if ((image.getRGB(x, y) >>> 24) == 0) {
                        transparent++;
                    }
                }
            }

            if (LEAF_TEXTURES.contains(id)) {
                double ratio =
                    transparent
                        / (double)(
                            image.getWidth()
                                * image.getHeight()
                        );

                check(
                    ratio >= 0.10D
                        && ratio <= 0.65D,
                    "Leaf alpha ratio outside 10%-65%: "
                        + id
                        + "="
                        + ratio
                );
            } else if (SAPLING_TEXTURES.contains(id)) {
                check(
                    transparent > 0,
                    "Sapling texture must contain transparent pixels: " + id
                );
            } else {
                check(
                    transparent == 0,
                    "Opaque block texture contains "
                        + "transparent pixels: "
                        + id
                );
            }
        }
    }

    private static void validateModelReferences()
        throws IOException {

        Path blockstates =
            ASSETS.resolve("blockstates");

        try (Stream<Path> files = Files.list(blockstates)) {
            for (
                Path file : files
                    .filter(path ->
                        path.toString().endsWith(".json")
                    )
                    .toList()
            ) {
                Set<String> models =
                    new LinkedHashSet<>();

                collectNamedStrings(
                    readJson(file),
                    "model",
                    models
                );

                for (String model : models) {
                    requireModModel(model, file);
                }
            }
        }

        Path models =
            ASSETS.resolve("models");

        try (Stream<Path> files = Files.walk(models)) {
            for (
                Path file : files
                    .filter(path ->
                        path.toString().endsWith(".json")
                    )
                    .toList()
            ) {
                JsonObject model =
                    readJson(file).getAsJsonObject();

                if (model.has("parent")) {
                    requireModModel(
                        model.get("parent").getAsString(),
                        file
                    );
                }

                if (!model.has("textures")) {
                    continue;
                }

                for (
                    var entry :
                        model.getAsJsonObject("textures")
                            .entrySet()
                ) {
                    String texture =
                        entry.getValue().getAsString();

                    if (
                        texture.startsWith("#")
                            || !texture.startsWith(
                                "chaoticd:"
                            )
                    ) {
                        continue;
                    }

                    String local =
                        texture.substring(
                            "chaoticd:".length()
                        );

                    requireFile(
                        ASSETS.resolve(
                            "textures/"
                                + local
                                + ".png"
                        ),
                        "Missing texture "
                            + texture
                            + " referenced by "
                            + file
                    );
                }
            }
        }
    }

    private static void validateEmeraldAndDreamAssets()
        throws IOException {

        for (String id : EMERALD_ITEMS) {
            requireFile(
                ASSETS.resolve(
                    "models/item/" + id + ".json"
                )
            );

            Path texture =
                ASSETS.resolve(
                    "textures/item/" + id + ".png"
                );

            requireFile(texture);

            BufferedImage image =
                ImageIO.read(texture.toFile());

            check(
                image != null,
                "Unreadable item texture: "
                    + texture
            );

            check(
                image.getWidth() == 16
                    && image.getHeight() == 16,
                "Item texture is not 16x16: "
                    + texture
            );
        }

        requireFile(
            ASSETS.resolve(
                "textures/item/death_totem.png"
            )
        );

        validateArmorLayer("emerald_layer_1.png");
        validateArmorLayer("emerald_layer_2.png");
        validateArmorLayer("ruby_layer_1.png");
        validateArmorLayer("ruby_layer_2.png");
        validateArmorLayer("titanium_layer_1.png");
        validateArmorLayer("titanium_layer_2.png");

        requireFile(
            DATA.resolve(
                "structures/dream_fluid_island.nbt"
            )
        );

        requireFile(
            DATA.resolve(
                "worldgen/structure/"
                    + "dream_fluid_island.json"
            )
        );

        requireFile(
            DATA.resolve(
                "worldgen/structure_set/"
                    + "dream_fluid_islands.json"
            )
        );

        requireFile(
            DATA.resolve(
                "structures/dream_fluid_cavern.nbt"
            )
        );

        requireFile(
            DATA.resolve(
                "worldgen/structure/"
                    + "dream_fluid_cavern.json"
            )
        );

        requireFile(
            DATA.resolve(
                "worldgen/structure/"
                    + "dream_fluid_sky_lake.json"
            )
        );

        requireFile(
            DATA.resolve(
                "worldgen/structure_set/"
                    + "dream_fluid_overworld_lakes.json"
            )
        );

        requireFile(
            DATA.resolve(
                "tags/worldgen/structure/"
                    + "dream_fluid_aurora.json"
            )
        );

        requireFile(
            DATA.resolve(
                "tags/worldgen/structure/"
                    + "dream_fluid_overworld.json"
            )
        );
    }

    private static void validateProgressionAssets()
        throws IOException {

        for (String id : PROGRESSION_ITEM_MODELS) {
            requireFile(
                ASSETS.resolve("models/item/" + id + ".json")
            );
        }

        validateArmorLayer("jaxy_layer_1.png");
        validateArmorLayer("jaxy_layer_2.png");
        validateArmorLayer("rosalita_layer_1.png");
        validateArmorLayer("rosalita_layer_2.png");
    }

    private static void validateArmorLayer(
        String fileName
    ) throws IOException {

        Path file =
            ASSETS.resolve(
                "textures/models/armor/" + fileName
            );

        requireFile(file);

        BufferedImage image =
            ImageIO.read(file.toFile());

        check(
            image != null,
            "Unreadable armor texture: " + file
        );

        check(
            image.getWidth() == 64
                && image.getHeight() == 32,
            "Armor layer is not 64x32: " + file
        );
    }

    private static void validateRequiredTags()
        throws IOException {
        requireFile(
            DATA.resolve(
                "tags/fluids/dream_fluid.json"
            )
        );

        requireFile(
            ROOT.resolve(
                "data/minecraft/tags/fluids/water.json"
            )
        );

        requireFile(
            DATA.resolve(
                "tags/blocks/aurora_logs.json"
            )
        );

        requireFile(
            DATA.resolve(
                "tags/blocks/shadow_logs.json"
            )
        );

        Path dirtTag = ROOT.resolve(
            "data/minecraft/tags/blocks/dirt.json"
        );

        requireFile(dirtTag);

        JsonArray values = readJson(dirtTag)
            .getAsJsonObject()
            .getAsJsonArray("values");

        Set<String> dirtBlocks = new LinkedHashSet<>();

        for (JsonElement value : values) {
            dirtBlocks.add(value.getAsString());
        }

        check(
            dirtBlocks.contains("chaoticd:aurora_dirt")
                && dirtBlocks.contains("chaoticd:aurora_grass_block"),
            "Aurora surface blocks are missing from #minecraft:dirt; "
                + "tree, flower and ground-cover survival predicates would reject them"
        );

        Set<String> auroraLogs = stringValues(
            DATA.resolve("tags/blocks/aurora_logs.json")
        );

        check(
            auroraLogs.containsAll(Set.of(
                "chaoticd:aurora_pinkko_wood",
                "chaoticd:stripped_aurora_pinkko_wood",
                "chaoticd:aurora_souless_wood",
                "chaoticd:stripped_aurora_souless_wood",
                "chaoticd:aurora_sky_wood",
                "chaoticd:stripped_aurora_sky_wood"
            )),
            "Aurora wood variants are missing from #chaoticd:aurora_logs"
        );

        Set<String> crystalSensitive = stringValues(
            DATA.resolve("tags/blocks/crystal_sensitive.json")
        );

        check(
            crystalSensitive.containsAll(Set.of(
                "chaoticd:crystal_dirt",
                "chaoticd:crystal_grass_block",
                "chaoticd:crystal_log",
                "chaoticd:crystal_planks",
                "chaoticd:crystal_leaves_1",
                "chaoticd:crystal_leaves_2",
                "chaoticd:crystal_leaves_3",
                "chaoticd:crystal_red_plant",
                "chaoticd:crystal_yellow_plant",
                "chaoticd:crystal_blue_plant",
                "chaoticd:crystal_green_plant",
                "chaoticd:crystal_furnace",
                "chaoticd:crystal_crafting_table"
            )),
            "Crystal Silk Touch tag does not cover every Crystal block"
        );
    }

    private static void validateProgressionWorldgen()
        throws IOException {

        for (var entry : OVERWORLD_ORE_TARGETS.entrySet()) {
            Path feature = DATA.resolve(
                "worldgen/configured_feature/"
                    + entry.getKey()
                    + ".json"
            );

            JsonArray targets = readJson(feature)
                .getAsJsonObject()
                .getAsJsonObject("config")
                .getAsJsonArray("targets");

            List<String> states = targets.asList().stream()
                .map(JsonElement::getAsJsonObject)
                .map(target -> target.getAsJsonObject("state"))
                .map(state -> state.get("Name").getAsString())
                .toList();

            check(
                states.equals(entry.getValue()),
                "Overworld ore targets are invalid for "
                    + entry.getKey()
                    + "; expected="
                    + entry.getValue()
                    + ", actual="
                    + states
            );
        }

        Set<String> pickaxeBlocks = stringValues(
            ROOT.resolve("data/minecraft/tags/blocks/mineable/pickaxe.json")
        );

        check(
            pickaxeBlocks.containsAll(PROGRESSION_PICKAXE_BLOCKS),
            "Progression ores/blocks are missing from #minecraft:mineable/pickaxe: "
                + missingValues(PROGRESSION_PICKAXE_BLOCKS, pickaxeBlocks)
        );
    }

    private static void validateSmithingProgression()
        throws IOException {

        for (SmithingStep step : SMITHING_STEPS) {
            for (String gear : SMITHING_GEAR) {
                String id = step.outputPrefix() + "_" + gear;
                Path recipe = DATA.resolve(
                    "recipes/" + id + step.recipeSuffix() + ".json"
                );
                JsonObject json = readJson(recipe).getAsJsonObject();

                check(
                    "minecraft:smithing_transform".equals(
                        json.get("type").getAsString()
                    ),
                    "Progression recipe is not smithing: " + recipe
                );

                check(
                    step.template().equals(
                        json.getAsJsonObject("template")
                            .get("item")
                            .getAsString()
                    ),
                    "Wrong template for " + recipe
                );

                check(
                    (step.basePrefix() + "_" + gear).equals(
                        json.getAsJsonObject("base")
                            .get("item")
                            .getAsString()
                    ),
                    "Wrong base item for " + recipe
                );

                check(
                    step.addition().equals(
                        json.getAsJsonObject("addition")
                            .get("item")
                            .getAsString()
                    ),
                    "Wrong material for " + recipe
                );

                check(
                    ("chaoticd:" + id).equals(
                        json.getAsJsonObject("result")
                            .get("item")
                            .getAsString()
                    ),
                    "Wrong smithing result for " + recipe
                );
            }
        }

        for (String gear : SMITHING_GEAR) {
            check(
                !Files.exists(DATA.resolve(
                    "recipes/rosalita_" + gear + "_smithing.json"
                )),
                "Rosalita cannot bypass its ten planned intermediary tiers"
            );
        }
    }

    private static void validateAnimatedSoulAssets()
        throws IOException {

        for (String id : ANIMATED_SOUL_TEXTURES) {
            Path texture = ASSETS.resolve("textures/item/" + id + ".png");
            Path metadata = ASSETS.resolve("textures/item/" + id + ".png.mcmeta");
            requireFile(texture);
            requireFile(metadata);

            BufferedImage image = ImageIO.read(texture.toFile());
            check(image != null, "Unreadable animated soul texture: " + texture);
            check(
                image.getWidth() == 22 && image.getHeight() == 88,
                "Animated soul must be a four-frame 22x88 sheet: " + texture
            );

            JsonObject root = readJson(metadata).getAsJsonObject();
            check(root.has("animation"), "Missing animation metadata: " + metadata);
            JsonObject animation = root.getAsJsonObject("animation");
            check(
                animation.has("frametime") && animation.get("frametime").getAsInt() == 2,
                "Animated soul frametime must be two ticks: " + metadata
            );
            JsonArray frames = animation.getAsJsonArray("frames");
            check(
                frames != null && frames.size() == 4,
                "Animated soul must declare four frames: " + metadata
            );
            for (int index = 0; index < 4; index++) {
                check(
                    frames.get(index).getAsInt() == index,
                    "Animated soul frame order must be 0..3: " + metadata
                );
            }
        }
    }

    private static Set<String> stringValues(Path file)
        throws IOException {

        JsonElement root = readJson(file);
        JsonArray values = root.getAsJsonObject().getAsJsonArray("values");
        Set<String> result = new LinkedHashSet<>();

        for (JsonElement value : values) {
            result.add(value.getAsString());
        }

        return result;
    }

    private static Set<String> missingValues(
        Set<String> expected,
        Set<String> actual
    ) {
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(actual);
        return missing;
    }

    private static JsonElement readJson(Path file)
        throws IOException {

        try (
            Reader reader =
                Files.newBufferedReader(file)
        ) {
            JsonElement json =
                JsonParser.parseReader(reader);

            check(
                json != null
                    && !json.isJsonNull(),
                "Empty JSON: " + file
            );

            return json;
        } catch (RuntimeException exception) {
            throw new IllegalStateException(
                "Invalid JSON: " + file,
                exception
            );
        }
    }

    private static void collectNamedStrings(
        JsonElement element,
        String field,
        Set<String> values
    ) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(
                child ->
                    collectNamedStrings(
                        child,
                        field,
                        values
                    )
            );

            return;
        }

        if (!element.isJsonObject()) {
            return;
        }

        for (
            var entry :
                element.getAsJsonObject().entrySet()
        ) {
            if (
                entry.getKey().equals(field)
                    && entry.getValue()
                        .isJsonPrimitive()
            ) {
                values.add(
                    entry.getValue().getAsString()
                );
            } else {
                collectNamedStrings(
                    entry.getValue(),
                    field,
                    values
                );
            }
        }
    }

    private static void requireModModel(
        String id,
        Path source
    ) {
        if (!id.startsWith("chaoticd:")) {
            return;
        }

        String local =
            id.substring("chaoticd:".length());

        requireFile(
            ASSETS.resolve(
                "models/" + local + ".json"
            ),
            "Missing model "
                + id
                + " referenced by "
                + source
        );
    }

    private static void requireFile(Path file) {
        requireFile(
            file,
            "Missing required file: " + file
        );
    }

    private static void requireFile(
        Path file,
        String message
    ) {
        check(
            Files.isRegularFile(file),
            message
        );
    }

    private static void check(
        boolean condition,
        String message
    ) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private record SmithingStep(
        String outputPrefix,
        String basePrefix,
        String template,
        String addition,
        String recipeSuffix
    ) {
    }
}
