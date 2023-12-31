package pl.pabilo8.modworks.processors;

import com.google.auto.service.AutoService;
import com.google.gson.stream.JsonWriter;
import pl.pabilo8.modworks.annotations.item.GeneratedItemModels;
import pl.pabilo8.modworks.annotations.item.GeneratedSubItemModel;
import pl.pabilo8.modworks.annotations.item.ItemModelType;
import pl.pabilo8.modworks.utils.GeneralUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes({
		"pl.pabilo8.modworks.annotations.item.GeneratedItemModels",
		"pl.pabilo8.modworks.annotations.item.GeneratedSubItemModel"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ItemModelProcessor extends AbstractModProcessor
{
	@Override
	protected boolean doProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GeneratedItemModels.class);
		for(Element element : elements)
		{
			GeneratedItemModels itemModel = element.getAnnotation(GeneratedItemModels.class);
			//Model properties
			String fileName = itemModel.itemName();
			String texturePath = itemModel.texturePath().isEmpty()?fileName: itemModel.texturePath();

			//Based on another enum
			if(createAnotherEnumItemModel(itemModel, fileName, texturePath))
				continue;

			//Based on an annotated enum
			List<Element> enumValues = GeneralUtils.getEnumValues(element);
			if(!enumValues.isEmpty()) //is an enum with multiple models
				for(Element value : enumValues)
				{
					//SubModel overrides/complements the main settings
					GeneratedSubItemModel subModel = value.getAnnotation(GeneratedSubItemModel.class);
					if(itemModel.onlyInAnnotated()&&subModel==null)
						continue;

					String subName = fileName+"/"+GeneralUtils.simpleNameOf(value);
					String subTexture = texturePath+"/"+GeneralUtils.simpleNameOf(value);

					if(subModel!=null&&!subModel.customTexturePath().isEmpty())
						subTexture = subModel.customTexturePath();

					tryWriteModel(subName, subTexture, itemModel.type());

				}
			else //is a class with a single model
				tryWriteModel(fileName, texturePath, itemModel.type());
		}


		return !elements.isEmpty();
	}

	private boolean createAnotherEnumItemModel(GeneratedItemModels itemModel, String fileName, String texturePath)
	{
		List<String> valueSet;
		try
		{
			Class<? extends Enum> clazz = itemModel.valueSet();
			valueSet = Arrays.stream(clazz.getEnumConstants())
					.map(Enum::name)
					.map(String::toLowerCase)
					.collect(Collectors.toList());

		} catch(MirroredTypeException mte)
		{
			DeclaredType classTypeMirror = (DeclaredType)mte.getTypeMirror();
			valueSet = GeneralUtils.getEnumValues(classTypeMirror.asElement())
					.stream()
					.map(GeneralUtils::simpleNameOf)
					.map(String::toLowerCase)
					.collect(Collectors.toList());
		}

		for(String value : valueSet)
			tryWriteModel(fileName+"/"+value, texturePath+"/"+value, itemModel.type());

		return !valueSet.isEmpty();
	}

	void tryWriteModel(String modelName, String texturePath, ItemModelType type)
	{
		try(JsonWriter writer = GeneralUtils.writeJSON(processingEnv, String.format("%s/assets/%s/models/item/%s.json", DIR_RESOURCES, MODID, modelName)))
		{
			writer.beginObject();

			//Parent model this one is based on
			writer.name("parent").value(type.getParentModel());

			//Add textures
			writer.name("textures").beginObject();
			switch(type)
			{
				case ITEM_SIMPLE:
				case ITEM_SIMPLE_TOOL:
					writer.name("layer0").value(String.format("%s:items/%s", MODID, texturePath));
					break;
				case ITEM_SIMPLE_AUTOREPLACED:
					break;
			}
			writer.endObject();

			writer.endObject();
		} catch(IOException e)
		{
			processingEnv.getMessager().printMessage(Kind.NOTE,
					"Could not build a sound file for, "+modelName);
			processingEnv.getMessager().printMessage(Kind.NOTE, e.getLocalizedMessage());
		}
	}
}
