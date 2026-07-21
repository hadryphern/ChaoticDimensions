package net.blue.chaoticd.content.recipe;

import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModItems.SapphireToolType;
import net.blue.chaoticd.content.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Sapphire tool recipes whose crafted result starts with its intended enchantments. */
public final class SapphireToolRecipe extends CustomRecipe {
    public SapphireToolRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        SapphireToolType type = type();
        return type != null && matchesPattern(inventory, type);
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registries) {
        SapphireToolType type = type();
        return type == null ? ItemStack.EMPTY : ModItems.createSapphireTool(type);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 2 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SAPPHIRE_TOOL;
    }

    private SapphireToolType type() {
        return switch (getId().getPath()) {
            case "sapphire_pickaxe" -> SapphireToolType.PICKAXE;
            case "sapphire_axe" -> SapphireToolType.AXE;
            case "sapphire_shovel" -> SapphireToolType.SHOVEL;
            case "sapphire_hoe" -> SapphireToolType.HOE;
            default -> null;
        };
    }

    private static boolean matchesPattern(CraftingContainer inventory, SapphireToolType type) {
        String[] pattern = switch (type) {
            case PICKAXE -> new String[] {"GGG", " S ", " S "};
            case AXE -> new String[] {"GG ", "GS ", " S "};
            case SHOVEL -> new String[] {"G", "S", "S"};
            case HOE -> new String[] {"GG ", " S ", " S "};
        };
        int width = pattern[0].length();
        for (int offsetX = 0; offsetX <= inventory.getWidth() - width; offsetX++) {
            if (matchesAt(inventory, pattern, offsetX)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAt(CraftingContainer inventory, String[] pattern, int offsetX) {
        for (int x = 0; x < inventory.getWidth(); x++) {
            for (int y = 0; y < inventory.getHeight(); y++) {
                char expected = x >= offsetX && x < offsetX + pattern[0].length() && y < pattern.length
                    ? pattern[y].charAt(x - offsetX) : ' ';
                ItemStack stack = inventory.getItem(x + y * inventory.getWidth());
                if (expected == 'G' && stack.is(ModItems.SAPPHIRE_GEM)) continue;
                if (expected == 'S' && stack.is(Items.STICK)) continue;
                if (expected == ' ' && stack.isEmpty()) continue;
                return false;
            }
        }
        return true;
    }
}
