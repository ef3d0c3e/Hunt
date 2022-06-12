package org.ef3d0c3e.hunt.kits;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.util.Arrays;


public class KitTom extends Kit
{
	static ItemStack magicCarpet;
	static ItemStack magicCarpetModel;

	@Override
	public String getName() { return "tom"; }
	@Override
	public String getDisplayName() { return "Tom"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return Items.createGuiItem(Material.COMPASS, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Les battements de son coeur",
			Kit.itemLoreColor + " indiquent la distance à sa cible",
			Kit.itemLoreColor + "╸ Peut traquer son chasseur",
			Kit.itemLoreColor + "╸ Obtient un tapis volant"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bEntendez votre coeur battre lorsque vous êtes proche de votre chasseur.::§d● Plus il bat fort,\n plus vous êtes proche\n§d● Si la distance est inférieure\n à 30 blocs, vous obtenez\n Speed I",
				"§c ╸ §bVous pouvez traquer votre chasseur en faisant click gauche avec le §6Tracker§b.",
				"§c ╸ §bVous pouvez fabriquer un §6Tapis Brûlé§b en droppant un §6Bloc de Charbon§b sur un §6Tapis§b.",
				"§c ╸ §bVous pouvez utiliser le tapis pour vous envoler et vous déplacer sur de longue distances.",
				"§c ╸ §bLe tapis laisse une traînée de feu derrière lui.",
			}
		};

