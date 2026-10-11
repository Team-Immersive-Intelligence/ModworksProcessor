package test;

import pl.pabilo8.modworks.annotations.item.GeneratedItemModels;
import pl.pabilo8.modworks.annotations.recipe.GeneratedShapedRecipe;
import pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe;
import pl.pabilo8.modworks.annotations.recipe.RecipeIngredient;
import pl.pabilo8.modworks.annotations.recipe.RecipeResult;

/**
 * Provides material subtypes with the same metadata order as the II item enums.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
public class ItemRecipeMaterials
{
	@GeneratedItemModels(itemName = "material_ingot", texturePath = "material/ingot")
	@GeneratedShapelessRecipe(itemName = "material_ingot", name = "{item}/{subtype}/from_block",
			result = @RecipeResult(count = 9), ingredients = @RecipeIngredient(ore = "block{oreSubtype}"))
	@GeneratedShapedRecipe(itemName = "material_ingot", name = "{item}/{subtype}/from_nuggets",
			exclude = "MAGNET",
			pattern = {"NNN", "NNN", "NNN"}, ingredients = @RecipeIngredient(key = 'N', ore = "nugget{oreSubtype}"))
	public enum Ingots
	{
		ADVANCED_ELECTRONIC_ALLOY,
		BRASS,
		PLATINUM,
		TUNGSTEN,
		ZINC,
		MAGNET,
		SILICON,
		DURALUMINIUM
	}

	@GeneratedShapelessRecipe(itemName = "material_nugget", name = "{item}/{subtype}/from_ingot",
			exclude = "RUBBER",
			result = @RecipeResult(count = 9), ingredients = @RecipeIngredient(ore = "ingot{oreSubtype}"))
	public enum Nuggets
	{
		ADVANCED_ELECTRONIC_ALLOY,
		BRASS,
		PLATINUM,
		TUNGSTEN,
		ZINC,
		SILICON,
		DURALUMINIUM,
		RUBBER
	}
}
