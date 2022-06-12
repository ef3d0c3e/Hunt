package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.text.MessageFormat;
import java.util.*;

public class KitThomas extends Kit
{
	static ItemStack chalkItem;
	static ItemStack maskItem;
	static ItemStack crownItem;
	static ItemStack cautionItem;

	static HashMap<Location, HuntPlayer> furnaces = new HashMap<>(); // Furnaces

	@Override
	public String getName() { return "thomas"; }
	@Override
	public String getDisplayName() { return "Thomas"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return Items.createGuiItem(Material.ENDER_EYE, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Tire des craies topologiques",
			Kit.itemLoreColor + "╸ Ses fours lui rapportent des intérêts"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bObtenez §6La Caution§b que vous pouvez utiliser pour créer 100€ de dégâts autour de vous.",
				"§c ╸ §bObtenez la §6Boîte à Craies§b qui tire des craies.::§d● Vous avez 10 munitions",
				"§c ╸ §bSi une craie touche un bloc, l'espace Minecraft est privé d'une boule unité pour une certaine norme.",
				"§c ╸ §bSi une craie touche une entitée, l'espace Minecraft devient l'union de l'espace Minecraft et d'une boule unité pour une certaine norme.",
				"§c ╸ §bLe Jour, vous devenez le Roi des roublards.",
				"§c ╸ §bLa Nuit, vous devenez Ventou dé la noui.",
				"§c ╸ §bEn forme Roublard, placer un four le lie à vous. Lorsque qu'une ressource cuit dans un four lié, cette ressource est automatiquement transférée dans votre inventaire ainsi qu'un peu d'XP.::§d● Vous avez une chance de\n recevoir un lingot supplémentaire\n lorsque vous faite cuire\n du fer ou de l'or\n§d● Le four doit être placé\n le jour mais continue\n de fonctionner la nuit",
				"§c ╸ §bEn forme Ventou, la §6Boîte à Craies§b se recharge.",
			}
		};

