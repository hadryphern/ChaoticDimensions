package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.item.SapphireSwordItem;
import net.blue.chaoticd.content.item.SapphireTier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Map;

/** The first clean-slate items: the manual Sapphire assets and their sword. */
public final class ModItems {
    public static final Item SAPPHIRE_GEM = register("sapphire_gem", new Item(new Item.Properties()));
    public static final Item SAPPHIRE_SWORD = register("sapphire_sword", new SapphireSwordItem(new Item.Properties()));
    public static final Item SAPPHIRE_PICKAXE = register("sapphire_pickaxe", new PickaxeItem(SapphireTier.INSTANCE, 1, -2.8F, new Item.Properties()));
    public static final Item SAPPHIRE_AXE = register("sapphire_axe", new AxeItem(SapphireTier.INSTANCE, 7.0F, -3.0F, new Item.Properties()));
    public static final Item SAPPHIRE_SHOVEL = register("sapphire_shovel", new ShovelItem(SapphireTier.INSTANCE, 1.5F, -3.0F, new Item.Properties()));
    public static final Item SAPPHIRE_HOE = register("sapphire_hoe", new HoeItem(SapphireTier.INSTANCE, -2, -1.0F, new Item.Properties()));

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

    public static ItemStack createSapphireTool(SapphireToolType type) {
        ItemStack result = new ItemStack(type.item());
        Map<net.minecraft.world.item.enchantment.Enchantment, Integer> enchantments = switch (type) {
            case PICKAXE -> Map.of(Enchantments.BLOCK_FORTUNE, 10, Enchantments.BLOCK_EFFICIENCY, 10, Enchantments.UNBREAKING, 10);
            case AXE -> Map.of(Enchantments.BLOCK_FORTUNE, 10, Enchantments.BLOCK_EFFICIENCY, 10,
                Enchantments.UNBREAKING, 10, Enchantments.SHARPNESS, 1);
            case SHOVEL, HOE -> Map.of(Enchantments.BLOCK_EFFICIENCY, 10, Enchantments.UNBREAKING, 10);
        };
        EnchantmentHelper.setEnchantments(enchantments, result);
        return result;
    }

    public enum SapphireToolType {
        PICKAXE(SAPPHIRE_PICKAXE), AXE(SAPPHIRE_AXE), SHOVEL(SAPPHIRE_SHOVEL), HOE(SAPPHIRE_HOE);

        private final Item item;

        SapphireToolType(Item item) {
            this.item = item;
        }

        public Item item() {
            return item;
        }
    }

    public static void initialize() {
        // Entries belong only to the Chaotic Dimensions category family.
    }
}
