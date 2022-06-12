package org.ef3d0c3e.hunt.kits;

import net.minecraft.world.entity.monster.ZombifiedPiglin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.HPlayerDeathEvent;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import static org.bukkit.event.EventPriority.LOWEST;

/**
 * Jean-Baptiste's kit
 */
public class KitJb extends Kit
{
	@Override
	public String getName() { return "jb"; }
	@Override
	public String getDisplayName() { return "Jean-Baptiste"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return Items.createGuiItem(Material.WITHER_ROSE, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Corromp le monde autour de lui",
			Kit.itemLoreColor + "╸ Donne hunger en tapant",
			Kit.itemLoreColor + "╸ Donne poison à l'arc",
			Kit.itemLoreColor + "╸ Donne wither en étant tué; et si",
			Kit.itemLoreColor + " mort s'en suit, vous êtes réssuscité"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bDonne hunger lorsqu'il tape.",
				"§c ╸ §bDonne poison à l'arc.",
				"§c ╸ §bImmunisé aux éclairs.",
				"§c ╸ §bCorromp le monde autour de lui lorsqu'il se déplace.",
				"§c ╸ §bLorsqu'il est proche d'un villageois, d'un creeper ou d'un cochon il a une chance de faire s'abbatre la foudre.",
				"§c ╸ §bAprès avoir été tué, le joueur qui vous a tué est maudit, et obtient des effets de potion négatifs. Si il meurt dans les 5 secondes suivantes vous êtes réssuscié.",
				"§c ╸ §bVous n'êtes pas attaqué par les Squelettes, les Zombies et les Piglins Zombifié.",
			}
		};

