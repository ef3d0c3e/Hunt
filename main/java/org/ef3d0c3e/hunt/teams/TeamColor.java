package org.ef3d0c3e.hunt.teams;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.items.HuntItems;

public class TeamColor
{
	public String name;
	public String color; // Color code
	public Material concrete; // Colored concrete block
	public Material banner; // Colored banner item
	
	public ItemStack getGuiItem()
	{
		return HuntItems.createGuiItem(concrete, 0, Messager.getColored(color + name));
	}
	
	public String getName()
	{
		return name;
	}

	public String getColoredName()
	{
		return color + name;
	}

	public String getColor()
	{
		return color;
	}

	private TeamColor(String name, String color, Material concrete, Material banner)
	{
		this.name = name;
		this.color = color;
		this.concrete = concrete;
		this.banner = banner;
	}
	
	public static TeamColor list[] =
	{
		new TeamColor("Orange", "<#D09000>", Material.ORANGE_CONCRETE, Material.ORANGE_BANNER),
		new TeamColor("Magenta", "<#D050A0>", Material.MAGENTA_CONCRETE, Material.MAGENTA_BANNER),
		new TeamColor("BleuClair", "<#40B0F0>", Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_BANNER),
		new TeamColor("Jaune", "<#D0F000>", Material.YELLOW_CONCRETE, Material.YELLOW_BANNER),
		new TeamColor("VertClair", "<#20D050>", Material.LIME_CONCRETE, Material.LIME_BANNER),
		new TeamColor("Rose", "<#F090D0>", Material.PINK_CONCRETE, Material.PINK_BANNER),
		new TeamColor("Gris", "<#404040>", Material.GRAY_CONCRETE, Material.GRAY_BANNER),
		new TeamColor("GrisClair", "<#909090>", Material.LIGHT_GRAY_CONCRETE, Material.LIGHT_GRAY_BANNER),
		new TeamColor("Cyan", "<#20D0D0>", Material.CYAN_CONCRETE, Material.CYAN_BANNER),
		new TeamColor("Violet", "<#FF30B0>", Material.PURPLE_CONCRETE, Material.PURPLE_BANNER),
		new TeamColor("Bleu", "<#1050ff>", Material.BLUE_CONCRETE, Material.BLUE_BANNER),
		new TeamColor("Marron", "<#C06A00>", Material.BROWN_CONCRETE, Material.BROWN_BANNER),
		new TeamColor("Vert", "<#00FF20>", Material.GREEN_CONCRETE, Material.GREEN_BANNER),
		new TeamColor("Rouge", "<#F04040>", Material.RED_CONCRETE, Material.RED_BANNER),
	};
}
