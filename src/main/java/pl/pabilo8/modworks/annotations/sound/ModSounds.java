package pl.pabilo8.modworks.annotations.sound;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pabilo8
 * @since 31.07.2023
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface ModSounds
{
	ModSound[] value();
}
