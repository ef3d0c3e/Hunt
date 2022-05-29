package org.ef3d0c3e.hunt.player;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.Normal;
import org.ef3d0c3e.hunt.Pair;
import org.ef3d0c3e.hunt.Round;
import org.ef3d0c3e.hunt.events.HPKilledWrongEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.island.IslandData;
import org.ef3d0c3e.hunt.items.HuntItems;
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

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import fr.mrmicky.fastboard.FastBoard;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

// Represents a player, not necessarily connected
public class HuntPlayer implements Listener
{
	private OfflinePlayer m_player;
	
	private boolean m_alive; // Is player alive
	private boolean m_playing; // Is playing in this game
	private int m_score; // Current player's score
	
	private HuntPlayer m_target; // Player hunted by *this*
	private HuntPlayer m_hunter; // Player hunting *this*
	
	private int m_skinId;
	private Kit m_kit; // The player's kit
	private Team m_team; // The player's team
	private IslandData m_island;
	Round.Data m_roundData; // State in current round

	private org.bukkit.scoreboard.Team m_nametagTeam; // For tag
	private FastBoard m_fb;
	
	private HuntPlayer m_lastAttacker; // Who last attacked this
	private int m_lastAttackedTime; // When was this last attacked (in seconds)
	private int m_deathTime; // Last death time (-1 if not set) [used for JB]

	private Location m_lastPortal = null; // Last (overworld) portal entered by player


	private PlayerInteractions.InteractionsData m_interactions = null;
	private HashMap<String, StatValue> m_stats; ///< List of stats associated with player

	public HuntPlayer(Player p)
	{
		m_alive = false;
		m_playing = false;
		m_score = 0;
		m_target = null;
		m_hunter= null;
		m_skinId = -1;
		m_kit = null;
		m_team = null;
		m_island = null;
		m_roundData = new Round.Data();
		m_fb = null;
		m_lastAttacker = null;
		m_lastAttackedTime = 0;
		m_deathTime = -1;
		m_interactions = new PlayerInteractions.InteractionsData();
		m_stats = new HashMap<>();
	}
	
