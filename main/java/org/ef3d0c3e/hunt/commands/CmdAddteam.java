package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.teams.Team;
import org.ef3d0c3e.hunt.teams.TeamColor;
import org.ef3d0c3e.hunt.teams.TeamMenu;

public class CmdAddteam
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
		

		String colorName;
		if (args.length >= 1)
			colorName = args[0];
		else
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier la couleur de l'éqipe.");
			return true;
		}
		TeamColor color = null;
		for (TeamColor tc : TeamColor.list)
		{
			if (!tc.getName().equalsIgnoreCase(colorName))
				continue;
			
			color = tc;
			break;
		}
		if (color == null)
		{
			Messager.ErrorMessage(sender, MessageFormat.format("La couleur ''{0}'' n'existe pas.", colorName));
			return true;
		}
		if (Game.teamColorTaken(color))
		{
			Messager.ErrorMessage(sender, MessageFormat.format("Il y a déjà une équipe de couleur ''{0}''.", color.getName()));
			return true;
		}

		String name = "";
		if (args.length >= 2)
		{
			for (int i = 1; i < args.length; ++i)
			{
				if (i > 1)
					name += " ";
				name += args[i] ;
			}
		}
		else
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier le nom de l'éqipe.");
			return true;
		}
		if (Game.teamExists(name))
		{
			Messager.ErrorMessage(sender, MessageFormat.format("L''équipe ''{0}'' existe déjà!", name));
			return true;
		}
		if (name.length() >= 48)
		{
			Messager.ErrorMessage(sender, MessageFormat.format("Le nom de l'équipe ne doit pas dépasser 48 caractères.", name));
			return true;
		}

		Team team = Game.addTeam(color, name);
		Messager.broadcast(MessageFormat.format("&8[&9Hunt&8] &7L''équipe {0}&7 vient d''être crée!", team.getColoredName()));
		TeamMenu.updateInventory();
		
		return true;
	}
}