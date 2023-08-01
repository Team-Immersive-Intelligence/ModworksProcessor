package pl.pabilo8.modworks.processors;

import com.google.auto.service.AutoService;
import pl.pabilo8.modworks.utils.GeneralUtils;
import pl.pabilo8.modworks.annotations.sound.ModSound;
import pl.pabilo8.modworks.annotations.sound.ModSounds;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.regex.Matcher;

@SupportedAnnotationTypes({
		"pl.pabilo8.modworks.annotations.sound.ModSound",
		"pl.pabilo8.modworks.annotations.sound.ModSounds"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class SoundProcessor extends AbstractModProcessor
{
	@Override
	protected boolean doProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		try
		{
			//create the file
			FileObject file = GeneralUtils.getFile(processingEnv, DIR_RESOURCES+"/assets/"+MODID+"/sounds.json");
			OutputStream stream = file.openOutputStream();

			//write elements to json
			GeneralUtils.writeStream(stream, "{\n");
			boolean first = true;

			//write single sounds
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ModSound.class);
			for(Element element : elements)
			{
				Name name = element.getSimpleName();
				ModSound sound = element.getAnnotation(ModSound.class);

				if(!first)
					GeneralUtils.writeStream(stream, ",\n");
				writeSound(stream, name.toString(), sound);
				first = false;
			}

			//write multiple sounds (multiple ModSound annotations get converted to ModSounds)
			elements = roundEnv.getElementsAnnotatedWith(ModSounds.class);
			for(Element element : elements)
			{
				Name name = element.getSimpleName();
				ModSound[] sounds = element.getAnnotation(ModSounds.class).value();
				for(ModSound sound : sounds)
				{
					if(!first)
						GeneralUtils.writeStream(stream, ",\n");
					writeSound(stream, name.toString(), sound);
					first = false;
				}
			}

			//enough of you
			GeneralUtils.writeStream(stream, "\n}");
			stream.close();
			processingEnv.getMessager().printMessage(Kind.NOTE, "Successfully built sounds.json");

		} catch(IOException e)
		{
			processingEnv.getMessager().printMessage(Kind.NOTE,
					"Could not build the sounds.json file, "+e);
		}

		return true;
	}

	private void writeSound(OutputStream stream, String name, ModSound sound) throws IOException
	{
		//Use the custom name over field's name, if possible
		if(!sound.name().isEmpty())
			name = sound.name();

		//Resource locations have to snake_case
		name = GeneralUtils.toSnakeCase(name);

		GeneralUtils.writeStream(stream, "\"%s\":{", name);
		GeneralUtils.writeStream(stream, "\"sounds\":[");

		boolean first = true;
		for(String s : sound.sounds())
		{
			//Replace all asterisks with sound name
			s = s.replace("*", name);

			if(!s.contains(":"))
				s = MODID+":"+s;

			//Add entries for number pattern {a..n}
			Matcher matcher = PATTERN_NUM_RANGE.matcher(s);
			if(matcher.find())
			{
				int start = Integer.parseInt(matcher.group(1)), finish = Integer.parseInt(matcher.group(2));
				s = matcher.replaceAll("");
				for(int i = start; i <= finish; i++)
				{
					GeneralUtils.writeStream(stream, "%c\"%s%d\"", (first?' ': ','), s, i);
					first = false;
				}

			}
			else
				GeneralUtils.writeStream(stream, "\"%s\"%c", s, (first?' ': ','));
			first = false;
		}

		GeneralUtils.writeStream(stream, "],");

		//Add subtitle, if subtitle name starts with "subtitle", it's a vanilla one, else format it to include the mod id
		String subtitle = sound.subtitle();
		if(subtitle.startsWith("subtitle"))
			GeneralUtils.writeStream(stream, "\"subtitle\":\"%s\"", subtitle);
		else
			GeneralUtils.writeStream(stream, "\"subtitle\":\"subtitle.%s.%s\"", MODID,
					subtitle.replace("*", name));

		GeneralUtils.writeStream(stream, "}");
	}

}
