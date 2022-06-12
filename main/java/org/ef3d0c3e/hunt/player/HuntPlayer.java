package org.ef3d0c3e.hunt.player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.ef3d0c3e.hunt.*;
import org.ef3d0c3e.hunt.events.*;
import org.ef3d0c3e.hunt.game.Combat;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.island.IslandData;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitJb;
import org.ef3d0c3e.hunt.kits.KitLanczos;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.skins.Skin;
import org.ef3d0c3e.hunt.stats.StatLong;
import org.ef3d0c3e.hunt.stats.StatSaves;
import org.ef3d0c3e.hunt.stats.StatValue;
import org.ef3d0c3e.hunt.teams.Team;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;

import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

// Represents a player, not necessarily connected
public class HuntPlayer implements Listener
{
	@Getter
	private OfflinePlayer offlinePlayer;
	/**
	 * Updates underlying player
	 * @param p Player
	 */
	public void setPlayer(final Player p)
	{
		offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
	}

	@Getter
	private boolean alive; // Is player alive
	@Getter @Setter
	private boolean playing; // Is playing in this game
	private int score; // Current player's score

	@Getter @Setter
	private HuntPlayer target; // Player hunted by *this*
	@Getter @Setter
	private HuntPlayer hunter; // Player hunting *this*

	@Getter @Setter
	private int skin;
	@Getter
	private Kit kit; // The player's kit
	@Getter
	private Team team; // The player's team
	@Getter @Setter
	private IslandData islandData;
	@Getter @Setter
	Round.Data roundData; // State in current round

	@Getter
	private org.bukkit.scoreboard.Team nametagTeam; // For tag
	private FastBoard fb;

	@Getter
	private Combat.Data combatData;
	@Getter @Setter
	private int deathTime; // Last death time (-1 if not set) [used for JB]

	@Getter @Setter
	private Location lastPortal = null; // Last (overworld) portal entered by player


	@Getter
	private PlayerInteractions.InteractionsData interactions = null;
	@Getter
	private HashMap<String, StatValue> stats; ///< List of stats associated with player

	public HuntPlayer(Player p)
	{
		offlinePlayer = p;
		alive = false;
		playing = false;
		score = 0;
		target = null;
		hunter= null;
		skin = -1;
		kit = null;
		team = null;
		islandData = null;
		roundData = new Round.Data();
		fb = null;
		combatData = new Combat.Data();
		deathTime = -1;
		interactions = new PlayerInteractions.InteractionsData();
		stats = new HashMap<>();
	}
	
	//-------//
	// Hooks //
	//-------//

