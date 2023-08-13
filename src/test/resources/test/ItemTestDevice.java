package test;

import pl.pabilo8.modworks.annotations.item.GeneratedItemModels;
import pl.pabilo8.modworks.annotations.item.GeneratedSubItemModel;
import pl.pabilo8.modworks.annotations.item.ItemModelType;
import pl.pabilo8.modworks.annotations.sound.ModSound;
import pl.pabilo8.modworks.annotations.MCModInfo;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
public class ItemTestDevice
{

	@GeneratedItemModels(itemName = "test_device")
	public enum Devices
	{
		TEST1,
		@GeneratedSubItemModel(customTexturePath = "nucleardevice")
		TEST2,
		TEST3;
	}
}