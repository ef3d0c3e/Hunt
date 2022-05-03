package org.ef3d0c3e.hunt;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.teams.Team;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Vector;

/**
 * Utilities for normal game mode
 */
public class Normal
{
	/**
	 * Called when a player dies/is revived
	 */
	public static void onPlayerDeathRevive()
	{
		// Update player count
		int playerNum = 0;
		for (final HuntPlayer hp : Game.getPlayerList().values())
			if (hp.isAlive())
				++playerNum;
		Game.setPlayerNum(playerNum);

		// Update every player's scoreboard
		for (HuntPlayer hp : Game.getPlayerList().values())
			if (hp.isOnline())
				hp.updateScoreboard();

		if (!Game.isTeamMode() && Game.getPlayerNum() <= 1)
		{
			int score = Integer.MIN_VALUE;
			java.util.Vector<String> winners = new java.util.Vector<String>();
			for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
			{
				if (!set.getValue().isPlaying()) // Player is not part of the game
					continue;
				score = Math.max(score, set.getValue().getScore());
			}
			for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
				if (set.getValue().getScore() == score)
				{
					if (!set.getValue().isPlaying()) // Player is not part of the game
						continue;
					winners.add(set.getKey());
				}
			for (final String p : winners)
			{
				final HuntPlayer hp = Game.getPlayer(p);
				if (Game.isKitMode() && hp.getKit() != null)
					hp.getKit().onWin(hp);
			}

			Messager.broadcastColor("<#FF8010>&m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m ");
			Messager.broadcastColor("&r &r &r &r &r &r &r &r &r &r <#10FFA0>Fin de la partie!");
			if (winners.size() == 1) // Single winner
				Messager.broadcastColor(MessageFormat.format("&r &r &r &r &r &r &r &r &r &b{0} <#10FFA0>a gagné!", winners.get(0)));
			else // Multiple winners
			{
				String win = "";
				for (int i = 0; i < winners.size(); ++i)
				{
					if (i != 0)
					{
						if (i == winners.size() - 1)
							win += "<#10FFA0> et &b" + winners.get(i);
						else
							win += "<#10FFA0>, &b" + winners.get(i);
					} else
						win += winners.get(i);
				}
				Messager.broadcastColor(MessageFormat.format("&r &r &b{0} <#10FFA0>ont gagné!", win));
			}
			Messager.broadcastColor(MessageFormat.format("&r &r &r &r &r &r &r &r &r &r &r <#10FFA0>Avec &e{0} <#10FFA0>points.", score));
			Messager.broadcastColor("<#FF8010>&m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m ");

			Game.stop();
		}
		else
		{
			int aliveTeams = 0;
			for (final Team team : Game.getTeamList().values())
			{
				if (team.isAlive())
					++aliveTeams;
			}

			if (aliveTeams <= 1)
			{
				int score = Integer.MIN_VALUE;
				java.util.Vector<String> winners = new java.util.Vector<String>();
				for (final Team team : Game.getTeamList().values())
				{
					score = Math.max(score, team.getScore());
				}

				for (final Team team : Game.getTeamList().values())
				{
					if (team.getScore() != score)
						continue;

					winners.add(team.getColoredName());
					team.forAll((hp) ->
					{
						if (Game.isKitMode() && hp.getKit() != null)
							hp.getKit().onWin(hp);
					});
				}

				Messager.broadcastColor("<#FF8010>&m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m ");
				Messager.broadcastColor("&r &r &r &r &r &r &r &r &r &r <#10FFA0>Fin de la partie!");
				if (winners.size() == 1) // Single winner
					Messager.broadcastColor(MessageFormat.format("&r &r &r &r &r &r &r &r &r {0} <#10FFA0>a gagné!", winners.get(0)));
				else // Multiple winners
				{
					String win = "";
					for (int i = 0; i < winners.size(); ++i)
					{
						if (i != 0)
						{
							if (i == winners.size() - 1)
								win += "<#10FFA0> et " + winners.get(i);
							else
								win += "<#10FFA0>, " + winners.get(i);
						} else
							win += winners.get(i);
					}
					Messager.broadcastColor(MessageFormat.format("&r &r {0} <#10FFA0>ont gagné!", win));
				}
				Messager.broadcastColor(MessageFormat.format("&r &r &r &r &r &r &r &r &r &r &r <#10FFA0>Avec &e{0} <#10FFA0>points.", score));
				Messager.broadcastColor("<#FF8010>&m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m &m ");

				Game.stop();
			}
		}
	}