	public void spreadRandom()
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				String.format("spreadplayers 0 0 0 %d false %s", Math.abs(Game.getBorderRadius() - 10), getName()) );
	}
	//---------//
	// Getters //
	//---------//
	public Player getPlayer()
	{
		return getOfflinePlayer().getPlayer();
	}
	
	public boolean isOnline()
	{
		return getOfflinePlayer().isOnline();
	}
	
	public String getName()
	{
		return getOfflinePlayer().getName();
	}

	/**
	 * Gets the player name with team color
	 * @return Player's name colored with team color
	 */
	public String getTeamColoredName()
	{
		return Messager.getColored(MessageFormat.format("{0}{1}", getTeam().getColor().color, getName()));
	}

	/**
	 * Gets player's UUID
	 * @return Player's UUID
	 */
	public UUID getUUID()
	{
		return getOfflinePlayer().getUniqueId();
	}

	/**
	 * Gets whether player is a spectator or not
	 * @return True if player is a spectator
	 */
	public boolean isSpectator()
	{
		return !isAlive() && isOnline();
	}

	/**
	 * Gets player's score (or player team's score if teams are on)
	 * @return If teams are on, player team's score; otherwise player's score
	 */
	public int getScore()
	{
		if (Game.isTeamMode())
			return getTeam().getScore();
		else
			return score;
	}

	/**
	 * Gets whether player and this can kill each other
	 * @param hp The other player
	 * @return True if they can kill each other
	 */
	public boolean canKill(HuntPlayer hp)
	{
		if (Game.isTeamMode())
			return team.getTarget() == hp.getTeam() || hp.getTeam().getTarget() == team;
		else if (Game.isRoundMode() && !(getRoundData().isGhost() || hp.getRoundData().isGhost()) )
			return true;
		else
			return getTarget() == hp || hp.getTarget() == this;
	}

	/**
	 * Gets stats from key name
	 * @param key Key of stat
	 * @return StatValue if found, null otherwise
	 */
	public StatValue getStat(final String key)
	{
		return getStats().get(key);
	}

	/**
	 * Sets stats from key name
	 * @param key Key of stat
	 * @param value Value of stat
	 */
	public void setStat(final String key, final StatValue value)
	{
		getStats().put(key, value);
	}

	/**
	 * Increment specific (long) stat
	 * @param key Stat's key
	 */
	public void incStat(final String key)
	{
		final StatLong s = (StatLong)getStats().get(key);
		++s.value;
	}

	/**
	 * Get wether or not player can damage another player
	 * @param other The player that would be damaged
	 * @return true If this player can damage other
	 */
	public boolean canDamage(HuntPlayer other)
	{
		if (!Game.isTeamMode())
			return Game.inHunt() && !(getRoundData().isGhost() || other.getRoundData().isGhost());
		if (getTeam() == other.getTeam())
			return false;
		else
			return Game.inHunt() && !(getRoundData().isGhost() || other.getRoundData().isGhost());
	}
	
	//---------//
	// Setters //
	//---------//

	/**
	 * Sets player's score
	 * @param score Player's new score
	 */
	public void setScore(final int score)
	{
		if (Game.isTeamMode())
			getTeam().setScore(score);
		else
			this.score = score;
	}

	/**
	 * Sets whether player is alive or not
	 * @param alive Whether player is alive or not
	 */
	public void setAlive(boolean alive)
	{
		this.alive = alive;
		if (Game.isTeamMode())
			getTeam().updateAlive();
	}

	/**
	 * Sets player's kit
	 * @param kit Player's new kit
	 */
	public void setKit(Kit kit)
	{
		if (getKit() != null)
			KitMenu.setTaken(getKit(), false);
		this.kit = kit;
		if (getKit() != null)
			KitMenu.setTaken(getKit(), true);
		updateScoreboard();
		updateTabname();
	}

	/**
	 * Sets player's team
	 * @param team Player's new team
	 */
	public void setTeam(Team team)
	{
		this.team = team;
		updateScoreboard();
		updateTabname();
		updateNametag();
	}

	/**
	 * Revive player
	 * @note Player must be online
	 */
	public void revive()
	{
		setPlaying(true);
		setAlive(true);
		getPlayer().setGameMode(GameMode.SURVIVAL);
		getPlayer().teleport(new Location(Game.getOverworld(), 0, 256, 0));
		getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 7*20, 10));
		getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		if (Game.isIslandMode()) // TODO: EV
			Island.onStart(this);

		if (!Game.isRoundMode())
			Normal.onPlayerDeathRevive();
		else
			Round.onPlayerDeathRevive();

		if (Game.isTeamMode())
			Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' {0}§7 a été ressuscité!", getTeamColoredName()));
		else
			Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' §b{0}§7 a été ressuscité!", getName()));

		Bukkit.getPluginManager().callEvent(new HPSpawnEvent(this, true));
	}

	/*
	 * Cosmetics
	 */
	/**
	 * Updates player's scoreboard
	 */
	public void updateScoreboard()
	{
		fb.updateTitle("§b🗡 §6§lHUNT§b 🪓");
		ArrayList<String> l = new ArrayList<String>();
		l.ensureCapacity(6);

		if (Game.hasStarted()) // In game
		{
			if (!Game.isRoundMode())
				l.add(MessageFormat.format("§7En vie: §e{0}", Game.getPlayerNum()));
			else
				l.add(MessageFormat.format("§7En vie: §e{0}", Round.getAliveNum()));
			l.add(MessageFormat.format("§7Score: §e{0}", getScore()));
			if (!Game.isTeamMode())
			{
				if (getTarget() != null &&
					(!Game.isRoundMode() || getRoundData().isAlive()))
					l.add(MessageFormat.format("§7Cible: §b{0}", getTarget().getName()));
			}
			else if (getTeam() != null)
			{
				if (getTeam().getTarget() != null &&
					(!Game.isRoundMode() || getRoundData().isAlive()))
					l.add(MessageFormat.format("§7Cible: §b{0}", getTeam().getTarget().getColoredName()));
			}
			
			l.add("§0");
			l.add(MessageFormat.format("§7Border: §a+{0}", Game.getBorderRadius()));
			if (Game.isRoundMode() && Game.inHunt())
				l.add(MessageFormat.format("§7Round: §d{0}§7/§d{1}", Round.getCurrentRound(), Round.getRoundNum()));

			if (Game.isRoundMode() && Game.inHunt())
			{
				final Pair<Integer, Integer> left = Round.getRoundTimeLeft();
				String lmins, lsecs;
				lmins = MessageFormat.format("{0}", left.first);
				if (left.second < 10)
					lsecs = MessageFormat.format("0{0}", left.second);
				else
					lsecs = MessageFormat.format("{0}", left.second);

				final Pair<Integer, Integer> prol = Round.getProlongationsDisplay();
				String pmins, psecs;
				pmins = MessageFormat.format("{0}", prol.first);
				if (prol.second < 10)
					psecs = MessageFormat.format("0{0}", prol.second);
				else
					psecs = MessageFormat.format("{0}", prol.second);

				l.add(MessageFormat.format("§6{0}:{1}§7+§c§o{2}:{3}", lmins, lsecs, pmins, psecs));
			}
			else
			{
				String mins, secs;
				if (Game.getMinutes() < 10)
					mins = MessageFormat.format("0{0}", Game.getMinutes());
				else
					mins = MessageFormat.format("{0}", Game.getMinutes());
				if (Game.getSeconds() < 10)
					secs = MessageFormat.format("0{0}", Game.getSeconds());
				else
					secs = MessageFormat.format("{0}", Game.getSeconds());
				l.add(MessageFormat.format("§e{0}:{1}", mins, secs));
			}
		}
		else // Lobby
		{
			if (Game.isKitMode() && Game.isTeamMode())
			{
				l.add("§7La partie va commencer!");
				if (getKit() == null && getTeam() == null)
					l.add("§7Choisissez un §nkit§7 et une §néquipe");
				else
				{
					if (getKit() == null)
						l.add("§7Choisissez un §nkit");
					else
						l.add(MessageFormat.format("§7Kit: §a{0}", getKit().getDisplayName()));
					if (getTeam() == null)
						l.add("§7Choisissez une §néquipe");
					else
						l.add(
							Messager.getColored(MessageFormat.format("§7Équipe: {0}", getTeam().getColoredName()))
						);
				}
			}
			else if (Game.isKitMode())
			{
				l.add("§7La partie va commencer!");
				if (getKit() == null)
					l.add("§7Choisissez un §nkit");
				else
					l.add(MessageFormat.format("§7Kit: §a{0}", getKit().getDisplayName()));

			}
			else if (Game.isTeamMode())
			{
				l.add("§7La partie va commencer!");
				if (getTeam() == null)
					l.add("§7Choisissez une §néquipe");
				else
					l.add(
						Messager.getColored(MessageFormat.format("§7Équipe: {0}", getTeam().getColoredName()))
					);
			}
			else
			{
				l.add("§7La partie va commencer!");
			}
		}

		fb.updateLines(l);
	}

	/**
	 * Update the tab list (header and footer) for the player
	 */
	public void updateTablist()
	{
		String header, footer;
		
		header = " §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l §l \n"
				+ "§6§l<§f§l°§6§l)))>< §a §l✤ §c§lHUNT §a§l✤ §6 §l><(((§f§l°§6§l>\n";
		footer = "\n"
				+ "§b§lVersion:§e 3.6 §7§o[/changelog]\n"
				+ "§c§lSite internet:§d pundalik.org/hunt\n";
		
		getPlayer().setPlayerListHeaderFooter(header, footer);
	}

	/**
	 * Update player's name in tab
	 */
	public void updateTabname()
	{
		Player p = getPlayer();
		String color, prefix, suffix;
		if (Game.hasStarted() && !isAlive()) // Player is dead
			color = "§7§o";
		else
			color = "§e";
		if (Game.isKitMode() && getKit() != null)
			suffix = " §7" + getKit().getDisplayName();
		else
			suffix = "";
		prefix = "";

		if (Game.isTeamMode() && getTeam() != null)
		{
			prefix += Messager.getColored(getTeam().getColor().color) + "§l" + getTeam().getName() + " §8: §r";
			color += Messager.getColored(getTeam().getColor().color);
		}

		if (Game.isRoundMode() && !getRoundData().isAlive())
		{
			color += "§o";
			if (getRoundData().isZombie())
				suffix = " §2☠" + suffix;
			else if (getRoundData().isGhost())
				suffix = " §8☠" + suffix;
		}

		p.setPlayerListName(MessageFormat.format("{1}{2}{0}{3}", p.getName(), prefix, color, suffix));
	}
	
	//

	/**
	 * Updates player's nametag (prefix & suffix)
	 * @note Must be called when player's team changes (or team mode is toggled)
	 */
	public void updateNametag()
	{
		if (getNametagTeam() == null)
		{
			nametagTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(getName());
			if (getNametagTeam() == null)
				nametagTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(getName());
			getNametagTeam().setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER); // Just set this once
		}

		//TODO: Round+TEAM
		getNametagTeam().setPrefix("");
		getNametagTeam().setSuffix("");
		if (Game.isTeamMode())
		{
			if (getTeam() != null)
			{
				getNametagTeam().setPrefix(Messager.getColored(getTeam().getColor().color) + "§l" + getTeam().getName() + " ");
				getNametagTeam().setColor(ChatColor.DARK_GRAY);
			}
			else
			{
				getNametagTeam().setPrefix("§7§o<sans équipe> ");
				getNametagTeam().setColor(ChatColor.DARK_GRAY);
			}
		}
		else
		{
			if (Game.isRoundMode())
			{
				if (getRoundData().isAlive())
				{
					getNametagTeam().setColor(ChatColor.YELLOW);
					getNametagTeam().setPrefix("");
				}
				else if (getRoundData().isZombie())
				{
					getNametagTeam().setColor(ChatColor.GOLD);
					getNametagTeam().setPrefix("§2§lZOMBIE ");
					getNametagTeam().setSuffix(" §c☠");
				}
				else if (getRoundData().isGhost())
				{
					getNametagTeam().setColor(ChatColor.GOLD);
					getNametagTeam().setPrefix("§8§lFANTÔME ");
					getNametagTeam().setSuffix(" §c☠");
				}
			}
			else
			{
				getNametagTeam().setColor(ChatColor.YELLOW);
			}
		}
		
		if (getNametagTeam().getEntries().isEmpty()) // Should only ever contain a single player
			getNametagTeam().addEntry(getName());
	}

	/**
	 * Updates player's health display
	 */
	public void updateHealth()
	{
		Objective obj = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME);
		if (obj == null)
		{
			obj = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("health", "health", Messager.getColored("<#FF0000>✚"));
			obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}
		for (Player p : Bukkit.getOnlinePlayers())
			obj.getScore(p.getName()).setScore((int)p.getHealth());;
	}

	public boolean updateTracking(boolean reverse)
	{
		if (Game.isTeamMode())
		{
			if (!reverse)
			{
				if (getTeam().getTarget() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de cible!");
					return false;
				}

				final HuntPlayer closest = getTeam().getTarget().getClosestPlayer(this);
				if (closest == null)
				{
					getPlayer().sendMessage("§cAucun joueur à traquer!");
					return false;
				}

				getPlayer().setCompassTarget(closest.getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§a{0} §8§l|| §eDistance: §7{1}m", closest.getName(), getPlayer().getLocation().distance(closest.getPlayer().getLocation()))));
				return true;
			}
			else
			{
				if (getTeam().getHunter() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de chasseur!");
					return false;
				}

				final HuntPlayer closest = getTeam().getHunter().getClosestPlayer(this);
				if (closest == null)
				{
					getPlayer().sendMessage("§cAucun joueur à traquer!");
					return false;
				}

				getPlayer().setCompassTarget(closest.getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§c{0} §8§l|| §eDistance: §7{1}m", closest.getName(), getPlayer().getLocation().distance(closest.getPlayer().getLocation()))));
				return true;
			}
		}
		else
		{
			// NOTE: [Tom] if m_target is null then m_hunter is too
			if (!reverse)
			{
				if (getTarget() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de cible!");
					return false;
				}

				if (!getTarget().isOnline())
				{
					getPlayer().sendMessage("§cVotre cible est déconnectée!");
					return false;
				} else if (getTarget().getPlayer().getWorld() != getPlayer().getWorld())
				{
					getPlayer().sendMessage("§cVotre cible est dans une autre dimension.");
					return false;
				}

				getPlayer().setCompassTarget(getTarget().getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§b§l>>>§e §lDistance§e: {0}m §b§l<<<", getPlayer().getLocation().distance(getTarget().getPlayer().getLocation()))));
				return true;
			}
			else
			{
				if (getHunter() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de chasseur!");
					return false;
				}

				if (!getHunter().isOnline())
				{
					getPlayer().sendMessage("§cVotre chasseur est déconnecté!");
					return false;
				}
				else if (getHunter().getPlayer().getWorld() != getPlayer().getWorld())
				{
					getPlayer().sendMessage("§cVotre chasseur est dans une autre dimension.");
					return false;
				}

				getPlayer().setCompassTarget(getHunter().getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§b§l>>>§e §lDistance§e: {0}m §b§l<<<", getPlayer().getLocation().distance(getHunter().getPlayer().getLocation()))));
				return true;
			}
		}
	}

	static private HashMap<String, HuntPlayer> playerList = new HashMap<>();

	/**
	 * Adds a player to the player list
	 * @param p The player to add
	 * @return A HuntPlayer object corresponding to the player
	 */
	public static HuntPlayer addPlayer(Player p)
	{
		HuntPlayer hp;
		hp = playerList.get(p.getName());
		if (hp != null)
			return hp;

		// Unknown player
		hp = new HuntPlayer(p);
		playerList.put(p.getName(), hp);
		return hp;
	}

	/**
	 * Gets player from playerlist by his name
	 * @param player The bukkit player
	 * @return HuntPlayer associated with name (null if no player is found)
	 */
	public static HuntPlayer getPlayer(final Player player)
	{
		return playerList.get(player.getName());
	}

	/**
	 * Gets player from playerlist by his name
	 * @param name Player's name
	 * @return HuntPlayer associated with name (null if no player is found)
	 */
	public static HuntPlayer getPlayer(final String name)
	{
		return playerList.get(name);
	}

	public interface ForEach
	{
		void operation(final HuntPlayer hp);
	}

	static public void forEach(final ForEach f)
	{
		for (final HuntPlayer hp : playerList.values())
			f.operation(hp);
	}

	public static class Events implements Listener
	{
		@EventHandler
		public void onJoin(final HPlayerJoinEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();
			Bukkit.broadcastMessage(MessageFormat.format("§8[§a+§8] §7{0}", hp.getName()));

			hp.fb = new FastBoard(hp.getPlayer());
			StatSaves.load(hp);

			if (Game.hasStarted() && !hp.isAlive())
				hp.getPlayer().setGameMode(GameMode.SPECTATOR);

			Skin.updatePlayerSkin(hp);

			hp.updateScoreboard();
			hp.updateTablist();
			hp.updateTabname();
			hp.updateNametag();
			hp.updateHealth();

			// Load achievement progress
			new BukkitRunnable() // TODO: EV move into
			{
				@Override
				public void run()
				{
					HuntAchievement.getManager().addPlayer(hp.getPlayer());
					//HuntAchievement.getManager().loadProgress(p, HuntAchievement.getSave()); TODO
					HuntAchievement.HUNT.award(hp.getPlayer(), 1);
				}
			}.runTaskLater(Hunt.plugin, 2);

			// Call interactions hook
			hp.getInteractions().onJoin(hp);
		}

		@EventHandler
		public static void onQuit(final HPlayerQuitEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();

			Bukkit.broadcastMessage(MessageFormat.format("§8[§c-§8] §7{0}", hp.getName()));
			StatSaves.save(hp);

			// Unset kit/team so that players are removed from list
			if (!Game.hasStarted())
			{
				hp.setKit(null);
				hp.setTeam(null);
			}
		}

		/**
		 * Send message to other team members
		 * @param hp Player sending message
		 * @param msg Message sent by player
		 */
		public static void teamMessage(final HuntPlayer hp, final String msg)
		{
			String formatted = Messager.getColored(MessageFormat.format("{0}#{1} &7&o{2}", hp.getTeam().getColor().color, hp.getName(), msg));
			// Need to loop all players because team's playerlist may not have been populated yet
			HuntPlayer.forEach(other -> {
				if (other.getTeam() == hp.getTeam())
					other.getPlayer().sendMessage(formatted);
			});
		}

		@EventHandler
		public static void onChat(AsyncPlayerChatEvent ev)
		{
			HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			String prefix, suffix;
			if (Game.isTeamMode() && hp.getTeam() != null && ev.getMessage().length() >= 1 && ev.getMessage().charAt(0) == '.')
			{
				teamMessage(hp, ev.getMessage().substring(1));
				ev.setCancelled(true);
				return;
			}

			if (Game.hasStarted() && !hp.isAlive()) // Player is dead
			{
				prefix = "&7[MORT] ";
				if (Game.isTeamMode() && hp.getTeam() != null)
					prefix += MessageFormat.format("{0}'('{1}')' ", hp.getTeam().getColor().color, hp.getTeam().getName());
				suffix = "&8: &7&o";
			}
			else if (Game.hasStarted()) // In game
			{
				suffix = ": &7";
				if (Game.isTeamMode())
				{
					prefix = MessageFormat.format("{0}'('{1}')' ", hp.getTeam().getColor().color, hp.getTeam().getName());

					if (Game.isRoundMode())
					{
						switch (hp.getRoundData().getState())
						{
							case GHOST:
								prefix += "&8";
								suffix = " ☠" + suffix;
								break;
							case ZOMBIE:
								prefix += "&2";
								suffix = "§2☠" + suffix;
								break;
						}
					}
				}
				else
				{
					prefix = "";
					if (Game.isRoundMode())
					{
						switch (hp.getRoundData().getState())
						{
							case ALIVE:
								prefix = "&e";
								break;
							case GHOST:
								prefix = "&8";
								suffix = " ☠" + suffix;
								break;
							case ZOMBIE:
								prefix = "&2";
								suffix = " §2☠" + suffix;
								break;
						}
					}
					else
						prefix = "&e";
				}
			}
			else // Waiting for game to start
			{
				if (Game.isTeamMode() && hp.getTeam() != null)
					prefix = MessageFormat.format("{0}'('{1}')' ", hp.getTeam().getColor().color, hp.getTeam().getName());
				else
					prefix = "&e";
				suffix = "&8: &7";
			}

			ev.setFormat(Messager.getColored(prefix + hp.getName() + suffix + "%2$s"));

			if (hp.getPlayer().isOp())
				ev.setMessage(Messager.getColored(ev.getMessage()));
			else
				ev.setMessage(ev.getMessage());
		}

		// Update combat data
		@EventHandler
		public void onTakeDamage(final HPlayerDamageEvent ev)
		{
			if (ev.getAttacker() == null)
				return;

			final Combat.Data data = ev.getVictim().getCombatData();
			data.damagedNow(ev.getAttacker());
		}

		/**
		 * Score & messages
		 * @param ev Event
		 */
		@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
		public void onDeath(final HPlayerDeathEvent ev)
		{
			// Set death time
			ev.getVictim().setDeathTime(Game.getTime());

			if (ev.getPlayerKiller() == null) // Natural death
			{
				if (!Game.isTeamMode())
				{
					Bukkit.broadcastMessage(MessageFormat.format("§8'{§c☠§8}' §b{0}§7 est mort.", ev.getVictim().getName()));
				}
				else
				{
					Bukkit.broadcastMessage(MessageFormat.format("§8'{§c☠§8}' {0}§7 est mort.", ev.getVictim().getTeamColoredName()));
				}
			}
			else // Killed by player
			{
				// Give victim's skull to killer
				PlayerInteractions.giveItem(ev.getPlayerKiller(), new ItemStack[] { Items.getDeathSkull(ev.getVictim()) }, true, true);

				if (!Game.isRoundMode() || (ev.getPlayerKiller().getRoundData().isAlive() && ev.getVictim().getRoundData().isAlive()))
				{
					ev.getPlayerKiller().setScore(ev.getPlayerKiller().getScore() + 3);
					if (!Game.isTeamMode())
						ev.getPlayerKiller().getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §7Vous avez tué §b{0}§7, vous gagnez §e3 §7points.", ev.getVictim().getName()));
					else
						ev.getPlayerKiller().getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §7Vous avez tué {0}§7, votre équipe gagne §e3 §7points.", ev.getVictim().getTeamColoredName()));
				}

				// Notify players
				HuntPlayer.forEach(hp -> {
					if (hp == ev.getPlayerKiller() || !hp.isOnline())
						return;

					if (!hp.isAlive() || hp == ev.getVictim())
					{
						if (!Game.isTeamMode())
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §b{0}§7 a tué §b{1}§7.", ev.getPlayerKiller().getName(), ev.getVictim().getName()));
						else
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7.", ev.getPlayerKiller().getTeamColoredName(), ev.getVictim().getTeamColoredName()));

						return;
					}

					// Notify team members
					if (Game.isTeamMode() && !Game.isRoundMode())
					{
						if (hp.getTeam() == ev.getPlayerKiller().getTeam())
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7, votre équipe gagne §e3 §7point.", ev.getPlayerKiller().getTeamColoredName(), ev.getVictim().getTeamColoredName()));
						else if (hp.getTeam() != ev.getVictim().getTeam())
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7, tout les équipes en vie gagnent §e1 §7point.", ev.getPlayerKiller().getTeamColoredName(), ev.getVictim().getTeamColoredName()));

					}
				});

				// All alive teams earn 1 point
				if (Game.isTeamMode() && !Game.isRoundMode())
				{
					Team.forEach(team -> {
						if (team.isAlive() &&
							team != ev.getPlayerKiller().getTeam() &&
							team != ev.getVictim().getTeam())
							team.setScore(team.getScore()+1);
					});
				}
				else if (!Game.isRoundMode() && !Game.isTeamMode()) // All alive players earn 1 point
				{
					HuntPlayer.forEach(hp -> {
						if (hp == ev.getPlayerKiller())
							return;
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §b{0}§7 a tué §b{1}§7, tout les joueurs en vie gagnent §e1 §7point.", ev.getPlayerKiller().getName(), ev.getVictim().getName()));
						hp.setScore(hp.getScore()+1);
					});
				}
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onKilledWrong(final HPlayerKilledWrongEvent ev)
		{
			ev.getAttacker().spreadRandom();
			ev.getAttacker().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
			ev.getAttacker().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));

			ev.getVictim().getPlayer().setHealth(ev.getVictim().getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			ev.getVictim().getPlayer().setFireTicks(0);

			if (!Game.isTeamMode())
			{
				ev.getVictim().getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de vous tuer, §b{0} §7a perdu §e2 §7points.", ev.getAttacker().getName()));
				ev.getAttacker().getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer §b{0}§7, vous avez perdu §e2 §7points.", ev.getVictim().getName()));
			}
			else
			{
				ev.getVictim().getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de vous tuer, {0} §7a fait perdre §e2 §7points à son équipe.", ev.getAttacker().getTeamColoredName()));
				ev.getAttacker().getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer {0}§7, votre équipe a perdu §e2 §7points.", ev.getVictim().getTeamColoredName()));
				ev.getAttacker().getTeam().forAllPlayers(hp -> {
					if (hp == ev.getAttacker())
						return;
					hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer {0}§7, {1}§7 a fait perdre §e2 §7points à votre équipe.", ev.getVictim().getTeamColoredName(), ev.getAttacker().getTeamColoredName()));
				});
			}
			ev.getAttacker().getPlayer().playSound(ev.getAttacker().getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 65536.f, 1.f);
			ev.getVictim().getPlayer().playSound(ev.getVictim().getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 65536.f, 1.f);

			ev.getAttacker().setScore(ev.getAttacker().getScore() - 2);
		}
	}
}
