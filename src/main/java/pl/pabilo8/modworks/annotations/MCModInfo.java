package pl.pabilo8.modworks.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pabilo8
 * @since 11.08.2023
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface MCModInfo
{
}
