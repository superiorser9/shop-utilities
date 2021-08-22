package com.shoputilities;

import com.google.common.collect.ImmutableMap;

public interface Shop
{

	ImmutableMap<Integer, ShopItemInfo> getShopItems();
	ShopRates getShopRates();

}