package com.shoputilities;

import com.google.common.primitives.Ints;
import com.google.inject.Provides;
import com.shoputilities.overlays.InventoryOverlay;
import com.shoputilities.overlays.ProfitOverlay;
import com.shoputilities.overlays.ShopOverlay;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Shop Utilities"
)
public class ShopUtilitiesPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ShopOverlay shopOverlay;

	@Inject
	private InventoryOverlay inventoryOverlay;

	@Inject
	private ProfitOverlay profitOverlay;

	@Inject
	public ShopUtilitiesConfig config;

	public HashMap<Integer, Integer> currentShopStock = new HashMap<>();

	@Getter
	private HashMap<Integer, Integer> itemDifference = new HashMap<>();

	private List<Item> lastItems = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(shopOverlay);
		overlayManager.add(inventoryOverlay);
		overlayManager.add(profitOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		currentShopStock.clear();
		QuantityDrop.removeAll();
		overlayManager.remove(shopOverlay);
		overlayManager.remove(inventoryOverlay);
		overlayManager.remove(profitOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		currentShopStock.clear();
		QuantityDrop.removeAll();
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == WidgetID.SHOP_GROUP_ID)
		{
			currentShopStock.clear();
			QuantityDrop.removeAll();
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		for (Iterator<WeakReference<QuantityDrop>> it = QuantityDrop.instances.iterator(); it.hasNext();)
		{
			WeakReference<QuantityDrop> instance = it.next();
			QuantityDrop quantityDrop = instance.get();
			if (quantityDrop == null)
			{
				continue;
			}
			Instant time = quantityDrop.getInstant();
			if (ChronoUnit.SECONDS.between(time, Instant.now()) > 2 || quantityDrop.getChangedAmount() == 0)
			{
				it.remove();
				QuantityDrop.removeQuantityDrop(instance);
			}
		}
		if (currentShopStock.isEmpty())
		{
			return;
		}
		Widget shopItemContainer = client.getWidget(WidgetID.SHOP_GROUP_ID, 16);
		if (shopItemContainer == null)
		{
			return;
		}
		Widget[] children = shopItemContainer.getDynamicChildren();
		HashMap<Integer, Integer> shopStock = new HashMap<>();
		for (Widget item : children)
		{
			if (item.getItemId() < 0)
			{
				continue;
			}
			shopStock.put(item.getItemId(), item.getItemQuantity());
		}
		List<Map.Entry<Integer, Integer>> removeFromStock = currentShopStock.entrySet().stream()
			.filter(e -> !shopStock.containsKey(e.getKey()))
			.collect(Collectors.toList());
		removeFromStock.forEach(e -> currentShopStock.remove(e.getKey()));
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{

		final ItemContainer container = event.getItemContainer();

		if (event.getItemContainer() != client.getItemContainer(InventoryID.INVENTORY))
		{
			return;
		}

		final Item[] items = container.getItems();

		final List<Item> itemList = Arrays.asList(items);

		Widget shopWidget = client.getWidget(WidgetID.SHOP_GROUP_ID, 0);
		if (shopWidget == null)
		{
			lastItems = itemList;
			return;
		}

		if (itemList.equals(lastItems))
		{
			return;
		}

		for (int i = 0; i < itemList.size(); i++)
		{
			Item itemListElement = itemList.get(i);
			if (!lastItems.isEmpty() && i < lastItems.size())
			{
				Item lastItemsElement = lastItems.get(i);

				if (lastItemsElement.equals(itemListElement))
				{
					continue;
				}

				if (itemListElement.getId() == -1)
				{
					add(lastItemsElement.getId(), -lastItemsElement.getQuantity());
				}
				else
				{
					add(itemListElement.getId(), itemListElement.getQuantity() - lastItemsElement.getQuantity());
				}
			}
			else
			{
				add(itemListElement.getId(), itemListElement.getQuantity());
			}
		}
		lastItems = itemList;
	}

	private void add(int itemID, int quantity)
	{
		if (lastItems.isEmpty())
		{
			return;
		}
		if (itemID == -1)
		{
			return;
		}
		if (itemDifference.containsKey(itemID))
		{
			itemDifference.replace(itemID, itemDifference.get(itemID) + quantity);
			if (itemDifference.get(itemID) == 0)
			{
				itemDifference.remove(itemID);
			}
		}
		else
		{
			itemDifference.put(itemID, quantity);
		}
	}

	public int getBuyPrice(BigDecimal sellRate, BigDecimal ratio, BigDecimal defaultPrice, int deltaStock)
	{
		final BigDecimal rate = ratio.multiply(BigDecimal.valueOf(deltaStock));
		final BigDecimal changeRate = sellRate.add(rate);
		final int currentPrice = changeRate.multiply(defaultPrice).intValue();
		int minValue = Math.max(1, defaultPrice.multiply(BigDecimal.valueOf(0.1)).intValue());
		int maxValue = Math.max(1, defaultPrice.multiply(BigDecimal.valueOf(6)).intValue());
		return Ints.constrainToRange(currentPrice, minValue, maxValue);
	}

	public int getSellPrice(BigDecimal buyRate, BigDecimal ratio, BigDecimal defaultSell, BigDecimal defaultBuy, int deltaStock)
	{
		final BigDecimal rate = ratio.multiply(BigDecimal.valueOf(deltaStock));
		final BigDecimal changeRate = buyRate.add(rate);
		final int currentPrice = changeRate.multiply(defaultSell).intValue();
		int maxValue = defaultSell.add(defaultBuy).intValue();
		return Math.min(currentPrice, maxValue);
	}

	public int getGeneralStoreBuyPrice(BigDecimal sellRate, BigDecimal ratio, BigDecimal defaultPrice, int deltaStock)
	{
		final BigDecimal rate = ratio.multiply(BigDecimal.valueOf(deltaStock));
		final BigDecimal changeRate = sellRate.subtract(rate);
		final int currentPrice = defaultPrice.multiply(changeRate).intValue();
		int maxPrice = Math.max(defaultPrice.multiply(BigDecimal.valueOf(0.1)).intValue(), sellRate.multiply(defaultPrice).subtract(defaultPrice).intValue());
		return Math.max(1, Math.max(currentPrice, maxPrice));
	}

	public int getGeneralStoreSellPrice(BigDecimal buyRate, BigDecimal ratio, BigDecimal defaultPrice, int deltaStock)
	{
		final BigDecimal rate = ratio.multiply(BigDecimal.valueOf(deltaStock));
		final BigDecimal changeRate = buyRate.add(rate);
		final int currentPrice = changeRate.multiply(defaultPrice).intValue();
		int minValue = defaultPrice.multiply(BigDecimal.valueOf(0.1)).intValue();
		return Math.max(currentPrice, minValue);
	}

	@Provides
	ShopUtilitiesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ShopUtilitiesConfig.class);
	}
}