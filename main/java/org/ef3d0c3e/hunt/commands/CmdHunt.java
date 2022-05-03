package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;
import java.util.Vector;
import java.util.HashMap;

import net.minecraft.world.entity.PlayerRideable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.Normal;
import org.ef3d0c3e.hunt.Round;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

public class CmdHunt
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		String option;
		if (args.length != 0)
			option = args[0];
		else
			option = "help";
		
		if (option.equalsIgnoreCase("border"))
		{
			if (args.length != 3)
			{
				Messager.ErrorMessage(sender, "Veuillez spécifier le rayon maximum et minimum de la border!");
				return true;
			}
			
			try
			{
				int max = Integer.valueOf(args[1]);
				int min = Integer.valueOf(args[2]);
				
				if (min > max)
				{
					Messager.ErrorMessage(sender, "Le maximum doit être plus grand que le minimum!");
					return true;
				}
				
				Game.setBorders(max, min);
				Messager.HuntBroadcast(MessageFormat.format("La taille de la border a été changée! ({0}m → {1}m)", max, min));
			}
			catch (NumberFormatException e)
			{
				Messager.ErrorMessage(sender, "Veuillez saisir des valeurs numériques pour les diamètres!");
				return true;
			}
		}
		else if (option.equalsIgnoreCase("time"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}
			if (args.length != 3)
			{
				Messager.ErrorMessage(sender, "Veuillez spécifier la durée de la partie et la durée avant le hunt!");
				return true;
			}
			
			try
			{
				int game = Integer.valueOf(args[1]);
				int hunt = Integer.valueOf(args[2]);
				
				if (hunt > game)
				{
					Messager.ErrorMessage(sender, "La durée totale doit être plus grande que la durée avant le hunt!");
					return true;
				}
				
				Game.setDurations(game, hunt);
				Messager.HuntBroadcast(MessageFormat.format("La durée de la partie a été changée ({0}min / {1}min)", game, hunt));
			}
			catch (NumberFormatException e)
			{
				Messager.ErrorMessage(sender, "Veuillez saisir des valeurs numériques pour les durées!");
				return true;
			}
		}
		else if (option.equalsIgnoreCase("kit"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}
			
			Game.setKit(!Game.isKitMode());
			if (Game.isKitMode())
				Messager.HuntBroadcast("Les kits viennent d'être activé!");
			else
				Messager.HuntBroadcast("Les kits viennent d'être désactivé.");

			for (Player p : Bukkit.getOnlinePlayers())
			{
				HuntPlayer hp = Game.getPlayer(p.getName());
				hp.updateScoreboard();
				hp.updateTabname();
				hp.updateNametag();
			}
		}
		else if (option.equalsIgnoreCase("team"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}
			
			Game.setTeam(!Game.isTeamMode());
			if (Game.isTeamMode())
				Messager.HuntBroadcast("Les équipes viennent d'être activé!");
			else
				Messager.HuntBroadcast("Les équipes viennent d'être désactivé.");

			for (Player p : Bukkit.getOnlinePlayers())
			{
				HuntPlayer hp = Game.getPlayer(p.getName());
				hp.updateScoreboard();
				hp.updateTabname();
				hp.updateNametag();
			}

		}
		else if (option.equalsIgnoreCase("island"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}

			Game.setIsland(!Game.isIslandMode());
			if (Game.isIslandMode())
				Messager.HuntBroadcast("Le mode île vient d'être activé!");
			else
				Messager.HuntBroadcast("Le mode île a été désactivé.");
		}
		else if (option.equalsIgnoreCase("round"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}
			if (args.length != 2)
			{
				if (Game.isRoundMode()) // Turn off
				{
					Game.setRoundMode(false);
					Messager.HuntBroadcast("Le mode round vient d'être désactivé.");
					return true;
				}

				Messager.ErrorMessage(sender, "Veuillez spécifier le nombre de rounds");
				return true;
			}

			try
			{
				int rounds = Integer.valueOf(args[1]);

				if (rounds <= 1)
				{
					Messager.ErrorMessage(sender, "La partie doit avoir au moins deux rounds.");
					return true;
				}

				Game.setRoundMode(true);
				Round.setRounds(rounds);

				Messager.HuntBroadcast(MessageFormat.format("Le mode round vient d''être activé, avec §e{0}§7 rounds!", Round.getRounds()));

				return true;
			}
			catch (NumberFormatException e)
			{
				Messager.ErrorMessage(sender, "Veuillez saisir un nombre pour le nombre de rounds!");
				return true;
			}
		}
		else if (option.equalsIgnoreCase("fast"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}

			Game.setFastMode(!Game.isFastMode());
			if (Game.isFastMode())
				Messager.HuntBroadcast("Le mode fast vient d'être activé!");
			else
				Messager.HuntBroadcast("Le mode fast a été désactivé.");
		}
		else if (option.equalsIgnoreCase("start"))
		{
			if (Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie a déjà commencé!");
				return true;
			}
			
			// During this task, player can still issue commands that may break things
			// We (at least) check if every player has a kit
			new BukkitRunnable()
			{
				int i = 0;
				Vector<String> list;

				@Override
				public void run()
				{
					if (i == 0) // Store list of players
					{
						list = new Vector<String>();
						for (Player p : Bukkit.getOnlinePlayers())
							list.add(p.getName());

						// Check if all players have kits
						if (Game.isKitMode())
						{
							for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
							{
								if (!set.getValue().isOnline() || set.getValue().getKit() != null)
									continue;

								Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' n''a pas de kit.", set.getKey()));
								cancel();
								return;
							}
						}
						// Check if all players have teams
						if (Game.isTeamMode())
						{
							for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
							{
								if (!set.getValue().isOnline() || set.getValue().getTeam() != null)
									continue;

								Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' n''a pas d''équipe.", set.getKey()));
								cancel();
								return;
							}
						}
					}

					if (i == 5)
					{
						this.cancel();
						// NOTE: If a player relogs, the address of its player object may change
						// Check if players are still online
						for (String name : list)
						{
							boolean found = false;
							for (Player p : Bukkit.getOnlinePlayers())
								if (p.getName() == name)
								{
									found = true;
									break;
								}
							if (found)
								continue;

							Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' vient de se déconnecter.", name));
							return;
						}
						// Check if new players are online
						for (Player p : Bukkit.getOnlinePlayers())
						{
							boolean found = false;
							for (String name : list)
								if (p.getName() == name)
								{
									found = true;
									break;
								}
							if (found)
								continue;

							Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' vient de se connecter.", p.getName()));
							return;
						}
						// NOTE: Both of these checks can happen if a player relogs during the timer
						// Check if all players still have kits
						if (Game.isKitMode())
						{
							for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
							{
								if (!set.getValue().isOnline() || set.getValue().getKit() != null)
									continue;

								Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' n''a pas de kit.", set.getKey()));
								return;
							}
						}
						if (Game.isTeamMode())
						{
							// Check if all players still have teams
							for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
							{
								if (!set.getValue().isOnline() || set.getValue().getTeam() != null)
									continue;

								Messager.HuntBroadcast(MessageFormat.format("&cImpossible de lancer la partie: ''{0}'' n''a pas d''équipe.", set.getKey()));
								return;
							}
						}

						for (Player p : Bukkit.getOnlinePlayers())
						{
							// Clear player's inventory
							p.closeInventory();
							if (p.getOpenInventory() != null)
								p.getOpenInventory().getTopInventory().clear();
							p.getInventory().clear();
							p.setItemOnCursor(null);
							p.updateInventory();
							// Messages
							p.playSound(new Location(p.getWorld(), 0.0, 0.0, 0.0), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 65536.f, 1.6f);
							p.sendTitle(
								"§aLa chasse est ouverte!",
								"§6Méfiez-vous des golems...",
								0, 50, 10
							);
						}

						Game.start();

						return;
					}

					for (Player p : Bukkit.getOnlinePlayers())
					{
						p.sendTitle(
							"§aLa partie va commencer!",
							MessageFormat.format("§6{0}...", 5-i),
							0, 30, 0
						);
						p.playSound(new Location(p.getWorld(), 0.0, 0.0, 0.0), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 65536.f, 1.f);
					}
					
					++i;
				}
			}.runTaskTimer(Game.getPlugin(), 0, 20);
			
		}
		else if (option.equalsIgnoreCase("shuffle"))
		{
			if (!Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
				return true;
			}
			Game.clearTargets();
			if (!Game.isRoundMode())
				Normal.updateTargets();
			else
				Round.updateTargets(true);
		}
		else if (option.equalsIgnoreCase("list")) // list
		{
			Bukkit.broadcastMessage("Name | Alive | Playing | Online");
			for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
			{
				HuntPlayer hp = set.getValue();
				Bukkit.broadcastMessage(MessageFormat.format("{0} | {1} | {2} | {3}", hp.getName(), hp.isAlive(), hp.isPlaying(), hp.isOnline()));
			}
		}
		else if (option.equalsIgnoreCase("revive"))
		{
			if (!Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
				return true;
			}
			if (args.length != 2)
			{
				Messager.ErrorMessage(sender, "Veuillez spécifier le nom du joueur à resusciter.");
				return true;
			}
			
			HuntPlayer hp = Game.getPlayer(args[1]);
			if (hp == null)
			{
				Messager.ErrorMessage(sender, "Impossible de trouver ce joueur.");
				return true;
			}
			
			if (hp.isAlive())
			{
				Messager.ErrorMessage(sender, "Le joueur doit être mort.");
				return true;
			}

			if (PlayerInteractions.schedule(hp, (h) -> { h.revive(); }) != null)
				sender.sendMessage("§7§oLe joueur sera ressuscité dés qu'il se reconnectera.");
		}
		else if (option.equalsIgnoreCase("kill"))
		{
			if (!Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
				return true;
			}
			if (args.length != 2)
			{
				Messager.ErrorMessage(sender, "Veuillez spécifier le nom du joueur à tuer.");
				return true;
			}
			
			HuntPlayer hp = Game.getPlayer(args[1]);
			if (hp == null)
			{
				Messager.ErrorMessage(sender, "Impossible de trouver ce joueur.");
				return true;
			}
			
			if (!hp.isAlive())
			{
				Messager.ErrorMessage(sender, "Le joueur doit être en vie.");
				return true;
			}
			
			if (!hp.isPlaying())
			{
				Messager.ErrorMessage(sender, "Le joueur doit faire partie de la partie.");
				return true;
			}

			/*
			hp.setAlive(false);
			if (hp.isOnline())
				hp.getPlayer().setGameMode(GameMode.SPECTATOR);

			Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' §b{0}§7 a été tué!", hp.getName()));
			Game.onPlayerDeathRevive();
			*/
			if (Game.isTeamMode())
				Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' {0}§7 a été tué!", hp.getTeamColoredName()));
			else
				Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' §b{0}§7 a été tué!", hp.getName()));
			if (!Game.isRoundMode())
				Normal.onDeath(hp, null);
			else
				Round.onDeath(hp, null);
		}
		else if (option.equalsIgnoreCase("damage"))
		{
			if (args.length < 3)
			{
				Messager.ErrorMessage(sender, "Veuillez spécifier le joueur et le montant.");
				return true;
			}

			final HuntPlayer hp = Game.getPlayer(args[1]);
			if (hp == null)
			{
				Messager.ErrorMessage(sender, "Impossible de trouver ce joueur.");
				return true;
			}

			if (!hp.isAlive())
			{
				Messager.ErrorMessage(sender, "Le joueur doit être en vie.");
				return true;
			}

			double amount;
			try
			{
				amount = Double.valueOf(args[2]);
			}
			catch (NumberFormatException e)
			{
				Messager.ErrorMessage(sender, "Veuillez saisir un nombre pour le montant!");
				return true;
			}

			HuntPlayer attacker = null;
			if (args.length == 4)
			{
				attacker = Game.getPlayer(args[3]);
				if (attacker == null)
				{
					Messager.ErrorMessage(sender, "Impossible de trouver ce joueur.");
					return true;
				}
			}

			PlayerInteractions.damage(hp, amount, attacker);
		}
		else if (option.equalsIgnoreCase("next"))
		{
			if (!Game.hasStarted())
			{
				Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
				return true;
			}

			if (!Game.isRoundMode())
			{
				Messager.ErrorMessage(sender, "Les rounds ne sont pas activé!");
				return true;
			}

			Round.roundEnd();
		}
		else if (option.equalsIgnoreCase("help"))
		{
			String text = "&8&m &m &m &m &m &m &m &m &m &m &m &8[ &c&lHunt &8]&m &m &m &m &m &m &m &m &m &m &m &r\n";
			text += " &7&l╸&r &ehelp&r Affiche l'aide\n";
			text += " &7&l╸&r &eborder <max> <min>&r Change la taille de la border\n";
			text += " &7&l╸&r &etime <game> <hunt>&r Change la durée de la partie\n";
			text += " &7&l╸&r &ekit&r Active/désactive les kits\n";
			text += " &7&l╸&r &eteam&r Active/désactive les équipes\n";
			text += " &7&l╸&r &eisland&r Active/désactive le mode île\n";
			text += " &7&l╸&r &eround <nombre>&r Active/désactive le mode round\n";
			text += " &7&l╸&r &estart&r Lance la partie\n";
			text += " &7&l╸&r &erevive <joueur>&r Resuscite un joueur\n";
			text += " &7&l╸&r &ekill <joueur>&r Tue un joueur\n";
			text += " &7&l╸&r &elist&r Liste les joueur\n";
			text += " &7&l╸&r &eshuffle&r Lance de force l'algorithme des cibles\n";
			text += " &7&l╸&r &edamage <joueur> <montant> [<source>]&r Inflige des dégâts à un joueur\n" ;
			text += " &7&l╸&r &enext&r Fait passer le round\n";
			text += "&8&m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m";
			Messager.PrintColored(sender, text);
		}
		else
		{
			Messager.ErrorMessage(sender, MessageFormat.format("Action inconnue ''{0}''.", option));
			return true;
		}

		return true;
	}

}
