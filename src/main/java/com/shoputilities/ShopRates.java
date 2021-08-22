package com.shoputilities;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopRates
{

	@Getter
	private final double sell;

	@Getter
	private final double buy;

	@Getter
	private final double change;

}
