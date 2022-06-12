package org.ef3d0c3e.hunt.island;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.util.Arrays;

public class Island
{
	static private ItemStack m_grapple;
	public static ItemStack getGrapple() { return m_grapple; }

	static private ItemStack m_hookModel;
	public static ItemStack getHookModel() { return m_hookModel; }

	public static void start()
	{
		m_grapple = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = m_grapple.getItemMeta();
			meta.setCustomModelData(4);
			meta.setDisplayName("§6Grappin");
			meta.setLore(Arrays.asList(
				"aa"
			));
			m_grapple.setItemMeta(meta);
		}

		m_hookModel = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = m_hookModel.getItemMeta();
			meta.setCustomModelData(5);
			m_hookModel.setItemMeta(meta);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new IslandEvents(), Hunt.plugin);
	}

	public static void onStart(HuntPlayer hp)
	{
		hp.setIslandData(new IslandData());
		PlayerInteractions.giveItem(hp, new ItemStack[] { m_grapple }, true, false);
	}

	public static void onDeath(HuntPlayer hp)
	{
		hp.getIslandData().reset();
	}

	public static boolean itemFilter(final ItemStack item)
	{
		return !Util.isSimilarBasic(item, getGrapple());
	}
}
