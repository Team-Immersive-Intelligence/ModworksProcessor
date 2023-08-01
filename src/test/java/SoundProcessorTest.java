import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import pl.pabilo8.modworks.processors.SoundProcessor;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
class SoundProcessorTest
{
	public static final JavaFileObject SOURCE = JavaFileObjects.forResource("test/HelloWorld.java");

	@Test
	void doProcessing()
	{
		assertAbout(javaSource())
				.that(SOURCE)
				.withCompilerOptions("-Amodworks.modid=testmod")
				.processedWith(new SoundProcessor())
				.compilesWithoutError()
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources.assets.testmod", "sounds.json");
		//TODO: 02.08.2023 withContents
	}

	@Test
	void doProcessingWithParams()
	{
		assertAbout(javaSource())
				.that(SOURCE)
				.withCompilerOptions("-Amodworks.modid=testmod")
				.withCompilerOptions("-Amodworks.javadir=java")
				.withCompilerOptions("-Amodworks.resourcedir=resources")
				.processedWith(new SoundProcessor())
				.compilesWithoutError()
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources.assets.testmod", "sounds.json");
	}
}