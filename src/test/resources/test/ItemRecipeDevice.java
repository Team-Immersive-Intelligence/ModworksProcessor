package test;

import pl.pabilo8.modworks.annotations.recipe.GeneratedShapedRecipe;
import pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe;
import pl.pabilo8.modworks.annotations.recipe.RecipeIngredient;
import pl.pabilo8.modworks.annotations.recipe.RecipeResult;

/**
 * Provides shaped and shapeless recipe declarations on an item class.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@GeneratedShapedRecipe(itemName = "test_device", group = "devices", mirrored = false, pattern = {" P ", "ICI"}, ingredients = {
		@RecipeIngredient(key = 'P', item = "minecraft:planks", data = "32767"),
		@RecipeIngredient(key = 'I', ore = "ingotIron"),
		@RecipeIngredient(key = 'C', item = "material", data = "3")
})
@GeneratedShapelessRecipe(itemName = "test_device", name = "{item}/recycled", ingredients = {
		@RecipeIngredient(item = "minecraft:iron_ingot", count = 2),
		@RecipeIngredient(item = "material", data = "1")
})
@GeneratedShapelessRecipe(itemName = "test_device", name = "{item}/foreign_output", result = @RecipeResult(item = "othermod:component", data = "7", count = 2),
		ingredients = @RecipeIngredient(item = "test_device")
)
public class ItemRecipeDevice
{
}
