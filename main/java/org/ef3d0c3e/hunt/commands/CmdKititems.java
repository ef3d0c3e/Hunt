package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;

public class CmdKititems
{
	static Vector<Inventory> m_pages = null;

	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}	
		if (!Game.hasStarted())
		{
			Messager.ErrorMessage(sender, "La partie n'a pas encore commencée.");
			return true;
		}
		if (!Game.isKitMode())
		{
			Messager.ErrorMessage(sender, "Les kits ne sont pas activés");
			return true;
		}
		
		int page;
		if (args.length == 0)
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier une page!");
			return true;
		}

		try
		{
			page = Integer.valueOf(args[0]) - 1;
		}
		catch (NumberFormatException e)
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier un nombre.");
			return true;
		}
		
		if (m_pages == null)
		{
			m_pages = new Vector<Inventory>();
			m_pages.add(Bukkit.createInventory(null, 54));
			int j = 0;
			int i = 0;
			
			for (Kit kit : KitMenu.getList())
			{
				for (ItemStack item : kit.getItems())
				{
					m_pages.get(j).setItem(i++, item);
					
					if (i == 54)
					{
						++j;
						i = 0;
						m_pages.add(Bukkit.createInventory(null, 54));
					}
				}
			}
		}

		if (page < 0 || page >= m_pages.size())
		{
			Messager.ErrorMessage(sender, MessageFormat.format("La page ''{0}'' n'existe pas.", page + 1));
			return true;
		}
		
		Inventory inv = Bukkit.createInventory(null, m_pages.get(page).getSize(), MessageFormat.format("§cPage {0}", page + 1));
		inv.setContents(m_pages.get(page).getContents());
		((Player)sender).openInventory(inv);
		
		return true;
	}
}
