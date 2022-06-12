package org.ef3d0c3e.hunt;


import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.events.HPlayerDeathEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;
import org.ef3d0c3e.hunt.teams.Team;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Round
{
	@Getter @Setter
	static private int roundNum = 5; // Total number of rounds
	@Getter @Setter
	static private int currentRound = 0; // Round number
	@Getter
	static private int lastDamaged = 0; // Time a player was damaged for the last time (by another player)
	@Getter @Setter
	static private int prolongations = 0; // Round prolongation (seconds)
	@Getter @Setter
	static private int aliveNum = 0; // Number of ALIVE (state) players

	@Getter @Setter
	static private int roundBegin = 0; // When round began (seconds)

	static private String m_deathMenuName = "§2Choisissez";
	static private Inventory getDeathMenu()
	{
		Inventory inv = Bukkit.createInventory(null, 9, m_deathMenuName);

		ItemStack ghost = new ItemStack(Material.IRON_PICKAXE);
		{
			ItemMeta meta = ghost.getItemMeta();
			meta.setDisplayName("§dFantôme");
			meta.setLore(Arrays.asList(
				"§7Devenez un fantôme utilisez le",
				"§7temps restant pour vous stuffer.",
				"",
				"§7À la fin du round actuel, vous",
				"§7serez §8spread§7 et garderez",
				"§7le stuff que vous avez obtenu.",
				"",
				"§7Si vous mourrez une fois de plus",
				"§7vous serez pénalisé."
			));
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			ghost.setItemMeta(meta);
		}

		ItemStack zombie = new ItemStack(Material.ZOMBIE_HEAD);
		{
			ItemMeta meta = zombie.getItemMeta();
			meta.setDisplayName("§dZombie");
			meta.setLore(Arrays.asList(
				"§7Devenez un zombie et essayez",
				"§7de revenir dans le round.",
				"",
				"§7Vous allez réapparaître §8full",
				"§8fer§7, et devez essayer de tuer",
				"§7un joueur pour récupérer sa §8cible§7",
				"§7et son §8stuff§7.",
				"",
				"§7Au round prochain, vous retrouverez",
				"§7votre ancien stuff et pourrez §8garder",
				"§8le stuff§7 du joueur que vous avez tué."
			));
			zombie.setItemMeta(meta);
		}

		ItemStack leave = new ItemStack(Material.BARRIER);
		{
			ItemMeta meta = leave.getItemMeta();
			meta.setDisplayName("§cQuitter");
			meta.setLore(Arrays.asList("§7Quitter définitivement la partie"));
			leave.setItemMeta(meta);
		}

		inv.setItem(0, ghost);
		inv.setItem(1, zombie);
		inv.setItem(8, leave);
		return inv;
	}


	private static ItemStack m_zombieHelmet, m_zombieChestplate, m_zombieLeggings, m_zombieBoots, m_zombieSword, m_zombiePickaxe;
	/**
	 * Initializes items
	 */
	public static void init()
	{
		m_zombieHelmet = new ItemStack(Material.IRON_HELMET);
		{
			ItemMeta meta = m_zombieHelmet.getItemMeta();
			meta.setDisplayName("§bCasque de Zombie");
			meta.setUnbreakable(true);
			m_zombieHelmet.setItemMeta(meta);
		}

		m_zombieChestplate = new ItemStack(Material.IRON_CHESTPLATE);
		{
			ItemMeta meta = m_zombieChestplate.getItemMeta();
			meta.setDisplayName("§bPlastron de Zombie");
			meta.setUnbreakable(true);
			m_zombieChestplate.setItemMeta(meta);
		}

		m_zombieLeggings = new ItemStack(Material.IRON_LEGGINGS);
		{
			ItemMeta meta = m_zombieLeggings.getItemMeta();
			meta.setDisplayName("§bJambières de Zombie");
			meta.setUnbreakable(true);
			m_zombieLeggings.setItemMeta(meta);
		}

		m_zombieBoots = new ItemStack(Material.IRON_BOOTS);
		{
			ItemMeta meta = m_zombieBoots.getItemMeta();
			meta.setDisplayName("§bBottes de Zombie");
			meta.setUnbreakable(true);
			m_zombieBoots.setItemMeta(meta);
		}

		m_zombieSword = new ItemStack(Material.IRON_SWORD);
		{
			ItemMeta meta = m_zombieSword.getItemMeta();
			meta.setDisplayName("§bÉpée de Zombie");
			meta.setUnbreakable(true);
			m_zombieSword.setItemMeta(meta);
		}

		m_zombiePickaxe = new ItemStack(Material.IRON_PICKAXE);
		{
			ItemMeta meta = m_zombiePickaxe.getItemMeta();
			meta.setDisplayName("§bPioche de Zombie");
			meta.setUnbreakable(true);
			m_zombiePickaxe.setItemMeta(meta);
		}
	}

	/**
	 * Filters items
	 * @param item The item to filter
	 * @return True if item was not filtered, false otherwise
	 */
	public static boolean itemFilter(final ItemStack item)
	{
		return !item.isSimilar(m_zombieHelmet) &&
			!item.isSimilar(m_zombieChestplate) &&
			!item.isSimilar(m_zombieLeggings) &&
			!item.isSimilar(m_zombieBoots) &&
			!item.isSimilar(m_zombieSword) &&
			!item.isSimilar(m_zombiePickaxe) &&
			!item.isSimilar(Items.getZombieTracker());
	}

	public enum RoundState
	{
		ALIVE,
		GHOST,
		ZOMBIE,
	}

	/**
	 * Round data for player
	 */
	public static class Data
	{
		private RoundState m_state;
		private ItemStack[] m_inv; // Store player's inventory on death
		@Getter @Setter
		private int deathTime; // Time when player died
		private int m_lastUpdatedTracker; // Time when player last updated his tracker
		@Getter @Setter
		private boolean inMenu; // Whether player is in death menu or not
		@Getter @Setter
		private boolean stealing; // Whether player is currently stealing or not
		private Location m_freeze; // Freeze location (between rounds)
		private HashMap<Location, Inventory> m_chests; // Stores player's chests

		/**
		 * Constructor
		 */
		public Data()
		{
			m_state = RoundState.ALIVE;
			m_inv = null;
			deathTime = -1;
			m_lastUpdatedTracker = -60;
			inMenu = false;
			stealing = false;
			m_freeze = null;
			m_chests = new HashMap<>();
		}

		/**
		 * Gets player's state
		 * @return Player's state
		 */
		public RoundState getState()
		{
			return m_state;
		}

		/**
		 * Sets player's state
		 * @param state Player's new state
		 */
		public void setState(final RoundState state)
		{
			m_state = state;
		}

		/**
		 * Gets whether the player is alive
		 * @return True if the player is alive
		 */
		public boolean isAlive()
		{
			return m_state == RoundState.ALIVE;
		}

		/**
		 * Gets whether the player is a ghost
		 * @return True if the player is a ghost
		 */
		public boolean isGhost()
		{
			return m_state == RoundState.GHOST;
		}

		/**
		 * Gets whether the player is a zombie
		 * @return True if the player is a zombie
		 */
		public boolean isZombie()
		{
			return m_state == RoundState.ZOMBIE;
		}

		/**
		 * Saves player's inventory
		 * @param inv Player's inventory
		 */
		public void saveInventory(final PlayerInventory inv)
		{
			m_inv = inv.getContents().clone();
		}

		/**
		 * Returns player's inventory
		 * @param hp Player to restore inventory of
		 */
		public void restoreInventory(HuntPlayer hp)
		{
			hp.getPlayer().getInventory().setContents(m_inv);
		}

		/**
		 * Gets time player last updated his zombie tracker
		 * @return The last time player updated his zombie tracker (in seconds)
		 */
		public int getLastUpdatedTracker()
		{
			return m_lastUpdatedTracker;
		}

		/**
		 * Sets time player last updated his zombie tracker
		 * @param seconds The time player last updated his tracker
		 */
		public void setLastUpdatedTracker(final int seconds)
		{
			m_lastUpdatedTracker = seconds;
		}

		/**
		 * Gets player's freeze location
		 * @return Player's freeze location
		 */
		public Location getFreezeLocation()
		{
			return m_freeze;
		}

		/**
		 * Sets player's freeze location
		 * @param location Player's new freeze location
		 */
		public void setFreezeLocation(final Location location)
		{
			m_freeze = location;
		}

		/**
		 * Gets list of chests
		 * @return Lists of player's chests
		 */
		public HashMap<Location, Inventory> getChests()
		{
			return m_chests;
		}

		/**
		 * Adds a chest to list of chests
		 * @return Lists of player's chests
		 */
		public void addChest(final Location loc, final Inventory inv)
		{
			if (m_chests.containsKey(loc))
				m_chests.replace(loc, inv);
			else
				m_chests.put(loc, inv);
		}

	}

	public static class Events implements Listener
	{
		@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
		public void onDeath(final HPlayerDeathEvent ev)
		{
			final HuntPlayer hp = ev.getVictim();
			final HuntPlayer killer = ev.getPlayerKiller();

			hp.setTarget(null);
			hp.setHunter(null);

			onPlayerDeathRevive();
			// Death of non alive players cannot end round
			boolean endRound = getAliveNum() <= 2 && hp.getRoundData().isAlive();
			if (endRound) // End round
				roundEnd();

			if (!hp.getRoundData().isZombie() && !hp.getRoundData().isStealing()) // Store inv
				//NOTE: When stealing, you have to survive until the round ends to keep your stuff
				hp.getRoundData().saveInventory(hp.getPlayer().getInventory());
			hp.getRoundData().setStealing(false);

			// Sound
			if (hp.getRoundData().isAlive() || killer != null)
				for (Player p : Bukkit.getOnlinePlayers())
					p.playSound(p.getLocation(), "minecraft:hunt.astronomia", SoundCategory.MASTER, 65536.f, 1.f);

			// Tomb : Chest containing player's inventory (do it before stealPlayer is called)
			if (hp.getRoundData().isAlive() && (killer == null || killer.getRoundData().isAlive()))
				Util.spawnTomb(hp.getPlayer().getLocation(), hp);

			boolean update = true;
			if (!endRound && hp.getRoundData().isAlive() && killer != null && killer.getRoundData().isZombie())
			{
				update = false; // No need to call updateTargets()
				stealPlayer(hp, killer);
			}
			else if (Game.isKitMode() && hp.getKit() != null)
			{
				hp.getKit().changeOwner(hp, null);
				hp.setKit(null);
			}

			// Island hook
			if (Game.isIslandMode())
				Island.onDeath(hp);

			if (!endRound)
			{
				switch (hp.getRoundData().getState())
				{
					case ALIVE:
						hp.getRoundData().setDeathTime(Game.getTime()); // Save most recent death (as alive)

						if (getCurrentRound() == getRoundNum()) // Must zombie
							makeZombie(hp, true);
						else // Player can choose between zombie & ghost
						{
							hp.getPlayer().openInventory(getDeathMenu());
							hp.getRoundData().setInMenu(true);
							hp.getPlayer().setGameMode(GameMode.SPECTATOR);
							hp.getRoundData().setState(RoundState.GHOST); // Set to ghost to avoir being targeted by shuffle algorithm etc...
						}

						break;
					case ZOMBIE:
						// Regen for killer
						if (killer != null && killer.isOnline())
							killer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 3, true, true));
						makeZombie(hp, false);
						break;
					case GHOST:
						makeGhost(hp, false);
						hp.getPlayer().addPotionEffects(Arrays.asList(
							new PotionEffect(PotionEffectType.CONFUSION, 180, 0, true, false),
							new PotionEffect(PotionEffectType.SLOW, 200, 0, true, false),
							new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 0, true, false)
						));
						break;
				}
			}
			else if (hp.getRoundData().isAlive())
			{
				hp.getRoundData().setDeathTime(Game.getBorderRadius());
			}

			// Heal
			hp.getPlayer().setHealth(hp.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			hp.getPlayer().setFoodLevel(20);
			hp.getPlayer().setSaturation(20);
			hp.getPlayer().setFireTicks(0);
			for (PotionEffect effect : hp.getPlayer().getActivePotionEffects())
				hp.getPlayer().removePotionEffect(effect.getType());

			// Clear
			hp.getCombatData().damagedNow(null);

			if (update && !endRound)
				updateTargets(true);
		}

		/**
		 * Prevent player from closing inventory
		 * @param ev Event
		 */
		@EventHandler
		public void onInventoryClose(InventoryCloseEvent ev) // Reopen
		{
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getPlayer());
			if (!hp.getRoundData().isInMenu() || !ev.getView().getTitle().equals(m_deathMenuName))
				return;

			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					hp.getPlayer().openInventory(getDeathMenu());
					hp.getPlayer().sendMessage("§7Vous devez choisir une option.");
				}
			}.runTaskLater(Hunt.plugin, 1);
		}

		/**
		 * Opens death menu to player if needed
		 * @param ev Event
		 */
		@EventHandler
		public void onJoin(PlayerJoinEvent ev)
		{
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (!hp.getRoundData().isInMenu()) // Force reopen menu
				return;

			ev.getPlayer().openInventory(getDeathMenu());
			hp.getPlayer().setGameMode(GameMode.SPECTATOR);
		}

		/**
		 * Processes click events while in menu
		 * @param ev Event
		 */
		@EventHandler
		public void onInventoryClick(InventoryClickEvent ev)
		{
			if (!ev.getView().getTitle().equals(m_deathMenuName))
				return;

			ev.setCancelled(true);

			HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getWhoClicked());
			if (ev.getCurrentItem() == null || ev.getInventory() == hp.getPlayer().getInventory()) // Make sure player is not clicking his own inventory
				return;

			if (ev.getCurrentItem().getType() == Material.IRON_PICKAXE)
				makeGhost(hp, true);
			else if (ev.getCurrentItem().getType() == Material.ZOMBIE_HEAD)
				makeZombie(hp, true);
			else if (ev.getCurrentItem().getType() == Material.BARRIER)
			{
				hp.setAlive(false);
				hp.getPlayer().setGameMode(GameMode.SPECTATOR);
				if (Game.isTeamMode())
					Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' {0} §7a décidé d''abandonner la partie.", hp.getTeamColoredName()));
				else
					Messager.broadcast(MessageFormat.format("§8'{§d☀§8}' §b{0} §7a décidé d''abandonner la partie.", hp.getName()));
				onPlayerDeathRevive();
			}

			hp.getRoundData().setInMenu(false);
			hp.getPlayer().closeInventory();
		}

		/**
		 * Prevents dragging while in menu
		 * @param ev Event
		 */
		@EventHandler
		public void onInventoryDrag(InventoryDragEvent ev)
		{
			if (!ev.getView().getTitle().equals(m_deathMenuName))
				return;
			ev.setCancelled(true);
		}

		/**
		 * Updates zombie tracker
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null || !ev.getItem().isSimilar(Items.getZombieTracker()))
				return;
			ev.setCancelled(true);

			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (!hp.getRoundData().isZombie()) // Destroy item if player is not a zombie
			{
				ev.getPlayer().sendMessage("§cVous n'êtes pas un zombie!");
				ev.getPlayer().getItemInUse().setAmount(0);
				return;
			}

			if (Game.getTime() - hp.getRoundData().getLastUpdatedTracker() < 60) // Used too recently
			{
				hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new TextComponent(
						MessageFormat.format("§7Votre traqueur n''a pas récupéré! (§c{0}s§7)", 60 - Game.getTime() + hp.getRoundData().getLastUpdatedTracker())
					)
				);
				return;
			}

			// Find closest alive player
			AtomicReference<HuntPlayer> closest = new AtomicReference<>(null);
			AtomicDouble dist = new AtomicDouble(Double.MAX_VALUE);
			HuntPlayer.forEach(o ->
			{
				// Note o != hp (always) because o must be alive while hp must be a zombie
				if (!o.isOnline() || !o.isAlive() || !o.getRoundData().isAlive())
					return;
				if (o.getPlayer().getWorld() != hp.getPlayer().getWorld())
					return;
				if (Game.isTeamMode() && o.getTeam() == hp.getTeam()) // Prevent targeting members of own team
					return;

				final double d = o.getPlayer().getLocation().distanceSquared(hp.getPlayer().getLocation());
				if (d < dist.get())
				{
					dist.set(d);
					closest.set(o);
				}
			});

			if (closest.get() == null) // No player found
			{
				hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new TextComponent(
						"§7Aucun joueur n'a été trouvé..."
					)
				);
				return;
			}

			hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				new TextComponent(
					MessageFormat.format("§7Joueur le plus proche: §c{0}m", Math.sqrt(dist.get()))
				)
			);
			hp.getPlayer().setCompassTarget(closest.get().getPlayer().getLocation());
			hp.getRoundData().setLastUpdatedTracker(Game.getTime());
		}

		/**
		 * Prevents players from breaking locked chests
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockBreak(BlockBreakEvent ev)
		{
			if (ev.getBlock().getType() != Material.CHEST)
				return;

			final Chest chest = (Chest)ev.getBlock().getState();
			if (!chest.isLocked())
				return;

			ev.setCancelled(true);
		}

		/**
		 * Prevents explosions from breaking locked chests
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityExplode(EntityExplodeEvent ev)
		{
			Iterator<Block> it = ev.blockList().iterator();
			while (it.hasNext())
			{
				final Block b = it.next();
				if (b.getType() != Material.CHEST)
					continue;

				final Chest chest = (Chest)b.getState();
				if (!chest.isLocked())
					continue;

				it.remove();
			}
		}

		/**
		 * Open custom chest
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClickChest(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_BLOCK || ev.getClickedBlock().getType() != Material.CHEST)
				return;
			final Chest chest = (Chest)ev.getClickedBlock().getState();
			if (!chest.isLocked())
				return;
			ev.setCancelled(true);

			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (!hp.getRoundData().getChests().containsKey(ev.getClickedBlock().getLocation()))
			{
				hp.getPlayer().sendMessage("§cCe coffre n'est pas à vous!");
				return;
			}

			hp.getPlayer().openInventory(hp.getRoundData().getChests().get(ev.getClickedBlock().getLocation()));
		}
	}

	/**
	 * Sets the last time a player was damaged (by another player)
	 * @param seconds The last time a player was attacked
	 * @note This may modify prolongations
	 */
	public static void setLastDamaged(final int seconds)
	{
		setLastDamaged(seconds);
		if (getRoundTotalTimeLeft() < 30)
			setProlongations(getProlongations() + 15);
	}

	/**
	 * Gets prolongations as minutes and seconds
	 * @return Prolongations as minutes and seconds
	 */
	public static Pair<Integer, Integer> getProlongationsDisplay()
	{
		return new Pair<>(getProlongations() / 60, getProlongations() % 60);
	}

	/**
	 * Gets the duration of each round (except the last)
	 * @return The duration of each round
	 * @note This is theoretical value, *actual* duration may be different
	 */
	public static int getRoundTime()
	{
		return (Game.getGameTime()-Game.getHuntTime())/getRoundNum();
	}

	/**
	 * Gets the duration of the final round
	 * @return The duration of the final round
	 * @note This is theoretical value, *actual* duration may be different
	 */
	public static int getFinalRoundTime()
	{
		return (Game.getGameTime()-Game.getHuntTime()) - getRoundTime()*(getRoundNum()-1);
	}

	/**
	 * Gets time left till rounds ends
	 * @return Seconds before current rounds end (takes prolongations into account)
	 */
	public static int getRoundTotalTimeLeft()
	{
		int duration;
		if (getCurrentRound() == getRoundNum()) // Last round
			duration = Math.min(Game.getGameTime()*60 - getRoundBegin(), getFinalRoundTime()*60); // Use min to avoid super short (or even negative times)
		else
			duration = getRoundTime()*60;
		duration += getProlongations();

		return duration - Game.getTime() + getRoundBegin();
	}

	/**
	 * Gets time left till rounds ends
	 * @return Minutes and seconds before current rounds end (takes prolongations into account)
	 */
	public static Pair<Integer, Integer> getRoundTimeLeft()
	{
		int duration = getRoundTotalTimeLeft();
		return new Pair<>(duration / 60, duration % 60);
	}

	/**
	 * Gets time elapsed since round began
	 * @return Seconds since round began
	 * @note Also works for prep time (aka round 0)
	 */
	public static int getRoundTotalElapsedTime()
	{
		return Game.getTime() - getRoundBegin();
	}

	/**
	 * Gets time elapsed since round began
	 * @return Minutes and seconds since round began
	 * @note Also works for prep time (aka round 0)
	 */
	public static Pair<Integer, Integer> getRoundElapsedTime()
	{
		return new Pair<>(getRoundTotalElapsedTime() / 60, getRoundTotalElapsedTime() % 60);
	}

	/**
	 * Call whenever a player dies or revive (revive means that the player's stage goes back to alive)
	 */
	public static void onPlayerDeathRevive()
	{
		//And if the total count of players goes below 2 that means the game has to stop
		AtomicInteger playerNum = new AtomicInteger(0);
		HuntPlayer.forEach(hp -> {
			if (hp.isAlive())
				playerNum.addAndGet(1);
		});

		Game.setPlayerNum(playerNum.get());
		AtomicInteger alive = new AtomicInteger(0);
		HuntPlayer.forEach(hp -> {
			if (hp.isAlive() && hp.getRoundData().isAlive())
				alive.addAndGet(1);
		});
		setAliveNum(alive.get());

		// Update every player's scoreboard
		HuntPlayer.forEach(hp -> {
			if (hp.isOnline())
				hp.updateScoreboard();
		});
	}

	/**
	 * Hook to call when player dies
	 * @param hp The player
	 * @param killer Killer (may be null)
	 */
	public static void onDeath(HuntPlayer hp, HuntPlayer killer)
	{
		hp.setTarget(null);
		hp.setHunter(null);

		onPlayerDeathRevive();
		boolean endRound = getAliveNum() < 2;
		if (endRound) // End round
			roundEnd();

		if (!hp.getRoundData().isZombie() && !hp.getRoundData().isStealing()) // Store inv
			//NOTE: When stealing, you have to survive until the round ends to keep your stuff
			hp.getRoundData().saveInventory(hp.getPlayer().getInventory());
		hp.getRoundData().setStealing(false);

		// Sound
		if (hp.getRoundData().isAlive() || killer != null)
			for (Player p : Bukkit.getOnlinePlayers())
				p.playSound(p.getLocation(), "minecraft:hunt.astronomia", SoundCategory.MASTER, 65536.f, 1.f);

		// Tomb : Chest containing player's inventory (do it before stealPlayer is called)
		if (hp.getRoundData().isAlive() && (killer == null || killer.getRoundData().isAlive()))
			Util.spawnTomb(hp.getPlayer().getLocation(), hp);

		boolean update = true;
		if (!endRound && hp.getRoundData().isAlive() && killer != null && killer.getRoundData().isZombie())
		{
			update = false; // No need to call updateTargets()
			stealPlayer(hp, killer);
		}
		else if (Game.isKitMode() && hp.getKit() != null)
		{
			hp.getKit().changeOwner(hp, null);
			hp.setKit(null);
		}

		// Island hook
		if (Game.isIslandMode())
			Island.onDeath(hp);

		if (!endRound)
		{
			switch (hp.getRoundData().getState())
			{
				case ALIVE:
					hp.getRoundData().setDeathTime(Game.getTime()); // Save most recent death (as alive)

					if (getCurrentRound() == getRoundNum()) // Must zombie
						makeZombie(hp, true);
					else // Player can choose between zombie & ghost
					{
						hp.getPlayer().openInventory(getDeathMenu());
						hp.getRoundData().setInMenu(true);
						hp.getPlayer().setGameMode(GameMode.SPECTATOR);
						hp.getRoundData().setState(RoundState.GHOST); // Set to ghost to avoir being targeted by shuffle algorithm etc...
					}

					break;
				case ZOMBIE:
					// Regen for killer
					if (killer != null && killer.isOnline())
						killer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 3, true, true));
					makeZombie(hp, false);
					break;
				case GHOST:
					makeGhost(hp, false);
					hp.getPlayer().addPotionEffects(Arrays.asList(
						new PotionEffect(PotionEffectType.CONFUSION, 180, 0, true, false),
						new PotionEffect(PotionEffectType.SLOW, 200, 0, true, false),
						new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 0, true, false)
					));
					break;
			}
		}
		else if (hp.getRoundData().isAlive())
		{
			hp.getRoundData().setDeathTime(Game.getBorderRadius());
		}

		// Heal
		hp.getPlayer().setHealth(hp.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		hp.getPlayer().setFoodLevel(20);
		hp.getPlayer().setSaturation(20);
		hp.getPlayer().setFireTicks(0);
		for (PotionEffect effect : hp.getPlayer().getActivePotionEffects())
			hp.getPlayer().removePotionEffect(effect.getType());

		// Clear
		hp.getCombatData().damagedNow(null);

		if (update && !endRound)
			updateTargets(true);
	}

	/**
	 * Ran when game starts // TODO EV
	 */
	public static void start()
	{
		setAliveNum(Game.getPlayerNum());
	}

	/**
	 * Ran by the timer every seconds
	 */
	public static void run()
	{
		if (Game.getMinutes() == Game.getHuntTime() && Game.getSeconds() == 0) // Hunt just started
		{
			setCurrentRound(getCurrentRound()+1);
			setRoundBegin(Game.getTime());

			updateTargets(false);

			if (!Game.isTeamMode())
			{
				HuntPlayer.forEach(hp -> {
					if (!hp.isOnline())
						return;
					hp.getPlayer().sendTitle("§dDébut du premier round!",
						MessageFormat.format("§9Votre cible est: §b{0}", hp.getTarget().getName()),
						10, 80, 20
					);
					hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Début du premier round! Votre cible est §b{0}§7, tuez la pour gagner §e3 §7points.", hp.getTarget().getName()));
				});
			}
			else
			{
				Team.forEach(team -> {
					team.forAllPlayers((hp) -> {
						if (!hp.isOnline())
							return;
						hp.getPlayer().sendTitle(
							"§dDébut du premier round!",
							MessageFormat.format("§9Vous traquez l''équipe: {0}", team.getTarget().getColoredName()),
							10, 80, 20
						);
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Début du premier round! Vous traquez l''équipe {0}§7, tuez leurs joueurs pour gagner §c3 §7points.", team.getTarget().getColoredName()));
					});
				});
			}
		}
		else if (getCurrentRound() != 0 && getRoundTotalTimeLeft() == 0) // Round end
			roundEnd();
	}

	/**
	 * Call to end current round
	 */
	public static void roundEnd()
	{
		HuntPlayer.forEach(hp -> {
			if (!hp.isAlive())
				return;

			// Clear effects
			PlayerInteractions.schedule(hp, (o) ->
			{
				for (PotionEffect effect : o.getPlayer().getActivePotionEffects())
					o.getPlayer().removePotionEffect(effect.getType());
			});

			// Clear targets
			Game.clearTargets();

			// Clear in menu
			hp.getRoundData().setInMenu(false);

			// Additional point
			if (hp.getRoundData().isAlive())
				hp.setScore(hp.getScore()+1);

			if (hp.isOnline())
			{
				hp.getPlayer().sendTitle(
					MessageFormat.format("§dFin du round {0}!", getCurrentRound()),
					"",
					10, 80, 20
				);
				hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Fin du round {0}! Les joueurs en vie gagnent §e1§7 point.", getCurrentRound()));
			}

			// Kits hook
			if (Game.isKitMode() && hp.getKit() != null)
			{
				hp.getKit().changeOwner(hp, null);
				hp.setKit(null);
			}

			// Island hook TODO EV
			if (Game.isIslandMode())
				Island.onDeath(hp);

			Game.getOverworld().setGameRule(GameRule.NATURAL_REGENERATION, false);
			Game.getOverworld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
			Game.getOverworld().setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
			Game.getOverworld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			Game.getOverworld().setGameRule(GameRule.DO_WEATHER_CYCLE, false);

			// Freeze worldborder (should automatically restart after some time)
			Game.getOverworld().getWorldBorder().setSize(Game.getBorderSize(), 0);
			Game.getNether().getWorldBorder().setSize(Game.getBorderSize(), 0);
		});

		Game.pauseTimer();
		new BukkitRunnable()
		{
			int ticks = 0;
			ArrayList<Pair<Entity, Location>> frozen;
			ArrayList<HuntPlayer> pickOrder; // Order for kit
			int currentPick = 0;

			@Override
			public void run()
			{
				// Clears all interactions
				HuntPlayer.forEach(hp -> {
					PlayerInteractions.clearAll(hp);
				});

				if (ticks == 0)
				{
					// Store every entity (that aren't players) so that we can
					// 'freeze' them in place by repeatedly teleporting them to their former location
					// We also exclude players because players can keep 'appearing'
					// NOTE: We could subtract playercount to it but this would cause
					// issue if players were ever to reach the end dimension
					frozen = new ArrayList<>(Game.getOverworld().getEntities().size() + Game.getNether().getEntities().size());

					Iterator<Entity> it = Game.getOverworld().getEntities().iterator();
					while (it.hasNext())
					{
						final Entity ent = it.next();
						if (ent instanceof Player)
							continue;

						frozen.add(new Pair<>(ent, ent.getLocation()));
					}

					it = Game.getNether().getEntities().iterator();
					while (it.hasNext())
					{
						final Entity ent = it.next();
						if (ent instanceof Player)
							continue;

						frozen.add(new Pair<>(ent, ent.getLocation()));
					}

					if (Game.isKitMode())
					{
						pickOrder = new ArrayList<>(Game.getPlayerNum());
						HuntPlayer.forEach(hp -> {
							if (!hp.isAlive())
								return;

							pickOrder.add(hp);
						});
						pickOrder.sort((p1, p2) ->
						{
							if (p1.getDeathTime() == p2.getDeathTime())
								return 0;
							else if (p1.getDeathTime() == -1)
								return 1;
							else if (p2.getDeathTime() == -1)
								return -1;
							else if (p1.getDeathTime() < p2.getDeathTime())
								return -1;
							else
								return 1;
						});
					}
				}
				HuntPlayer picker = null;
				if (Game.isKitMode())
				{
					if (currentPick < pickOrder.size())
					{
						picker = pickOrder.get(currentPick);
						if (picker.getKit() != null) // Skip
						{
							++currentPick;

							Bukkit.broadcastMessage(MessageFormat.format("§6» §b{0} §7:: §d{1}", picker.getName(), picker.getKit().getDisplayName()));
							if (picker.isOnline() && picker.getPlayer().getOpenInventory() != null)
								picker.getPlayer().closeInventory();
						}
						else if (picker.isOnline() &&
							(picker.getPlayer().getOpenInventory() == null ||
							!(picker.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof KitMenu)))
						{
							if (picker.getPlayer().getOpenInventory() != null && !(picker.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof KitMenu))
								picker.getPlayer().closeInventory();
							picker.getPlayer().openInventory(new KitMenu(picker).getInventory());
							picker.getPlayer().sendMessage("§7Choisissez un kit!");
						}

					}
				}

				if (ticks % 20 == 0)
				{
					AtomicBoolean connected = new AtomicBoolean(true);
					HuntPlayer.forEach(hp -> {
						if (!hp.isAlive())
							return;
						if (!hp.isOnline())
						{
							connected.set(false);
							return;
						}

						if (Game.isKitMode() && currentPick != pickOrder.size())
							connected.set(false);

						hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
							new TextComponent("§7En attente de tous les joueurs")
						);
					});

					if (ticks != 0 && ticks % 200 == 0)
					{
						if (connected.get())
						{
							end();
							return;
						}
						else // Send message
						{
							AtomicReference<String> missing = new AtomicReference<>("");
							HuntPlayer.forEach(hp -> {
								if (hp.isAlive() && !hp.isOnline())
									missing.getAndUpdate(s -> s += "§a" + hp.getName() + "§7, ");
							});

							if (!missing.get().isEmpty())
								Messager.HuntBroadcast("En attente de: " + missing.get().subSequence(0, missing.get().length()-4));
						}
					}
				}

				final Vector zero = new Vector(0, 0, 0);


				// Freeze entities
				for (Pair<Entity, Location> p : frozen)
				{
					p.first.setVelocity(zero);
					p.first.teleport(p.second);
				}

				// Freeze players
				HuntPlayer finalPicker = picker;
				HuntPlayer.forEach(hp -> {
					if (!hp.isOnline() || hp == finalPicker)
						return;
					hp.getPlayer().setVelocity(zero);
					if (hp.getRoundData().getFreezeLocation() == null)
						hp.getRoundData().setFreezeLocation(hp.getPlayer().getLocation());
					else
						hp.getPlayer().teleport(hp.getRoundData().getFreezeLocation());
				});

				++ticks;
			}

			public void end()
			{
				this.cancel();
				Game.getOverworld().setGameRule(GameRule.NATURAL_REGENERATION, true);
				Game.getOverworld().setGameRule(GameRule.DO_MOB_SPAWNING, true);
				Game.getOverworld().setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
				Game.getOverworld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
				Game.getOverworld().setGameRule(GameRule.DO_WEATHER_CYCLE, true);

				// Erase freeze location so that next timer it is not set to some garbage (also gc)
				HuntPlayer.forEach(hp -> {
					hp.getRoundData().setFreezeLocation(null);
				});

				Game.resumeTimer();

				// Begin next round
				setCurrentRound(getCurrentRound()+1);
				setRoundBegin(Game.getTime());
				setProlongations(0);

				if (getCurrentRound() == getRoundNum()+1)
				{
					Messager.HuntBroadcast("Fin de la partie");
					ArrayList<HuntPlayer> sorted = new ArrayList<>();
					HuntPlayer.forEach(hp -> {
						if (hp.isPlaying())
							sorted.add(hp);
					});
					sorted.sort((p1, p2) ->
					{
						if (p1.getScore() == p2.getScore())
							return 0;
						else if (p1.getScore() < p2.getScore())
							return -1;
						else
							return 1;
					});


					return;
				}

				// At this point all players are online
				HuntPlayer.forEach(hp -> {
					if (!hp.isAlive())
						return;

					// Clear some stuff
					hp.getRoundData().setDeathTime(-1);

					makeAlive(hp);
					//if (Game.isKitMode())
					//	hp.getKit().onStart(hp);
					Bukkit.getPluginManager().callEvent(new HPSpawnEvent(hp, false));
				});


				updateTargets(false);
				if (!Game.isTeamMode())
				{
					HuntPlayer.forEach(hp -> {
						if (!hp.isOnline())
							return;
						hp.getPlayer().sendTitle(
							MessageFormat.format("§dDébut du round {0}!", getCurrentRound()),
							MessageFormat.format("§9Votre cible est: §b{0}", hp.getTarget().getName()),
							10, 80, 20
						);
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Début du round {0}! Votre cible est §b{1}§7, tuez la pour gagner §e3 §7points.", getCurrentRound(), hp.getTarget().getName()));
					});
				}
				else
				{
					Team.forEach(team -> {
						team.forAllPlayers((hp) ->
						{
							if (!hp.isOnline())
								return;
							hp.getPlayer().sendTitle(
								MessageFormat.format("§dDébut du round {0}!", getCurrentRound()),
								MessageFormat.format("§9Vous traquez l''équipe: {0}", team.getTarget().getColoredName()),
								10, 80, 20
							);
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Début du round {0}! Vous traquez l''équipe {1}§7, tuez leurs joueurs pour gagner §c3 §7points.", getCurrentRound(), team.getTarget().getColoredName()));
						});
					});
				}
			}
		}.runTaskTimer(Hunt.plugin, 0, 1);
	}

	/**
	 * Updates targets
	 */
	public static void updateTargets(final boolean announce)
	{
		if (!Game.isTeamMode())
		{
			// Fill 'list' with players that have no target or no hunter and should be counted
			java.util.Vector<HuntPlayer> list = new java.util.Vector<>();
			HuntPlayer.forEach(hp -> {
				if (!hp.isAlive() || !hp.getRoundData().isAlive())
					return;

				// No target or dead target
				if (hp.getTarget() == null ||
					!hp.getTarget().isAlive() ||
					!hp.getTarget().getRoundData().isAlive())
					list.add(hp);
					// No hunter or dead hunter
				else if (hp.getHunter() == null ||
					!hp.getHunter().isAlive() ||
					!hp.getHunter().getRoundData().isAlive())
					list.add(hp);
			});
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
					!hp1.getTarget().isAlive() ||
					!hp1.getTarget().getRoundData().isAlive())
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
					if (announce)
					{
						hp1.getPlayer().sendTitle(
							"§9Nouvelle cible!",
							MessageFormat.format("§b{0}", hp2.getName()),
							5, 50, 10
						);
						hp1.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Votre cible est §b{0}§7, tuez la pour gagner §e3 §7points.", hp2.getName()));
					}
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
				!hp1.getTarget().isAlive() ||
				!hp1.getTarget().getRoundData().isAlive())
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
				if (announce)
				{
					hp1.getPlayer().sendTitle(
						"§9Nouvelle cible!",
						MessageFormat.format("§b{0}", hp0.getName()),
						5, 50, 10
					);
					hp1.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Votre cible est §b{0}§7, tuez la pour gagner §e3 §7points.", hp0.getName()));
				}
				hp1.updateScoreboard();
			}
		}
		else
		{
			// Tracking will be done on the closest player (refund cost if player not found...)
			// Also modify winning 'screen' code to display winning teams instead of players
			// Fill 'list' with teams that have no target or no hunter and should be counted
			java.util.Vector<Team> list = new java.util.Vector<>();
			Team.forEach(team -> {
				if (!team.isAlive())
					return;
				if (Game.isRoundMode() && !team.isAliveRound())
					return;

				// No target or dead target
				if (team.getTarget() == null ||
					!team.getTarget().isAlive())
					list.add(team);
					// No hunter or dead hunter
				else if (team.getHunter() == null ||
					!team.getHunter().isAlive())
					list.add(team);
			});
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
					!t1.getTarget().isAlive() ||
					(Game.isRoundMode() && !t1.getTarget().isAliveRound()))
				{
					index = Game.nextPosInt() % list.size();
					Team t2 = list.get(index);
					list.remove(index);

					t1.setTarget(t2);
					t2.setHunter(t1);

					t1.forAllPlayers((hp) -> {
						if (!hp.isAlive() || !hp.isOnline())
							return;
						hp.getPlayer().playSound(
							hp.getPlayer().getLocation(),
							Sound.ENTITY_WITCH_CELEBRATE,
							SoundCategory.MASTER,
							65536.f,
							1.2f
						);
						if (announce)
						{
							hp.getPlayer().sendTitle(
								"§9Nouvelle cible!",
								MessageFormat.format("{0}", t2.getColoredName()),
								5, 50, 10
							);
							hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Vous traquez désormais l''équipe {0}§7, tuez leurs joueurs pour gagner §c3 §7points.", t2.getColoredName()));
						}
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
				!t1.getTarget().isAlive() ||
				(Game.isRoundMode() && !t1.getTarget().isAliveRound()))
			{
				t1.setTarget(t0);
				t0.setHunter(t1);

				t1.forAllPlayers((hp) -> {
					if (!hp.isAlive() || !hp.isOnline())
						return;
					hp.getPlayer().playSound(
						hp.getPlayer().getLocation(),
						Sound.ENTITY_WITCH_CELEBRATE,
						SoundCategory.MASTER,
						65536.f,
						1.2f
					);
					if (announce)
					{
						hp.getPlayer().sendTitle(
							"§9Nouvelle cible!",
							MessageFormat.format("§b{0}", t0.getColoredName()),
							5, 50, 10
						);
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7Vous traquez désormais l''équipe {0}§7, tuez leurs joueurs pour gagner §c3 §7points.", t0.getColoredName()));
					}
					hp.updateScoreboard();
				});
			}
		}
	}

	/**
	 * Steals a player
	 * @victim Player that will be stolen
	 * @attacker Player that will steal
	 * @note victim should always be ALIVE (i.e have a target/hunter) and attacker be a ZOMBIE
	 */
	public static void stealPlayer(HuntPlayer victim, HuntPlayer attacker)
	{
		victim.getRoundData().saveInventory(victim.getPlayer().getInventory());
		attacker.getPlayer().getInventory().setContents(victim.getPlayer().getInventory().getContents());
		attacker.getRoundData().setStealing(true);

		attacker.getRoundData().setState(RoundState.ALIVE);

		attacker.setTarget(victim.getTarget());
		attacker.setHunter(victim.getHunter());
		victim.getTarget().setHunter(attacker);
		victim.getHunter().setTarget(attacker);
		victim.getHunter().updateScoreboard(); // Notify

		victim.setTarget(null);
		victim.setHunter(null);

		victim.updateScoreboard();
		victim.updateNametag();
		victim.updateTabname();
		attacker.updateScoreboard();
		attacker.updateNametag();
		attacker.updateTabname();

		if (Game.isKitMode() && victim.getKit() != null)
		{
			final Kit kit = victim.getKit();
			attacker.setKit(kit);
			kit.changeOwner(victim, attacker);
			victim.setKit(null);
		}

		attacker.getPlayer().sendMessage(MessageFormat.format("§7Vous venez de prendre la place de §a{0}§7!", victim.getName()));
	}

	/**
	 * Turns player into a zombie
	 * @param hp Player to turn into a zombie
	 * @param first Set to true if this is the first time (in current round) the player becomes a zombie
	 */
	public static void makeZombie(HuntPlayer hp, final boolean first)
	{
		hp.getPlayer().setGameMode(GameMode.SURVIVAL);
		hp.getRoundData().setState(RoundState.ZOMBIE);
		hp.getRoundData().setLastUpdatedTracker(-60);

		if (first)
		{
			hp.getPlayer().sendMessage("§7Vous êtes devenu un zombie!");
			hp.getPlayer().sendMessage("§7Tuez un joueur pour récupérer sa cible et son stuff.");
		}

		PlayerInventory inv = hp.getPlayer().getInventory();
		inv.clear();

		if (Game.isIslandMode())
			Island.onStart(hp);

		inv.setHelmet(m_zombieHelmet);
		inv.setChestplate(m_zombieChestplate);
		inv.setLeggings(m_zombieLeggings);
		inv.setBoots(m_zombieBoots);

		inv.addItem(m_zombieSword, m_zombiePickaxe, new ItemStack(Material.CARROT, 24), Items.getZombieTracker());

		if (hp.getPlayer().getWorld() != Game.getOverworld()) // In case player is in another dimension
			hp.getPlayer().teleport(Game.getOverworld().getSpawnLocation());
		hp.spreadRandom();

		hp.updateScoreboard();
		hp.updateNametag();
		hp.updateTabname();
	}

	/**
	 * Turns player into a ghost
	 * @param hp Player to turn into a ghost
	 * @param first Set to true if this is the first time (in current round) the player becomes a ghost
	 */
	public static void makeGhost(HuntPlayer hp, final boolean first)
	{
		hp.getPlayer().setGameMode(GameMode.SURVIVAL);
		hp.getRoundData().setState(RoundState.GHOST);

		if (first)
		{
			hp.getPlayer().sendMessage("§7Vous êtes devenu un fantôme!");
			hp.getPlayer().sendMessage("§7Stuffez-vous librement jusqu'à la fin du round.");
		}

		if (hp.getPlayer().getWorld() != Game.getOverworld()) // In case player is in another dimension
			hp.getPlayer().teleport(Game.getOverworld().getSpawnLocation());
		hp.spreadRandom();

		hp.updateScoreboard();
		hp.updateNametag();
		hp.updateTabname();
	}

	/**
	 * Makes a player alive
	 * @param hp Player to make alive
	 */
	public static void makeAlive(HuntPlayer hp)
	{
		hp.getPlayer().setGameMode(GameMode.SURVIVAL);
		hp.getRoundData().setInMenu(false);
		// Close inventory
		hp.getPlayer().closeInventory();

		//Spread if not alive
		if (!hp.getRoundData().isAlive())
		{
			if (hp.getPlayer().getWorld() != Game.getOverworld()) // In case player is in another dimension
				hp.getPlayer().teleport(Game.getOverworld().getSpawnLocation());
			// TODO: Reunite teams (zombies get teleported to a random team member)
			hp.spreadRandom();
		}

		if (hp.getRoundData().isZombie()) // Restore inventory if zombie
			hp.getRoundData().restoreInventory(hp);
		if (hp.getRoundData().isStealing()) // Restore inventory & drop current inventory on ground if stealing
		{
			hp.getRoundData().setStealing(false);
			Inventory inv = Bukkit.createInventory(hp.getPlayer().getInventory().getHolder(), 45, MessageFormat.format("§cÉquipement du round {0}", getCurrentRound()-1));
			inv.setItem(0, hp.getPlayer().getInventory().getItem(EquipmentSlot.FEET));
			inv.setItem(1, hp.getPlayer().getInventory().getItem(EquipmentSlot.LEGS));
			inv.setItem(2, hp.getPlayer().getInventory().getItem(EquipmentSlot.CHEST));
			inv.setItem(3, hp.getPlayer().getInventory().getItem(EquipmentSlot.HEAD));
			inv.setItem(9, hp.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND));
			// Hotbar
			for (int i = 36; i < 45; ++i)
				inv.setItem(i, hp.getPlayer().getInventory().getItem(i-36));
			// Inventory
			for (int i = 9; i < 36; ++i)
				inv.setItem(i, hp.getPlayer().getInventory().getItem(i));
			hp.getRoundData().addChest(hp.getPlayer().getLocation().getBlock().getLocation(), inv);

			hp.getPlayer().getLocation().getBlock().setType(Material.CHEST);
			Chest chest = (Chest)hp.getPlayer().getLocation().getBlock().getState();
			chest.setLock("§"); // Unobtainable
			chest.update();

			hp.getRoundData().restoreInventory(hp);
		}

		hp.getRoundData().setState(RoundState.ALIVE);

		hp.updateScoreboard();
		hp.updateNametag();
		hp.updateTabname();
	}
}