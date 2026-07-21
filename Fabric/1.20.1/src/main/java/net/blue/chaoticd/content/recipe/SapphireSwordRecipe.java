package net.blue.chaoticd.content.recipe;

import net.blue.chaoticd.content.ModItems;
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

/** Two Sapphire Gems above a stick; results are born with both defining enchantments. */
public final class SapphireSwordRecipe extends CustomRecipe {
    public SapphireSwordRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        if (inventory.getWidth() < 1 || inventory.getHeight() < 3) {
            return false;
        }
        for (int candidateColumn = 0; candidateColumn < inventory.getWidth(); candidateColumn++) {
            boolean matches = true;
            for (int x = 0; x < inventory.getWidth() && matches; x++) {
                for (int y = 0; y < inventory.getHeight(); y++) {
                    ItemStack stack = inventory.getItem(x + y * inventory.getWidth());
                    if (x == candidateColumn && y < 2 && stack.is(ModItems.SAPPHIRE_GEM)) {
                        continue;
                    }
                    if (x == candidateColumn && y == 2 && stack.is(Items.STICK)) {
                        continue;
                    }
                    if (!stack.isEmpty()) {
                        matches = false;
                        break;
                    }
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingContainer inventory, RegistryAccess registries) {
        return ModItems.createSapphireSword();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SAPPHIRE_SWORD;
    }
}
