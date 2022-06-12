package org.ef3d0c3e.hunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.IGui;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.Items;

public class CmdCompass
{
	public static class Gui implements IGui
	{

		@Override
		public void onGuiClick(Player p, ClickType click, int slot, ItemStack item) {}
		@Override
		public void onGuiClose(Player p) {}
		@Override
		public void onGuiDrag(Player p, InventoryDragEvent ev) {}

		@Override
		public Inventory getInventory()
		{
			final Inventory inv = Bukkit.createInventory(this, InventoryType.WORKBENCH, "    §9Craft du Traqueur");
			inv.setItem(1, new ItemStack(Material.ROTTEN_FLESH));
			inv.setItem(2, new ItemStack(Material.BONE));
			inv.setItem(4, new ItemStack(Material.GUNPOWDER));
			inv.setItem(5, new ItemStack(Material.SPIDER_EYE));
			inv.setItem(0, Items.getTracker());
			return inv;
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
		p.openInventory(new Gui().getInventory());

		return true;
	}
}
