package org.ef3d0c3e.hunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;

public class CmdCompass
{
	private static Inventory m_craft;
	private static String m_name = "    §9Craft du Traqueur";
	static
	{
		m_craft = Bukkit.createInventory(null, InventoryType.WORKBENCH, m_name);
		m_craft.setItem(1, new ItemStack(Material.ROTTEN_FLESH));
		m_craft.setItem(2, new ItemStack(Material.BONE));
		m_craft.setItem(4, new ItemStack(Material.GUNPOWDER));
		m_craft.setItem(5, new ItemStack(Material.SPIDER_EYE));
		m_craft.setItem(0, HuntItems.getTracker());
	}

	/**
	 * Event Handler class that prevents players from stealing from GUI
	 */
	public static class CmdCompassEvents implements Listener
	{
		@EventHandler
		public void onInventoryClick(InventoryClickEvent ev)
		{
			if (ev.getView().getTitle().equals(m_name))
				ev.setCancelled(true);
		}

		@EventHandler
		public void onInventoryDrag(InventoryDragEvent ev)
		{
			if (ev.getView().getTitle().equals(m_name))
				ev.setCancelled(true);
		}
	}

	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}

		final Player p = ((Player)sender);
		p.openInventory(m_craft);

		return true;
	}
}
