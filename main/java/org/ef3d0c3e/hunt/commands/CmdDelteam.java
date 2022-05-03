package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.teams.TeamMenu;

public class CmdDelteam
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (Game.hasStarted())
		{
			Messager.ErrorMessage(sender, "La partie a déjà commencé.");
			return true;
		}
		if (!Game.isTeamMode())
		{
			Messager.ErrorMessage(sender, "Les équipes ne sont pas activés!");
			return true;
		}
		

		String name = "";
		if (args.length >= 1)
		{
			for (int i = 0; i < args.length; ++i)
			{
				if (i > 0)
					name += " ";
				name += args[i] ;
			}
		}
		else
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier le nom de l'éqipe.");
			return true;
		}
		if (!Game.teamExists(name))
		{
			Messager.ErrorMessage(sender, MessageFormat.format("L''équipe ''{0}'' n'existe pas!", name));
			return true;
		}
		
		Messager.broadcast(MessageFormat.format("&8[&9Hunt&8] &7L''équipe {0}&7 vient d''être supprimée.", Game.getTeam(name).getColoredName()));
		Game.delTeam(name);
		TeamMenu.updateInventory();

		return true;
	}
}