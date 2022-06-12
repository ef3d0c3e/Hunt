package org.ef3d0c3e.hunt.game;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.*;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.stats.StatSaves;
import org.ef3d0c3e.hunt.teams.Team;
import org.ef3d0c3e.hunt.teams.TeamBeacon;

import com.comphenix.protocol.ProtocolManager;

public class Game
{
	static private ProtocolManager m_manager;
	static private Random m_random;

	@Getter @Setter
	static private int playerNum; // Number of alive players
	@Getter @Accessors(fluent = true)
	static private boolean hasStarted;
	static private boolean m_netherActive;
	@Getter @Setter
	static private boolean kitMode;
	@Getter @Setter
	static private boolean teamMode;
	@Getter @Setter
	static private boolean islandMode;
	@Getter @Setter
	static private boolean roundMode;
	@Getter @Setter
	static private boolean fastMode;
	//static private boolean m_swap;

	static private Timer m_timer;
	@Getter
	static private boolean paused; // Pauses timer
	static private Listener pauseListener;
	static private int m_huntTime; // Number of minutes before hunt
	static private int m_gameTime; // Number of minutes before end

	@Getter
	static private World overworld;
	@Getter
	static private WorldBorder overworldBorder;
	@Getter
	static private World nether;
	@Getter
	static private WorldBorder netherBorder;
	
	static private int m_borderStart;
	static private int m_borderMax;
	static private int m_borderMin;

	static private Listener lobbyListener;

