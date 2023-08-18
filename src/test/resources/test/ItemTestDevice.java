package test;

import pl.pabilo8.modworks.annotations.item.GeneratedItemModels;
import pl.pabilo8.modworks.annotations.item.GeneratedSubItemModel;
import pl.pabilo8.modworks.annotations.item.ItemModelType;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
@GeneratedItemModels(itemName = "test2", valueSet = test.ItemTestDevice.ForeignDevices.class)
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

	@GeneratedItemModels(itemName = "test3", texturePath="t_e_s_t")
	protected enum ForeignDevices
	{
		wiUrndLy_FormattEd_Enum,
		PROPER_ENTRY
	}
}