package com.shoputilities;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class QuantityDrop
{

	@Getter
	private final int itemID;

	@Getter
	@Setter
	private int changedAmount;

	@Getter
	@Setter
	private Instant instant;

	@Getter
	@Setter
	private int y;

	public static List<WeakReference<QuantityDrop>> instances = new ArrayList<>();

	QuantityDrop(int itemID, int changedAmount, Instant instant)
	{
		this.itemID = itemID;
		this.changedAmount = changedAmount;
		this.instant = instant;
		this.y = 0;
	}

	public static void createQuantityDrop(int itemID, int changedAmount, Instant instant)
	{
		QuantityDrop quantityDrop = new QuantityDrop(itemID, changedAmount, instant);
		instances.add(new WeakReference<>(quantityDrop));
	}

	public static void removeQuantityDrop(WeakReference<QuantityDrop> amountDropInstance)
	{
		instances.remove(amountDropInstance);
		amountDropInstance.clear();
	}

	public static void removeAll()
	{
		for (Iterator<WeakReference<QuantityDrop>> it = instances.iterator(); it.hasNext();)
		{
			WeakReference<QuantityDrop> instance = it.next();
			QuantityDrop quantityDrop = instance.get();
			if (quantityDrop == null)
			{
				continue;
			}
			it.remove();
			removeQuantityDrop(instance);
		}
	}

	public static QuantityDrop getInstance(int itemID)
	{
		for (WeakReference<QuantityDrop> instance : instances)
		{
			QuantityDrop quantityDrop = instance.get();
			if (quantityDrop == null)
			{
				continue;
			}
			if (quantityDrop.getItemID() == itemID)
			{
				return quantityDrop;
			}
		}
		return null;
	}

}
