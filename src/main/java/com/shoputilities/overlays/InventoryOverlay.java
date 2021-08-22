package com.shoputilities.overlays;

import com.shoputilities.PriceDisplayMode;
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
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import static net.runelite.api.widgets.WidgetID.SHOP_INVENTORY_GROUP_ID;
import net.runelite.client.ui.overlay.components.TextComponent;

@Slf4j
public class InventoryOverlay extends WidgetItemOverlay
{

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ShopUtilitiesPlugin shopUtilitiesPlugin;

	@Inject
	InventoryOverlay()
	{
		showOnInterfaces(SHOP_INVENTORY_GROUP_ID);
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
			return;
		}
		if (itemId < 0 || itemId == ItemID.COINS_995)
		{
			return;
		}
		if ((generalStore == null && !shop.getShopItems().containsKey(itemManager.canonicalize(itemId))) || (generalStore != null && GeneralStore.cannotSellEverything(shopName)))
		{
			return;
		}
		graphics.setFont(FontManager.getRunescapeSmallFont());
		handlePrice(graphics, widgetItem, shop);
	}

	private void renderPrice(Graphics2D graphics, Rectangle bounds, String text, int delta)
	{
		final TextComponent textComponent = new TextComponent();
		textComponent.setPosition(new Point(bounds.x + shopUtilitiesPlugin.config.inventoryPriceX(), bounds.y + bounds.height + shopUtilitiesPlugin.config.inventoryPriceY()));
		textComponent.setColor(delta > 0 ? Color.BLUE : delta < 0 ? Color.RED : JagexColors.YELLOW_INTERFACE_TEXT);
		textComponent.setText(text);
		textComponent.render(graphics);
	}

	private void handlePrice(Graphics2D graphics, WidgetItem widgetItem, Shop shop)
	{
		if (shopUtilitiesPlugin.config.priceDisplayMode().equals(PriceDisplayMode.DISABLED))
		{
			return;
		}
		ShopItemInfo shopItems = shop.getShopItems().get(itemManager.canonicalize(widgetItem.getId()));
		int currentShopPrice;
		if (shop instanceof GeneralStore)
		{
			int stock = shopItems == null ? 0 : shopItems.getStock();
			int currentStock = shopUtilitiesPlugin.currentShopStock.getOrDefault(itemManager.canonicalize(widgetItem.getId()), 0);
			int deltaStock = stock - currentStock;
			currentShopPrice = shopUtilitiesPlugin.getGeneralStoreSellPrice(BigDecimal.valueOf(shop.getShopRates().getBuy()), BigDecimal.valueOf(shop.getShopRates().getChange()), BigDecimal.valueOf(itemManager.getItemComposition(widgetItem.getId()).getPrice()), deltaStock);
		}
		else
		{
			int deltaStock = shopItems.getStock() - shopUtilitiesPlugin.currentShopStock.get(itemManager.canonicalize(widgetItem.getId()));
			currentShopPrice = shopUtilitiesPlugin.getSellPrice(BigDecimal.valueOf(shop.getShopRates().getBuy()), BigDecimal.valueOf(shop.getShopRates().getChange()), BigDecimal.valueOf(shopItems.getSell()), BigDecimal.valueOf(shopItems.getBuy()), deltaStock);
		}
		int grandExchangePrice = itemManager.getItemPrice(widgetItem.getId());
		int deltaPrice = currentShopPrice - grandExchangePrice;
		renderPrice(graphics, widgetItem.getCanvasBounds(), shopUtilitiesPlugin.config.priceDisplayMode().equals(PriceDisplayMode.PRICE) ? "" + currentShopPrice : "" + deltaPrice, deltaPrice);
	}

}