	/**
	 * Call when a player dies
	 *
	 * @param hp     Player that died
	 * @param killer Killer (may be null)
	 */
	public static void onDeath(HuntPlayer hp, HuntPlayer killer)
	{
		hp.setAlive(false);
		hp.getPlayer().setGameMode(GameMode.SPECTATOR);
		onPlayerDeathRevive();

		if (Game.getPlayerNum() > 1)
			updateTargets();

		// Kits hook
		if (Game.isKitMode() && hp.getKit() != null)
			hp.getKit().onDeath(hp);

		// Island hook
		if (Game.isIslandMode())
			Island.onDeath(hp);

		// Sound
		for (Player p : Bukkit.getOnlinePlayers())
			p.playSound(p.getLocation(), "minecraft:hunt.astronomia", SoundCategory.MASTER, 65536.f, 1.f);

		// Tomb : Chest containing player's inventory
		Util.spawnTomb(hp.getPlayer().getLocation(), hp);
		hp.getPlayer().getInventory().clear();

		if (Game.isTeamMode() && hp.getTeam().isAlive())
			Messager.HuntMessage(hp.getPlayer(), "Vous êtes mort, mais votre équipe peut encore vous ressusciter.");
		else
			Messager.HuntMessage(hp.getPlayer(), "Vous êtes mort, vous pouvez observer librement la partie.");
	}

