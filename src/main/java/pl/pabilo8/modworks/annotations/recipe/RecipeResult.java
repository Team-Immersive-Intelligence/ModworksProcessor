package pl.pabilo8.modworks.annotations.recipe;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the output stack of a generated recipe.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@Retention(RetentionPolicy.SOURCE)
@Target({})
public @interface RecipeResult
{
	/** @return item registry name template; an empty value uses the annotated item */
	String item() default "";

	/** @return metadata template; the default uses the subtype ordinal, or zero for a class */
	String data() default "{meta}";

	/** @return output stack size from one to 64 */
	int count() default 1;
}
