package pl.pabilo8.modworks.annotations.item;

/**
 * @author Pabilo8
 * @since 12.08.2023
 */
public enum ItemModelType
{
	//For simple, batched items like crafting materials
	ITEM_SIMPLE("item/generated"),
	//For tool item models, like hammers, wrenches, pickaxes
	ITEM_SIMPLE_TOOL("item/handheld"),
	//For empty item models that will have contents generated dynamically
	ITEM_SIMPLE_AUTOREPLACED("item/generated");

	private final String parentModel;

	ItemModelType(String parentModel)
	{
		this.parentModel = parentModel;
	}

	public String getParentModel()
	{
		return parentModel;
	}
}
