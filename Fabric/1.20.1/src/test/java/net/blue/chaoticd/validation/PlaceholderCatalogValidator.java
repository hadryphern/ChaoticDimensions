package net.blue.chaoticd.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards the visual-only rule for the Chaotic Test creative tab without
 * bootstrapping Minecraft registries in a headless process.
 */
public final class PlaceholderCatalogValidator {
    private static final Path CATALOG = Path.of(
        "src/main/java/net/blue/chaoticd/content/PlaceholderCatalog.java"
    );
    private static final Path ITEM_GROUPS = Path.of(
        "src/main/java/net/blue/chaoticd/content/ModItemGroups.java"
    );
    private static final Pattern ENTRY = Pattern.compile(
        "new Entry\\(Mod(?:Items|Blocks)\\.[A-Z0-9_]+, PlaceholderKind\\.([A-Z_]+)"
    );

    private PlaceholderCatalogValidator() {
    }

    public static void main(String[] args) throws Exception {
        String catalog = Files.readString(CATALOG);
        String groups = Files.readString(ITEM_GROUPS);

        int entries = 0;
        int legacyAliases = 0;
        int vanillaFallbacks = 0;
        Matcher matcher = ENTRY.matcher(catalog);
        while (matcher.find()) {
            entries++;
            if (matcher.group(1).equals("LEGACY_ASSET_ALIAS")) {
                legacyAliases++;
            }
            if (matcher.group(1).equals("VANILLA_FALLBACK_TEXTURE")) {
                vanillaFallbacks++;
            }
        }

        require(entries == 22, "Expected exactly 22 central visual-placeholder entries, got " + entries);
        require(legacyAliases == 11, "Expected 11 legacy visual aliases, got " + legacyAliases);
        require(vanillaFallbacks == 11, "Expected 11 vanilla fallback visuals, got " + vanillaFallbacks);
        require(catalog.contains("public static boolean isPlaceholder"),
            "Placeholder detection must remain centralized");
        require(catalog.contains("A missing survival")
                && catalog.contains("placeholder by itself"),
            "Catalog documentation must reject non-visual placeholder criteria");
        require(groups.contains("private static void addTestItems(CreativeModeTab.Output entries)"),
            "Chaotic Test must use a dedicated population method");
        require(groups.contains("PlaceholderCatalog.addToCreativeTab(entries);"),
            "Chaotic Test must be populated by the central catalog");

        System.out.println(
            "Placeholder catalog validation passed: 22 visual-only entries in stable central order."
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
