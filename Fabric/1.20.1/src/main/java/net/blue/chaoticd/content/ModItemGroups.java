package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * The native Minecraft equivalent of Chaotic Dimensions sub-tabs.
 * Minecraft 1.20.1 has no nested creative tabs, so each category is a dedicated tab with the same prefix.
 */
public final class ModItemGroups {
    public static final CreativeModeTab MATERIALS = register("materials", "itemGroup.chaoticd.materials",
        () -> new ItemStack(ModItems.SAPPHIRE_GEM), (parameters, entries) -> entries.accept(ModItems.SAPPHIRE_GEM));
    public static final CreativeModeTab ENCHANTMENTS = register("enchantments", "itemGroup.chaoticd.enchantments",
        () -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.SAPPHIRIC, 1)),
        (parameters, entries) -> entries.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.SAPPHIRIC, 1))));
    public static final CreativeModeTab POTIONS = register("potions", "itemGroup.chaoticd.potions",
        () -> ModPotions.potion(Items.POTION), (parameters, entries) -> {
            entries.accept(ModPotions.potion(Items.POTION));
            entries.accept(ModPotions.potion(Items.SPLASH_POTION));
            entries.accept(ModPotions.potion(Items.LINGERING_POTION));
            entries.accept(ModPotions.potion(Items.TIPPED_ARROW));
        });
    public static final CreativeModeTab TOOLS = empty("tools", "itemGroup.chaoticd.tools", Items.DIAMOND_PICKAXE);
    public static final CreativeModeTab WEAPONS = register("weapons", "itemGroup.chaoticd.weapons",
        ModItems::createSapphireSword, (parameters, entries) -> entries.accept(ModItems.createSapphireSword()));
    public static final CreativeModeTab ARMOR = empty("armor", "itemGroup.chaoticd.armor", Items.DIAMOND_CHESTPLATE);
    public static final CreativeModeTab BLOCKS = empty("blocks", "itemGroup.chaoticd.blocks", Items.STONE);
    public static final CreativeModeTab ORES = empty("ores", "itemGroup.chaoticd.ores", Items.IRON_ORE);
    public static final CreativeModeTab NATURE = empty("nature", "itemGroup.chaoticd.nature", Items.OAK_SAPLING);
    public static final CreativeModeTab FOOD = empty("food", "itemGroup.chaoticd.food", Items.APPLE);
    public static final CreativeModeTab SPAWN_EGGS = empty("spawn_eggs", "itemGroup.chaoticd.spawn_eggs", Items.PIG_SPAWN_EGG);
    public static final CreativeModeTab USEFUL = empty("useful", "itemGroup.chaoticd.useful", Items.CRAFTING_TABLE);

    private ModItemGroups() {
    }

    private static CreativeModeTab empty(String id, String title, net.minecraft.world.item.Item icon) {
        return register(id, title, () -> new ItemStack(icon), (parameters, entries) -> { });
    }

    private static CreativeModeTab register(String id, String title,
                                            java.util.function.Supplier<ItemStack> icon,
                                            CreativeModeTab.DisplayItemsGenerator entries) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            new ResourceLocation(ChaoticDimensions.MOD_ID, id),
            FabricItemGroup.builder()
                .title(Component.translatable(title))
                .icon(icon)
                .displayItems(entries)
                .build());
    }

    public static void initialize() {
        // Loading this class performs the registry entries above.
    }
}
