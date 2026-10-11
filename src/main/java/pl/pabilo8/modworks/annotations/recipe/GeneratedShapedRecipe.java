package pl.pabilo8.modworks.annotations.recipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates Forge 1.12 shaped recipes for an item class or each subtype of an enum.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(GeneratedShapedRecipe.List.class)
public @interface GeneratedShapedRecipe
{
	/** @return registry name of the item; a missing namespace uses the mod ID */
	String itemName();

	/** @return recipe path template; an empty value uses the item, subtype and recipe type */
	String name() default "";

	/** @return subtype enum to use instead of the annotated type */
	Class<? extends Enum> valueSet() default Enum.class;

	/** @return enum constant names to skip; their ordinals remain reserved */
	String[] exclude() default {};

	/** @return recipe book group template; an empty value omits the group */
	String group() default "";

	/** @return output stack; an empty item uses itemName */
	RecipeResult result() default @RecipeResult;

	/** @return one to three rows of one to three slots with equal width */
	String[] pattern();

	/** @return whether the recipe also accepts a mirrored pattern */
	boolean mirrored() default true;

	/** @return ingredients with distinct keys used in the pattern */
	RecipeIngredient[] ingredients();

	/** Contains repeated recipe declarations. */
	@Retention(RetentionPolicy.SOURCE)
	@Target(ElementType.TYPE)
	@interface List
	{
		/** @return recipe declarations */
		GeneratedShapedRecipe[] value();
	}
}
