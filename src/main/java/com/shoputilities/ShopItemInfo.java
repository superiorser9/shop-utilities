package com.shoputilities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopItemInfo
{

	@Getter
	private final int stock;

	@Getter
	private final int restock;

	@Getter
	private final int sell;

	@Getter
	private final int buy;

}
