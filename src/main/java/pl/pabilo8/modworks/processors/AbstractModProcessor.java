package pl.pabilo8.modworks.processors;

import javax.annotation.Nonnull;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Pabilo8
 * @since 31.07.2023
 */
public abstract class AbstractModProcessor extends AbstractProcessor
{
	protected static final Pattern PATTERN_NUM_RANGE = Pattern.compile("\\{(\\d*)\\.\\.(\\d*)}");
	protected String MODID, DIR_JAVA, DIR_RESOURCES;

	@Override
	public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if(roundEnv.processingOver())
			return false;

		MODID = getOption("modworks.modid");
		DIR_JAVA = getOption("modworks.javadir", "java");
		DIR_RESOURCES = getOption("modworks.resourcedir","resources");

		return doProcessing(annotations, roundEnv);
	}

	@Nonnull
	protected final String getOption(String name, String ifAbsent)
	{
		return processingEnv.getOptions().getOrDefault(name, ifAbsent);
	}

	@Nonnull
	protected final String getOption(String name)
	{
		return getOption(name, "");
	}

	protected abstract boolean doProcessing(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
