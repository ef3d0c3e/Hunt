package org.ef3d0c3e.hunt.kits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.core.particles.ParticleType;
import org.ef3d0c3e.hunt.player.PlayerInteractions;
import org.ef3d0c3e.hunt.teams.Team;
import packets.EquipmentHelper;
import packets.LivingEntityHelper;
import packets.MetadataHelper;
import packets.TeamHelper;

/**
 * Baptiste's kit's events
 */
public class KitBaptisteEvents implements Listener
{
	/**
	 * Refunds arrow when miss & resets hit streak
	 * @param ev Event
	 */
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent ev)
	{
		if (!(ev.getEntity().getShooter() instanceof Player) || ev.getHitEntity() != null)
			return;
		final HuntPlayer hp = Game.getPlayer(((Player)ev.getEntity().getShooter()).getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.BAPTISTE)
			return;

		if (((KitBaptiste)hp.getKit()).hitStreak != 0)
		{
			((KitBaptiste)hp.getKit()).hitStreak = 0;
			hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_ANVIL_PLACE, 16.f, 0.41f);
		}


		if (Game.nextPosInt() % 3 == 0)
		{
			hp.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 1));
			ev.getEntity().remove();
		}
	}

	/**
	 * Applies glowing, displays health (if player) & shows additional damage (with sound)
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
	{
		if (!(ev.getEntity() instanceof LivingEntity) ||
			!(ev.getDamager() instanceof Projectile) ||
			!(((Projectile)ev.getDamager()).getShooter() instanceof Player))
			return;
		final HuntPlayer shooter = Game.getPlayer(((Player)((Projectile)ev.getDamager()).getShooter()).getName());
		if (shooter.getKit() == null || shooter.getKit().getID() != KitID.BAPTISTE || ((Projectile) ev.getDamager()).getShooter() == ev.getEntity())
			return;

		((LivingEntity)ev.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 160, 0));
		final double perc = 1.0 + Math.min(((KitBaptiste) shooter.getKit()).hitStreak, 5)/10.0;
		ev.setDamage(ev.getDamage() * perc);
		++((KitBaptiste)shooter.getKit()).hitStreak;
		shooter.getPlayer().playSound(shooter.getPlayer().getLocation(), Sound.BLOCK_ANVIL_PLACE, 16.f, (float)(perc+0.1));

		if (ev.getEntity() instanceof Player)
		{
			final Player victim = (Player)ev.getEntity();
			shooter.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				new TextComponent(
					MessageFormat.format("§c{0}/{1} ❤ §8[§e{2}%§8]",
						victim.getHealth(),
						victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
						(int)(perc*100.0)
					)
				)
			);
		}
		else
		{
			shooter.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR,
				new TextComponent(
					MessageFormat.format("§8[§e{0}%§8]",
						(int)(perc*100.0)
					)
				)
			);
		}
	}

	/**
	 * Drops feather when killing a sheep
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev)
	{
		if (!(ev.getEntity() instanceof Sheep))
			return;
		final HuntPlayer killer = Util.getPlayerKiller(ev.getEntity().getKiller());
		if (killer == null || killer.getKit() == null || killer.getKit().getID() != KitID.BAPTISTE)
			return;
		
		if (Game.nextPosInt() % 4 == 0)
			ev.getDrops().add(new ItemStack(Material.FEATHER, 1));
		if (Game.nextPosInt() % 10 == 0)
			ev.getDrops().add(new ItemStack(Material.FEATHER, 1));
	}

	/**
	 * Simulates an arrow and attempts to predict it's final location after a certain duration
	 * @param dir Shooter's initial direction
	 * @param loc Arrow's initial location
	 * @param ticks Number of ticks to simulate
	 * @return Arrow's predicted location after ```ticks```
	 *
	 * @note Will return null if ```ticks < 0```
	 */
	public static Location getArrowPrediction(org.bukkit.util.Vector dir, Location loc, int ticks)
	{
		double t = 0.0;
		for (int i = 0; i < ticks; ++i)
		{
			final double x = loc.getX() + dir.getX() * t;
			final double y = loc.getY() + dir.getY() * t -6.0 * t * t;
			final double z = loc.getZ() + dir.getZ() * t;
			if (i+1 == ticks)
				return new Location(loc.getWorld(), x, y, z);
			
			final Material mat = loc.getWorld().getBlockAt((int)x, (int)y, (int)z).getType();
			if (mat != Material.AIR && !Util.containsMaterial(Util.plantBlocks, mat))
				return new Location(loc.getWorld(), x, y, z);
			t += 0.01;
		}

		return null;
	}

	/**
	 * Arrows shot leave a trail behind
	 * @param ev Event
	 */
	@EventHandler
	public void onShoot(EntityShootBowEvent ev)
	{
		if (ev.getBow() == null || ev.getBow().getType() != Material.BOW)
			return;
		if (!(ev.getProjectile() instanceof Arrow) || !(ev.getEntity() instanceof Player))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.BAPTISTE)
			return;

		Arrow arrow = (Arrow)ev.getProjectile();
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (arrow == null || arrow.isOnGround() || arrow.isDead())
					this.cancel();

				arrow.getWorld().spawnParticle(Particle.DRIP_LAVA, arrow.getLocation(), 1, 0.0, 0.0, 0.0, 0);
			}
		}.runTaskTimer(Game.getPlugin(), 3, 1);
	}

	/**
	 * Initializes entity packets for arrow target prediction
	 * & Starts a runnable that displays prediction for every players
	 */
	public KitBaptisteEvents()
	{
		// Prediction
		new BukkitRunnable()
		{
			static PacketContainer metadataPacket;
			static PacketContainer equipmentPacket;
			static // Pre generate some packets
			{
				MetadataHelper metadata = new MetadataHelper();
				metadata.setStatus((byte)(MetadataHelper.Status.Glowing | MetadataHelper.Status.Invisible));
				metadata.setNoGravity(true);
				metadata.setArmorStandType((byte)(MetadataHelper.ArmorStandTypes.HasNoBasePlate | MetadataHelper.ArmorStandTypes.IsMarker));
				metadataPacket = metadata.getPacket(KitBaptiste.entityId, false);

				EquipmentHelper equipment = new EquipmentHelper();
				equipment.setItem(EquipmentHelper.Slot.HEAD, new ItemStack(Material.TARGET, 1));
				equipmentPacket = equipment.getPacket(KitBaptiste.entityId);
			}

			@Override
			public void run()
			{
				for (final HuntPlayer hp : Game.getPlayerList().values())
				{
					if (!hp.isOnline() || !hp.isAlive())
						continue;
					if (hp.getKit() == null || hp.getKit().getID() != KitID.BAPTISTE)
						continue;
					if (hp.getPlayer().getInventory().getItemInMainHand().getType() != Material.BOW)
						continue;
					
					final Location pred = getArrowPrediction(hp.getPlayer().getLocation().getDirection().normalize().multiply(44.659313), hp.getPlayer().getEyeLocation(), 400);

					LivingEntityHelper ent = new LivingEntityHelper(KitBaptiste.entityId, KitBaptiste.uuid);
					ent.setType(LivingEntityHelper.Mobs.ARMOR_STAND);
					ent.setPosition(pred.getX(), pred.getY()-1.52, pred.getZ()); // Sink it in the ground

					try
					{
						ProtocolManager manager = Game.getProtocolManager();
						PacketContainer spawn = ent.getPacket();
						manager.sendServerPacket(hp.getPlayer(), spawn);
						manager.sendServerPacket(hp.getPlayer(), metadataPacket);
						manager.sendServerPacket(hp.getPlayer(), equipmentPacket);
					}
					catch (Exception e)
					{

					}
				}
			}
		}.runTaskTimer(Game.getPlugin(), 0, 2);
	}
}
