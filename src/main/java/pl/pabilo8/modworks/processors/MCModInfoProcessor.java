package pl.pabilo8.modworks.processors;

import com.google.auto.service.AutoService;
import com.google.gson.stream.JsonWriter;
import pl.pabilo8.modworks.utils.GeneralUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.Set;

/**
 * Added
 */
@SupportedAnnotationTypes({
		"pl.pabilo8.modworks.annotations.MCModInfo",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class MCModInfoProcessor extends AbstractModProcessor
{
	@Override
	protected boolean doProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if(Boolean.parseBoolean(getOption("modworks.mcmod")))
		{
			try(JsonWriter wr = GeneralUtils.writeJSON(processingEnv, DIR_RESOURCES+"/mcmod.info"))
			{
				wr.beginObject();
				wr.name("modid").value(MODID);
				addProperty(wr, "name");
				addProperty(wr, "description");
				addProperty(wr, "version");
				addProperty(wr, "mcversion");
				addProperty(wr, "url");

				//TODO: 11.08.2023 Automated approach
				addProperty(wr, "updateUrl");
				addArray(wr, "authorList");

				addProperty(wr, "credits");
				addProperty(wr, "logoFile");
				addArray(wr, "screenshots");
				addArray(wr, "dependencies");
				wr.endObject();

			} catch(IOException e)
			{
				processingEnv.getMessager().printMessage(Kind.NOTE,
						"Could not build the mcmod.info file, "+e);
			}


		}
		return true;
	}

	private void addProperty(JsonWriter wr, String name) throws IOException
	{
		wr.name(name).value(getOption("modworks.mcmod."+GeneralUtils.toSnakeCase(name)));
	}

	private void addArray(JsonWriter wr, String name) throws IOException
	{
		wr.name(name).beginArray();
		for(String value : getOption("modworks.mcmod."+GeneralUtils.toSnakeCase(name)).split(","))
			if(!value.isEmpty())
				wr.value(value);
		wr.endArray();
	}

}
