package org.ef3d0c3e.hunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class Commands implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("hunt"))
			return CmdHunt.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("help"))
			return CmdHelp.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("info"))
			return CmdInfo.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("changelog"))
			return CmdChangelog.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("kit"))
			return CmdKit.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("kititems"))
			return CmdKititems.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("kitinfo"))
			return CmdKitinfo.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("addteam"))
			return CmdAddteam.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("delteam"))
			return CmdDelteam.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("join"))
			return CmdJoin.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("compass"))
			return CmdCompass.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("inv"))
			return CmdInv.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("cibles"))
			return CmdCibles.command(sender, cmd, label, args);
		else if (cmd.getName().equalsIgnoreCase("accessoires"))
			return CmdAccessoire.command(sender, cmd, label, args);

		return true;
	}
}
