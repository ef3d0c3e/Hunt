package org.ef3d0c3e.hunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.KitMenu;

public class CmdKit
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}	
		if (Game.hasStarted())
		{
			Messager.ErrorMessage(sender, "La partie a déjà commencée!");
			return true;
		}
		if (!Game.isKitMode())
		{
			Messager.ErrorMessage(sender, "Les kits ne sont pas activés");
			return true;
		}
		
		((Player)sender).openInventory(KitMenu.getInventory());
		
		return true;
	}
}
