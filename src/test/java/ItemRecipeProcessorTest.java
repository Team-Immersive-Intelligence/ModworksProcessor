import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.pabilo8.modworks.processors.ItemModelProcessor;
import pl.pabilo8.modworks.processors.ItemRecipeProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks recipe JSON generation and compiler diagnostics with example item types.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
class ItemRecipeProcessorTest
{
	@Test
	void generatesBlockConversionsAndUsesDestinationMetadata() throws IOException
	{
		Compilation compilation = compile(JavaFileObjects.forResource("test/ItemRecipeMaterials.java"));
		assertSucceeded(compilation);
		String[] ingots = {"advanced_electronic_alloy", "brass", "platinum", "tungsten", "zinc", "magnet", "silicon", "duraluminium"};
		String[] ores = {"AdvancedElectronicAlloy", "Brass", "Platinum", "Tungsten", "Zinc", "Magnet", "Silicon", "Duraluminium"};
		for(int i = 0; i < ingots.length; i++)
		{
			JsonObject recipe = recipe(compilation, "material_ingot/"+ingots[i]+"/from_block");
			assertEquals("forge:ore_shapeless", recipe.get("type").getAsString());
			assertEquals(1, recipe.getAsJsonArray("ingredients").size());
			assertJson("{\"type\":\"forge:ore_dict\",\"ore\":\"block"+ores[i]+"\"}",
					recipe.getAsJsonArray("ingredients").get(0).getAsJsonObject());
			assertResult(recipe, "testmod:material_ingot", i, 9);
		}
		assertResult(recipe(compilation, "material_ingot/silicon/from_block"), "testmod:material_ingot", 6, 9);
		assertResult(recipe(compilation, "material_nugget/silicon/from_ingot"), "testmod:material_nugget", 5, 9);
		assertFalse(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT,
				"resources/assets/testmod/recipes/material_nugget/rubber/from_ingot.json").isPresent());
		assertEquals(22, generatedJsonCount(compilation));
	}

	@Test
	void generatesNineSlotCompressionRecipes() throws IOException
	{
		Compilation compilation = compile(JavaFileObjects.forResource("test/ItemRecipeMaterials.java"));
		assertSucceeded(compilation);
		JsonObject recipe = recipe(compilation, "material_ingot/advanced_electronic_alloy/from_nuggets");
		assertEquals("forge:ore_shaped", recipe.get("type").getAsString());
		assertEquals(JsonParser.parseString("[\"NNN\",\"NNN\",\"NNN\"]"), recipe.get("pattern"));
		assertTrue(recipe.get("mirrored").getAsBoolean());
		assertJson("{\"N\":{\"type\":\"forge:ore_dict\",\"ore\":\"nuggetAdvancedElectronicAlloy\"}}", recipe.getAsJsonObject("key"));
		assertResult(recipe, "testmod:material_ingot", 0, 1);
		assertResult(recipe(compilation, "material_ingot/silicon/from_nuggets"), "testmod:material_ingot", 6, 1);
		assertFalse(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT,
				"resources/assets/testmod/recipes/material_ingot/magnet/from_nuggets.json").isPresent());
	}

	@Test
	void generatesClassRecipesAndExpandsRepeatedIngredients() throws IOException
	{
		Compilation compilation = compile(JavaFileObjects.forResource("test/ItemRecipeDevice.java"));
		assertSucceeded(compilation);
		JsonObject shaped = recipe(compilation, "test_device/shaped");
		assertEquals("testmod:devices", shaped.get("group").getAsString());
		assertFalse(shaped.get("mirrored").getAsBoolean());
		assertJson("{\"item\":\"minecraft:planks\",\"data\":32767}", shaped.getAsJsonObject("key").getAsJsonObject("P"));
		assertJson("{\"item\":\"testmod:material\",\"data\":3}", shaped.getAsJsonObject("key").getAsJsonObject("C"));
		assertResult(shaped, "testmod:test_device", 0, 1);

		JsonObject shapeless = recipe(compilation, "test_device/recycled");
		assertEquals(3, shapeless.getAsJsonArray("ingredients").size());
		assertEquals(shapeless.getAsJsonArray("ingredients").get(0), shapeless.getAsJsonArray("ingredients").get(1));
		assertJson("{\"item\":\"minecraft:iron_ingot\",\"data\":0}", shapeless.getAsJsonArray("ingredients").get(0).getAsJsonObject());
		assertFalse(shapeless.has("group"));
		assertFalse(shapeless.has("pattern"));
		assertResult(recipe(compilation, "test_device/foreign_output"), "othermod:component", 7, 2);
		assertEquals(3, generatedJsonCount(compilation));
	}

	@Test
	void usesReferencedEnumOnItemClassAndCustomResourceDirectory() throws IOException
	{
		Compilation compilation = Compiler.javac().withProcessors(new ItemRecipeProcessor())
				.withOptions("-Amodworks.modid=testmod", "-Amodworks.resourcedir=generated_resources")
				.compile(JavaFileObjects.forResource("test/ItemRecipeReferenced.java"));
		assertSucceeded(compilation);
		for(int metadata = 0; metadata < 2; metadata++)
		{
			String subtype = metadata==0?"iron_part": "silicon_part";
			JsonObject recipe = json(compilation, "generated_resources/assets/testmod/recipes/parts/"+subtype+"/copy.json");
			assertResult(recipe, "testmod:parts", metadata, 1);
			assertEquals(metadata, recipe.getAsJsonArray("ingredients").get(0).getAsJsonObject().get("data").getAsInt());
			assertEquals("testmod:parts", recipe.get("group").getAsString());
		}
		assertEquals(2, generatedJsonCount(compilation));
	}

	@Test
	void generatesModelsAndRecipesInTheSameCompilation() throws IOException
	{
		Compilation compilation = Compiler.javac().withProcessors(new ItemRecipeProcessor(), new ItemModelProcessor())
				.withOptions("-Amodworks.modid=testmod").compile(JavaFileObjects.forResource("test/ItemRecipeMaterials.java"));
		assertSucceeded(compilation);
		assertResult(recipe(compilation, "material_ingot/brass/from_block"), "testmod:material_ingot", 1, 9);
		assertEquals("item/generated", json(compilation, "resources/assets/testmod/models/item/material_ingot/brass.json").get("parent").getAsString());
		assertEquals(30, generatedJsonCount(compilation));
	}

	@Test
	void expandsRepeatedShapedAnnotations() throws IOException
	{
		String ingredient = "ingredients=@RecipeIngredient(key='X',ore=\"ingotIron\")";
		Compilation compilation = compile(source("Repeated",
				"@GeneratedShapedRecipe(itemName=\"part\",name=\"first\",pattern=\"X\","+ingredient+") "+
						"@GeneratedShapedRecipe(itemName=\"part\",name=\"second\",pattern=\"XX\","+ingredient+") "+
						"public class Repeated {}"));
		assertSucceeded(compilation);
		assertResult(recipe(compilation, "first"), "testmod:part", 0, 1);
		assertResult(recipe(compilation, "second"), "testmod:part", 0, 1);
		assertEquals(JsonParser.parseString("[\"XX\"]"), recipe(compilation, "second").get("pattern"));
		assertEquals(2, generatedJsonCount(compilation));
	}

	@Test
	void usesLocaleIndependentSubtypeNames() throws IOException
	{
		Locale previous = Locale.getDefault();
		try
		{
			Locale.setDefault(new Locale("tr", "TR"));
			Compilation compilation = compile(source("LocaleRecipe",
					"@GeneratedShapelessRecipe(itemName=\"ingots\", ingredients=@RecipeIngredient(ore=\"block{oreSubtype}\")) "+
							"public enum LocaleRecipe { SILICON, IRON_PART }"));
			assertSucceeded(compilation);
			assertEquals("blockSilicon", recipe(compilation, "ingots/silicon/shapeless").getAsJsonArray("ingredients")
						.get(0).getAsJsonObject().get("ore").getAsString());
			assertResult(recipe(compilation, "ingots/iron_part/shapeless"), "testmod:ingots", 1, 1);
		} finally
		{
			Locale.setDefault(previous);
		}
	}

	@Test
	void processesRecipesCreatedInLaterRounds() throws IOException
	{
		Compilation compilation = Compiler.javac().withProcessors(new LaterRecipeProcessor(), new ItemRecipeProcessor())
				.withOptions("-Amodworks.modid=testmod").compile(source("Seed", "public class Seed {}"));
		assertSucceeded(compilation);
		assertResult(recipe(compilation, "later/shapeless"), "testmod:later", 0, 1);
	}

	@Test
	void reportsDuplicatePathsAcrossRecipeTypes()
	{
		Compilation compilation = compile(source("Duplicate",
				"@GeneratedShapedRecipe(itemName=\"part\", name=\"same\", pattern=\"X\", ingredients=@RecipeIngredient(key='X',item=\"minecraft:iron_ingot\")) "+
						"@GeneratedShapelessRecipe(itemName=\"part\", name=\"same\", ingredients=@RecipeIngredient(item=\"minecraft:iron_ingot\")) "+
						"public class Duplicate {}"));
		assertFailedWith(compilation, "Duplicate recipe path: same");
	}

	@Test
	void reportsDuplicatePathsBetweenItemClasses()
	{
		String declaration = "@GeneratedShapelessRecipe(itemName=\"part\", ingredients=@RecipeIngredient(item=\"minecraft:iron_ingot\")) ";
		Compilation compilation = compile(source("First", declaration+"public class First {}"),
				source("Second", declaration+"public class Second {}"));
		assertFailedWith(compilation, "Duplicate recipe path");
	}

	@Test
	void rejectsMissingModId()
	{
		Compilation compilation = Compiler.javac().withProcessors(new ItemRecipeProcessor()).compile(source("MissingMod",
				"@GeneratedShapelessRecipe(itemName=\"part\", ingredients=@RecipeIngredient(item=\"minecraft:iron_ingot\")) public class MissingMod {}"));
		assertFailedWith(compilation, "Set a valid -Amodworks.modid");
	}

	@Test
	void rejectsEmptySubtypeEnums()
	{
		Compilation compilation = compile(source("Empty",
				"@GeneratedShapelessRecipe(itemName=\"part\", ingredients=@RecipeIngredient(ore=\"block{oreSubtype}\")) public enum Empty {}"));
		assertFailedWith(compilation, "The subtype enum must contain at least one constant");
	}

	@Test
	void rejectsUnknownEnumExclusions()
	{
		Compilation compilation = compile(source("Excluded",
				"@GeneratedShapelessRecipe(itemName=\"part\",exclude=\"UNKNOWN\",ingredients=@RecipeIngredient(ore=\"block{oreSubtype}\")) "+
						"public enum Excluded { BRASS }"));
		assertFailedWith(compilation, "Exclusions must use declared enum constant names");
	}

	@ParameterizedTest
	@MethodSource("invalidDeclarations")
	void rejectsInvalidRecipes(String annotation, String message)
	{
		Compilation compilation = compile(source("InvalidRecipe", annotation+" public class InvalidRecipe {}"));
		assertFailedWith(compilation, message);
	}

	private static Stream<Arguments> invalidDeclarations()
	{
		String shapeless = "@GeneratedShapelessRecipe(itemName=\"part\", ";
		String shaped = "@GeneratedShapedRecipe(itemName=\"part\", ";
		String iron = "@RecipeIngredient(item=\"minecraft:iron_ingot\")";
		String key = "@RecipeIngredient(key='X', item=\"minecraft:iron_ingot\")";
		return Stream.of(
				Arguments.of(shapeless+"exclude=\"BRASS\",ingredients="+iron+")", "Exclusions require a subtype enum"),
				Arguments.of(shapeless+"ingredients={})", "one to nine slots"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient())", "exactly one of item or ore"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",ore=\"ingotIron\"))", "exactly one of item or ore"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",count=0))", "Ingredient count"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",count=10))", "Ingredient count"),
				Arguments.of(shapeless+"ingredients={@RecipeIngredient(item=\"part\",count=9),"+iron+"})", "one to nine slots"),
				Arguments.of(shapeless+"ingredients="+key+")", "must not define keys"),
				Arguments.of(shapeless+"result=@RecipeResult(count=0),ingredients="+iron+")", "Result count"),
				Arguments.of(shapeless+"result=@RecipeResult(count=65),ingredients="+iron+")", "Result count"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",data=\"-1\"))", "non-negative integer"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",data=\"32768\"))", "Metadata is out of range"),
				Arguments.of(shapeless+"result=@RecipeResult(data=\"32767\"),ingredients="+iron+")", "Metadata is out of range"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"part\",data=\"9999999999999\"))", "Metadata is out of range"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(ore=\"ingotIron\",data=\"1\"))", "must not define metadata"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(ore=\"bad ore\"))", "Invalid Ore Dictionary name"),
				Arguments.of(shapeless+"ingredients=@RecipeIngredient(item=\"BadItem\"))", "Invalid item or group"),
				Arguments.of(shapeless+"group=\"BadGroup\",ingredients="+iron+")", "Invalid item or group"),
				Arguments.of(shapeless+"name=\"../escape\",ingredients="+iron+")", "Invalid recipe path"),
				Arguments.of(shapeless+"name=\"_ignored\",ingredients="+iron+")", "Invalid recipe path"),
				Arguments.of(shapeless+"name=\"{unknown}\",ingredients="+iron+")", "Unknown recipe placeholder"),
				Arguments.of(shapeless+"name=\"{subtype}\",ingredients="+iron+")", "Subtype placeholders require an enum"),
				Arguments.of(shaped+"pattern={},ingredients="+key+")", "one to three rows"),
				Arguments.of(shaped+"pattern={\"X\",\"X\",\"X\",\"X\"},ingredients="+key+")", "one to three rows"),
				Arguments.of(shaped+"pattern=\"XXXX\",ingredients="+key+")", "one to three columns"),
				Arguments.of(shaped+"pattern={\"X\",\"XX\"},ingredients="+key+")", "equal width"),
				Arguments.of(shaped+"pattern=\"Y\",ingredients="+key+")", "Undefined pattern key"),
				Arguments.of(shaped+"pattern=\"X\",ingredients={"+key+","+key+"})", "Duplicate shaped key"),
				Arguments.of(shaped+"pattern=\"X\",ingredients={"+key+",@RecipeIngredient(key='Y',item=\"part\")})", "unused keys"),
				Arguments.of(shaped+"pattern=\"X\",ingredients="+iron+")", "non-space key"),
				Arguments.of(shaped+"pattern=\"X\",ingredients=@RecipeIngredient(key='X',item=\"part\",count=2))", "must have count 1"),
				Arguments.of(shaped+"pattern=\" \",ingredients={})", "must contain an ingredient"));
	}

	private static Compilation compile(JavaFileObject... sources)
	{
		return Compiler.javac().withProcessors(new ItemRecipeProcessor()).withOptions("-Amodworks.modid=testmod").compile(sources);
	}

	private static JavaFileObject source(String name, String declaration)
	{
		return JavaFileObjects.forSourceString("test."+name,
				"package test; import pl.pabilo8.modworks.annotations.recipe.*; "+declaration);
	}

	private static JsonObject recipe(Compilation compilation, String path) throws IOException
	{
		return json(compilation, "resources/assets/testmod/recipes/"+path+".json");
	}

	private static JsonObject json(Compilation compilation, String path) throws IOException
	{
		assertTrue(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, path).isPresent(), path);
		return JsonParser.parseString(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, path).get()
				.getCharContent(false).toString()).getAsJsonObject();
	}

	private static void assertResult(JsonObject recipe, String item, int data, int count)
	{
		assertJson("{\"item\":\""+item+"\",\"data\":"+data+",\"count\":"+count+"}", recipe.getAsJsonObject("result"));
	}

	private static void assertJson(String expected, JsonObject actual)
	{
		assertEquals(JsonParser.parseString(expected), actual);
	}

	private static void assertSucceeded(Compilation compilation)
	{
		assertEquals(Compilation.Status.SUCCESS, compilation.status(), compilation.errors().toString());
	}

	private static long generatedJsonCount(Compilation compilation)
	{
		return compilation.generatedFiles().stream().filter(file -> file.getName().endsWith(".json")).count();
	}

	private static void assertFailedWith(Compilation compilation, String message)
	{
		assertEquals(Compilation.Status.FAILURE, compilation.status());
		assertTrue(compilation.errors().stream().anyMatch(error -> error.getMessage(Locale.ROOT).contains(message)),
				compilation.errors().toString());
	}

	@SupportedAnnotationTypes("*")
	private static class LaterRecipeProcessor extends AbstractProcessor
	{
		private boolean generated;

		@Override
		public SourceVersion getSupportedSourceVersion()
		{
			return SourceVersion.latestSupported();
		}

		@Override
		public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
		{
			if(!generated&&!roundEnv.processingOver())
			{
				generated = true;
				try(Writer writer = processingEnv.getFiler().createSourceFile("test.LaterRecipe").openWriter())
				{
					writer.write("package test; import pl.pabilo8.modworks.annotations.recipe.*; "+
							"@GeneratedShapelessRecipe(itemName=\"later\",ingredients=@RecipeIngredient(item=\"minecraft:iron_ingot\")) "+
							"public class LaterRecipe {}");
				} catch(IOException e)
				{
					throw new IllegalStateException(e);
				}
			}
			return false;
		}
	}
}