	//-------//
	// Hooks //
	//-------//
	public void onConnect(Player p)
	{
		m_player = Bukkit.getOfflinePlayer(p.getUniqueId());
		m_fb = new FastBoard(p);
		StatSaves.load(this);

		if (!Game.hasStarted())
		{
			p.setGameMode(GameMode.ADVENTURE);
			
			// Skins
			p.getInventory().remove(HuntItems.getSkinSelector());
			p.getInventory().addItem(HuntItems.getSkinSelector());
			// Kits
			if (Game.isKitMode())
			{
				setKit(null);
				p.getInventory().remove(HuntItems.getKitSelector());
				p.getInventory().addItem(HuntItems.getKitSelector());
			}
			// Team
			if (Game.isTeamMode())
			{
				setTeam(null);
				p.getInventory().remove(HuntItems.getTeamSelector());
				p.getInventory().addItem(HuntItems.getTeamSelector());
			}
			// Stats
			p.getInventory().remove(HuntItems.getStatsMenu());
			p.getInventory().addItem(HuntItems.getStatsMenu());
		}
		else if (!isAlive())
			getPlayer().setGameMode(GameMode.SPECTATOR);
		
		// Update player skin
		if (getSkin() != -1)
			setSkin(getSkin());
		
		updateScoreboard();
		updateTablist();
		updateTabname();
		updateNametag();
		updateHealth();

		// Load achievement progress
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				HuntAchievement.getManager().addPlayer(p);
				//HuntAchievement.getManager().loadProgress(p, HuntAchievement.getSave()); TODO
				HuntAchievement.HUNT.award(p, 1);
			}
		}.runTaskLater(Game.getPlugin(), 2);

		// Call interactions hook
		m_interactions.onJoin(this);
	}

	public void onQuit(Player p)
	{
		StatSaves.save(this);
		if (Game.hasStarted())
			return;
		setKit(null);
		setTeam(null);
	}
	
	public void spreadRandom()
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
				String.format("spreadplayers 0 0 0 %d false %s", Math.abs(Game.getBorderRadius() - 10), getName()) );
	}
	
	/**
	 * Called when a player gets damaged by another player
	 * @param ev The damage event
	 * @param attacker Player damaging this
	 * @param direct Whether attacker directly damaged player (set to false if player is taking damage from other source)
	 */
	public void onDamageByPlayer(EntityDamageEvent ev, HuntPlayer attacker, boolean direct)
	{
		if (direct)
			setKiller(attacker);

		if (Game.isRoundMode())
			Round.setLastDamaged(Game.getTime());
		
		// FIXME: P1 and P2 are using JB
		// -> P1 'Kills' P2
		// -> P1 Dies from curse
		// [P2 Should be resuscited and get the curse]
		// -> P2 Gets cursed (as spectator) and nothing happens after the curse ends...
		if (ev.getFinalDamage() >= getPlayer().getHealth())
		{
			ev.setCancelled(true);
			if (attacker.canKill(this))
			{
				HuntPlayer victim = this;
				new BukkitRunnable()
				{
					int seconds = 0;
					Location loc = null;
					public void end()
					{
						if (Game.isKitMode() && KitLanczos.KitLanczosPreDeathHook(victim))
							return;

						// Give player's skull to killer
						PlayerInteractions.giveItem(attacker, new ItemStack[] { HuntItems.getDeathSkull(victim) }, true, true);

						// Set death time
						victim.setDeathTime(Game.getTime());

						if (!Game.isRoundMode() || (attacker.getRoundData().isAlive() && victim.getRoundData().isAlive()))
						{
							attacker.setScore(attacker.getScore() + 3);
							if (Game.isTeamMode())
								attacker.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §7Vous avez tué {0}§7, votre équipe gagne §e3 §7points.", getTeamColoredName()));
							else
								attacker.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §7Vous avez tué §b{0}§7, vous gagnez §e3 §7points.", getName()));
						}

						for (HuntPlayer hp : Game.getPlayerList().values())
						{
							if (hp == attacker)
								continue;
							if (!hp.isAlive() || hp == victim) // Send custom message
							{
								if (hp.isOnline())
								{
									if (Game.isTeamMode())
										hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7.", attacker.getTeamColoredName(), victim.getTeamColoredName()));
									else
										hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §b{0}§7 a tué §b{1}§7.", attacker.getName(), victim.getName()));
								}
								continue;
							}
							if (!hp.isOnline())
								continue;
							if (Game.isTeamMode() && !Game.isRoundMode())
							{
								if (hp.getTeam() == attacker.getTeam())
									hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7, votre équipe gagne §e3 §7point.", attacker.getTeamColoredName(), victim.getTeamColoredName()));
								else if (hp.getTeam() != getTeam())
									hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' {0}§7 a tué {1}§7, tout les équipes en vie gagnent §e1 §7point.", attacker.getTeamColoredName(), victim.getTeamColoredName()));
							}
							else if (!Game.isRoundMode())
							{
								hp.getPlayer().sendMessage(MessageFormat.format("§8'{§c🗡§8}' §b{0}§7 a tué §b{1}§7, tout les joueurs en vie gagnent §e1 §7point.", attacker.getName(), victim.getName()));
								hp.setScore(hp.getScore()+1);
							}
						}
						if (Game.isTeamMode() && !Game.isRoundMode())
						{
							for (Team team : Game.getTeamList().values())
							{
								if (team.isAlive() &&
									team != attacker.getTeam() &&
									team != getTeam())
									team.setScore(team.getScore()+1);
							}
						}

						if (!Game.isRoundMode())
							Normal.onDeath(victim, attacker);
						else
							Round.onDeath(victim, attacker);
					}
					@Override
					public void run()
					{
						if (!Game.isKitMode() || (victim.getKit() == null || !(victim.getKit() instanceof KitJb)))
						{
							end();
							cancel();
							return;
						}
						else if (seconds == 0)
						{
							attacker.getPlayer().sendTitle("§4§lMAUDIT!", "§cSURVIVEZ POUR GAGNER VOTRE KILL...", 5, 100, 20);
							victim.getPlayer().sendTitle("§4§lMorts...", "§7Sauf si votre assassin vient à mourir...", 5, 100, 20);
							
							loc = victim.getPlayer().getLocation();
							victim.getPlayer().setGameMode(GameMode.SPECTATOR);
							loc.getWorld().spawnEntity(loc, EntityType.BAT);
							loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 150, 0.4, 1.3, 0.4);
						
							attacker.setKiller(victim); // Register victim as attacker's killer to grant kill if attacker dies from curse
							attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
							attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));
							attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
						}
						// Kill if disconnect
						else if (attacker.getDeathTime()+1 == Game.getTime() || !attacker.isOnline() || seconds == 5)
						{
							if (attacker.getDeathTime()+1 != Game.getTime() && attacker.isOnline())
							{
								end();
								attacker.getPlayer().sendTitle("§4Chasseur de Vampire!", "§cVous avez survécu à la malédiction!", 5, 100, 20);
							 	attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 1));
							 	attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 4, 0));
							 	attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 1));

								HuntAchievement.VAMPIRE_KILL.award(attacker.getPlayer(), 1);
							}
							else
							{
								victim.getPlayer().teleport(loc);
								victim.getPlayer().sendTitle("§4Dracula!", "§cVous voilà ressuscité.", 5, 100, 20);
								victim.getPlayer().setGameMode(GameMode.SURVIVAL);
							 	victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3, 1));
							 	victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 4, 0));
							 	victim.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 4, 1));
								HuntAchievement.VAMPIRE_REVIVE.award(victim.getPlayer(), 1);

								// Force kill if offline
								if (!attacker.isOnline())
								{
									// FIXME: If player reconnects at round end, then he won't be counted as dead (still alive at end of round)
									PlayerInteractions.damage(attacker, 9999, victim);
								}
							}
							//attacker.setDeathTime(-1); // Resets it
							cancel();
						}
						++seconds;
					}
				}.runTaskTimer(Game.getPlugin(), 0, 20);
			}
			else
			{
				attacker.spreadRandom();
				getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				getPlayer().setFireTicks(0);
				attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 0));
				attacker.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));

				if (Game.isTeamMode())
				{
					getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de vous tuer, {0} §7a fait perdre §e2 §7points à son équipe.", attacker.getTeamColoredName()));
					attacker.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer {0}§7, votre équipe a perdu §e2 §7points.", getTeamColoredName()));
				}
				else
				{
					getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de vous tuer, §b{0} §7a perdu §e2 §7points.", attacker.getName()));
					attacker.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer §b{0}§7, vous avez perdu §e2 §7points.", getName()));
				}
				attacker.getPlayer().playSound(attacker.getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 65536.f, 1.f);
				getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 65536.f, 1.f);

				// Kits Hooks
				if (Game.isKitMode() && attacker.getKit() != null)
					Bukkit.getPluginManager().callEvent(new HPKilledWrongEvent(this, attacker));

				attacker.setScore(attacker.getScore() - 2);
			}
			if (Game.isTeamMode()) // Update scoreboard for team members (even dead members...)
			{
				for (final HuntPlayer hp : attacker.getTeam().getPlayerList())
				{
					if (!hp.isOnline())
						continue;
					hp.updateScoreboard();
					if (hp != attacker)
						hp.getPlayer().sendMessage(MessageFormat.format("§8'{§d☀§8}' §7En essayant de tuer {0}§7, {1}§7 a fait perdre §22 §7points à votre équipe.", getTeamColoredName(), attacker.getTeamColoredName()));
				}
			}
			else
				attacker.updateScoreboard();
		}
	}
	
	public void registerAttack(HuntPlayer attacker)
	{
		if (!Game.inHunt() || attacker == this)
			return;
		m_lastAttacker = attacker;
		m_lastAttackedTime = Game.getTime();
	}
	
	//---------//
	// Getters //
	//---------//
	public OfflinePlayer getOfflinePlayer()
	{
		return m_player;
	}
	
	public Player getPlayer()
	{
		return m_player.getPlayer();
	}
	
	public boolean isOnline()
	{
		return m_player.isOnline();
	}
	
	public String getName()
	{
		return m_player.getPlayer().getName();
	}

	/**
	 * Gets the player name with team color
	 * @return Player's name colored with team color
	 */
	public String getTeamColoredName()
	{
		return Messager.getColored(MessageFormat.format("{0}{1}", m_team.getColor().color, m_player.getPlayer().getName()));
	}

	/**
	 * Gets player's UUID
	 * @return Player's UUID
	 */
	public UUID getUUID()
	{
		return m_player.getPlayer().getUniqueId();
	}

	/**
	 * Gets whether player is alive or not
	 * @return True if player is alive
	 */
	public boolean isAlive()
	{
		return m_alive;
	}

	/**
	 * Gets whether player is part of the game or not
	 * @return True if player is poart of the game
	 */
	public boolean isPlaying()
	{
		return m_playing;
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
	 * @return Player team's score if teams are on, player's score otherwise
	 */
	public int getScore()
	{
		if (Game.isTeamMode())
			return m_team.getScore();
		else
			return m_score;
	}

	/**
	 * Gets player that would be awarded if this was to die
	 * @return The player that would be awarded this' death
	 * @note Returns null if no players would be awarded
	 */
	public HuntPlayer getKiller()
	{
		if (Game.getTime() - m_lastAttackedTime > 8)
			return null;
		return m_lastAttacker;
	}

	/**
	 * Gets whether player and this can kill each other
	 * @param hp The other player
	 * @return True if they can kill each other
	 */
	public boolean canKill(HuntPlayer hp)
	{
		if (Game.isTeamMode())
			return m_team.getTarget() == hp.getTeam() || hp.getTeam().getTarget() == m_team;
		else if (Game.isRoundMode() && !(getRoundData().isGhost() || hp.getRoundData().isGhost()) )
			return true;
		else
			return m_target == hp || hp.m_target == this;
	}

	/**
	 * Gets this' target
	 * @return Player targeted by this
	 * @note Use ```getTeam().getTarget()``` for team mode
	 */
	public HuntPlayer getTarget()
	{
		return m_target;
	}

	/**
	 * Gets this' hunter
	 * @return Player targeting this
	 * @note Use ```getTeam().getHunter()``` for team mode
	 */
	public HuntPlayer getHunter()
	{
		return m_hunter;
	}

	/**
	 * Gets player's custom skin id
	 * @return Player's skin id (-1 for none)
	 */
	public int getSkin()
	{
		return m_skinId;
	}

	/**
	 * Gets player's kit
	 * @return Player's kit
	 */
	public Kit getKit()
	{
		return m_kit;
	}

	/**
	 * Gets player's team
	 * @return Player's team
	 */
	public Team getTeam()
	{
		return m_team;
	}

	/**
	 * Gets player's island data
	 * @return Player's island data
	 */
	public IslandData getIsland()
	{
		return m_island;
	}

	/**
	 * Gets player's round data
	 * @return Player's round data
	 */
	public Round.Data getRoundData()
	{
		return m_roundData;
	}

	/**
	 * Gets time player died
	 * @return Time when player died (in seconds, -1 for not set)
	 */
	public int getDeathTime()
	{
		return m_deathTime;
	}

	/**
	 * Sets time when player died
	 * @param seconds Seconds during which player died
	 */
	public void setDeathTime(final int seconds)
	{
		m_deathTime = seconds;
	}

	/**
	 * Gets player's last portal's location
	 * @return Player's last portal's location
	 */
	public Location getLastPortal()
	{
		return m_lastPortal;
	}

	/**
	 * Get player interactions manager
	 * @return Player Interactions for this playe
	 */
	public PlayerInteractions.InteractionsData getInteractions()
	{
		return m_interactions;
	}

	/**
	 * Gets stats from key name
	 * @param key Key of stat
	 * @return StatValue if found, null otherwise
	 */
	public StatValue getStat(final String key)
	{
		return m_stats.get(key);
	}

	/**
	 * Sets stats from key name
	 * @param key Key of stat
	 * @param value Value of stat
	 */
	public void setStat(final String key, final StatValue value)
	{
		m_stats.put(key, value);
	}

	/**
	 * Increment specific (long) stat
	 * @param key Stat's key
	 */
	public void incStat(final String key)
	{
		final StatLong s = (StatLong)m_stats.get(key);
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
			return Game.inHunt() && !(m_roundData.isGhost() || other.m_roundData.isGhost());
		if (m_team == other.m_team)
			return false;
		else
			return Game.inHunt() && !(m_roundData.isGhost() || other.m_roundData.isGhost());
	}
	
	//---------//
	// Setters //
	//---------//

	/**
	 * Sets player's score
	 * @param score New player's score
	 */
	public void setScore(int score)
	{
		if (Game.isTeamMode())
			m_team.setScore(score);
		else
			m_score = score;
	}

	/**
	 * Sets whether player is alive or not
	 * @param alive Whether player is alive or not
	 */
	public void setAlive(boolean alive)
	{
		m_alive = alive;
		if (Game.isTeamMode())
			m_team.updateAlive();
	}

	/**
	 * Sets whether player is playing or not
	 * @param playing Whether player is playing or not
	 */
	public void setPlaying(boolean playing)
	{
		m_playing = playing;
	}

	/**
	 * Sets player that would be awarded this' death
	 * @param killer Player that would be awarded this' death
	 */
	public void setKiller(HuntPlayer killer)
	{
		m_lastAttacker = killer;
		m_lastAttackedTime = Game.getTime();
	}

	/**
	 * Sets player's target
	 * @param target New player's target
	 */
	public void setTarget(HuntPlayer target)
	{
		m_target = target;
	}

	/**
	 * Sets player's hunter
	 * @param hunter New player's hunter
	 */
	public void setHunter(HuntPlayer hunter)
	{
		m_hunter = hunter;
	}

	/**
	 * Sets player's custom skin id
	 * @param skinId Player's new skin id
	 */
	public void setSkin(int skinId)
	{
		m_skinId = skinId;
		if (m_skinId == -1)
			return;
		Skin skin = Skin.getList().get(m_skinId);
		PropertyMap pm = ((net.minecraft.world.entity.player.Player) ((CraftPlayer)getPlayer()).getHandle() ).getGameProfile().getProperties();
		Property prop = pm.get("textures").iterator().next();
		
		pm.remove("textures", prop);
		pm.put("textures", new Property("textures", skin.getTexture(), skin.getSignature()));
		
		for (Player p : Bukkit.getOnlinePlayers())
		{
			if (p == getPlayer())
				continue;
			p.hidePlayer(Game.getPlugin(), getPlayer());
			p.showPlayer(Game.getPlugin(), getPlayer());
		}

		// Spawn particles
		getPlayer().getWorld().spawnParticle(Particle.REDSTONE, getPlayer().getLocation(), 80, 0.25, 0.80, 0.25, 1.0, new Particle.DustOptions(Color.ORANGE, 1.f));
		getPlayer().getWorld().spawnParticle(Particle.GLOW_SQUID_INK, getPlayer().getLocation(), 25, 0.15, 0.3, 0.15, 0.3);
	}

	/**
	 * Sets player's kit
	 * @param kit Player's new kit
	 */
	public void setKit(Kit kit)
	{
		if (m_kit != null)
			KitMenu.setTaken(m_kit, false);
		m_kit = kit;
		if (m_kit != null)
			KitMenu.setTaken(m_kit, true);
		updateScoreboard();
		updateTabname();
	}

	/**
	 * Sets player's team
	 * @param team Player's new team
	 */
	public void setTeam(Team team)
	{
		m_team = team;
		updateScoreboard();
		updateTabname();
		updateNametag();
	}

	/**
	 * Sets player's island data
	 * @param island Player's new island data
	 */
	public void setIsland(IslandData island)
	{
		m_island = island;
	}

	/**
	 * Sets player's round data
	 * @param data Player's new round data
	 */
	public void setRoundData(Round.Data data)
	{
		m_roundData = data;
	}

	/**
	 * Sets player's last portal's location
	 * @param portal Player's new last portal's location
	 */
	public void setLastPortal(final Location portal)
	{
		m_lastPortal = portal;
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
		m_fb.updateTitle("§b🗡 §6§lHUNT§b 🪓");
		ArrayList<String> l = new ArrayList<String>();
		l.ensureCapacity(6);

		if (Game.hasStarted()) // In game
		{
			if (!Game.isRoundMode())
				l.add(MessageFormat.format("§7En vie: §e{0}", Game.getPlayerNum()));
			else
				l.add(MessageFormat.format("§7En vie: §e{0}", Round.getNumberAlive()));
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
				l.add(MessageFormat.format("§7Round: §d{0}§7/§d{1}", Round.getCurrentRound(), Round.getRounds()));

			if (Game.isRoundMode() && Game.inHunt())
			{
				final Pair<Integer, Integer> left = Round.getRoundTimeLeft();
				String lmins, lsecs;
				lmins = MessageFormat.format("{0}", left.first);
				if (left.second < 10)
					lsecs = MessageFormat.format("0{0}", left.second);
				else
					lsecs = MessageFormat.format("{0}", left.second);

				final Pair<Integer, Integer> prol = Round.getProlongations();
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
				if (m_kit == null && m_team == null)
					l.add("§7Choisissez un §nkit§7 et une §néquipe");
				else
				{
					if (m_kit == null)
						l.add("§7Choisissez un §nkit");
					else
						l.add(MessageFormat.format("§7Kit: §a{0}", m_kit.getDisplayName()));
					if (m_team == null)
						l.add("§7Choisissez une §néquipe");
					else
						l.add(
							Messager.getColored(MessageFormat.format("§7Équipe: {0}", m_team.getColoredName()))
						);
				}
			}
			else if (Game.isKitMode())
			{
				l.add("§7La partie va commencer!");
				if (m_kit == null)
					l.add("§7Choisissez un §nkit");
				else
					l.add(MessageFormat.format("§7Kit: §a{0}", m_kit.getDisplayName()));

			}
			else if (Game.isTeamMode())
			{
				l.add("§7La partie va commencer!");
				if (m_team == null)
					l.add("§7Choisissez une §néquipe");
				else
					l.add(
						Messager.getColored(MessageFormat.format("§7Équipe: {0}", m_team.getColoredName()))
					);
			}
			else
			{
				l.add("§7La partie va commencer!");
			}
		}

		m_fb.updateLines(l);
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
		
		m_player.getPlayer().setPlayerListHeaderFooter(header, footer);
	}

	/**
	 * Update player's name in tab
	 */
	public void updateTabname()
	{
		Player p = m_player.getPlayer();
		String color, prefix, suffix;
		if (Game.hasStarted() && !isAlive()) // Player is dead
			color = "§7§o";
		else
			color = "§e";
		if (Game.isKitMode() && m_kit != null)
			suffix = " §7" + m_kit.getDisplayName();
		else
			suffix = "";
		prefix = "";

		if (Game.isTeamMode() && m_team != null)
		{
			prefix += Messager.getColored(m_team.getColor().color) + "§l" + m_team.getName() + " §8: §r";
			color += Messager.getColored(m_team.getColor().color);
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
		if (m_nametagTeam == null)
		{
			m_nametagTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(getName());
			if (m_nametagTeam == null)
				m_nametagTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(getName());
			m_nametagTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER); // Just set this once
		}

		//TODO: Round+TEAM
		m_nametagTeam.setPrefix("");
		m_nametagTeam.setSuffix("");
		if (Game.isTeamMode())
		{
			if (m_team != null)
			{
				m_nametagTeam.setPrefix(Messager.getColored(m_team.getColor().color) + "§l" + m_team.getName() + " ");
				m_nametagTeam.setColor(ChatColor.DARK_GRAY);
			}
			else
			{
				m_nametagTeam.setPrefix("§7§o<sans équipe> ");
				m_nametagTeam.setColor(ChatColor.DARK_GRAY);
			}
		}
		else
		{
			if (Game.isRoundMode())
			{
				if (getRoundData().isAlive())
				{
					m_nametagTeam.setColor(ChatColor.YELLOW);
					m_nametagTeam.setPrefix("");
				}
				else if (getRoundData().isZombie())
				{
					m_nametagTeam.setColor(ChatColor.GOLD);
					m_nametagTeam.setPrefix("§2§lZOMBIE ");
					m_nametagTeam.setSuffix(" §c☠");
				}
				else if (getRoundData().isGhost())
				{
					m_nametagTeam.setColor(ChatColor.GOLD);
					m_nametagTeam.setPrefix("§8§lFANTÔME ");
					m_nametagTeam.setSuffix(" §c☠");
				}
			}
			else
			{
				m_nametagTeam.setColor(ChatColor.YELLOW);
			}
		}
		
		if (m_nametagTeam.getEntries().isEmpty()) // Should only ever contain a single player
			m_nametagTeam.addEntry(getName());
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
				if (m_team.getTarget() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de cible!");
					return false;
				}

				final HuntPlayer closest = m_team.getTarget().getClosestPlayer(this);
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
				if (m_team.getHunter() == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de chasseur!");
					return false;
				}

				final HuntPlayer closest = m_team.getHunter().getClosestPlayer(this);
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
				if (m_target == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de cible!");
					return false;
				}

				if (!m_target.isOnline())
				{
					getPlayer().sendMessage("§cVotre cible est déconnectée!");
					return false;
				} else if (m_target.getPlayer().getWorld() != getPlayer().getWorld())
				{
					getPlayer().sendMessage("§cVotre cible est dans une autre dimension.");
					return false;
				}

				getPlayer().setCompassTarget(m_target.getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§b§l>>>§e §lDistance§e: {0}m §b§l<<<", getPlayer().getLocation().distance(m_target.getPlayer().getLocation()))));
				return true;
			}
			else
			{
				if (m_hunter == null)
				{
					getPlayer().sendMessage("§cVous n'avez pas de chasseur!");
					return false;
				}

				if (!m_hunter.isOnline())
				{
					getPlayer().sendMessage("§cVotre chasseur est déconnecté!");
					return false;
				}
				else if (m_hunter.getPlayer().getWorld() != getPlayer().getWorld())
				{
					getPlayer().sendMessage("§cVotre chasseur est dans une autre dimension.");
					return false;
				}

				getPlayer().setCompassTarget(m_hunter.getPlayer().getLocation());
				getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§b§l>>>§e §lDistance§e: {0}m §b§l<<<", getPlayer().getLocation().distance(m_hunter.getPlayer().getLocation()))));
				return true;
			}
		}
	}

	public interface ForEach
	{
		void operation(final HuntPlayer hp);
	}

	static public void forEach(final ForEach f)
	{
		for (final HuntPlayer hp : Game.getPlayerList().values())
			f.operation(hp);
	}
}
