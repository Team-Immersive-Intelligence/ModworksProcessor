package test;

import pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe;
import pl.pabilo8.modworks.annotations.recipe.RecipeIngredient;

/**
 * Uses a referenced enum to generate recipes for an item class.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@GeneratedShapelessRecipe(itemName = "parts", valueSet = ItemRecipeReferenced.Parts.class,
		name = "{item}/{subtype}/copy", group = "{modid}:parts",
		ingredients = @RecipeIngredient(item = "parts", data = "{meta}"))
public class ItemRecipeReferenced
{
	public enum Parts
	{
		IRON_PART,
		SILICON_PART;

		private int unrelatedField;
	}
}
