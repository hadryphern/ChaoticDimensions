package net.blue.chaoticd.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Headless guardrail for the removable Orespawn reference-test harness.
 *
 * <p>The validator intentionally reads only authored project files. It does
 * not open, execute or inspect the locally extracted legacy material.</p>
 */
public final class OrespawnTestIsolationValidator {
    private static final Path SOURCE = Path.of("src/main/java/net/blue/chaoticd/test/orespawn");
    private static final Path ASSETS = Path.of("build/resources/main/assets/chaoticd");
    private static final Path GITIGNORE = Path.of(".gitignore");
    private static final List<String> LANGUAGES = List.of("pt_br", "en_us", "es_co", "es_mx");
    private static final List<String> TRANSLATION_KEYS = List.of(
        "itemGroup.chaoticd.orespawn",
        "item.chaoticd.orespawn_test_reference_marker",
        "item.chaoticd.orespawn_test_reference_proxy_spawn_egg",
        "entity.chaoticd.orespawn_test_reference_proxy",
        "tooltip.chaoticd.orespawn_test.local_only",
        "tooltip.chaoticd.orespawn_test.no_legacy_asset",
        "command.chaoticd.orespawn_test.status_enabled",
        "command.chaoticd.orespawn_test.status_disabled",
        "command.chaoticd.orespawn_test.disabled",
        "command.chaoticd.orespawn_test.list_items",
        "command.chaoticd.orespawn_test.list_entities",
        "command.chaoticd.orespawn_test.list_bosses",
        "command.chaoticd.orespawn_test.list_blocked",
        "command.chaoticd.orespawn_test.marker_given",
        "command.chaoticd.orespawn_test.proxy_failed",
        "command.chaoticd.orespawn_test.proxy_summoned",
        "command.chaoticd.orespawn_test.validation_failed",
        "command.chaoticd.orespawn_test.validation_passed",
        "command.chaoticd.orespawn_test.players_only"
    );

    private OrespawnTestIsolationValidator() {
    }

    public static void main(String[] args) throws Exception {
        require(Files.isDirectory(SOURCE), "Orespawn test source package is missing");
        require(Files.readString(GITIGNORE).contains(".assets/"),
            "Local extracted assets must remain ignored by Git");
        validateSourceIsolation();
        validateModels();
        validateTranslations();
        validateToggleContract();

        System.out.println(
            "Orespawn test isolation validation passed: authored harness only, no legacy runtime assets."
        );
    }

    private static void validateSourceIsolation() throws IOException {
        try (Stream<Path> paths = Files.walk(SOURCE)) {
            for (Path file : paths.filter(path -> path.toString().endsWith(".java")).toList()) {
                String source = Files.readString(file);
                require(!source.contains("danger.orespawn"),
                    file + " must not reference a legacy code namespace");
                require(!source.contains(".assets/Orespawn"),
                    file + " must not load local reference assets at runtime");
                require(!source.contains("decompiled-reference"),
                    file + " must not load decompiled legacy code");

                boolean clientPackage = file.toString().contains("/client/");
                if (!clientPackage) {
                    require(!source.contains("net.minecraft.client"),
                        file + " loads a client class from common/server code");
                    require(!source.contains("fabric.api.client"),
                        file + " loads a Fabric client API from common/server code");
                }
            }
        }
    }

    private static void validateModels() throws IOException {
        Path marker = ASSETS.resolve("models/item/orespawn_test_reference_marker.json");
        Path egg = ASSETS.resolve("models/item/orespawn_test_reference_proxy_spawn_egg.json");
        require(Files.isRegularFile(marker), "Missing authored marker item model");
        require(Files.isRegularFile(egg), "Missing authored proxy spawn egg model");
        require(Files.readString(marker).contains("minecraft:item/barrier"),
            "Marker must intentionally use only the vanilla barrier icon");
        require(Files.readString(egg).contains("minecraft:item/template_spawn_egg"),
            "Proxy egg must use the vanilla spawn-egg template");
    }

    private static void validateTranslations() throws IOException {
        for (String language : LANGUAGES) {
            Path file = ASSETS.resolve("lang/" + language + ".json");
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject translations = JsonParser.parseReader(reader).getAsJsonObject();
                for (String key : TRANSLATION_KEYS) {
                    require(translations.has(key), language + " is missing " + key);
                }
            }
        }
    }

    private static void validateToggleContract() throws IOException {
        Path config = SOURCE.resolve("OrespawnTestConfig.java");
        Path module = SOURCE.resolve("OrespawnTestModule.java");
        String configSource = Files.readString(config);
        String moduleSource = Files.readString(module);

        require(configSource.contains("enableOrespawnTestContent"),
            "The explicit Orespawn test config key is missing");
        require(configSource.contains("setProperty(ENABLE_KEY, \"false\")"),
            "The Orespawn test config must default to false");
        require(moduleSource.contains("OrespawnTestEntities.initialize()"),
            "The stable entity compatibility registry is missing");
        require(moduleSource.contains("tab is empty and action commands are blocked"),
            "The disabled-mode compatibility limitation must be documented in code");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
