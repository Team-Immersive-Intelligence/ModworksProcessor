package pl.pabilo8.modworks.utils;

import com.google.gson.stream.JsonWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
}
