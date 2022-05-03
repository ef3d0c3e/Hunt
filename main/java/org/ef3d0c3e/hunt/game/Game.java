package org.ef3d0c3e.hunt.game;

import java.text.MessageFormat;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Fast;
import org.ef3d0c3e.hunt.Normal;
import org.ef3d0c3e.hunt.Round;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.teams.Team;
import org.ef3d0c3e.hunt.teams.TeamBeacon;
import org.ef3d0c3e.hunt.teams.TeamColor;

import com.comphenix.protocol.ProtocolManager;

public class Game
{
	static private Plugin m_plugin;
	static private ProtocolManager m_manager;
	static private Random m_random;
	
	static private HashMap<String, HuntPlayer> m_playerList;
	static private int m_playerNum; // Number of alive players
	static private boolean m_started;
	static private boolean m_netherActive;
	static private boolean m_kit;
	static private boolean m_team;
	static private boolean m_island;
	static private boolean m_round;
	static private boolean m_fast;
	//static private boolean m_swap;

	static private Timer m_timer;
	static private boolean m_pause; // Pauses timer
	static private int m_huntTime; // Number of minutes before hunt
	static private int m_gameTime; // Number of minutes before end

	static private World m_overworld;
	static private WorldBorder m_borderOverworld;
	static private World m_nether;
	static private WorldBorder m_borderNether;
	
	static private int m_borderStart;
	static private int m_borderMax;
	static private int m_borderMin;
	
	static private HashMap<String, Team> m_teamList;

	/**
	 * Initialization Hook
	 * @param plugin Bukkit plugin for Hunt
	 * @param manager ProtocolManager from Protocollib
	 */
	public static void init(Plugin plugin, ProtocolManager manager)
	{
		m_plugin = plugin;
		m_manager = manager;
		m_random = new Random(System.currentTimeMillis());
		
		m_playerList = new HashMap<String, HuntPlayer>();
		m_playerNum = 0;
		m_started = false;
		m_netherActive = true;
		m_kit = true;
		m_team = false;
		m_island = false;
		m_round = true;
		m_fast = true;

		m_timer = new Timer();
		m_pause = false;
		m_huntTime = 0;
		m_gameTime = 60;

		m_overworld = Bukkit.getWorld("world");
		m_borderOverworld = m_overworld.getWorldBorder();
		m_nether = Bukkit.getWorld("world_nether");
		m_borderNether = m_nether.getWorldBorder();

		m_borderStart = 50;
		m_borderMax = 100;
		m_borderMin = 50;
		m_borderOverworld.setCenter(0.0, 0.0);;
		m_borderOverworld.setSize(m_borderStart);
		m_borderOverworld.setWarningDistance(0);
		m_borderNether.setCenter(0.0, 0.0);
		m_borderNether.setSize(m_borderStart);
		m_borderNether.setWarningDistance(0);
		
		// Overworld
		m_overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		m_overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		m_overworld.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
		m_overworld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
		m_overworld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		m_overworld.setGameRule(GameRule.DO_INSOMNIA, false);
		m_overworld.setGameRule(GameRule.SPAWN_RADIUS, 0);
		m_overworld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		m_overworld.setTime(6000);
		m_overworld.setWeatherDuration(0);
		m_overworld.setDifficulty(Difficulty.PEACEFUL);
		m_overworld.setSpawnLocation(0, m_overworld.getHighestBlockYAt(0, 0), 0);
		// Nether
		m_nether.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		m_nether.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		m_nether.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
		m_nether.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
		m_nether.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		m_nether.setGameRule(GameRule.DO_INSOMNIA, false);
		m_nether.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		m_nether.setTime(6000);
		m_nether.setWeatherDuration(0);
		m_nether.setDifficulty(Difficulty.PEACEFUL);
		
		m_teamList = new HashMap<>();
	}