		return desc;
	}

	static
	{
		chalkItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = chalkItem.getItemMeta();
			meta.setDisplayName("§aBoîte à Craies");
			meta.setCustomModelData(1);
			meta.setLore(Arrays.asList(
				"§7Click droit pour tirer une craie"
			));
			chalkItem.setItemMeta(meta);
		}

		maskItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = maskItem.getItemMeta();
			meta.setDisplayName("§8Masque de Ventou dé la nouit");
			meta.setCustomModelData(3);
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			maskItem.setItemMeta(meta);
		}

		crownItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = crownItem.getItemMeta();
			meta.setDisplayName("§8Couronne du roi des roublards");
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
			meta.setCustomModelData(4);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			crownItem.setItemMeta(meta);
		}

		cautionItem = new ItemStack(Material.PAPER, 4);
		{
			ItemMeta meta = cautionItem.getItemMeta();
			meta.setDisplayName("§bLa caution");
			meta.setLore(Arrays.asList(
				"§7Click droit pour créer 100€",
				"§7de dégâts autour de vous!"
			));
			cautionItem.setItemMeta(meta);
		}
	}

	@Override
	public void changeOwner(final HuntPlayer prev, final HuntPlayer next)
	{
		if (next == null)
		{
			Iterator<Map.Entry<Location, HuntPlayer>> it = furnaces.entrySet().iterator();
			while (it.hasNext())
			{
				final Map.Entry<Location, HuntPlayer> e = it.next();
				if (e.getValue() == prev)
					it.remove();
			}
			return;
		}


		Iterator<Map.Entry<Location, HuntPlayer>> it = furnaces.entrySet().iterator();
		while (it.hasNext())
		{
			final Map.Entry<Location, HuntPlayer> e = it.next();
			if (e.getValue() == prev)
				e.setValue(next);
		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]
		{
			chalkItem,
			maskItem,
			crownItem,
			cautionItem
		};
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !Util.containsItem(getItems(), item) || item.isSimilar(cautionItem);
	}

	public KitThomas()
	{
		nightMode = false;
		ammo = 10;
		arrows = new HashSet<>();
	}

	boolean nightMode;
	int ammo;
	HashSet<Arrow> arrows;

	public static class Events implements Listener
	{
		public static boolean isNightMode()
		{
			return (Game.getOverworld().getTime() / 1000) >= 13;
			// Hour = getTime() / 1000 + 6
			// At '0' : 6:00
			// At '6000' : 12:00
		}

		/**
		 * Gives players his items & awards achievements for playing with this kit
		 * @param ev Event
		 */
		@EventHandler
		public void onSpawn(final HPSpawnEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitThomas))
				return;

			hp.getPlayer().getInventory().addItem(cautionItem);
			final ItemStack item = hp.getPlayer().getInventory().getHelmet();
			if (item != null)
				PlayerInteractions.giveItem(hp, new ItemStack[]{ item.clone() }, true, true);
			if (((KitThomas)hp.getKit()).nightMode)
				hp.getPlayer().getInventory().setHelmet(maskItem);
			else
				hp.getPlayer().getInventory().setHelmet(crownItem);
			hp.getPlayer().getInventory().addItem(chalkItem);
			HuntAchievement.PLAY_THOMAS.award(hp.getPlayer(), 1);
		}

		/**
		 * Registers placed furnace into hashset
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockPlace(BlockPlaceEvent ev)
		{
			// Does not link at night...
			if (isNightMode())
				return;
			if (ev.getBlockPlaced().getType() != Material.FURNACE)
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitThomas))
				return;

			hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7Ce four sera lié au compte en banque de Patrick B."));
			KitThomas.furnaces.put(ev.getBlockPlaced().getLocation(), hp);
		}

		/**
		 * Notifies player when their furnaces have been broken
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockBreak(BlockBreakEvent ev)
		{
			if (ev.getBlock().getType() != Material.FURNACE)
				return;
			final HuntPlayer owner = KitThomas.furnaces.get(ev.getBlock().getLocation());
			if (owner == null)
				return;

			KitThomas.furnaces.remove(ev.getBlock().getLocation());
			if (owner.isOnline())
				owner.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§7Votre four a été détruit par §e{0}", ev.getPlayer())));
		}

		/**
		 * Notifies player when their furnaces have been broken (by an explosion)
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockExplode(EntityExplodeEvent ev)
		{
			for (Block b : ev.blockList())
			{
				if (b.getType() != Material.FURNACE)
					continue;
				final HuntPlayer owner = KitThomas.furnaces.get(b.getLocation());
				if (owner == null)
					continue;

				KitThomas.furnaces.remove(b.getLocation());
				if (owner.isOnline())
					owner.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7Votre four a été détruit"));
			}
		}

		/**
		 * Smelting transfers items into player's inventory & may increases yield
		 * @param ev Event
		 */
		@EventHandler
		public void onSmelt(FurnaceSmeltEvent ev)
		{
			final HuntPlayer owner = KitThomas.furnaces.get(ev.getBlock().getLocation());
			if (owner == null)
				return;

			ItemStack result = ev.getResult();
			if (ev.getResult().getType() == Material.IRON_INGOT && Game.nextPosInt() % 10 < 7)
				result.setAmount(result.getAmount()+1);
			else if (ev.getResult().getType() == Material.GOLD_INGOT && Game.nextPosInt() % 10 < 7)
				result.setAmount(result.getAmount()+1);
			PlayerInteractions.giveItem(owner, new ItemStack[]{result}, true, true);
			PlayerInteractions.schedule(owner, (hp) -> { hp.getPlayer().giveExp(2); });

			if (owner.isOnline())
				owner.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§7Votre four vous rapporte des intérêts!"));

			ev.setResult(null);
			final Location loc = ev.getBlock().getLocation();
			loc.getWorld().spawnParticle(Particle.PORTAL, loc, 80, 0.5, 0.5, 0.5);
		}


		/**
		 * Shoots chalk from chalk box
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null || !ev.getItem().isSimilar(KitThomas.chalkItem))
				return;
			final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitThomas))
				return;

			// Stop if clicking an inventory
			if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;

			ev.setCancelled(true);

			final KitThomas kit = (KitThomas) hp.getKit();
			if (kit.ammo == 0)
			{
				hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_DISPENSER_FAIL, SoundCategory.MASTER, 6.f, 1.5f);
				return;
			}

			--kit.ammo;
			hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.ENTITY_SKELETON_SHOOT, SoundCategory.MASTER, 8.f, 1.5f);
			Arrow arrow = hp.getPlayer().getWorld().spawnArrow(hp.getPlayer().getEyeLocation(), hp.getPlayer().getLocation().getDirection(), 2.f, 0.05f);
			arrow.setShooter(hp.getPlayer());
			kit.arrows.add(arrow);

			// Trail
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (arrow == null || arrow.isOnGround() || arrow.isDead())
						this.cancel();

					arrow.getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation().add(0.0, -.35, 0.0), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.RED, 2));
					arrow.getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation(), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.WHITE, 2));
					arrow.getWorld().spawnParticle(Particle.REDSTONE, arrow.getLocation().add(0.0, .35, 0.0), 1, 0.0, 0.0, 0.0, new Particle.DustOptions(Color.GREEN, 2));
				}
			}.runTaskTimer(Hunt.plugin, 1, 1);
		}

		/**
		 * Spawn glass sphere or remove sphere around hit location
		 * @param ev Event
		 */
		@EventHandler
		public void onArrowHit(ProjectileHitEvent ev)
		{
			if (!(ev.getEntity() instanceof Arrow))
				return;
			if (!(ev.getEntity().getShooter() instanceof Player))
				return;
			final HuntPlayer owner = HuntPlayer.getPlayer((Player)ev.getEntity().getShooter());
			if (owner.getKit() == null || (owner.getKit() instanceof KitThomas))
				return;
			final KitThomas kit = (KitThomas)owner.getKit();
			if (!kit.arrows.contains((Arrow)ev.getEntity()))
				return;

			ev.setCancelled(true);
			if (ev.getHitEntity() == null)
			{
				final Location loc = new Location(ev.getEntity().getWorld(),
					ev.getEntity().getLocation().getX(),
					ev.getEntity().getLocation().getY(),
					ev.getEntity().getLocation().getZ()
				);
				for (double x = -3.0; x <= 3.0; x += 1.0)
					for (double y = -3.0; y <= 3.0; y += 1.0)
						for (double z = -3.0; z <= 3.0; z += 1.0)
						{
							if (x*x + y*y + z*z > 9.0)
								continue;
							Block b = loc.getWorld().getBlockAt((int)(loc.getX()+x), (int)(loc.getBlockY()+y), (int)(loc.getZ()+z));
							if (b == null || Util.containsMaterial(Util.nonMovableBlocks, b.getType()))
								continue;
							b.setType(Material.AIR);
						}
			}
			else
			{
				final Location loc = new Location(ev.getHitEntity().getWorld(),
					ev.getHitEntity().getLocation().getX(),
					ev.getHitEntity().getLocation().getY(),
					ev.getHitEntity().getLocation().getZ()
				);
				for (double x = -3.0; x <= 3.0; x += 1.0)
					for (double y = -3.0; y <= 3.0; y += 1.0)
						for (double z = -3.0; z <= 3.0; z += 1.0)
						{
							if (x*x + y*y + z*z > 9.0)
								continue;
							Block b = loc.getWorld().getBlockAt((int)(loc.getX()+x), (int)(loc.getBlockY()+y), (int)(loc.getZ()+z));
							if (b == null || Util.containsMaterial(Util.nonMovableBlocks, b.getType()))
								continue;
							b.setType(Material.GLASS);
						}
			}
			kit.arrows.remove(ev.getEntity());
			ev.getEntity().remove();
		}

		/**
		 * Right click with caution item
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick2(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null || !ev.getItem().isSimilar(KitThomas.cautionItem))
				return;

			// Stop if clicking an inventory
			if (!ev.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;

			ev.setCancelled(true);

			// If we don't wait, paper will be used twice when facing a block
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					ev.getItem().setAmount(ev.getItem().getAmount() - 1);

					final Location loc = ev.getPlayer().getLocation();
					int count = 0;
					for (double x = -5.0; x <= 5.0; x += 1.0)
					{
						for (double y = -5.0; y <= 5.0; y += 1.0)
						{
							for (double z = -5.0; z <= 5.0; z += 1.0)
							{
								if (x * x + y * y + z * z > 25.0)
									continue;
								Block b = loc.getWorld().getBlockAt((int) (loc.getX() + x), (int) (loc.getBlockY() + y), (int) (loc.getZ() + z));
								if (b == null || Util.containsMaterial(Util.nonMovableBlocks, b.getType()))
									continue;
								if (Game.nextPosInt() % 2 == 0 && count != 50)
								{
									FallingBlock f = ev.getPlayer().getWorld().spawnFallingBlock(b.getLocation(), b.getType().createBlockData());
									f.setVelocity(new org.bukkit.util.Vector((float) (Game.nextPosInt() % 21 - 10) / 10.f, (float) (Game.nextPosInt() % 11) / 10.f, (float) (Game.nextPosInt() % 21 - 10) / 10.f));
									++count;
								}

								b.setType(Material.AIR);
							}
						}
					}
				}
			}.runTaskLater(Hunt.plugin, 1);
		}


		/**
		 * Timer task that notifies players when night/day comes
		 */
		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			// Night mode
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (Game.isPaused())
						return;

					HuntPlayer.forEach(hp -> {
						if (!hp.isOnline() || !hp.isAlive())
							return;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitThomas))
							return;

						final KitThomas kit = (KitThomas) hp.getKit();
						if (isNightMode() == kit.nightMode)
							return;

						kit.nightMode = isNightMode();
						if (kit.nightMode)
						{
							hp.getPlayer().sendMessage("§7§oDans la nuit surgit une ombre... L'ombre de Ventou dé la noui");
							hp.getPlayer().getInventory().setHelmet(KitThomas.maskItem);
						}
						else
						{
							hp.getPlayer().sendMessage("§7§oLe jour se lève! Le roi des roublards reprend son rôle...");
							hp.getPlayer().getInventory().setHelmet(KitThomas.crownItem);
						}
					});
				}
			}.runTaskTimer(Hunt.plugin, 0, 100);

			// Reload
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!isNightMode() || Game.isPaused())
						return;

					HuntPlayer.forEach(hp -> {
						if (!hp.isOnline() || !hp.isAlive())
							return;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitThomas))
							return;

						final KitThomas kit = (KitThomas) hp.getKit();
						if (kit.ammo == 10)
							return;

						hp.getPlayer().getWorld().playSound(hp.getPlayer().getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, SoundCategory.MASTER, 6.f, 1.f);
						++kit.ammo;
						hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§e{0}/10", kit.ammo)));
					});
				}
			}.runTaskTimer(Hunt.plugin, 0, 15);
		}
	}
}
