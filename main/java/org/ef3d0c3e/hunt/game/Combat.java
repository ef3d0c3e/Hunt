package org.ef3d0c3e.hunt.game;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.events.HPlayerDamageEvent;
import org.ef3d0c3e.hunt.events.HPlayerDeathEvent;
import org.ef3d0c3e.hunt.events.HPlayerJoinEvent;
import org.ef3d0c3e.hunt.events.HPlayerKilledWrongEvent;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class Combat
{
	static public class Data
	{
		private HuntPlayer lastAttacker;
		private int lastAttacked;

		/**
		 * Maximum time after which lastAttacker will be attributed kill
		 * @return Time in seconds
		 */
		public static int killTime()
		{
			return 10;
		}

		/**
		 * Gets who should be attributed kill
		 * @return Player
		 */
		public HuntPlayer getKiller()
		{
			if (Game.getTime() - lastAttacked >= killTime())
				return null;

			return lastAttacker;
		}

		/**
		 * Makes cp become last damager
		 * @param hp Player that damages
		 */
		public void damagedNow(final HuntPlayer hp)
		{
			lastAttacker = hp;
			lastAttacked = Game.getTime();
		}

		/**
		 * Constructor
		 */
		public Data()
		{
			lastAttacked = - killTime();
			lastAttacker = null;
		}
	}

	public static void updateAttackSpeed(Player p)
	{
		AttributeInstance attr = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		attr.setBaseValue(24);
		p.saveData();
	}

	public static double getDamage(final ItemStack item, double damage)
	{
		switch (item.getType())
		{
			case WOODEN_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case GOLDEN_AXE:
			case DIAMOND_AXE:
			case NETHERITE_AXE:
				return damage / 2.0;
			default:
				return damage;
		}
	}

	public static class Events implements Listener
	{
		@EventHandler
		public void onJoin(HPlayerJoinEvent ev)
		{
			updateAttackSpeed(ev.getPlayer().getPlayer());
		}

		@EventHandler
		public void onPlayerTeleport(PlayerTeleportEvent ev)
		{
			if (ev.getFrom().getWorld() == ev.getTo().getWorld())
				return;
			// World change
			updateAttackSpeed(ev.getPlayer());
		}

		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
		{
			if (ev.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
				return;
			if (!(ev.getDamager() instanceof Player))
				return;
			// Could technically work on other entity types
			Player attacker = (Player)ev.getDamager();
			ev.setDamage(getDamage(attacker.getInventory().getItemInMainHand(), ev.getDamage()));
		}

		@EventHandler(priority = EventPriority.HIGHEST)
		public void onPlayerTakeDamage(final EntityDamageEvent ev)
		{
			if (!(ev.getEntity() instanceof Player))
				return;
			final HuntPlayer victim = HuntPlayer.getPlayer((Player) ev.getEntity());

			if (!Game.inHunt()) // Border's spirit
			{
				ev.setCancelled(true);
				victim.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§3Vous êtes protégé par l'esprit de la border."));
				return;
			}


			HuntPlayer attacker = null;
			if (ev instanceof EntityDamageByEntityEvent)
			{
				final EntityDamageByEntityEvent pev = (EntityDamageByEntityEvent) ev;
				attacker = Util.getPlayerAttacker(pev);

				if (attacker != null && attacker != victim)
				{
					// No friendlyfire
					if (!attacker.canDamage(victim))
					{
						ev.setCancelled(true);
						return;
					}

					// Register
					victim.getCombatData().damagedNow(attacker);

					// Blood
					victim.getPlayer().getWorld().spawnParticle(
						Particle.BLOCK_CRACK,
						victim.getPlayer().getLocation().clone().add(0.0, victim.getPlayer().getHeight() / 1.5, 0.0),
						(int) Math.min(50.0, ev.getFinalDamage() * 8.0),
						0.4, 0.4, 0.4,
						Material.REDSTONE_BLOCK.createBlockData()
					);
				}
			}


			// Would not die
			if (ev.getFinalDamage() < victim.getPlayer().getHealth())
			{
				Bukkit.getPluginManager().callEvent(new HPlayerDamageEvent(victim, attacker, ev, false));
				return;
			}

			// Would die
			ev.setCancelled(true);
			final HuntPlayer killer = victim.getCombatData().getKiller();

			Bukkit.getPluginManager().callEvent(new HPlayerDamageEvent(victim, killer, ev, true));

			if (killer != null && !killer.canKill(victim))
				Bukkit.getPluginManager().callEvent(new HPlayerKilledWrongEvent(victim, killer));
			else
				Bukkit.getPluginManager().callEvent(new HPlayerDeathEvent(victim, killer, true));
		}
	}
}
