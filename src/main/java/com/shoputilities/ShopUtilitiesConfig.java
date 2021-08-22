package com.shoputilities;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup("shoputilities")
public interface ShopUtilitiesConfig extends Config
{
	@ConfigSection(
		name = "Positions",
		description = "Adjust overlay text positions",
		position = 0
	)
	String positionSection = "positionSection";

	@ConfigItem(
		keyName = "priceDisplayMode",
		name = "Price Display",
		description = "Display mode on shop items bottom left corner"
	)
	default PriceDisplayMode priceDisplayMode()
	{
		return PriceDisplayMode.PRICE;
	}

	@ConfigItem(
		keyName = "quantityChange",
		name = "Display Quantity Change",
		description = "Change drop"
	)
	default boolean quantityChange()
	{
		return true;
	}

	@ConfigItem(
		keyName = "profitCanvas",
		name = "Display Profit on Canvas",
		description = "Display profit on Canvas"
	)
	default boolean profitCanvas()
	{
		return true;
	}

	@ConfigItem(
		keyName = "shopPriceX",
		name = "Shop Price X",
		description = "Changes X axis of price text on shop widget",
		position = 1,
		section = positionSection
	)
	@Range(
		min = -1,
		max = 20
	)
	@Units(Units.PIXELS)
	default int shopPriceX()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "shopPriceY",
		name = "Shop Price Y",
		description = "Changes Y axis of price text on shop widget",
		position = 2,
		section = positionSection
	)
	@Range(
		min = -20,
		max = 10
	)
	@Units(Units.PIXELS)
	default int shopPriceY()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "inventoryPriceX",
		name = "Inventory Price X",
		description = "Changes X axis of price text on inventory widget",
		position = 3,
		section = positionSection
	)
	@Range(
		min = -10,
		max = 20
	)
	@Units(Units.PIXELS)
	default int inventoryPriceX()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "inventoryPriceY",
		name = "Inventory Price Y",
		description = "Changes Y axis of price text on inventory widget",
		position = 4,
		section = positionSection
	)
	@Range(
		min = -20,
		max = 10
	)
	@Units(Units.PIXELS)
	default int inventoryPriceY()
	{
		return 0;
	}

}