		return desc;
	}

	static
	{
		magicCarpet = new ItemStack(Material.LIME_CARPET);
		{
			ItemMeta meta = magicCarpet.getItemMeta();
			meta.setDisplayName("§aTapis Brûlé");
			meta.setLore(Arrays.asList(
				"§7Click droit pour se lancer",
				"§7dans les airs."
			));
			magicCarpet.setItemMeta(meta);
		}
		magicCarpetModel = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = magicCarpetModel.getItemMeta();
			meta.setCustomModelData(2);
			magicCarpetModel.setItemMeta(meta);
		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]{ magicCarpet };
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !item.isSimilar(magicCarpet);
	}

	public KitTom() {}

	public static class Events implements Listener
	{
		/**
		 * Gives carpet to player
		 * @param ev Event
		 */
		@EventHandler
		public void onSpawn(final HPSpawnEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
				return;

			PlayerInteractions.giveItem(hp, new ItemStack[] { magicCarpet }, true, true);
		}

		/**
		 * Spawns a flying carpet
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(final PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null || !ev.getItem().isSimilar(KitTom.magicCarpet))
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
				return;

			// Stop if clicking an inventory
			if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;

			ev.setCancelled(true);
			ev.getItem().setAmount(ev.getItem().getAmount()-1);

			final Vector speed = new Vector(0.60, 0.65, 0.60); // Speed in blocks/ticks

			// Carpet
			ArmorStand carpet = (ArmorStand)hp.getPlayer().getLocation().getWorld().spawnEntity(hp.getPlayer().getLocation(), EntityType.ARMOR_STAND);
			carpet.setInvisible(true);
			carpet.setInvulnerable(true);
			carpet.setSilent(true);
			carpet.addPassenger(hp.getPlayer());
			carpet.getEquipment().setItemInMainHand(KitTom.magicCarpetModel);
			carpet.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.ADDING_OR_CHANGING);
			carpet.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.ADDING_OR_CHANGING);
			carpet.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.ADDING_OR_CHANGING);
			carpet.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

			hp.getPlayer().playSound(carpet.getLocation(), "hunt.jojo.awake", SoundCategory.MASTER, 65536.f, 1.f);

			new BukkitRunnable()
			{
				int ticks = 0;

				@Override
				public void run()
				{
					if (Game.isPaused() || ticks == 400 || !carpet.getPassengers().contains(hp.getPlayer()))
					{
						this.cancel();
						// Remove carpet
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								if (carpet != null && !carpet.isDead())
									carpet.remove();
							}
						}.runTaskLater(Hunt.plugin, 30);
						return;
					}

					// Carpet's velocity
					Vector velocity = hp.getPlayer().getLocation().getDirection().normalize().multiply(speed);
					carpet.setVelocity(velocity);
					// Carpet's 'angle'
					carpet.setRightArmPose(new EulerAngle(0, 0, 3.14159+(180.0 - hp.getPlayer().getLocation().getPitch()) / 180.0 * 3.1415926535));
					carpet.setRotation(hp.getPlayer().getLocation().getYaw()-90.f, 0);


					// Effects
					((Player)carpet.getPassengers().get(0)).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20*5, 0));
					((Player)carpet.getPassengers().get(0)).addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20*1, 2));
					final Location rear = carpet.getLocation().add(0, 1.1, 0).subtract(hp.getPlayer().getLocation().getDirection().normalize().multiply(2.9));
					carpet.getWorld().spawnParticle(Particle.LAVA, rear, 10, 0.0, 0.0, 0.0);

					// Spawn explosive
					if (ticks % 15 == 0)
					{
						Fireball ball = (Fireball)carpet.getWorld().spawnEntity(rear, EntityType.FIREBALL);
						ball.setDirection(new Vector(0, -2, 0));
						ball.setIsIncendiary(true);
						ball.setShooter(hp.getPlayer());
						ball.setYield(2.1f);
					}

					if (ticks % 11 == 0)
						carpet.getWorld().spawnFallingBlock(rear.clone().subtract(0.0, 1.5, 0.0), Material.FIRE.createBlockData());

					++ticks;
				}
			}.runTaskTimer(Hunt.plugin, 0, 1);
		}

		/**
		 * While player is riding the magic carpet, the player is immune to fall damage
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDamage(final EntityDamageEvent ev)
		{
			if (!(ev.getEntity() instanceof Player))
				return;
			if (ev.getCause() != EntityDamageEvent.DamageCause.FALL)
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getEntity());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
				return;

			if (!(hp.getPlayer().getVehicle() instanceof ArmorStand))
				return;
			if (!((ArmorStand) hp.getPlayer().getVehicle()).getEquipment().getItemInMainHand().isSimilar(KitTom.magicCarpetModel))
				return;

			ev.setCancelled(true);
		}

		/**
		 * Creates a flying carpet using a carpet & a coal block
		 * @param ev Event
		 */
		@EventHandler
		public void onDrop(final PlayerDropItemEvent ev)
		{
			if (ev.getItemDrop().getItemStack().getType() != Material.COAL_BLOCK)
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
				return;

			// Wait 2 seconds
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (ev.getItemDrop() == null || ev.getItemDrop().isDead())
						return;
					if (!Util.containsMaterial(Util.carpets, ev.getItemDrop().getLocation().getBlock().getType()))
						return;

					ev.getItemDrop().getLocation().getBlock().setType(Material.AIR);
					ev.getItemDrop().getLocation().getWorld().dropItemNaturally(ev.getItemDrop().getLocation(), KitTom.magicCarpet);
					ev.getItemDrop().getLocation().getWorld().spawnParticle(Particle.LAVA, ev.getItemDrop().getLocation(), 100, 0.25f, 0.66f, 0.25f);
					ev.getItemDrop().getLocation().getWorld().playSound(ev.getItemDrop().getLocation(), Sound.BLOCK_ANVIL_USE, SoundCategory.MASTER, 8.f, 1.f);
					if (ev.getItemDrop().getItemStack().getAmount() > 1)
						ev.getItemDrop().getItemStack().setAmount(ev.getItemDrop().getItemStack().getAmount()-1);
					else
						ev.getItemDrop().remove();
				}
			}.runTaskLater(Hunt.plugin, 40);
		}

		/**
		 * Updates tracker to track hunter
		 * @param ev Event
		 */
		@EventHandler
		public void onLeftClick(final PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.LEFT_CLICK_AIR && ev.getAction() != Action.LEFT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null)
				return;
			if (!ev.getItem().isSimilar(Items.getTracker()))
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
				return;

			ev.setCancelled(true);
			if (!hp.getPlayer().getInventory().contains(Material.ROTTEN_FLESH) &&
				!hp.getPlayer().getInventory().contains(Material.BONE) &&
				!hp.getPlayer().getInventory().contains(Material.GUNPOWDER) &&
				!hp.getPlayer().getInventory().contains(Material.SPIDER_EYE))
			{
				hp.getPlayer().sendMessage("§cVous n'avez pas les éléments requis pour actualiser votre traqueur!");
				return;
			}

			if (!hp.updateTracking(true))
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

		/**
		 * Timer task that makes player's heart beat
		 */
		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					// We check that the player's hunter is not null (even though it should not be required) because when testing
					// we often force the inHunt stage even when players have no targets/hunters
					// [TEAM MODE ONLY] also 'hunter' can be null in case hunter is offline/not in the same dim (because getClosestPlayer returns null in those cases)
					if (!Game.inHunt())
						return;

					HuntPlayer.forEach(hp -> {
						if (!hp.isAlive() || !hp.isOnline())
							return;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitTom))
							return;

						HuntPlayer hunter = null;
						if (!Game.isTeamMode())
							hunter = hp.getHunter();
						else
							hunter = hp.getTeam().getHunter().getClosestPlayer(hp);
						if (hunter == null || !hunter.isOnline() || hp.getPlayer().getWorld() != hunter.getPlayer().getWorld() || !hunter.isAlive())
							return;

						final double dist = hunter.getPlayer().getLocation().distanceSquared(hp.getPlayer().getLocation());
						if (dist < 30.0 * 30.0)
						{
							hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 0));
							hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 8.f, .1f);
							new BukkitRunnable()
							{
								@Override
								public void run()
								{
									hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 8.f, .1f);
								}
							}.runTaskLater(Hunt.plugin, 5);
						} else if (dist < 50.0 * 50.0)
						{
							hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 1.f, .1f);
						} else if (dist < 80.0 * 80.0)
						{
							hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.MASTER, 0.5f, .1f);
						}
					});
				}
			}.runTaskTimer(Hunt.plugin, 0, 20);
		}
	}
}
