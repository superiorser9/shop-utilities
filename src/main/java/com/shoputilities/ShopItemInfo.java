package com.shoputilities;

import lombok.Data;

@Data
public class ShopItemInfo
{

	private final int stock;
	private final int restock;
	private final int sell;
	private final int buy;

}
