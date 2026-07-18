package net.blue.chaoticd.content.item;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

/** Registers every item model recovered from the Forge archive. */
public final class ModItems {
    private static final Map<String, Item> LEGACY_ITEMS = new LinkedHashMap<>();
    private static final Map<String, Item> NEW_ITEMS = new LinkedHashMap<>();
    private static boolean initialized;

    private ModItems() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        for (Map.Entry<String, net.minecraft.world.level.block.Block> entry : ModBlocks.entries()) {
            register(entry.getKey(), new BlockItem(entry.getValue(), new Item.Properties()),
                ModBlocks.isNewContent(entry.getKey()));
        }

        for (String id : "aluminium_armor_boots,aluminium_armor_chestplate,aluminium_armor_helmet,aluminium_armor_leggings,aluminium_axe,aluminium_hoe,aluminium_ingot,aluminium_pickaxe,aluminium_shovel,aluminium_sword,apple_cow_spawn_egg,armadura_ametista_boots,armadura_ametista_chestplate,armadura_ametista_helmet,armadura_ametista_leggings,armadura_esmeralda_boots,armadura_esmeralda_chestplate,armadura_esmeralda_helmet,armadura_esmeralda_leggings,armadura_ruby_boots,armadura_ruby_chestplate,armadura_ruby_helmet,armadura_ruby_leggings,borracha_armor_boots,crystal_apple_cow_spawn_egg,crystal_creeper_spawn_egg,crystal_eye,crystal_golden_apple_spawn_egg,dimension_apple,dimension_pig_spawn_egg,enxada_ametista,enxada_esmeralda,enxada_madeira_sombra,enxada_ruby,espada_ametista,espada_esmeralda,espada_madeira_sombra,espada_ruby,espada_vortex,espadasombra,gemarosalita,gold_dimension_apple,gold_dimension_pig_spawn_egg,golden_apple_cow_spawn_egg,graveto_sombra,gravetobedrock,joiasombra,joiavortex,machado_ametista,machado_esmeralda,machado_madeira_sombra,machado_ruby,multi_esmeralda,pa_ametista,pa_esmeralda,pa_madeira_sombra,pa_ruby,pepitasombra,picareta_ametista,picareta_esmeralda,picareta_madeira_sombra,picareta_ruby,picaretasombra,ruby,titanium_armor_boots,titanium_armor_chestplate,titanium_armor_helmet,titanium_armor_leggings,titanium_axe,titanium_hoe,titanium_ingot,titanium_pickaxe,titanium_shovel,titanium_sword,totem_sombra,toxic_armor_boots,toxic_armor_chestplate,toxic_armor_helmet,toxic_armor_leggings,toxic_axe,toxic_hoe,toxic_ingot,toxic_pickaxe,toxic_shovel,toxic_sword".split(",")) {
            register(id, new Item(new Item.Properties()), false);
        }
    }

    public static Item get(String id) {
        Item item = NEW_ITEMS.containsKey(id) ? NEW_ITEMS.get(id) : LEGACY_ITEMS.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Unknown Chaotic Dimensions item: " + id);
        }
        return item;
    }

    public static Collection<Item> values() {
        return LEGACY_ITEMS.values();
    }

    public static Collection<Item> newContentValues() {
        return NEW_ITEMS.values();
    }

    private static void register(String id, Item item, boolean newContent) {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ChaoticDimensions.MOD_ID, id), item);
        (newContent ? NEW_ITEMS : LEGACY_ITEMS).put(id, item);
    }
}
