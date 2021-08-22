package com.shoputilities.overlays;

import com.shoputilities.ShopUtilitiesPlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.SplitComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class ProfitOverlay extends OverlayPanel
{

	@Inject
	private ItemManager itemManager;

	@Inject
	private ShopUtilitiesPlugin shopUtilitiesPlugin;

	ProfitOverlay()
	{
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!shopUtilitiesPlugin.config.profitCanvas())
		{
			return null;
		}
		graphics.setFont(FontManager.getRunescapeSmallFont());
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Shop Utilities")
			.color(JagexColors.YELLOW_INTERFACE_TEXT)
			.build());
		int profit = 0;
		final LineComponent emptyLine = LineComponent.builder()
			.left("")
			.build();
		for (Map.Entry<Integer, Integer> entry : shopUtilitiesPlugin.getItemDifference().entrySet())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left(itemManager.getItemComposition(entry.getKey()).getName())
				.right(Integer.toString(entry.getValue()))
				.rightColor(entry.getValue() > 0 ? Color.GREEN : Color.RED)
				.build());
			profit += itemManager.getItemPrice(entry.getKey()) * entry.getValue();
		}
		final LineComponent profitComponent = LineComponent.builder()
			.left("Profit:")
			.right(profit + " gp")
			.leftColor(JagexColors.YELLOW_INTERFACE_TEXT)
			.rightColor(profit > 0 ? Color.GREEN : Color.RED)
			.build();
		panelComponent.getChildren().add(SplitComponent.builder()
			.first(emptyLine)
			.second(profitComponent)
			.orientation(ComponentOrientation.VERTICAL)
			.build());
		return super.render(graphics);
	}

}
