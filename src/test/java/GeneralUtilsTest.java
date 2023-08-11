import org.junit.jupiter.api.Test;
import pl.pabilo8.modworks.utils.GeneralUtils;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
class GeneralUtilsTest
{
	@Test
	void toSnakeCase()
	{
		assertEquals(GeneralUtils.toSnakeCase("iWillBecomeSnakeCase"), "i_will_become_snake_case");
	}

	@Test
	void writeStream()
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		assertDoesNotThrow(() -> GeneralUtils.writeStream(stream, "[{('%s %s')}]", "hello", "world"));

		assertEquals(stream.toString(), "[{('hello world')}]");
	}

	@Test
	void writeJSON()
	{
		//TODO: 11.08.2023 test
	}
}