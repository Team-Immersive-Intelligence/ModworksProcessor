package pl.pabilo8.modworks.processors;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import pl.pabilo8.modworks.annotations.recipe.GeneratedShapedRecipe;
import pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe;
import pl.pabilo8.modworks.annotations.recipe.RecipeIngredient;
import pl.pabilo8.modworks.annotations.recipe.RecipeResult;
import pl.pabilo8.modworks.utils.GeneralUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Generates crafting recipe JSON files for item classes and subtype enums.
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 11.10.2026
 */
@SupportedAnnotationTypes({
		"pl.pabilo8.modworks.annotations.recipe.GeneratedShapedRecipe",
		"pl.pabilo8.modworks.annotations.recipe.GeneratedShapedRecipe.List",
		"pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe",
		"pl.pabilo8.modworks.annotations.recipe.GeneratedShapelessRecipe.List"
})
@SupportedOptions({"modworks.modid", "modworks.resourcedir", "modworks.javadir"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ItemRecipeProcessor extends AbstractModProcessor
{
	private static final Gson GSON = new Gson();
	private final Set<String> writtenPaths = new HashSet<>();

	@Override
	protected boolean doProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		Map<String, Element> elements = new TreeMap<>();
		collect(elements, roundEnv, GeneratedShapedRecipe.class);
		collect(elements, roundEnv, GeneratedShapedRecipe.List.class);
		collect(elements, roundEnv, GeneratedShapelessRecipe.class);
		collect(elements, roundEnv, GeneratedShapelessRecipe.List.class);
		if(elements.isEmpty())
			return false;

		for(Element element : elements.values())
		{
			for(GeneratedShapedRecipe recipe : element.getAnnotationsByType(GeneratedShapedRecipe.class))
				generate(element, recipe.itemName(), recipe.name(), recipe.group(), recipe.result(),
						recipe.ingredients(), recipe::valueSet, recipe.exclude(), recipe.pattern(), recipe.mirrored());
			for(GeneratedShapelessRecipe recipe : element.getAnnotationsByType(GeneratedShapelessRecipe.class))
				generate(element, recipe.itemName(), recipe.name(), recipe.group(), recipe.result(),
						recipe.ingredients(), recipe::valueSet, recipe.exclude(), null, true);
		}
		return true;
	}

	private void collect(Map<String, Element> elements, RoundEnvironment roundEnv, Class<? extends Annotation> annotation)
	{
		for(Element element : roundEnv.getElementsAnnotatedWith(annotation))
			elements.put(((TypeElement)element).getQualifiedName().toString(), element);
	}

	private void generate(Element element, String itemName, String name, String group, RecipeResult result,
			RecipeIngredient[] ingredients, Supplier<Class<? extends Enum>> valueSet, String[] exclude,
			String[] pattern, boolean mirrored)
	{
		try
		{
			require(MODID.matches("[a-z0-9_.-]+"), "Set a valid -Amodworks.modid before generating recipes");
			require(element.getKind()==ElementKind.CLASS||element.getKind()==ElementKind.ENUM,
					"Recipe annotations require an item class or a subtype enum");
			String item = resourceLocation(itemName);
			Element enumType = resolveValueSet(valueSet);
			if(enumType==null&&element.getKind()==ElementKind.ENUM)
				enumType = element;
			if(enumType==null)
			{
				require(exclude.length==0, "Exclusions require a subtype enum");
				generateOne(element, item, "", 0, name, group, result, ingredients, pattern, mirrored);
			}
			else
			{
				require(enumType.getKind()==ElementKind.ENUM, "valueSet must refer to an enum");
				List<Element> values = GeneralUtils.getEnumValues(enumType);
				require(!values.isEmpty(), "The subtype enum must contain at least one constant");
				Set<String> excluded = new HashSet<>(Arrays.asList(exclude));
				Set<String> names = new HashSet<>();
				for(Element value : values)
					names.add(value.getSimpleName().toString());
				require(names.containsAll(excluded), "Exclusions must use declared enum constant names");
				for(int metadata = 0; metadata < values.size(); metadata++)
					if(!excluded.contains(values.get(metadata).getSimpleName().toString()))
						generateOne(element, item, values.get(metadata).getSimpleName().toString().toLowerCase(Locale.ROOT),
								metadata, name, group, result, ingredients, pattern, mirrored);
			}
		} catch(IllegalArgumentException e)
		{
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage(), element);
		}
	}

	private Element resolveValueSet(Supplier<Class<? extends Enum>> valueSet)
	{
		Element type;
		try
		{
			type = elementUtils.getTypeElement(valueSet.get().getCanonicalName());
		} catch(MirroredTypeException e)
		{
			type = typeUtils.asElement(e.getTypeMirror());
		}
		require(type instanceof TypeElement, "valueSet must refer to an enum");
		return ((TypeElement)type).getQualifiedName().contentEquals("java.lang.Enum")?null: type;
	}

	private void generateOne(Element element, String item, String subtype, int metadata, String name, String group,
			RecipeResult result, RecipeIngredient[] ingredients, String[] pattern, boolean mirrored)
	{
		try
		{
			boolean shaped = pattern!=null;
			String defaultName = item.substring(item.indexOf(':')+1)+(subtype.isEmpty()?"": "/"+subtype)
					+(shaped?"/shaped": "/shapeless");
			String recipeName = expand(name.isEmpty()?defaultName: name, item, subtype, metadata);
			require(validPath(recipeName)&&!recipeName.startsWith("_"), "Invalid recipe path: "+recipeName);
			JsonObject json = new JsonObject();
			json.addProperty("type", shaped?"forge:ore_shaped": "forge:ore_shapeless");
			if(!group.isEmpty())
				json.addProperty("group", resourceLocation(expand(group, item, subtype, metadata)));
			if(shaped)
				addShaped(json, pattern, ingredients, item, subtype, metadata, mirrored);
			else
				addShapeless(json, ingredients, item, subtype, metadata);

			require(result.count() >= 1&&result.count() <= 64, "Result count must be between 1 and 64");
			JsonObject output = new JsonObject();
			output.addProperty("item", resourceLocation(expand(result.item().isEmpty()?item: result.item(), item, subtype, metadata)));
			output.addProperty("data", parseMetadata(expand(result.data(), item, subtype, metadata), false));
			output.addProperty("count", result.count());
			json.add("result", output);

			String path = DIR_RESOURCES+"/assets/"+MODID+"/recipes/"+recipeName+".json";
			require(writtenPaths.add(path), "Duplicate recipe path: "+recipeName);
			try(JsonWriter writer = GeneralUtils.writeJSON(processingEnv, path))
			{
				GSON.toJson(json, writer);
			}
		} catch(IllegalArgumentException|IOException e)
		{
			processingEnv.getMessager().printMessage(Kind.ERROR,
					"Cannot generate recipe for "+item+(subtype.isEmpty()?"": "/"+subtype)+": "+e.getMessage(), element);
		}
	}

	private void addShaped(JsonObject json, String[] pattern, RecipeIngredient[] ingredients, String item,
			String subtype, int metadata, boolean mirrored)
	{
		require(pattern.length >= 1&&pattern.length <= 3, "A shaped pattern must contain one to three rows");
		int width = pattern[0].length();
		require(width >= 1&&width <= 3, "A shaped pattern must contain one to three columns");
		Map<Character, JsonObject> keys = new LinkedHashMap<>();
		for(RecipeIngredient ingredient : ingredients)
		{
			require(ingredient.key()!=' ', "A shaped ingredient must define a non-space key");
			require(ingredient.count()==1, "A shaped ingredient must have count 1; repeat its key in the pattern");
			require(!keys.containsKey(ingredient.key()), "Duplicate shaped key: "+ingredient.key());
			keys.put(ingredient.key(), ingredient(ingredient, item, subtype, metadata));
		}
		Set<Character> used = new HashSet<>();
		JsonArray rows = new JsonArray();
		for(String row : pattern)
		{
			require(row.length()==width, "Pattern rows must have equal width");
			rows.add(row);
			for(char symbol : row.toCharArray())
				if(symbol!=' ')
				{
					require(keys.containsKey(symbol), "Undefined pattern key: "+symbol);
					used.add(symbol);
				}
		}
		require(!used.isEmpty(), "A shaped pattern must contain an ingredient");
		require(used.equals(keys.keySet()), "Shaped ingredients contain unused keys");
		JsonObject key = new JsonObject();
		keys.forEach((symbol, value) -> key.add(symbol.toString(), value));
		json.add("pattern", rows);
		json.add("key", key);
		json.addProperty("mirrored", mirrored);
	}

	private void addShapeless(JsonObject json, RecipeIngredient[] ingredients, String item, String subtype, int metadata)
	{
		JsonArray slots = new JsonArray();
		for(RecipeIngredient ingredient : ingredients)
		{
			require(ingredient.key()==' ', "Shapeless ingredients must not define keys");
			require(ingredient.count() >= 1&&ingredient.count() <= 9, "Ingredient count must be between 1 and 9");
			require(slots.size()+ingredient.count() <= 9, "A shapeless recipe must contain one to nine slots");
			JsonObject value = ingredient(ingredient, item, subtype, metadata);
			for(int i = 0; i < ingredient.count(); i++)
				slots.add(value);
		}
		require(slots.size() > 0, "A shapeless recipe must contain one to nine slots");
		json.add("ingredients", slots);
	}

	private JsonObject ingredient(RecipeIngredient ingredient, String item, String subtype, int metadata)
	{
		require(ingredient.item().isEmpty()!=ingredient.ore().isEmpty(), "An ingredient must define exactly one of item or ore");
		JsonObject json = new JsonObject();
		if(!ingredient.ore().isEmpty())
		{
			require(ingredient.data().equals("0"), "Ore Dictionary ingredients must not define metadata");
			String ore = expand(ingredient.ore(), item, subtype, metadata);
			require(ore.matches("[a-zA-Z0-9_:.-]+"), "Invalid Ore Dictionary name: "+ore);
			json.addProperty("type", "forge:ore_dict");
			json.addProperty("ore", ore);
		}
		else
		{
			json.addProperty("item", resourceLocation(expand(ingredient.item(), item, subtype, metadata)));
			json.addProperty("data", parseMetadata(expand(ingredient.data(), item, subtype, metadata), true));
		}
		return json;
	}

	private String expand(String template, String item, String subtype, int metadata)
	{
		if(subtype.isEmpty())
			require(!template.contains("{subtype}")&&!template.contains("{oreSubtype}"), "Subtype placeholders require an enum");
		StringBuilder oreSubtype = new StringBuilder();
		for(String word : subtype.split("_"))
			if(!word.isEmpty())
				oreSubtype.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
		String value = template.replace("{modid}", MODID).replace("{item}", item.substring(item.indexOf(':')+1))
				.replace("{subtype}", subtype).replace("{oreSubtype}", oreSubtype).replace("{meta}", Integer.toString(metadata));
		require(!value.contains("{")&&!value.contains("}"), "Unknown recipe placeholder in: "+template);
		return value;
	}

	private String resourceLocation(String name)
	{
		String value = name.contains(":")?name: MODID+":"+name;
		int colon = value.indexOf(':');
		require(value.substring(0, colon).matches("[a-z0-9_.-]+")&&validPath(value.substring(colon+1)),
				"Invalid item or group resource location: "+value);
		return value;
	}

	private static boolean validPath(String path)
	{
		if(!path.matches("[a-z0-9_.-]+(/[a-z0-9_.-]+)*"))
			return false;
		for(String part : path.split("/"))
			if(part.equals(".")||part.equals(".."))
				return false;
		return true;
	}

	private static int parseMetadata(String value, boolean wildcard)
	{
		require(value.matches("[0-9]+"), "Metadata must be a non-negative integer: "+value);
		int data;
		try
		{
			data = Integer.parseInt(value);
		} catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Metadata is out of range: "+value);
		}
		require(data <= (wildcard?32767: 32766), "Metadata is out of range: "+value);
		return data;
	}

	private static void require(boolean condition, String message)
	{
		if(!condition)
			throw new IllegalArgumentException(message);
	}
}
