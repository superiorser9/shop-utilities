package com.shoputilities.overlays;

import com.shoputilities.PriceDisplayMode;
import com.shoputilities.QuantityDrop;
import com.shoputilities.Shop;
import com.shoputilities.ShopItemInfo;
import com.shoputilities.ShopUtilitiesPlugin;
import com.shoputilities.shops.GeneralStore;
import com.shoputilities.shops.SpecialtyShop;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.math.BigDecimal;
import java.time.Instant;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import static net.runelite.api.widgets.WidgetID.SHOP_GROUP_ID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

@Slf4j
public class ShopOverlay extends WidgetItemOverlay
{

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ShopUtilitiesPlugin shopUtilitiesPlugin;

	@Inject
	ShopOverlay()
	{
		showOnInterfaces(SHOP_GROUP_ID);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		Widget shopNameWidget = client.getWidget(WidgetID.SHOP_GROUP_ID, 1);
		String shopName = shopNameWidget.getDynamicChildren()[1].getText();
		GeneralStore generalStore = GeneralStore.getData(shopName);
		Shop shop = generalStore == null ? SpecialtyShop.getData(shopName) : generalStore;
		if (shop == null)
		{
			log.debug("Shop {} not found", shopName);
			return;
		}
		if (itemId < 0)
		{
			return;
		}
		if (!shop.getShopItems().containsKey(itemId) && generalStore == null)
		{
			return;
		}
		final int itemAmount = widgetItem.getQuantity();
		if (shopUtilitiesPlugin.currentShopStock.containsKey(itemId))
		{
			int oldAmount = shopUtilitiesPlugin.currentShopStock.get(itemId);
			if (oldAmount != itemAmount)
			{
				QuantityDrop quantityDrop = QuantityDrop.getInstance(itemId);
				if (quantityDrop != null)
				{
					quantityDrop.setChangedAmount(quantityDrop.getChangedAmount() + (itemAmount - oldAmount));
					quantityDrop.setInstant(Instant.now());
				}
				else
				{
					QuantityDrop.createQuantityDrop(itemId, itemAmount - oldAmount, Instant.now());
				}
			}
		}
		shopUtilitiesPlugin.currentShopStock.put(itemId, itemAmount);
		graphics.setFont(FontManager.getRunescapeSmallFont());
		handlePrice(graphics, widgetItem, shop);
		handleQuantityDrop(graphics, widgetItem);
	}

	private void renderPrice(Graphics2D graphics, Rectangle bounds, String text, int delta)
	{
		final TextComponent textComponent = new TextComponent();
		textComponent.setPosition(new Point(bounds.x + shopUtilitiesPlugin.config.shopPriceX(), bounds.y + bounds.height + shopUtilitiesPlugin.config.shopPriceY()));
		textComponent.setColor(delta > 0 ? Color.BLUE : delta < 0 ? Color.RED : JagexColors.YELLOW_INTERFACE_TEXT);
		textComponent.setText(text);
		textComponent.render(graphics);
	}

	private void renderAmount(Graphics2D graphics, Point point, int amount)
	{
		final TextComponent textComponent = new TextComponent();
		textComponent.setPosition(point);
		textComponent.setColor(JagexColors.YELLOW_INTERFACE_TEXT);
		textComponent.setText((amount > 0 ? "+" : "") + amount);
		textComponent.render(graphics);
	}

	private void handleQuantityDrop(Graphics2D graphics, WidgetItem widgetItem)
	{
		if (!shopUtilitiesPlugin.config.quantityChange())
		{
			return;
		}
		QuantityDrop quantityDrop = QuantityDrop.getInstance(widgetItem.getId());
		if (quantityDrop == null)
		{
			return;
		}
		int y = quantityDrop.getY();
		renderAmount(graphics, new Point((int) widgetItem.getCanvasBounds().getX() + 25, (int) widgetItem.getCanvasBounds().getY() + (int) widgetItem.getCanvasBounds().getHeight() - y), quantityDrop.getChangedAmount());
		if (y < 20)
		{
			quantityDrop.setY(y+1);
		}
	}

	private void handlePrice(Graphics2D graphics, WidgetItem widgetItem, Shop shop)
	{
		if (shopUtilitiesPlugin.config.priceDisplayMode().equals(PriceDisplayMode.DISABLED))
		{
			return;
		}
		ShopItemInfo shopItems = shop.getShopItems().get(widgetItem.getId());
		int currentShopPrice;
		if (shop instanceof GeneralStore)
		{
			int stock = shopItems == null ? 0 : shopItems.getStock();
			int deltaStock = shopUtilitiesPlugin.currentShopStock.get(widgetItem.getId()) - stock;
			currentShopPrice = shopUtilitiesPlugin.getGeneralStoreBuyPrice(BigDecimal.valueOf(shop.getShopRates().getSell()), BigDecimal.valueOf(shop.getShopRates().getChange()), BigDecimal.valueOf(itemManager.getItemComposition(widgetItem.getId()).getPrice()), deltaStock);
		}
		else
		{
			int deltaStock = shopItems.getStock() - shopUtilitiesPlugin.currentShopStock.get(widgetItem.getId());
			currentShopPrice = shopUtilitiesPlugin.getBuyPrice(BigDecimal.valueOf(shop.getShopRates().getSell()), BigDecimal.valueOf(shop.getShopRates().getChange()), BigDecimal.valueOf(shopItems.getSell()), deltaStock);
		}
		int grandExchangePrice = itemManager.getItemPrice(widgetItem.getId());
		int deltaPrice = grandExchangePrice - currentShopPrice;
		renderPrice(graphics, widgetItem.getCanvasBounds(), shopUtilitiesPlugin.config.priceDisplayMode().equals(PriceDisplayMode.PRICE) ? "" + currentShopPrice : "" + deltaPrice, deltaPrice);
	}

}