	/**
	 * Updates targets for every dead player
	 */
	public static void updateTargets()
	{
		if (!Game.isTeamMode())
		{
			// Fill 'list' with players that have no target or no hunter and should be counted
			Vector<HuntPlayer> list = new Vector<>();
			for (HuntPlayer hp : Game.getPlayerList().values())
			{
				if (!hp.isAlive())
					continue;

				// No target or dead target
				if (hp.getTarget() == null ||
					!hp.getTarget().isAlive())
					list.add(hp);
					// No hunter or dead hunter
				else if (hp.getHunter() == null ||
					!hp.getHunter().isAlive())
					list.add(hp);
			}
			if (list.size() == 0)
				return;

			int index = Game.nextPosInt() % list.size();
			HuntPlayer hp1 = list.get(index);
			list.remove(index);
			HuntPlayer hp0 = hp1;

			while (list.size() >= 1)
			{
				// No target or dead target
				if (hp1.getTarget() == null ||
					!hp1.getTarget().isAlive())
				{
					index = Game.nextPosInt() % list.size();
					HuntPlayer hp2 = list.get(index);
					list.remove(index);

					hp1.setTarget(hp2);
					hp2.setHunter(hp1);

					hp1.getPlayer().playSound(
						hp1.getPlayer().getLocation(),
						Sound.ENTITY_WITCH_CELEBRATE,
						SoundCategory.MASTER,
						65536.f,
						1.2f
					);
					hp1.getPlayer().sendTitle(
						"§9Nouvelle cible!",
						MessageFormat.format("§b{0}", hp2.getName()),
						5, 50, 10
					);
					hp1.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Votre cible est §b{0}§7, tuez la pour gagner §e3 §7points.", hp2.getName()));
					hp1.updateScoreboard();

					hp1 = hp2;
				}
				// Get another player
				else
				{
					index = Game.nextPosInt() % list.size();
					hp1 = list.get(index);
					list.remove(index);
				}
			}
			// [LAST PLAYER] No target or dead target
			if (hp1.getTarget() == null ||
				!hp1.getTarget().isAlive())
			{
				hp1.setTarget(hp0);
				hp0.setHunter(hp1);

				hp1.getPlayer().playSound(
					hp1.getPlayer().getLocation(),
					Sound.ENTITY_WITCH_CELEBRATE,
					SoundCategory.MASTER,
					65536.f,
					1.2f
				);
				hp1.getPlayer().sendTitle(
					"§9Nouvelle cible!",
					MessageFormat.format("§b{0}", hp0.getName()),
					5, 50, 10
				);
				hp1.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Votre cible est §b{0}§7, tuez la pour gagner §e3 §7points.", hp0.getName()));
				hp1.updateScoreboard();
			}
		}
		else
		{
			// Tracking will be done on the closest player (refund cost if player not found...)
			// Also modify winning 'screen' code to display winning teams instead of players
			// Fill 'list' with teams that have no target or no hunter and should be counted
			Vector<Team> list = new Vector<>();
			for (Team team : Game.getTeamList().values())
			{
				if (!team.isAlive())
					continue;

				// No target or dead target
				if (team.getTarget() == null ||
					!team.getTarget().isAlive())
					list.add(team);
					// No hunter or dead hunter
				else if (team.getHunter() == null ||
					!team.getHunter().isAlive())
					list.add(team);
			}
			if (list.size() == 0)
				return;

			int index = Game.nextPosInt() % list.size();
			Team t1 = list.get(index);
			list.remove(index);
			Team t0 = t1; // Save it for the last iteration

			while (list.size() >= 1)
			{
				// No target or dead target
				if (t1.getTarget() == null ||
					!t1.getTarget().isAlive())
				{
					index = Game.nextPosInt() % list.size();
					Team t2 = list.get(index);
					list.remove(index);

					t1.setTarget(t2);
					t2.setHunter(t1);

					t1.forAll((hp) -> {
						if (!hp.isAlive() || !hp.isOnline())
							return;
						hp.getPlayer().playSound(
							hp.getPlayer().getLocation(),
							Sound.ENTITY_WITCH_CELEBRATE,
							SoundCategory.MASTER,
							65536.f,
							1.2f
						);
						hp.getPlayer().sendTitle(
							"§9Nouvelle cible!",
							MessageFormat.format("{0}", t2.getColoredName()),
							5, 50, 10
						);
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Vous traquez désormais l''équipe {0}§7, tuez leurs joueurs pour gagner §c3 §7points.", t2.getColoredName()));
						hp.updateScoreboard();
					});

					t1 = t2;
				}
				// Get another team
				else
				{
					index = Game.nextPosInt() % list.size();
					t1 = list.get(index);
					list.remove(index);
				}
			}
			// [LAST TEAM] No target or dead target
			if (t1.getTarget() == null ||
				!t1.getTarget().isAlive())
			{
				t1.setTarget(t0);
				t0.setHunter(t1);

				t1.forAll((hp) -> {
					if (!hp.isAlive() || !hp.isOnline())
						return;
					hp.getPlayer().playSound(
						hp.getPlayer().getLocation(),
						Sound.ENTITY_WITCH_CELEBRATE,
						SoundCategory.MASTER,
						65536.f,
						1.2f
					);
					hp.getPlayer().sendTitle(
						"§9Nouvelle cible!",
						MessageFormat.format("§b{0}", t0.getColoredName()),
						5, 50, 10
					);
					hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Vous traquez désormais l''équipe {0}§7, tuez leurs joueurs pour gagner §c3 §7points.", t0.getColoredName()));
					hp.updateScoreboard();
				});
			}
		}
	}

	/**
	 * Ran by the timer every seconds
	 */
	public static void run()
	{
		if (Game.getMinutes() == Game.getHuntTime() && Game.getSeconds() == 0)
			updateTargets();
	}

	/**
	 * Gets time elapsed since game began
	 * @return Seconds since game began
	 * @note Also works for prep time (aka round 0)
	 */
	public static int getTotalElapsedTime()
	{
		return Game.getTime();
	}

	/**
	 * Gets time elapsed since game began
	 * @return Minutes and seconds since game began
	 * @note Also works for prep time (aka round 0)
	 */
	public static Pair<Integer, Integer> getElapsedTime()
	{
		return new Pair<>(getTotalElapsedTime() / 60, getTotalElapsedTime() % 60);
	}
}