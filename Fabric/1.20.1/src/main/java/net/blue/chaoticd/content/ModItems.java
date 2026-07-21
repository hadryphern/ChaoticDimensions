package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.item.SapphireSwordItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

/** The first clean-slate items: the manual Sapphire assets and their sword. */
public final class ModItems {
    public static final Item SAPPHIRE_GEM = register("sapphire_gem", new Item(new Item.Properties()));
    public static final Item SAPPHIRE_SWORD = register("sapphire_sword", new SapphireSwordItem(new Item.Properties()));

    private ModItems() {
    }

    private static Item register(String id, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(ChaoticDimensions.MOD_ID, id), item);
    }

    /** The canonical crafted form of the sword, including both of its defining enchantments. */
    public static ItemStack createSapphireSword() {
        ItemStack result = new ItemStack(SAPPHIRE_SWORD);
        EnchantmentHelper.setEnchantments(Map.of(
            ModEnchantments.SAPPHIRIC, 1,
            ModEnchantments.DHEATHIC, 1), result);
        return result;
    }

    public static void initialize() {
        // Entries belong only to the Chaotic Dimensions category family.
    }
}
