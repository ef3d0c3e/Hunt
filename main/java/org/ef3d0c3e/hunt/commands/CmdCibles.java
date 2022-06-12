package org.ef3d0c3e.hunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.teams.Team;

import java.text.MessageFormat;

public class CmdCibles
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}

		if (!Game.hasStarted())
		{
			Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
			return true;
		}

		final HuntPlayer hp = HuntPlayer.getPlayer((Player)sender);
		if (hp.isAlive())
		{
			Messager.ErrorMessage(sender, "Vous ne pouvez pas faire cette commande tant que vous êtes en vie!");
			return true;
		}

		if (Game.isTeamMode() && hp.getTeam() != null && hp.getTeam().isAlive())
		{
			Messager.ErrorMessage(sender, "Vous ne pouvez pas faire cette commande tant que votre équipe est en vie!");
			return true;
		}

		if (Game.isTeamMode())
		{
			Team.forEach(team -> {
				if (!team.isAlive() || team.getTarget() == null)
					return;

				sender.sendMessage(MessageFormat.format("§c{0} §9→ §e§l{1} §9→ §a{2}", team.getHunter().getName(), team.getName(), team.getTarget().getName()));
			});
		}
		else
		{
			HuntPlayer.forEach(other -> {
				if (!other.isAlive() || other.getTarget() == null)
					return;

				sender.sendMessage(MessageFormat.format("§c{0} §9→ §e§l{1} §9→ §a{2}", other.getHunter().getName(), other.getName(), other.getTarget().getName()));
			});
		}

		return true;
	}
}
