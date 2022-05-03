package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;

import net.md_5.bungee.api.chat.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;

import net.md_5.bungee.api.chat.hover.content.Text;

// FIXME: At page 2, numbers still maintains click and hover events from left arrow
public class CmdKitinfo
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		String option;
		if (args.length != 0)
			option = args[0];
		else
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier le nom d'un kit.");
			return true;
		}
		
		int page;
		if (args.length >= 2)
		{
			try
			{
				page = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e)
			{
				Messager.ErrorMessage(sender, "Veuillez saisir un nombre pour la page!");
				return true;
			}
		}
		else
			page = 1;
		
		boolean found = false;
		for (Kit k : KitMenu.getList())
		{
			if (!k.getName().equalsIgnoreCase(option))
				continue;
			
			if (page - 1 >= k.getDescription().length || page - 1 < 0)
			{
				Messager.ErrorMessage(sender, MessageFormat.format("La page ''{0}'' n''existe pas.", page));
				return true;
			}
			
			for (int i = 0; i < k.getDescription()[page - 1].length; ++i)
				sender.spigot().sendMessage(parseEntry(k.getDescription()[page - 1][i]));

			ComponentBuilder navigator = new ComponentBuilder();
			if (page-1 != 0)
			{
				TextComponent arrowLeft = new TextComponent(TextComponent.fromLegacyText("§l §l §c§l << "));
				arrowLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageFormat.format("/kitinfo {0} {1}", option, page-1)));
				arrowLeft.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new Text(MessageFormat.format("§7Page {0}", page-1))
				));
				navigator.append(arrowLeft);
			}
			else
			{
				TextComponent arrowLeft = new TextComponent(TextComponent.fromLegacyText("§l §l §7§l << "));
				navigator.append(arrowLeft);
			}
			TextComponent numbers = new TextComponent(TextComponent.fromLegacyText(MessageFormat.format(
				"§6§l(§e{0}/{1}§6§l)", page , k.getDescription().length
			)));
			navigator.append(numbers);
			if (page < k.getDescription().length)
			{
				TextComponent arrowRight = new TextComponent(TextComponent.fromLegacyText("§c§l >> "));
				arrowRight.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageFormat.format("/kitinfo {0} {1}", option, page + 1)));
				arrowRight.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new Text(MessageFormat.format("§7Page {0}", page + 1))
				));
				navigator.append(arrowRight);
			}
			else
			{
				TextComponent arrowRight = new TextComponent(TextComponent.fromLegacyText("§7§l >> "));
				navigator.append(arrowRight);
			}
			sender.spigot().sendMessage(navigator.create());
			
			found = true;
			break;
		}
		
		if (!found)
			Messager.ErrorMessage(sender, MessageFormat.format("Le kit ''{0}'' n''existe pas.", option));
		
		return true;
	}

	private static BaseComponent[] parseEntry(String entry)
	{
		ComponentBuilder builder = new ComponentBuilder();

		char prev = '\0';
		for (int i = 0; i < entry.length(); ++i)
		{
			if (entry.charAt(i) == ':' && prev == ':')
			{
				builder.append(new TextComponent(TextComponent.fromLegacyText(entry.substring(0, i-1))));

				TextComponent desc = new TextComponent(TextComponent.fromLegacyText(" §8[§e§oDétails§8]"));
				desc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(entry.substring(i+1))));
				builder.append(desc);

				break;
			}
			else if (i == entry.length()-1)
			{
				builder.append(new TextComponent(TextComponent.fromLegacyText(entry.substring(0, entry.length()))));

				break;
			}

			prev = entry.charAt(i);
		}

		return builder.create();
	}
}
