package pl.pabilo8.modworks.utils;

import com.google.common.base.Functions;
import com.google.gson.stream.JsonWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pabilo8
 * @since 19.04.2023
 */
public class GeneralUtils
{
	public static FileObject getFile(ProcessingEnvironment env, String path) throws IOException
	{
		return env.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", path);
	}

	public static String toSnakeCase(String value)
	{
		return value.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
	}

	public static void writeStream(OutputStream stream, String text, Object... params) throws IOException
	{
		stream.write(String.format(text, params).getBytes(StandardCharsets.UTF_8));
	}

	public static JsonWriter writeJSON(ProcessingEnvironment env, String path) throws IOException
	{
		FileObject file = getFile(env, path);
		JsonWriter writer = new JsonWriter(file.openWriter());
		writer.setIndent("\t");
		return writer;
	}

	public static List<Element> getEnumValues(Element elementEnum)
	{
		return elementEnum.getEnclosedElements().stream()
				.filter(element -> element.getKind().equals(ElementKind.ENUM_CONSTANT))
				.collect(Collectors.toList());
	}

	public static String simpleNameOf(Element element)
	{
		return element.getSimpleName().toString().toLowerCase();
	}
}
