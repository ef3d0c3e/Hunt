package org.ef3d0c3e.hunt;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Messager
{

	public static String translateHexColorCodes(String startTag, String endTag, String message)
	{
		final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
		Matcher matcher = hexPattern.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find())
		{
			String group = matcher.group(1);
			matcher.appendReplacement(buffer,
					ChatColor.COLOR_CHAR + "x" + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR
							+ group.charAt(1) + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR
							+ group.charAt(3) + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR
							+ group.charAt(5));
		}
		return matcher.appendTail(buffer).toString();
	}

	public static TextComponent parseMessage(String message)
	{
		TextComponent tc = new TextComponent();
		// Parse <run:...>
		String text = "";
		String command = "";
		String tooltip = "";
		int cmdMode = 0; // 0 -> Nothing, 1 -> Message cliquable 2 -> La commande
		int hoverMode = 0;
		for (int i = 0; i < message.length(); ++i)
		{
			char c = message.charAt(i);

			// "[r" + "]LOREM...[/r]"
			if (i < message.length() - 3 && message.substring(i, i + 2).equals("[r"))
			{
				tc.addExtra(new TextComponent(text));
				text = "";
				cmdMode = 2;
				i += 1;
			}
			else if (cmdMode == 2 && c == ']')
			{
				cmdMode = 1;
			}
			else if (i < message.length() - 5 && message.substring(i, i + 4).equals("[/r]"))
			{
				cmdMode = 0;
				TextComponent run = new TextComponent(text);
				run.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
				tc.addExtra(run);
				text = command = "";
				i += 3;
			}
			// "[h" + "]LOREM...[/h]"
			else if (i < message.length() - 3 && message.substring(i, i + 2).equals("[h"))
			{
				tc.addExtra(new TextComponent(text));
				text = "";
				hoverMode = 2;
				i += 1;
			}
			else if (cmdMode == 2 && c == ']')
			{
				hoverMode = 1;
			}
			else if (i < message.length() - 5 && message.substring(i, i + 4).equals("[/h]"))
			{
				hoverMode = 0;
				TextComponent hover = new TextComponent(text);
				hover.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip)));
				tc.addExtra(hover);
				text = tooltip = "";
				i += 3;
			}
			else
			{
				// TODO: MU TEX
				if (cmdMode == 2)
					command += c;
				else if (hoverMode == 2)
					tooltip += c;
				else
					text += c;
			}
			
			if (i == message.length()-1)
			{
				TextComponent msg = new TextComponent(text);
				tc.addExtra(msg);
			}

		}
		/*
		 * TextComponent tc = new
		 * TextComponent(ChatColor.translateAlternateColorCodes('&',
		 * "\n&e&lThe sumo event is starting in &n%time%&e&l seconds!\n&7&oType &n/sumo join&7&o to join the event!\n"
		 * .replaceAll("%time%", String.valueOf(6 * 10)))); tc.setClickEvent(new
		 * ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sumo join"));
		 * tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new
		 * ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
		 * "&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*\n&eClick to &6join &6the sumo event!\n&eWin a &6reward&e by playing!\n&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*&6&l*&e&l*"
		 * )))
		 * 
		 * .create()));
		 */

		return tc;
	}

	public static String getColored(String message)
	{
		return ChatColor.translateAlternateColorCodes('&', translateHexColorCodes("<#", ">", message));
	}

	public static void PrintColored(CommandSender sender, String message)
	{
		sender.sendMessage(getColored(message));
	}

	public static void HuntMessage(CommandSender sender, String fmt, String... args)
	{
		for (int i = 0; i < args.length; ++i)
			args[i] += "&r&7";
		String formatted = new MessageFormat(fmt).format(args);

		PrintColored(sender, "&8[&9Hunt&8] &7" + formatted);
	}

	public static void HuntMessage(CommandSender sender, String message)
	{
		PrintColored(sender, "&8[&9Hunt&8] &7" + message);
	}

	public static void HuntBroadcast(String fmt, String... args)
	{
		for (int i = 0; i < args.length; ++i)
			args[i] += "&r&7";
		String formatted = new MessageFormat(fmt).format(args);

		String message = getColored("&8[&9Hunt&8] &7" + formatted);
		Bukkit.getServer().broadcastMessage(message);
	}

	public static void HuntBroadcast(String message)
	{
		Bukkit.getServer().broadcastMessage(getColored("&8[&9Hunt&8] &7" + message));
	}
	
	public static void broadcast(String msg)
	{
		Bukkit.getServer().broadcastMessage(getColored(msg));
	}

	public static void broadcastColor(String msg)
	{
		Bukkit.getServer().broadcastMessage(getColored(msg));
	}

	public static void ErrorMessage(CommandSender sender, String fmt, String... args)
	{
		for (int i = 0; i < args.length; ++i)
			args[i] += "&r&7";
		String formatted = new MessageFormat(fmt).format(args);

		PrintColored(sender, "&cErreur&8> &7" + formatted);
	}

	public static void ErrorMessage(CommandSender sender, String message)
	{
		PrintColored(sender, "&cErreur&8> &7" + message);
	}

	public static void send(CommandSender sender, String message)
	{
		PrintColored(sender, message);
	}

	public static void send(CommandSender sender, String fmt, String... args)
	{
		for (int i = 0; i < args.length; ++i)
			args[i] += "&r&7";
		String formatted = getColored(new MessageFormat(fmt).format(args));

		sender.spigot().sendMessage(parseMessage(formatted));
	}
}

