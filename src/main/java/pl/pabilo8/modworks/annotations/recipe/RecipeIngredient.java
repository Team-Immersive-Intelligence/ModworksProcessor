package pl.pabilo8.modworks.annotations.recipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an item or Ore Dictionary ingredient for a generated recipe.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface RecipeIngredient
{
	/** @return symbol used by a shaped recipe; spaces are reserved for empty slots */
	char key() default ' ';

	/** @return item registry name template; set either item or ore */
	String item() default "";

	/** @return Ore Dictionary name template; set either item or ore */
	String ore() default "";

	/** @return item metadata template; use 32767 for wildcard metadata */
	String data() default "0";

	/** @return number of occupied slots in a shapeless recipe; shaped recipes require one */
	int count() default 1;
}
