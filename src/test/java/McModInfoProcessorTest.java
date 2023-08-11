import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.testing.compile.*;
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.pabilo8.modworks.processors.MCModInfoProcessor;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import java.io.IOException;
import java.lang.Compiler;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
class McModInfoProcessorTest
{
	public static final JavaFileObject SOURCE = JavaFileObjects.forResource("test/HelloWorld.java");
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
				.processedWith(new MCModInfoProcessor())
				.compilesWithoutError()
				.and()
				.generatesFileNamed(StandardLocation.SOURCE_OUTPUT, "resources", "mcmod.info");
	}

	@After
	@Test
	void checkFileContents()
	{
		Compilation compile = javac()
				.withProcessors(new MCModInfoProcessor())
				.withOptions(
						"-Amodworks.modid=testmod",
						"-Amodworks.mcmod=true",
						"-Amodworks.flat_json=true",
						"-Amodworks.mcmod.name=Gradle Test Mod",
						"-Amodworks.mcmod.description=Test description",
						"-Amodworks.mcmod.version=0.1.0",
						"-Amodworks.mcmod.mcversion=1.12.2",
						"-Amodworks.mcmod.url=https://example.com",
						"-Amodworks.mcmod.author_list=Pabilo8,Carver,Schaeferd,Bastian,GabrielV,Automated Carver Device"
				)
				.compile(SOURCE);

		CompilationSubject.assertThat(compile).succeeded();

		Optional<JavaFileObject> mcmodGenerated = compile.generatedFile(StandardLocation.SOURCE_OUTPUT, "resources", "mcmod.info");
		Assertions.assertTrue(mcmodGenerated.isPresent());

		Assertions.assertDoesNotThrow(
				() -> {
					String original = strip(getContent(MCMOD));
					String generated = strip(getContent(mcmodGenerated.get()));

					Assertions.assertEquals(
							original, generated
					);
				}
		);
	}

	private String getContent(JavaFileObject fob) throws IOException
	{
		return String.valueOf(fob.getCharContent(true));
	}

	private String strip(String text)
	{
		return Arrays.stream(text.split("\n"))
				.map(String::trim).collect(Collectors.joining())
				.replaceAll(" ","");
	}
}