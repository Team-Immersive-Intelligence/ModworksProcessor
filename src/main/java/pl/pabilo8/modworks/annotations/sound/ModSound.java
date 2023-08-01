package pl.pabilo8.modworks.annotations.sound;

import java.lang.annotation.*;

/**
 * @author Pabilo8
 * @since 31.07.2023
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@Repeatable(value = ModSounds.class)
public @interface ModSound
{
	/**
	 * Used by fields annotated with multiple sounds
	 *
	 * @return name of this sound, if it's different than the annotated field's name<br>
	 */
	String name() default "";

	String subtitle() default "";

	/**
	 * Offers replacement for characters:
	 * <ul>
	 *     <li>* - replaced with </li>
	 *     <li>{a..n} - list of sounds will be generated with numbers from a to n</li>
	 * </ul>
	 * A location without a domain will have the domain added to it, based on the path of the class that's in.
	 *
	 * @return sound file resource locations
	 */
	String[] sounds() default {};
}
