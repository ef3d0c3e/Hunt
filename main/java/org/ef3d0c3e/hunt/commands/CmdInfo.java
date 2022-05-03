package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.Round;
import org.ef3d0c3e.hunt.game.Game;

public class CmdInfo
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		String text = "&8╒════════╡ &9&lInfo&8 ╞════════╕\n";
		if (Game.isKitMode() || Game.isTeamMode() || Game.isIslandMode() || Game.isRoundMode())
		{
			boolean first = true;
			text += " &7&l╸&r &7Mode: ";
			String names[] = {"Kit", "Team", "Île", "Rounds", "Fast"};
			boolean values[] = {Game.isKitMode(), Game.isTeamMode(), Game.isIslandMode(), Game.isRoundMode(), Game.isFastMode()};
			for (int i = 0; i < names.length; ++i)
			{
				if (!values[i])
					continue;
				if (!first)
					text += " | ";
				
				first = false;
				text += MessageFormat.format("&a{0}&r", names[i]);
			}
			
			text += "\n";
		}
		if (Game.isRoundMode())
			text += MessageFormat.format(" &7&l╸&r &7Rounds: §e{0}×{1}min&r + &e1×{2}min\n", Round.getRounds()-1, Round.getRoundTime(), Round.getFinalRoundTime());
		text += MessageFormat.format(" &7&l╸&r &7Border: &e{0}m&r → &e{1}m\n", Game.getMaxBorder(), Game.getMinBorder());
		text += MessageFormat.format(" &7&l╸&r &7Temps: &e{0}min&r / &e{1}min\n", Game.getGameTime(), Game.getHuntTime());
		text += "&8╘═════════════════════╛";
		Messager.PrintColored(sender, text);
		
		return true;
	}
}
