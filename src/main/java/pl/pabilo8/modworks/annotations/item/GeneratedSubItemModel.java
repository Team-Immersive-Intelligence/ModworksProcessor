package pl.pabilo8.modworks.annotations.item;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate an enum with item
 * @author Pabilo8
 * @since 11.08.2023
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface GeneratedSubItemModel
{
	String customTexturePath() default "";
}
