package org.ef3d0c3e.hunt.kits;

import net.minecraft.world.entity.monster.ZombifiedPiglin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

/**
 * Jean-Baptiste's kit's events
 */
public class KitJbEvents implements Listener
{
	/**
	 * Corrupts world around player & causes lightnings
	 * @param ev Event
	 */
	@EventHandler
	void onPlayerMove(final PlayerMoveEvent ev)
	{
		HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive() || hp.getKit() == null || hp.getKit().getID() != KitID.JB)
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
		if (hp == null || hp.getKit() == null || hp.getKit().getID() != KitID.JB)
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
		if (ev.getCause() != DamageCause.LIGHTNING)
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.JB)
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
		final HuntPlayer hp = Game.getPlayer(ev.getTarget().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.JULIEN)
			return;
		ev.setCancelled(true);
	}
}
