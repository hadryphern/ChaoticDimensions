package net.blue.chaoticd.content;

import java.util.List;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;

/**
 * Single source of truth for entries with intentionally provisional visuals.
 *
 * <p>The creative test tab is driven only by this catalog. A missing survival
 * recipe, a future progression position or an unfinished mechanic is not a
 * placeholder by itself and must not put an otherwise finished item here.</p>
 */
public final class PlaceholderCatalog {
    public enum PlaceholderKind {
        LEGACY_ASSET_ALIAS,
        VANILLA_FALLBACK_TEXTURE,
        MISSING_FINAL_TEXTURE,
        PROVISIONAL_MODEL,
        DEBUG
    }

    public record Entry(ItemLike content, PlaceholderKind kind, String reason) {
    }

    private static final List<Entry> ENTRIES = List.of(
        new Entry(ModBlocks.PASTEL_GRASS, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora grass alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_SOIL, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora soil alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_AURORA_STONE, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora stone alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_AURORA_LOG, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora log alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_AURORA_WOOD, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora wood alias awaiting its own final asset."),
        new Entry(ModBlocks.STRIPPED_PASTEL_AURORA_LOG, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy stripped Aurora log alias awaiting its own final asset."),
        new Entry(ModBlocks.STRIPPED_PASTEL_AURORA_WOOD, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy stripped Aurora wood alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_AURORA_PLANKS, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy Aurora plank alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_PINK_LEAVES, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy pastel leaves alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_PURPLE_LEAVES, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy pastel leaves alias awaiting its own final asset."),
        new Entry(ModBlocks.PASTEL_BLUE_LEAVES, PlaceholderKind.LEGACY_ASSET_ALIAS,
            "Legacy pastel leaves alias awaiting its own final asset."),
        new Entry(ModItems.VORTEX_SOUL, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.DEMONITH, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.THE_CRYSTALINE, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.THE_UNDERGUER, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.THE_CALICE_QUEEN, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.MONTHRA_SCALE, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.CRYSTALINE_SIGIL, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.UNDERGUER_SIGIL, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.VOID, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.TITAN_SOULD, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists."),
        new Entry(ModItems.SOLAR_OBSIDIAN, PlaceholderKind.VANILLA_FALLBACK_TEXTURE,
            "Uses a vanilla fallback icon until an original texture exists.")
    );

    private PlaceholderCatalog() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static boolean isPlaceholder(ItemLike content) {
        return ENTRIES.stream().anyMatch(entry -> entry.content().asItem() == content.asItem());
    }

    public static void addToCreativeTab(CreativeModeTab.Output entries) {
        for (Entry entry : ENTRIES) {
            entries.accept(entry.content());
        }
    }
}
