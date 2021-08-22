package com.shoputilities;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class Utils
{

	public static void fetch(String... shopNames)
	{
		for (String shopName : shopNames)
		{
			fetchWiki(shopName);
		}
	}

	private static void fetchWiki(String shop)
	{
		HttpUrl httpUrl = HttpUrl.parse("https://oldschool.runescape.wiki/w/" + shop);
		Request request = new Request.Builder()
			.url(httpUrl)
			.header("User-Agent", "RuneLite")
			.build();

		try (Response responseOk = RuneLiteAPI.CLIENT.newCall(request).execute())
		{
			if (!responseOk.isSuccessful())
			{
				throw new IOException("Error retrieving prices: " + responseOk.message());
			}
			StringBuilder stringBuilder = new StringBuilder();
			String body = responseOk.body().string();
			String shopName = body.substring(body.indexOf("<title>") + "<title>".length(), body.indexOf(" - OSRS"));
			String enumName = shopName.replaceAll("!", "").replaceAll("'", "_").replaceAll("\\.", "").replaceAll(" ", "_").replaceAll("\\(", "").replaceAll("\\)", "").toUpperCase();
			stringBuilder.append(enumName);
			stringBuilder.append("(\"");
			stringBuilder.append(shopName);
			stringBuilder.append("\", ");
			String string = body.split("<table class=\"wikitable sortable\"")[1];
			String itemTable = string.substring(string.indexOf("style="), string.indexOf("</table>"));
			String changeRateText = itemTable.substring(itemTable.indexOf("Change per: ") + "Change per: ".length(), itemTable.indexOf("</caption>")).replace("%", "");
			double changeRate = Double.parseDouble(changeRateText) / 100;
			String sellText = itemTable.substring(itemTable.indexOf("Sells at: ") + "Sells at: ".length(), itemTable.indexOf("%"));
			double sell = Double.parseDouble(sellText) / 100;
			String buyText = itemTable.substring(itemTable.indexOf("Buys at: ") + "Buys at: ".length(), itemTable.indexOf("%", itemTable.indexOf("Buys at: ")));
			double buy = Double.parseDouble(buyText) / 100;
			stringBuilder.append("new ShopRates(");
			stringBuilder.append(sell);
			stringBuilder.append(", ");
			stringBuilder.append(buy);
			stringBuilder.append(", ");
			stringBuilder.append(changeRate);
			stringBuilder.append(")");
			stringBuilder.append(", ImmutableMap.<Integer, ShopItemInfo>builder()\n");
			String[] items = itemTable.split("<tr style=\"text-align:center\">");
			for (String item : items)
			{
				String s = item.split("title=")[1];
				String itemName = s.substring(s.indexOf("\""), s.indexOf(">")).replaceAll("\"", "").toUpperCase().replaceAll(" ", "_").replaceAll("&#39;", "").replaceAll("-", "");
				if (!itemName.equalsIgnoreCase("GRAND_EXCHANGE"))
				{
					String b = item.split("title=")[2].split("</a></td>")[1];
					String defaultStock = b.substring(b.indexOf("<td>"), b.indexOf("</td>")).replaceAll("<td>", "");
					String restockText = b.split("</td>")[1].split(">")[1].trim().replaceAll(" ", "+").replaceAll("s", "x1000").replaceAll("m", "x60000").replaceAll("h", "x3600000");
					int restock = (int) (Double.parseDouble(restockText.split("x")[0]) * Integer.parseInt(restockText.split("x")[1].split("\\+")[0]));
					if (restockText.split("x").length > 2)
					{
						int second = (int) (Double.parseDouble(restockText.split("x")[1].split("\\+")[1]) * Double.parseDouble(restockText.split("x")[2]));
						restock += second;
					}
					String soldText = item.split("title=\"Coins\"")[1];
					String sold = soldText.substring(soldText.indexOf("</a>"), soldText.indexOf("</td>")).replaceAll("</a>", "").trim().replaceAll(",", "");
					String bought;
					if (item.split("title=\"Coins\"").length > 2)
					{
						String boughtText = item.split("title=\"Coins\"")[2];
						bought = boughtText.substring(boughtText.indexOf("</a>"), boughtText.indexOf("</td>")).replaceAll("</a>", "").trim().replaceAll(",", "");
					}
					else {
						bought = "0";
					}
					stringBuilder.append(".put(ItemID.").append(itemName).append(", new ShopItemInfo(").append(defaultStock).append(", ").append(restock).append(", ").append(sold).append(", ").append(bought).append("))\n");
				}
			}
			stringBuilder.append(".build()\n");
			stringBuilder.append("),");
			System.out.println(stringBuilder.toString());
		}
		catch (IOException e)
		{
			log.warn("Error ", e);
		}
	}

	public static void wiki(String shop)
	{
		HttpUrl httpUrl = HttpUrl.parse("https://oldschool.runescape.wiki/w/" + shop);
		Request request = new Request.Builder()
			.url(httpUrl)
			.header("User-Agent", "RuneLite")
			.build();

		try (Response responseOk = RuneLiteAPI.CLIENT.newCall(request).execute())
		{
			if (!responseOk.isSuccessful())
			{
				throw new IOException("Error retrieving prices: " + responseOk.message());
			}
			String body = responseOk.body().string();
			String[] splits = body.split("<li><a href=");
			StringBuilder stringBuilder = new StringBuilder();
			for (String s : splits)
			{
				if (s.contains("Most towns have at least one.") || s.contains("vectorMenu emptyPortlet"))
				{
					continue;
				}
				String a = s.substring(s.indexOf("\"/w/") + "\"/w/".length(), s.indexOf(" title="));
				stringBuilder.append("\"");
				stringBuilder.append(a);
				stringBuilder.append(",\n");
			}
			System.out.println(stringBuilder);
		}
		catch (IOException e)
		{
			log.warn("Error ", e);
		}
	}

}