	/**
	 * Start hook
	 * Called when game starts
	 * @note Is responsible for calling other start hooks
	 */
	public static void start()
	{
		m_timer.runTaskTimer(m_plugin, 0, 20);
		m_started = true;
		
		m_borderOverworld.setDamageBuffer(12); // Player can go safely outside this much
		m_borderOverworld.setDamageAmount(0.15); // Damage per seconds per blocks
		m_borderOverworld.setWarningTime(20); // Red screen starts 20 seconds before border reaches player
		m_borderOverworld.setWarningDistance(5); // Red screen starts 5 blocks around the border
		m_borderOverworld.setSize(m_borderMax*2);

		m_borderNether.setDamageBuffer(12);
		m_borderNether.setDamageAmount(0.15);
		m_borderNether.setWarningTime(20);
		m_borderNether.setWarningDistance(5);
		m_borderNether.setSize(m_borderMax*2);

		// Overworld
		m_overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		m_overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
		m_overworld.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
		m_overworld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
		m_overworld.setTime(0);
		m_overworld.setDifficulty(Difficulty.NORMAL);
		// Nether
		m_nether.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		m_nether.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
		m_nether.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
		m_nether.setGameRule(GameRule.DO_MOB_SPAWNING, true);
		m_nether.setTime(0);
		m_nether.setDifficulty(Difficulty.NORMAL);

		// Call kit start hooks
		if (isKitMode())
		{
			for (Kit kit : KitMenu.getList())
				kit.start();
		}
		if (isIslandMode())
			Island.start();

		for (final HuntPlayer hp : m_playerList.values())
		{
			if (!hp.isOnline())
				continue;

			hp.setAlive(true);
			hp.setPlaying(true);
			hp.getPlayer().setGameMode(GameMode.SURVIVAL);
			if (isKitMode() && hp.getKit() != null)
				hp.getKit().onStart(hp);
			if (isIslandMode())
				Island.onStart(hp);

			// Fire & Saturation
			hp.getPlayer().setFireTicks(0);
			hp.getPlayer().setFoodLevel(20);
			hp.getPlayer().setSaturation(20.f);
			// Spread (non team)
			if (!Game.isTeamMode())
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
					String.format("spreadplayers 0 0 0 %d false %s", m_borderMax - 5, hp.getName()) );

			++m_playerNum;
		}

		// Call team start hook
		if (isTeamMode())
		{
			Iterator<Map.Entry<String, Team>> it = m_teamList.entrySet().iterator();
			while (it.hasNext())
			{
				final Team team = it.next().getValue();
				team.start();
				if (team.getPlayerList().isEmpty()) // Remove it because team target algorithm would select empty teams
					it.remove();
			}

			Bukkit.getServer().getPluginManager().registerEvents(new Team.TeamEvents(), Game.getPlugin());
		}

