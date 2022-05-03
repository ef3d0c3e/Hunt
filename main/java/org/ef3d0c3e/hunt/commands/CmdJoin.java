package org.ef3d0c3e.hunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.teams.TeamMenu;

public class CmdJoin
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
			Messager.ErrorMessage(sender, "La partie a déjà commencée.");
			return true;
		}
		if (!Game.isTeamMode())
		{
			Messager.ErrorMessage(sender, "Les équipes ne sont pas activés!");
			return true;
		}
		
		HuntPlayer hp = Game.getPlayer(sender.getName());
		((Player)sender).openInventory(TeamMenu.getInventory());
		
		return true;
	}
}
