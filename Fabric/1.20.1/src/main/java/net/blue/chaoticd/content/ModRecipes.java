package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.recipe.SapphireSwordRecipe;
import net.blue.chaoticd.content.recipe.SapphireToolRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

/** Serializers for recipes which need to set data on their result. */
public final class ModRecipes {
    public static final RecipeSerializer<SapphireSwordRecipe> SAPPHIRE_SWORD = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "sapphire_sword"),
        new SimpleCraftingRecipeSerializer<>(SapphireSwordRecipe::new));
    public static final RecipeSerializer<SapphireToolRecipe> SAPPHIRE_TOOL = Registry.register(
        BuiltInRegistries.RECIPE_SERIALIZER,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "sapphire_tool"),
        new SimpleCraftingRecipeSerializer<>(SapphireToolRecipe::new));

    private ModRecipes() {
    }

    public static void initialize() {
        // Static registration is intentionally triggered during common initialization.
    }
}