		// Team Spread
		// Only spread a player per team and teleport team members to this player
		if (Game.isTeamMode())
		{
			for (final Team t : m_teamList.values())
			{
				final HuntPlayer hp = t.getPlayerList().get(0);
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
					String.format("spreadplayers 0 0 0 %d false %s", m_borderMax - 5, hp.getName()));
				for (final HuntPlayer other : t.getPlayerList())
				{
					other.getPlayer().teleport(hp.getPlayer());
				}
			}
		}

		// Register tracker recipe
		{
			NamespacedKey key = new NamespacedKey(getPlugin(), "tracker");
			ShapelessRecipe recipe = new ShapelessRecipe(key, HuntItems.getTracker());
			recipe.addIngredient(Material.ROTTEN_FLESH);
			recipe.addIngredient(Material.GUNPOWDER);
			recipe.addIngredient(Material.BONE);
			recipe.addIngredient(Material.SPIDER_EYE);
			Bukkit.addRecipe(recipe);
		}

		// Start achievements
		HuntAchievement.onStart();

		// Spawn beacon
		if (Game.isTeamMode())
		{
			TeamBeacon.onStart();
			Bukkit.getServer().getPluginManager().registerEvents(new TeamBeacon(), getPlugin());
		}

		// Round events & start
		if (Game.isRoundMode())
		{
			Round.start();
			Bukkit.getServer().getPluginManager().registerEvents(new Round.RoundEvents(), Game.getPlugin());
		}

		// Fast events & start
		if (Game.isFastMode())
		{
			Fast.start();
			Bukkit.getServer().getPluginManager().registerEvents(new Fast.FastEvents(), Game.getPlugin());
		}
	}

	/**
	 * Stops the game completely
	 * and puts every player in spectator
	 */
	public static void stop()
	{
		m_timer.cancel();
		for (Player p : Bukkit.getOnlinePlayers())
			p.setGameMode(GameMode.SPECTATOR);
	}
	
	/**
	 * Adds a player to the player list
	 * @param p The player to add
	 * @return A HuntPlayer object corresponding to the player
	 */
	public static HuntPlayer addPlayer(Player p)
	{
		HuntPlayer hp;
		hp = m_playerList.get(p.getName());
		if (hp != null)
			return hp;

		// Unknown player
		hp = new HuntPlayer(p);
		m_playerList.put(p.getName(), hp);
		return hp;
	}
	
	/**
	 * Gets player from playerlist by his name
	 * @param name The player's name
	 * @return HuntPlayer associated with name (null if no player is found)
	 */
	public static HuntPlayer getPlayer(String name)
	{
		return m_playerList.get(name);
	}

	/**
	 * Adds a team
	 * @param color Team's color (muse be unique)
	 * @param name Team's name
	 * @return The newly created team
	 */
	public static Team addTeam(TeamColor color, String name)
	{
		Team team;
		team = m_teamList.get(name);
		if (team != null)
			return team;
		
		team = new Team(color, name);
		m_teamList.put(name, team);
		return team;
	}

	/**
	 * Gets team from name
	 * @param name Team's name
	 * @return The team corresponding to name
	 */
	public static Team getTeam(String name)
	{
		return m_teamList.get(name);
	}

	/**
	 * Deletes team from name
	 * @param name Team's name
	 */
	public static void delTeam(String name)
	{
		Team team = m_teamList.get(name);
		for (HashMap.Entry<String, HuntPlayer> set : m_playerList.entrySet())
		{
			if (set.getValue().getTeam() == team)
				set.getValue().setTeam(null);
		}
		m_teamList.remove(name);
	}
	
	/*
	 * Getters
	 */

	/**
	 * Gets the number of alive players
	 * @return The number of alive players
	 */
	public static int getPlayerNum()
	{
		return m_playerNum;
	}

	/**
	 * Sets the number of alive player
	 * @param playerNum Number of alive player
	 */
	public static void setPlayerNum(final int playerNum)
	{
		m_playerNum = playerNum;
	}

	/**
	 * Gets whether kits are enabled
	 * @return True if kits are enabled, false otherwise
	 */
	public static boolean isKitMode()
	{
		return m_kit;
	}

	/**
	 * Gets whether teams are enabled
	 * @return True if teams are enabled, false otherwise
	 */
	public static boolean isTeamMode()
	{
		return m_team;
	}

	/**
	 * Gets whether island mode is on
	 * @return True if island mode is on, false otherwise
	 */
	public static boolean isIslandMode()
	{
		return m_island;
	}

	/**
	 * Gets whether round mode is on
	 * @return True if round mode is on, false otherwise
	 */
	public static boolean isRoundMode()
	{
		return m_round;
	}

	/**
	 * Gets whether the game has started
	 * @return True if the game has started, false otherwise
	 */
	public static boolean hasStarted()
	{
		return m_started;
	}

	/**
	 * Gets whether the game has ended
	 * @return True if the game has ended, false otherwise
	 */
	public static boolean hasEnded()
	{
		return hasStarted() && m_timer.getMinutes() >= m_gameTime;
	}

	/**
	 * Gets whether the nether is active
	 * @return True if the nether is active, false otherwise
	 * @note Should be checked whenever a player (or an entity) tries to go into the nether
	 */
	public static boolean isNetherActive()
	{
		return m_netherActive && (getMinutes() < (int)(.75f * m_gameTime));
	}

	/**
	 * Gets whether the game is in the hunting phase
	 * @return True if the game is in hunting phase mode
	 * @note Should be used for checking whether damage is enabled or not
	 */
	public static boolean inHunt()
	{
		return hasStarted() && m_timer.getMinutes() >= m_huntTime;
	}

	/**
	 * Gets the number of elapsed minutes since game bagan
	 * @return The number of minutes since game began
	 * @note ```getTime() = getMinutes()*60 + getSeconds()```
	 */
	public static int getMinutes()
	{
		return m_timer.getMinutes();
	}
	/**
	 * Gets the number of elapsed seconds in the current minute
	 * @return The number of seconds in the current minute
	 * @note ```getTime() = getMinutes()*60 + getSeconds()```
	 */
	public static int getSeconds()
	{
		return m_timer.getSeconds();
	}

	/**
	 * Gets the current elapsed time
	 * @return Elapsed time sin game started (in seconds)
	 * @note ```getTime() = getMinutes()*60 + getSeconds()```
	 */
	public static int getTime()
	{
		return m_timer.getTime();
	}

	/**
	 * Gets the border's current size
	 * @return The border current size (rounded down to the nearest integer)
	 */
	public static int getBorderSize()
	{
		return (int)m_borderOverworld.getSize();
	}

	/**
	 * Gets the border's radius size
	 * @return The border current radius (rounded down to the nearest integer)
	 * @note Not the same as ```getBorderSize()/2```
	 */
	public static int getBorderRadius()
	{
		return (int)(0.5*m_borderOverworld.getSize());
	}

	/**
	 * Gets the size of the border at the beginning of the game
	 * @return The border's size when game starts
	 */
	public static int getMaxBorder()
	{
		return m_borderMax;
	}

	/**
	 * Gets the size of the border at the end of the game
	 * @return The border's size when game ends
	 */
	public static int getMinBorder()
	{
		return m_borderMin;
	}

	/**
	 * Gets the game's duration
	 * @return The game's duration (in minutes)
	 */
	public static int getGameTime()
	{
		return m_gameTime;
	}

	/**
	 * Gets the time after which the hunt starts
	 * @return The time after which the hunt starts (in minutes)
	 * @note This is also the duration of the preparation phase
	 */
	public static int getHuntTime()
	{
		return m_huntTime;
	}

	/**
	 * Gets Bukkit's plugin associated with the Hunt plugin
	 * @return The plugin associated with Hunt
	 */
	public static Plugin getPlugin()
	{
		return m_plugin;
	}

	/**
	 * Gets ProtocolLib's ProtocolManager
	 * @return The ProcotolManager
	 */
	public static ProtocolManager getProtocolManager()
	{
		return m_manager;
	}

	/**
	 * Gets the overworld dimension
	 * @return The overworld dimension
	 * @note Meant for faster access
	 */
	public static World getOverworld()
	{
		return m_overworld;
	}
	/**
	 * Gets the nether dimension
	 * @return The nether dimension
	 * @note Meant for faster access
	 */
	public static World getNether()
	{
		return m_nether;
	}

	/**
	 * Gets the list of players
	 * @return The list of players
	 */
	public static HashMap<String, HuntPlayer> getPlayerList()
	{
		return m_playerList;
	}

	/**
	 * Gets the list of teams
	 * @return The list of teams
	 */
	public static HashMap<String, Team> getTeamList()
	{
		return m_teamList;
	}
	
	/*
	 * Setters
	 */
	/**
	 * Sets the border's size
	 * @param max Border size when game begins (diameter in meter)
	 * @param min Border size when game ends (diameter in meter)
	 * @note Border will reduce linearly
	 */
	public static void setBorders(final int max, final int min)
	{
		m_borderMax = max;
		m_borderMin = min;
	}

	/**
	 * Sets the game's durations
	 * @param game Entire game's duration (in minutes)
	 * @param hunt Duration before hunt mode starts i.e preparation time (in minutes)
	 */
	public static void setDurations(final int game, final int hunt)
	{
		m_gameTime = game;
		m_huntTime = hunt;
	}

	/**
	 * Enables/Disables kit mode
	 * @param v true to enable kit mode, false to disable it
	 */
	public static void setKit(final boolean v)
	{
		m_kit = v;
	}

	/**
	 * Enables/Disables team mode
	 * @param v true to enable team mode, false to disable it
	 */
	public static void setTeam(final boolean v)
	{
		m_team = v;
	}

	/**
	 * Enables/Disables island mode
	 * @param v true to enable island mode, false to disable it
	 */
	public static void setIsland(final boolean v)
	{
		m_island = v;
	}

	/**
	 * Enables/Disables round mode
	 * @param v true to enable round mode, false to disable it
	 */
	public static void setRoundMode(final boolean v)
	{
		m_round = v;
	}

	/**
	 * Gets whether the game is in fast mode or not
	 * @return True if game is in fast mode
	 */
	public static boolean isFastMode()
	{
		return m_fast;
	}

	/**
	 * Enable/Disables fast mode
	 * @param v true to enable fast mode, false to disable it
	 */
	public static void setFastMode(final boolean v)
	{
		m_fast = v;
	}

	public static int nextPosInt()
	{
		return m_random.nextInt(65536);
	}
	
	/**
	 * Timer task
	 */
	public static void run()
	{
		// Update border every 20 seconds
		if (getTime() % 20 == 0)
		{
			double factor = getTime() / (m_gameTime*60.0);
			double size = m_borderMax * (1.0 - factor) + m_borderMin * factor;

			m_borderOverworld.setSize(size*2, 20);
			m_borderNether.setSize(size*2, 20);
		}
		
		// Update scoreboard for every players every seconds
		for (HuntPlayer hp : m_playerList.values())
		{
			if (hp.isOnline())
				hp.updateScoreboard();
		}

		// Nether code
		if (getMinutes() == (int)(m_gameTime * .75f) && getSeconds() == 0)
		{
			// Can no longer enter nether by then
			new BukkitRunnable()
			{
				int seconds = 0;
				int ticks = 0;
				final BossBar bar = Bukkit.createBossBar("§c§lFermeture du NETHER!§e 20s...", BarColor.YELLOW, BarStyle.SEGMENTED_20);
				@Override
				public void run()
				{
					if (seconds == 20)
					{
						bar.removeAll();
						this.cancel();
					}
					if (seconds == 5 && ticks % 20 == 0)
					{
						for (Player p : getNether().getPlayers())
						{
							HuntPlayer hp = getPlayer(p.getName());
							if (!hp.isPlaying())
								continue;
							
							if (hp.getLastPortal() != null)
								hp.getPlayer().teleport(hp.getLastPortal());
							hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0));
							hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 4));
							hp.getPlayer().sendMessage("§cIl vous reste 15 secondes pour quitter le nether.");
						}
					}
					if (ticks == 0)
					{
						for (Player p : Bukkit.getOnlinePlayers())
						{
							p.sendMessage("§8{§c☀§8} §7Le nether sera désactivé dans §e20§7 secondes!");
							bar.addPlayer(p);
						}
					}
					bar.setProgress(1.0-ticks/400.0);
					++ticks;
					if (ticks % 20 == 0)
					{
						++seconds;
						bar.setTitle(MessageFormat.format("§c§lFermeture du NETHER!§e {0}s...", 20-seconds));
					}
				}
			}.runTaskTimer(getPlugin(), 0, 1);
		}
		// Kill players still in nether
		if (getMinutes()*60 + getSeconds() >= (int)(m_gameTime * .75f)*60 + 25)
		{
			for (Player p : getNether().getPlayers())
			{
				p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1));
			}
		}


		if (!Game.isRoundMode())
			Normal.run();
		else
			Round.run();
	}

	/**
	 * Clear every player's/team's target
	 */
	public static void clearTargets()
	{
		if (Game.isTeamMode())
		{
			for (Team team : m_teamList.values())
			{
				team.setTarget(null);
				team.setHunter(null);
			}
		}
		else
		{
			for (HashMap.Entry<String, HuntPlayer> set : m_playerList.entrySet())
			{
				set.getValue().setTarget(null);
				set.getValue().setHunter(null);
			}
		}
	}

	/**
	 * Get whether or not a team exists
	 * @param name The team's name
	 * @return true If the team exists, false otherwise
	 */
	public static boolean teamExists(String name)
	{
		return m_teamList.get(name) != null;
	}

	/**
	 * Get whether or not a team color is already taken
	 * @param color The team color
	 * @return true If the team color is taken, false otherwise
	 */
	public static boolean teamColorTaken(TeamColor color)
	{
		for (HashMap.Entry<String, Team> set : m_teamList.entrySet())
		{
			if (set.getValue().getColor() == color)
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Pauses timer
	 */
	public static void pauseTimer()
	{
		m_pause = true;
	}

	/**
	 * Resumes timer
	 */
	public static void resumeTimer()
	{
		m_pause = false;
	}

	/**
	 * Gets whether game is paused or not
	 * @return True if game is paused, false otherwise
	 */
	public static boolean isPaused()
	{
		return m_pause;
	}
}