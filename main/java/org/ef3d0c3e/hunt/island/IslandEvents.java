package org.ef3d0c3e.hunt.island;

import net.minecraft.world.item.alchemy.Potion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.KitHasagi;
import org.ef3d0c3e.hunt.kits.KitID;
import org.ef3d0c3e.hunt.kits.KitTom;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.awt.desktop.QuitEvent;

public class IslandEvents implements Listener
{
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent ev)
	{
		if (!Util.isSimilarBasic(ev.getItemDrop().getItemStack(), Island.getGrapple()))
			return;
		ev.setCancelled(true);

		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive())
			return;
		hp.getIsland().reset();
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive())
			return;
		hp.getIsland().reset();
	}

	// Prevent right clicking on slime
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent ev)
	{
		if (!(ev.getRightClicked() instanceof Slime))
			return;
		if (!((Slime) ev.getRightClicked()).isLeashed())
			return;
		ev.setCancelled(true);
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (Game.isPaused())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null)
			return;
		if (!Util.isSimilarBasic(ev.getItem(), Island.getGrapple()))
			return;
		ev.setCancelled(true);

		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		hp.getIsland().reset();

		ArmorStand hook = (ArmorStand)hp.getPlayer().getWorld().spawnEntity(hp.getPlayer().getLocation().add(hp.getPlayer().getLocation().getDirection().normalize().multiply(2)), EntityType.ARMOR_STAND);
		hook.setInvulnerable(true);
		hook.setGravity(false);
		hook.setSilent(true);
		hook.setSmall(true);
		hook.setMarker(true);
		hook.getEquipment().setItem(EquipmentSlot.HEAD, Island.getHookModel());
		hook.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
		hook.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
		hook.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
		hook.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

		Slime slime = (Slime) hp.getPlayer().getWorld().spawnEntity(hp.getPlayer().getLocation().add(hp.getPlayer().getLocation().getDirection().normalize().multiply(2)), EntityType.SLIME);
		slime.setInvulnerable(true);
		slime.setGravity(false);
		slime.setSilent(true);
		slime.setSize(0);
		slime.setRemoveWhenFarAway(false);
		slime.setPersistent(true);
		// If we set NoAi, we can't move the slime
		slime.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 99999, 50, false, false));
		slime.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 50, false, false));

		slime.setLeashHolder(hp.getPlayer());


		// We use an arrow because it can stick into a wall
		Arrow arrow = (Arrow)hp.getPlayer().getWorld().spawnEntity(hp.getPlayer().getEyeLocation().add(hp.getPlayer().getLocation().getDirection().normalize().multiply(3)), EntityType.ARROW);
		arrow.setGravity(false);
		arrow.setInvulnerable(true);

		slime.setInvisible(true);
		hook.setInvisible(true);
		arrow.addPassenger(hook);


		final float pitch = hp.getPlayer().getLocation().getPitch();
		final float yaw = hp.getPlayer().getLocation().getYaw();
		final Vector dir = hp.getPlayer().getLocation().getDirection().normalize().multiply(1.5);
		arrow.setVelocity(dir);
		slime.setVelocity(dir);

		hp.getIsland().hook = hook;
		hp.getIsland().arrow = arrow;
		hp.getIsland().slime = slime;
		hp.getIsland().reel = new BukkitRunnable()
		{
			Location lastPos;
			int ticks = 0;
			@Override
			public void run()
			{
				if (ticks == 50 || Game.isPaused())
				{
					hook.remove();
					slime.remove();
					arrow.remove();
					this.cancel();
					return;
				}
				if (arrow != null && arrow.isOnGround())
				{
					slime.setVelocity(new Vector(0.0, 0.0, 0.0));
					hook.setVelocity(new Vector(0.0, 0.0, 0.0));
					arrow.remove();

					slime.teleport(hook.getLocation().add(0.0, 0.32, 0.0).add(dir.clone().multiply(-0.4)));
					slime.setAI(false);
					hook.setAI(false);
					hook.setMarker(true);


					this.cancel();
					// Apply physics to player
					hp.getIsland().reelBack = new BukkitRunnable()
					{
						@Override
						public void run()
						{
							if (Game.isPaused())
								hp.getIsland().reset();

							final double dsq = hp.getPlayer().getLocation().distanceSquared(hook.getLocation());
							Vector dir = hook.getLocation().subtract(hp.getPlayer().getLocation()).toVector().normalize();
							if (hp.getPlayer().isSneaking())
							{
								return;
							}

							final double factor = Math.max(0.0, 1.0 - 1.0/(dsq / 10.0 + 0.90))*0.2;
							hp.getPlayer().setVelocity(hp.getPlayer().getVelocity().add(dir.multiply(new Vector(factor, factor*1.1, factor))));

							hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_STEP, 0.7f, 1.f);
						}
					};
					hp.getIsland().reelBack.runTaskTimer(Game.getPlugin(), 0, 1);
					return;
				}

				arrow.setVelocity(dir);
				slime.setVelocity(dir);
				if (ticks != 0)
				{
					Location l = lastPos.add(0.0, 0.4, 0.0);
					l.setPitch(pitch);
					l.setYaw(yaw);
					slime.teleport(l);
				}
				lastPos = arrow.getLocation();
				++ticks;
			}
		};
		hp.getIsland().reel.runTaskTimer(Game.getPlugin(), 1, 1);
	}

	// No fall damage when reeling (and reduced otherwise)
	@EventHandler
	public void onEntityDamage(EntityDamageEvent ev)
	{
		if (!(ev.getEntity() instanceof Player))
			return;
		if (ev.getCause() != EntityDamageEvent.DamageCause.FALL)
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
		ev.setCancelled(true);
		if (hp.getIsland().reelBack == null || hp.getIsland().reelBack.isCancelled())
			ev.setCancelled(true);
		else
		{
			ev.setDamage(ev.getDamage() * 0.20);
			if (ev.getDamage()+2.0 > hp.getPlayer().getHealth())
				ev.setCancelled(true);
		}
	}

	// Player can't get shot by arrow
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev)
	{
		if (!(ev.getEntity() instanceof Arrow))
			return;
		if (ev.getHitEntity() == null) // Block hit events should happen normally...
			return;
		final Arrow arrow = (Arrow)ev.getEntity();
		if (arrow.getPassengers().size() != 1)
			return;
		if (!(arrow.getPassengers().get(0) instanceof ArmorStand))
			return;

		ev.setCancelled(true);
	}

	// Stop reeling
	@EventHandler
	public void onLeftClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.LEFT_CLICK_AIR && ev.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null)
			return;
		if (!Util.isSimilarBasic(ev.getItem(), Island.getGrapple()))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());

		ev.setCancelled(true);
		IslandData d = hp.getIsland();

		if (d.reelBack == null || d.reelBack.isCancelled())
			return;

		d.reset();
		hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.ITEM_SHIELD_BREAK, 1.f, 1.f);
	}
}
