import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import pl.pabilo8.modworks.processors.ItemModelProcessor;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
class ItemModelProcessorTest
{
	public static final JavaFileObject SOURCE = JavaFileObjects.forResource("test/ItemTestDevice.java");
	public static final JavaFileObject MCMOD = JavaFileObjects.forResource("jsons/mcmod.info");

	@Test
	void doProcessing()
	{
		final CharSequence[] text = new CharSequence[1];
		assertDoesNotThrow(() -> text[0] = MCMOD.getCharContent(true));

		assertAbout(javaSource())
				.that(SOURCE)
				.withCompilerOptions("-Amodworks.modid=testmod")
				.withCompilerOptions("-Amodworks.mcmod=true")
				.processedWith(new ItemModelProcessor())
				.compilesWithoutError()
				//Test 1
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test_device", "test1.json")
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test_device", "test2.json")
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test_device", "test3.json")
				//Test 2
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test2", "wiurndly_formatted_enum.json")
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test2", "proper_entry.json")
				//Test 3
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test3", "wiurndly_formatted_enum.json")
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources/assets/testmod/models/item/test3", "proper_entry.json");
	}
}