package org.ef3d0c3e.hunt.kits;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.packets.DestroyEntityHelper;
import org.ef3d0c3e.hunt.packets.EquipmentHelper;
import org.ef3d0c3e.hunt.packets.LivingEntityHelper;
import org.ef3d0c3e.hunt.packets.MetadataHelper;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

/**
 * Baptiste's kit
 */
public class KitBaptiste extends Kit
{
	@Override
	public String getName() { return "baptiste"; }
	@Override
	public String getDisplayName() { return "Baptiste"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return Items.createGuiItem(Material.SPECTRAL_ARROW, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Affiche la trajectoire des flèches",
			Kit.itemLoreColor + "╸ Illumine ses cibles",
			Kit.itemLoreColor + "╸ Connaît les PV de ses cibles",
			Kit.itemLoreColor + "╸ A une chance de récupérer une",
			Kit.itemLoreColor + " flèche lorsqu'il rate à l'arc"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bLe point d'aterrissage de vos flèches s'affichent avant de tirer.",
				"§c ╸ §bVos flèches donnent glowing à vos cibles.",
				"§c ╸ §bLorsque vous touchez une cible, vous connaissez ses points de vie.",
				"§c ╸ §bLorsque vous ratez à l'arc, vous avez une chance de récupérer votre flèche.",
				"§c ╸ §bLorsque vous tuez un mouton, vous pouvez dropper des plumes.",
				"§c ╸ §bToucher plusieurs flèches d'affilé augmente vos dégâts.::§d● +10% de dégâts par flèche,\n jusqu'à 150%",
			}
		};

		return desc;
	}

	@Override
	public void changeOwner(final HuntPlayer prev, final HuntPlayer next)
	{
		// Delete indicator
		try
		{
			ProtocolManager manager = Game.getProtocolManager();
			manager.sendServerPacket(prev.getPlayer(), entityDestroy.getPacket());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public KitBaptiste() {}

	static UUID uuid = UUID.randomUUID();
	static int entityId = Game.nextPosInt();
	static DestroyEntityHelper entityDestroy = new DestroyEntityHelper(Arrays.asList(entityId));

	private int hitStreak = 0;

	public static class Events implements Listener
	{
		/**
		 * Refunds arrow when miss & resets hit streak
		 * @param ev Event
		 */
		@EventHandler
		public void onProjectileHit(final ProjectileHitEvent ev)
		{
			if (!(ev.getEntity().getShooter() instanceof Player) || ev.getHitEntity() != null)
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getEntity().getShooter());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitBaptiste))
				return;

			final KitBaptiste kit = (KitBaptiste)hp.getKit();
			if (kit.hitStreak != 0)
			{
				kit.hitStreak = 0;
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
			final HuntPlayer shooter = HuntPlayer.getPlayer((Player)((Projectile)ev.getDamager()).getShooter());
			if (shooter.getKit() == null || !(shooter.getKit() instanceof KitBaptiste) || ((Projectile) ev.getDamager()).getShooter() == ev.getEntity())
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
			if (killer == null || killer.getKit() == null || !(killer.getKit() instanceof KitBaptiste))
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
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getEntity());
			if (hp.getKit() == null || hp.getKit() instanceof KitBaptiste)
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
			}.runTaskTimer(Hunt.plugin, 3, 1);
		}

		/**
		 * Initializes entity org.ef3d0c3e.hunt.packets for arrow target prediction
		 * & Starts a runnable that displays prediction for every players
		 */
		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			// Prediction
			new BukkitRunnable()
			{
				static PacketContainer metadataPacket;
				static PacketContainer equipmentPacket;
				static // Pre generate some org.ef3d0c3e.hunt.packets
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
					HuntPlayer.forEach(hp -> {
						if (!hp.isOnline() || !hp.isAlive())
							return;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitBaptiste))
							return;
						if (hp.getPlayer().getInventory().getItemInMainHand().getType() != Material.BOW)
							return;

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
					});
				}
			}.runTaskTimer(Hunt.plugin, 0, 2);
		}
	}
}