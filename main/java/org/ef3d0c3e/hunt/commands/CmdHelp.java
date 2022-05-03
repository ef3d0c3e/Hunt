package org.ef3d0c3e.hunt.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CmdHelp
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		sender.sendMessage("§8§l§m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m ");
		sender.sendMessage(" §7╸ §a/info §fAfficher les informations sur la partie");
		sender.sendMessage(" §7╸ §a/changelog §fAfficher le changelog");
		sender.sendMessage(" §7╸ §a/kit §fChoisir un kit");
		sender.sendMessage(" §7╸ §a/kitinfo <Kit> §fAffiche la description d'un kit");
		sender.sendMessage(" §7╸ §a/join §fChoisir une équipe");
		sender.sendMessage(" §7╸ §a/compass §fAffiche le craft du Traqueur");
		sender.sendMessage(" §7╸ §a/inv <Joueur> §f(Spectateur) Affiche l'inventaire d'un joueur");
		sender.sendMessage(" §7╸ §a/cibles §f(Spectateur) Affiche le roulement des cibles");
		sender.sendMessage(" §7╸ §a.<Message> §fCommuniquer avec votre équipe");
		sender.sendMessage("§8§l§m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m §m ");

		return true;
	}
}
