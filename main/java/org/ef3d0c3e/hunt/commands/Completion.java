package org.ef3d0c3e.hunt.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.teams.Team;
import org.ef3d0c3e.hunt.teams.TeamColor;

public class Completion implements TabCompleter
{
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args)
	{
		ArrayList<String> l = new ArrayList<String>();
		if (cmd.getName().equalsIgnoreCase("hunt"))
		{
			if (args.length == 1)
			{
				l.add("help");
				l.add("border");
				l.add("time");
				l.add("kit");
				l.add("team");
				l.add("island");
				l.add("round");
				l.add("fast");
				l.add("start");
				l.add("revive");
				l.add("kill");
				l.add("list");
				l.add("shuffle");
				l.add("damage");
				l.add("next");
			}
			else if (args.length >= 2 && args[0].equalsIgnoreCase("revive"))
			{
				if (args.length == 2)
					for (final Player p : Bukkit.getOnlinePlayers())
						l.add(p.getName());
			}
			else if (args.length >= 2 && args[0].equalsIgnoreCase("kill"))
			{
				if (args.length == 2)
					for (final Player p : Bukkit.getOnlinePlayers())
						l.add(p.getName());
			}
			else if (args.length >= 2 && args[0].equalsIgnoreCase("damage"))
			{
				if (args.length == 2)
					for (final Player p : Bukkit.getOnlinePlayers())
						l.add(p.getName());
				if (args.length == 4)
					for (final Player p : Bukkit.getOnlinePlayers())
						l.add(p.getName());
			}
		}
		else if (cmd.getName().equalsIgnoreCase("kitinfo") && args.length == 1)
		{
			for (Kit k : KitMenu.getKitList())
				l.add(k.getName());
		}
		else if (cmd.getName().equalsIgnoreCase("addteam") && args.length == 1)
		{
			for (TeamColor tc : TeamColor.list)
				l.add(tc.getName());
		}
		else if (cmd.getName().equalsIgnoreCase("delteam") && args.length == 1)
		{
			Team.forEach(team -> l.add(team.getName()));
		}
		else if (cmd.getName().equalsIgnoreCase("inv") && args.length == 1)
		{
			for (final Player p : Bukkit.getOnlinePlayers())
				l.add(p.getName());
		}

		if (args.length != 0)
		{
			final String arg = args[args.length - 1];
			// Remove non matching entries
			for (int i = 0; i < l.size(); )
			{
				// Entry too short
				if (arg.length() > l.get(i).length())
				{
					l.remove(i);
					continue;
				}

				// Entry does not match
				if (!l.get(i).substring(0, arg.length()).equalsIgnoreCase(arg))
				{
					l.remove(i);
					continue;
				}
				++i;
			}
		}

		return l.isEmpty() ? null : l;
	}
}
