package org.ef3d0c3e.hunt.game;

import java.text.MessageFormat;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import  org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.Normal;
import org.ef3d0c3e.hunt.Round;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.kits.KitLanczos;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.spigotmc.event.entity.EntityMountEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class Events implements Listener
{
	@EventHandler
	public static void onPlayerJoin(PlayerJoinEvent ev)
	{
		Player p = ev.getPlayer();
		
		ev.setJoinMessage(Messager.getColored(MessageFormat.format("§8[§a+§8] §7{0}", p.getName())));
		HuntPlayer hp = Game.addPlayer(p);
		hp.onConnect(p);
	}
	
	@EventHandler
	public static void onPlayerQuit(PlayerQuitEvent ev)
	{
		Player p = ev.getPlayer();
		
		ev.setQuitMessage(Messager.getColored(MessageFormat.format("§8[§c-§8] §7{0}", p.getName())));
		HuntPlayer hp = Game.getPlayer(p.getName());
		hp.onQuit(p);
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
		for (final HuntPlayer other : Game.getPlayerList().values())
		{
			if (other.getTeam() == hp.getTeam())
				other.getPlayer().sendMessage(formatted);
		}
	}

	@EventHandler
	public static void onChat(AsyncPlayerChatEvent ev)
	{
		HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
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

	@EventHandler
	public static void onEntityTarget(EntityTargetEvent ev)
	{
		if (Game.hasStarted() && !Game.isPaused())
			return;

		ev.setCancelled(true);
		return;
	}

	@EventHandler
	public static void onFoodLevelChange(FoodLevelChangeEvent ev)
	{
		if (Game.hasStarted() && !Game.isPaused())
			return;

		ev.setCancelled(true);
		return;
	}

	@EventHandler
	public static void onEntityAirChange(EntityAirChangeEvent ev)
	{
		if (Game.hasStarted() && !Game.isPaused())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public static void onEntityMount(EntityMountEvent ev)
	{
		if (Game.isPaused())
		{
			ev.setCancelled(true);
			return;
		}

		if (Game.hasStarted())
			return;
		if (!(ev.getEntity() instanceof Player))
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public static void onPlayerInteractEntity(PlayerInteractEntityEvent ev)
	{
		if (Game.isPaused())
		{
			ev.setCancelled(true);
			return;
		}
		if (Game.hasStarted() || ev.getPlayer().isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public static void onPlayerInteractEvent(PlayerInteractEvent ev)
	{
		if (Game.isPaused())
		{
			ev.setCancelled(true);
			return;
		}

		if (Game.hasStarted() || ev.getPlayer().isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public static void onEntityPickupItem(EntityPickupItemEvent ev)
	{
		if (Game.isPaused())
		{
			ev.setCancelled(true);
			return;
		}

		if (Game.hasStarted())
			return;
		Entity ent = ev.getEntity();
		if (ent instanceof Player && ((Player)ent).isOp())
			return;

		ev.setCancelled(true);
	}
	
	@EventHandler
	public static void onPlayerDropItem(PlayerDropItemEvent ev)
	{
		if (Game.hasStarted() && !Game.isPaused()) // In game & not paused
			return;

		ev.setCancelled(true);
	}
	
	@EventHandler
	public static void onEntityDamage(EntityDamageEvent ev)
	{
		if (!Game.hasStarted() || Game.isPaused()) // Lobby or paused
		{
			ev.setCancelled(true);
			return;
		}
		
		if (!(ev.getEntity() instanceof Player))
			return;
		
		if (!Game.inHunt()) // Border's spirit :^)
		{
			ev.setCancelled(true);
			((Player)ev.getEntity()).spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§3Vous êtes protégé par l'esprit de la border."));
			return;
		}
		
		if (ev instanceof EntityDamageByEntityEvent) // onEntityDamageByEntity will be called instead
		{
			EntityDamageByEntityEvent pev = (EntityDamageByEntityEvent)ev;
			
			if ((pev.getDamager() instanceof Player) && ((Player)pev.getDamager()) != ev.getEntity())
				return;
			if ((pev.getDamager() instanceof Projectile) &&
				(((Projectile)pev.getDamager()).getShooter() instanceof Player) &&
				((Player)((Projectile)pev.getDamager()).getShooter()) != ev.getEntity())
				return;
		}

		HuntPlayer victim = Game.getPlayer(((Player)ev.getEntity()).getName());
		if (victim.getKiller() != null)
		{
			victim.onDamageByPlayer(ev, victim.getKiller(), false);
			return;
		}

		if (ev.getFinalDamage() < victim.getPlayer().getHealth())
			return;

		ev.setCancelled(true);
		if (Game.isKitMode() && KitLanczos.KitLanczosPreDeathHook(victim))
			return;
		if (Game.isTeamMode())
			Messager.broadcast(MessageFormat.format("§8'{§c☠§8}' {0}§7 est mort.", victim.getTeamColoredName()));
		else
			Messager.broadcast(MessageFormat.format("§8'{§c☠§8}' §b{0}§7 est mort.", victim.getName()));
		if (!Game.isRoundMode())
			Normal.onDeath(victim, null);
		else
			Round.onDeath(victim, null);
		if (!Game.isRoundMode())
		{
			if (Game.isTeamMode() && victim.getTeam().isAlive())
				Messager.HuntMessage(victim.getPlayer(), "Vous êtes mort, mais votre équipe peut encore vous ressusciter.");
			else
				Messager.HuntMessage(victim.getPlayer(), "Vous êtes mort, vous pouvez observer librement la partie.");
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public static void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
	{
		if (!Game.hasStarted() || Game.isPaused()) // Lobby or paused
			return;
		if (!(ev.getEntity() instanceof Player))
			return;

		HuntPlayer victim = Game.getPlayer(((Player)ev.getEntity()).getName());
		Entity atk = ev.getDamager();
		HuntPlayer hatk = null;
		if (atk != null)
			if (atk instanceof Projectile)
				if (((Projectile)atk).getShooter() instanceof Entity)
					atk = (Entity)((Projectile)atk).getShooter();
		
		if (atk == null || !(atk instanceof Player))
		{
			if (victim.getKiller() != null)
				hatk = victim.getKiller();
			else
				return;
		}
		else
			hatk = Game.getPlayer(((Player)atk).getName());
		
		// FIXME: Edge case: victim is killed by arrow, and attacker died right before arrow hit (could also be by using an arrow stored on a block...)
		// This may break with kit jb as jb would be instantly revived and attacker would get effects (even though nothing should happen because in spectator, or worse if disconnected)
		if (hatk != victim)
		{
			if (hatk.canDamage(victim))
				victim.onDamageByPlayer(ev, hatk, true);
			else
				ev.setCancelled(true);
		}
	}

	// Events
	@EventHandler
	public void onEntityPortal(EntityPortalEvent ev)
	{
		if (!Game.hasStarted() || Game.isPaused()) // Lobby or paused
		{
			ev.setCancelled(true);
			return;
		}

		if (ev.getFrom().getWorld() != Game.getOverworld()) // You can go back
			return;

		if (ev.getEntity() instanceof Player) // For players
		{
			final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
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

	// Update tracker on craft
	@EventHandler
	public void onCraftItem(CraftItemEvent ev)
	{
		HuntPlayer hp = Game.getPlayer(ev.getViewers().get(0).getName());
		if (!ev.getRecipe().getResult().isSimilar(HuntItems.getTracker()))
			return;
		
		hp.updateTracking(false);
	}

	// Update tracker (paying)
	@EventHandler
	public void onRightClickCompass(PlayerInteractEvent ev)
	{
		if (!Game.hasStarted())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (ev.getItem() == null || !ev.getItem().isSimilar(HuntItems.getTracker()))
			return;
	
		HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
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
		if (!Game.hasStarted())
			return;
		if (ev.getBlockPlaced().getType() != Material.PLAYER_HEAD)
			return;

		ev.setCancelled(true);
	}

	// Skull right click
	@EventHandler
	public void onRightClickSkull(PlayerInteractEvent ev)
	{
		if (!Game.hasStarted() || Game.isTeamMode())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (ev.getItem() == null || ev.getItem().getType() != Material.PLAYER_HEAD ||
			!ev.getItem().getItemMeta().getDisplayName().startsWith("§eTête de "))
			return;


		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
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