		return desc;
	}

	public KitJb()
	{
		movePackets = 0;
	}

	int movePackets;

	public static class Events implements Listener
	{
		/**
		 * Corrupts world around player & causes lightnings
		 * @param ev Event
		 */
		@EventHandler
		void onPlayerMove(final PlayerMoveEvent ev)
		{
			HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (!hp.isAlive() || hp.getKit() == null || !(hp.getKit() instanceof KitJb))
				return;

			for (LivingEntity ent : hp.getPlayer().getWorld().getLivingEntities())
			{
				if (ent.getLocation().distanceSquared(hp.getPlayer().getLocation()) > 25.0)
					continue;

				if (Game.nextPosInt() % 500 != 0)
					continue;

				if ((ent instanceof Creeper) ||
					(ent instanceof Villager) ||
					(ent instanceof Pig))
					ent.getWorld().strikeLightning(ent.getLocation());
			}

			// TODO: Make dripstones fall when walk past them
			++((KitJb)hp.getKit()).movePackets;
			if (((KitJb)hp.getKit()).movePackets == 8)
			{
				((KitJb)hp.getKit()).movePackets = 0;

				for (int x = -2; x <= 2; ++x)
				{
					for (int y = -2; y <= 2; ++y)
					{
						for (int z = -2; z <= 2; ++z)
						{
							if (hp.getPlayer().getLocation().getY() + y >= 256.0)
								continue;
							Block b = hp.getPlayer().getWorld().getBlockAt(hp.getPlayer().getLocation().clone().add(x, y, z));

							if (b.getType() == Material.GRASS_BLOCK || b.getType() == Material.DIRT || b.getType() == Material.FARMLAND || b.getType() == Material.DIRT_PATH)
							{
								if (Game.nextPosInt() % 20 == 0)
									b.setType(Material.PODZOL);
								else if (Game.nextPosInt() % 20 == 0)
									b.setType(Material.COARSE_DIRT);
								else if (Game.nextPosInt() % 20 == 0)
									b.setType(Material.MYCELIUM);
								else if (Game.nextPosInt() % 20 == 0)
									b.setType(Material.SAND, false);

								b = hp.getPlayer().getWorld().getBlockAt(b.getLocation().add(0.0, 1.0, 0.0));
								boolean first = true;
								while (b != null && Util.containsMaterial(Util.plantBlocks, b.getType()))
								{
									if (first)
									{
										if (Game.nextPosInt() % 2 == 0)
										{
											switch (Game.nextPosInt() % 4)
											{
												case 0:
													b.setType(Material.RED_MUSHROOM, false);
													break;
												case 1:
													b.setType(Material.BROWN_MUSHROOM, false);
													break;
												case 2:
													b.setType(Material.CRIMSON_FUNGUS, false);
													break;
												case 3:
													b.setType(Material.WARPED_FUNGUS, false);
													break;
											}
										}
										else if (Game.nextPosInt() % 20 == 0)
											b.setType(Material.WITHER_ROSE, false);
										else
											b.setType(Material.DEAD_BUSH, false);
									}
									else
										b.setType(Material.AIR, false);

									first = false;
									b = hp.getPlayer().getWorld().getBlockAt(b.getLocation().add(0.0, 1.0, 0.0));
								}
							}
							else if (b.getType() == Material.SAND || b.getType() == Material.RED_SAND || b.getType() == Material.GRAVEL)
							{
								if (Game.nextPosInt() % 15 == 0)
									b.setType(Material.SOUL_SAND);
								else if (Game.nextPosInt() % 15 == 0)
									b.setType(Material.SOUL_SOIL);
								else if (Game.nextPosInt() % 15 == 0)
									b.setType(Material.END_STONE);

								b = hp.getPlayer().getWorld().getBlockAt(b.getLocation().add(0.0, 1.0, 0.0));
								boolean first = true;
								while (b != null && Util.containsMaterial(Util.plantBlocks, b.getType()))
								{
									if (first)
									{
										if (Game.nextPosInt() % 2 == 0)
										{
											switch (Game.nextPosInt() % 4)
											{
												case 0:
													b.setType(Material.RED_MUSHROOM, false);
													break;
												case 1:
													b.setType(Material.BROWN_MUSHROOM, false);
													break;
												case 2:
													b.setType(Material.CRIMSON_FUNGUS, false);
													break;
												case 3:
													b.setType(Material.WARPED_FUNGUS, false);
													break;
											}
										}
										else if (Game.nextPosInt() % 10 == 0)
											b.setType(Material.WITHER_ROSE, false);
										else
											b.setType(Material.DEAD_BUSH, false);
									}
									else
										b.setType(Material.AIR, false);

									first = false;
									b = hp.getPlayer().getWorld().getBlockAt(b.getLocation().add(0.0, 1.0, 0.0));
								}
							}
							else if (b.getType() == Material.ICE)
							{
								if (Game.nextPosInt() % 7 == 0)
									b.setType(Material.FROSTED_ICE);
							}
							else if (b.getType() == Material.PACKED_ICE)
							{
								if (Game.nextPosInt() % 8 == 0)
									b.setType(Material.ICE);
							}
							else if (b.getType() == Material.BLUE_ICE)
							{
								if (Game.nextPosInt() % 9 == 0)
									b.setType(Material.PACKED_ICE);
							}
							else if (Util.containsMaterial(Util.logBlocks, b.getType()))
							{
								if (Game.nextPosInt() % 50 == 0)
									b.setType(Material.STONE);
								else if (Game.nextPosInt() % 40 == 0)
									b.setType(Material.MUSHROOM_STEM);
							}
							else if (Util.containsMaterial(Util.leaveBlocks, b.getType()))
							{
								if (Game.nextPosInt() % 80 == 0)
									b.setType(Material.COBWEB);
								else if (Game.nextPosInt() % 10 == 0)
									b.setType(Material.BROWN_MUSHROOM_BLOCK);
								else if (Game.nextPosInt() % 10 == 0)
									b.setType(Material.RED_MUSHROOM_BLOCK);
								else if (Game.nextPosInt() % 30 == 0)
									b.setType(Material.AIR);
							}
						}
					}
				}
			}
		}

		/**
		 * Applies hunger when hitting an entity & poison when shooting
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
		{
			if (!(ev.getEntity() instanceof LivingEntity))
				return;
			if (!Game.inHunt() && (ev.getEntity() instanceof Player))
				return;
			final HuntPlayer hp = Util.getPlayerAttacker(ev);
			if (hp == null || hp.getKit() == null || !(hp.getKit() instanceof KitJb))
				return;

			switch (ev.getCause())
			{
				case ENTITY_ATTACK:
					((LivingEntity)ev.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0));
					break;
				case PROJECTILE:
					((LivingEntity)ev.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
					break;
				default:
					break;
			}
		}

		/**
		 * Immune to lightning strikes
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDamage(EntityDamageEvent ev)
		{
			if (!(ev.getEntity() instanceof Player))
				return;
			if (ev.getCause() != EntityDamageEvent.DamageCause.LIGHTNING)
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getEntity());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitJb))
				return;

			ev.setCancelled(true);
		}

		/**
		 * Prevents Zombies, Skeletons & Zoglins from targeting JB
		 * @param ev
		 */
		@EventHandler
		public void onEntityTarget(EntityTargetLivingEntityEvent ev)
		{
			if (!(ev.getTarget() instanceof Player) ||
				!((ev.getEntity() instanceof Zombie) && (ev.getEntity() instanceof Skeleton) && (ev.getEntity() instanceof ZombifiedPiglin)))
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getTarget());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitJb))
				return;
			ev.setCancelled(true);
		}

		/**
		 * Curse: When a player kills jb, jb gets put into a statis and killer
		 * gets cursed. After 5 seconds the statis ends; if killer has died,
		 * jb is revived otherwise jb is killed and killer gets the reward
		 * @param ev Event
		 */
		@EventHandler(ignoreCancelled = true, priority = LOWEST)
		public void onDeath(final HPlayerDeathEvent ev)
		{
			if (ev.getVictim().getKit() == null || !(ev.getVictim().getKit() instanceof KitJb))
				return;
			if (ev.getPlayerKiller() == null || !(ev.isCancellable()))
				return;
			ev.setCancelled(true);

			new BukkitRunnable()
			{
				int seconds = 0;
				Location loc;

				@Override
				public void run()
				{
					if (seconds == 0)
					{
						ev.getPlayerKiller().getPlayer().sendTitle("§4§lMAUDIT!", "§cSURVIVEZ POUR GAGNER VOTRE KILL...", 5, 100, 20);
						ev.getVictim().getPlayer().sendTitle("§4§lMorts...", "§7Sauf si votre assassin vient à mourir...", 5, 100, 20);

						loc = ev.getVictim().getPlayer().getLocation();
						ev.getVictim().getPlayer().setGameMode(GameMode.SPECTATOR);
						loc.getWorld().spawnEntity(loc, EntityType.BAT);
						loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 150, 0.4, 1.3, 0.4);

						ev.getPlayerKiller().getCombatData().damagedNow(ev.getVictim()); // Victim will get awarded for killer's death
						ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
						ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));
						ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
					}
					else if (ev.getPlayerKiller().getDeathTime()+1 == Game.getTime() || !ev.getPlayerKiller().isOnline() || seconds == 5)
					{
						if (ev.getPlayerKiller().getDeathTime()+1 != Game.getTime() && ev.getPlayerKiller().isOnline())
						{
							ev.getPlayerKiller().getPlayer().sendTitle("§4Chasseur de Vampire!", "§cVous avez survécu à la malédiction!", 5, 100, 20);
							ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5, 1));
							ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 4, 0));
							ev.getPlayerKiller().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 1));

							HuntAchievement.VAMPIRE_KILL.award(ev.getPlayerKiller().getPlayer(), 1);
						}
						else
						{
							ev.getVictim().getPlayer().teleport(loc);
							ev.getVictim().getPlayer().sendTitle("§4Dracula!", "§cVous voilà ressuscité.", 5, 100, 20);
							ev.getVictim().getPlayer().setGameMode(GameMode.SURVIVAL);
							ev.getVictim().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 3, 1));
							ev.getVictim().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 4, 0));
							ev.getVictim().getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 4, 1));
							HuntAchievement.VAMPIRE_REVIVE.award(ev.getVictim().getPlayer(), 1);

							// Force kill if offline
							if (!ev.getPlayerKiller().isOnline())
							{
								// FIXME: If player reconnects at round end, then he won't be counted as dead (still alive at end of round)
								Bukkit.getPluginManager().callEvent(new HPlayerDeathEvent(ev.getVictim(), ev.getPlayerKiller(), false));
							}
						}
						//ev.getPlayerKiller().setDeathTime(-1); // Resets it
						cancel();
					}
					++seconds;
				}
			}.runTaskTimer(Hunt.plugin, 0, 20);
		}
	}
}