	/**
	 * Initialization Hook
	 * @param manager ProtocolManager from Protocollib
	 */
	public static void init(ProtocolManager manager)
	{
		m_manager = manager;
		m_random = new Random(System.currentTimeMillis());
		
		playerNum = 0;
		hasStarted = false;
		m_netherActive = true;
		kitMode = false;
		teamMode = true;
		islandMode = false;
		roundMode = false;
		fastMode = false;

		m_timer = new Timer();
		paused = false;
		pauseListener = new Pause();
		m_huntTime = 0;
		m_gameTime = 60;

		overworld = Bukkit.getWorld("world");
		overworldBorder = overworld.getWorldBorder();
		nether = Bukkit.getWorld("world_nether");
		netherBorder = nether.getWorldBorder();

		m_borderStart = 50;
		m_borderMax = 100;
		m_borderMin = 50;
		overworldBorder.setCenter(0.0, 0.0);;
		overworldBorder.setSize(m_borderStart);
		overworldBorder.setWarningDistance(0);
		netherBorder.setCenter(0.0, 0.0);
		netherBorder.setSize(m_borderStart);
		netherBorder.setWarningDistance(0);
		
		// Overworld
		overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		overworld.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
		overworld.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
		overworld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		overworld.setGameRule(GameRule.DO_INSOMNIA, false);
		overworld.setGameRule(GameRule.SPAWN_RADIUS, 0);
		overworld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		overworld.setTime(6000);
		overworld.setWeatherDuration(0);
		overworld.setDifficulty(Difficulty.PEACEFUL);
		overworld.setSpawnLocation(0, overworld.getHighestBlockYAt(0, 0), 0);
		// Nether
		nether.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		nether.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		nether.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
		nether.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
		nether.setGameRule(GameRule.DO_MOB_SPAWNING, false);
		nether.setGameRule(GameRule.DO_INSOMNIA, false);
		nether.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		nether.setTime(6000);
		nether.setWeatherDuration(0);
		nether.setDifficulty(Difficulty.PEACEFUL);
		
		StatSaves.init();
		Items.init();
		Round.init();
		Fast.init();

		// Events
		Bukkit.getPluginManager().registerEvents(new org.ef3d0c3e.hunt.Events(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new Events(), Hunt.plugin);
		Bukkit.getPluginManager().registerEvents(new IGui.Events(), Hunt.plugin);
		Bukkit.getPluginManager().registerEvents(new HuntPlayer.Events(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new SkinMenu(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new AccessoryMenu.AccessoryMenuEvents(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new KitMenu(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new TeamMenu(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new CmdCompass.CmdCompassEvents(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new CmdInv.CmdInvEvents(), Hunt.plugin);
		//Bukkit.getPluginManager().registerEvents(new StatsMenu(), Hunt.plugin);
		lobbyListener = new Lobby.Events();
		Bukkit.getPluginManager().registerEvents(lobbyListener, Hunt.plugin);
	}

	/**
	 * Start hook
	 * Called when game starts
	 * @note Is responsible for calling other start hooks
	 */
	public static void start()
	{
		HandlerList.unregisterAll(lobbyListener);

		m_timer.runTaskTimer(Hunt.plugin, 0, 20);
		hasStarted = true;
		
		overworldBorder.setDamageBuffer(12); // Player can go safely outside this much
		overworldBorder.setDamageAmount(0.15); // Damage per seconds per blocks
		overworldBorder.setWarningTime(20); // Red screen starts 20 seconds before border reaches player
		overworldBorder.setWarningDistance(5); // Red screen starts 5 blocks around the border
		overworldBorder.setSize(m_borderMax*2);

		netherBorder.setDamageBuffer(12);
		netherBorder.setDamageAmount(0.15);
		netherBorder.setWarningTime(20);
		netherBorder.setWarningDistance(5);
		netherBorder.setSize(m_borderMax*2);

		// Overworld
		overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
		overworld.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
		overworld.setGameRule(GameRule.DO_MOB_SPAWNING, true);
		overworld.setTime(0);
		overworld.setDifficulty(Difficulty.NORMAL);
		// Nether
		nether.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
		nether.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
		nether.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
		nether.setGameRule(GameRule.DO_MOB_SPAWNING, true);
		nether.setTime(0);
		nether.setDifficulty(Difficulty.NORMAL);

		// Start hooks
		if (Game.isKitMode()) // Register
		{
			for (final Kit kit : KitMenu.getKitList())
			{
				Class<? extends Kit> KitClass = kit.getClass();
				try
				{
					Class<?>[] SubClasses = KitClass.getDeclaredClasses();
					for (Class<?> SubClass : SubClasses)
					{
						if (!SubClass.getName().endsWith("$Events"))
							continue;

						final Listener listener = (Listener)SubClass.getDeclaredConstructor().newInstance();
						Bukkit.getPluginManager().registerEvents(listener, Hunt.plugin);
					}
				}
				catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
				{
					e.printStackTrace();
				}

			}
		}
		if (isIslandMode()) // TODO: EV
			Island.start();
		Bukkit.getPluginManager().registerEvents(new Events(), Hunt.plugin);
		Bukkit.getPluginManager().registerEvents(new Combat.Events(), Hunt.plugin);
		if (isTeamMode())
			Bukkit.getServer().getPluginManager().registerEvents(new Team.Events(), Hunt.plugin);

		Bukkit.getPluginManager().callEvent(new GameStartEvent());

		HuntPlayer.forEach(hp ->
		{
			if (!hp.isOnline())
				return;

			hp.setAlive(true);
			hp.setPlaying(true);
			hp.getPlayer().setGameMode(GameMode.SURVIVAL);
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

			Bukkit.getPluginManager().callEvent(new HPSpawnEvent(hp, false));
			++playerNum;
		});

		// Team Spread
		// Only spread one player per team and teleport team members to this player
		if (Game.isTeamMode())
		{
			Team.forEach(team -> {
				AtomicReference<HuntPlayer> target = new AtomicReference<>(null);
				team.forAllPlayers(hp -> {
					if (target.get() == null)
					{
						target.set(hp);
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
							String.format("spreadplayers 0 0 0 %d false %s", m_borderMax - 5, hp.getName()));
					}
					else
					{
						hp.getPlayer().teleport(target.get().getPlayer());
					}
				});
			});
		}

		// Register tracker recipe
		{
			NamespacedKey key = new NamespacedKey(Hunt.plugin, "tracker");
			ShapelessRecipe recipe = new ShapelessRecipe(key, Items.getTracker());
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
			Bukkit.getServer().getPluginManager().registerEvents(new TeamBeacon(), Hunt.plugin);
		}

		// Round events & start
		if (Game.isRoundMode())
		{
			Round.start();
			Bukkit.getServer().getPluginManager().registerEvents(new Round.Events(), Hunt.plugin);
		}
		else
		{
			Bukkit.getServer().getPluginManager().registerEvents(new Normal.Events(), Hunt.plugin);
		}

		// Fast events & start
		if (Game.isFastMode())
		{
			Fast.start();
			Bukkit.getServer().getPluginManager().registerEvents(new Fast.FastEvents(), Hunt.plugin);
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
	

	/*
	 * Getters
	 */

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
		return (int)overworldBorder.getSize();
	}

	/**
	 * Gets the border's radius size
	 * @return The border current radius (rounded down to the nearest integer)
	 * @note Not the same as ```getBorderSize()/2```
	 */
	public static int getBorderRadius()
	{
		return (int)(0.5*overworldBorder.getSize());
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
	 * Gets ProtocolLib's ProtocolManager
	 * @return The ProcotolManager
	 */
	public static ProtocolManager getProtocolManager()
	{
		return m_manager;
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

			overworldBorder.setSize(size*2, 20);
			netherBorder.setSize(size*2, 20);
		}
		
		// Update scoreboard for every players every seconds
		HuntPlayer.forEach(hp ->
		{
			if (hp.isOnline())
				hp.updateScoreboard();
		});

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
							HuntPlayer hp = HuntPlayer.getPlayer(p);
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
			}.runTaskTimer(Hunt.plugin, 0, 1);
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
			Team.forEach(team ->
			{
				team.setTarget(null);
				team.setHunter(null);
			});
		}
		else
		{
			HuntPlayer.forEach(hp -> {
				hp.setTarget(null);
				hp.setHunter(null);
			});
		}
	}

	/**
	 * Pauses timer
	 */
	public static void pauseTimer()
	{
		if (paused)
			return;

		paused = true;
		Bukkit.getPluginManager().registerEvents(pauseListener, Hunt.plugin);
	}

	/**
	 * Resumes timer
	 */
	public static void resumeTimer()
	{
		if (!paused)
			return;

		paused = false;
		HandlerList.unregisterAll(pauseListener);
	}

	public static class Events implements Listener
	{
		// Portal
		@EventHandler
		public void onEntityPortal(final EntityPortalEvent ev)
		{
			if (ev.getFrom().getWorld() != Game.getOverworld()) // You can go back
				return;

			if (ev.getEntity() instanceof Player) // For players
			{
				final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getEntity());
				if (!Game.isNetherActive())
				{
					ev.setCancelled(true);
					hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§cLe nether est désactivé"));
				}
				else // Store portal's location
					hp.setLastPortal(ev.getTo());
			}
			else if (!Game.isNetherActive()) // Prevent other entities
				ev.setCancelled(true);
		}

		// Update tracker when crafted
		@EventHandler
		public void onCraftItem(final CraftItemEvent ev)
		{
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getViewers().get(0).getName());
			if (!ev.getRecipe().getResult().isSimilar(Items.getTracker()))
				return;

			hp.updateTracking(false);
		}

		// Update tracker (paid)
		@EventHandler
		public void onRightClickCompass(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			if (ev.getItem() == null || !ev.getItem().isSimilar(Items.getTracker()))
				return;

			HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (!hp.getPlayer().getInventory().contains(Material.ROTTEN_FLESH) &&
				!hp.getPlayer().getInventory().contains(Material.BONE) &&
				!hp.getPlayer().getInventory().contains(Material.GUNPOWDER) &&
				!hp.getPlayer().getInventory().contains(Material.SPIDER_EYE))
			{
				hp.getPlayer().sendMessage("§cVous n'avez pas les éléments requis pour actualiser votre traqueur!");
				return;
			}

			if (!hp.updateTracking(false))
				return;

			if (hp.getPlayer().getInventory().contains(Material.ROTTEN_FLESH))
				Util.removeSingleItem(hp.getPlayer().getInventory(), Material.ROTTEN_FLESH);
			else if (hp.getPlayer().getInventory().contains(Material.BONE))
				Util.removeSingleItem(hp.getPlayer().getInventory(), Material.BONE);
			else if (hp.getPlayer().getInventory().contains(Material.GUNPOWDER))
				Util.removeSingleItem(hp.getPlayer().getInventory(), Material.GUNPOWDER);
			else if (hp.getPlayer().getInventory().contains(Material.SPIDER_EYE))
				Util.removeSingleItem(hp.getPlayer().getInventory(), Material.SPIDER_EYE);
		}

		// Prevent players from placing down skulls
		@EventHandler
		public void onBlockPlace(BlockPlaceEvent ev)
		{
			if (ev.getBlockPlaced().getType() != Material.PLAYER_HEAD)
				return;

			ev.setCancelled(true);
		}

		// Right click with skull -> show hunter
		@EventHandler
		public void onRightClickSkull(final PlayerInteractEvent ev)
		{
			if (Game.isTeamMode() || Game.isPaused())
				return;
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;

			if (ev.getItem() == null || ev.getItem().getType() != Material.PLAYER_HEAD ||
				!ev.getItem().getItemMeta().getDisplayName().startsWith("§eTête de "))
				return;


			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (Game.isRoundMode() && !hp.getRoundData().isAlive())
			{
				hp.getPlayer().sendMessage("§cVous n'avez pas de chasseur!");
				return;
			}
			if (!hp.getHunter().isOnline())
			{
				hp.getPlayer().sendMessage("§cVotre chasseur est déconnecté!");
				return;
			}
			else if (hp.getHunter().getPlayer().getWorld() != hp.getPlayer().getWorld())
			{
				hp.getPlayer().sendMessage("§cVotre chasseur est dans une autre dimension.");
				return;
			}

			hp.getPlayer().sendMessage(MessageFormat.format("§bVotre chasseur est: §e{0}§b, distance: §a{1}m",
				hp.getHunter().getName(),
				hp.getPlayer().getLocation().distance(hp.getHunter().getPlayer().getLocation())));

			ev.getItem().setAmount(ev.getItem().getAmount()-1);
		}
	}
}