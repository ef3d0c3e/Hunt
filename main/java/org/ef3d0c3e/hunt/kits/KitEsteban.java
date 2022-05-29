package org.ef3d0c3e.hunt.kits;

import java.util.Arrays;
import java.util.HashMap;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPSpawnEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

/**
 * Esteban's kit
 */
public class KitEsteban extends Kit
{
	static ItemStack tornadoItem;
	static ItemStack potionItem;
	static ItemStack splashPotionItem;

	@Override
	public String getName() { return "esteban"; }
	@Override
	public String getDisplayName() { return "Estéban"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.DIRT, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Place de la Dirt à l'infini",
			Kit.itemLoreColor + "╸ Récupère de la nourriture en cassant de la terre",
			Kit.itemLoreColor + "╸ Tire de la Dirt en la droppant",
			Kit.itemLoreColor + "╸ Peut crafter des potions de 'Luck'",
			Kit.itemLoreColor + " qui lui donnent un effet positif et",
			Kit.itemLoreColor + " négatif aux autres",
			Kit.itemLoreColor + "╸ Peut crafter une tornade"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bPeut tirer des blocs de terre en droppanet de la terre.",
				"§c ╸ §bQuand il place de la terre, elle reste dans son inventaire.",
				"§c ╸ §bCasser de la terre lui redonne un peu de nourriture.",
				"§c ╸ §bPeut crafter des potions de luck §e(voir p2-3)§b qui lui donnent un effet positif et négatif à ses adversaires.",
				"§c ╸ §bPeut crafter une tornade de terre §e(voir p4)§b qui lui permet de se déplacer, de creuser ou de repousser les joueurs."
			},
			{
				"§b§nCraft de la potion de luck:",
				"",
				" §8[ §dSSS §8]",
				" §8[ §dSWS §8]",
				" §8[ §dSSS §8]",
				"§c ╸ §dS§b = Sable des âmes",
				"§c ╸ §dW§b = Bouteille d'eau"
			},
			{
				"§b§nCraft de la potion de luck splash:",
				"",
				" §8[ §7### §8]",
				" §8[ §dGL§7# §8]",
				" §8[ §7### §8]",
				"§c ╸ §dG§b = Poudre à canon",
				"§c ╸ §dL§b = Potion de Luck"
			},
			{
				"§b§nCraft de la tornade de terre:",
				"",
				" §8[ §dDFD §8]",
				" §8[ §dD§7#§dD §8]",
				" §8[ §dDFD §8]",
				"§c ╸ §dD§b = Terre",
				"§c ╸ §dF§b = Lingot de fer",
				"§c ╸ §b§aClick droit§b pour créer une tornade à 3 blocs de vous",
				"§c ╸ §b§aSneak-Click droit§b pour créer une tornade qui vous emporte",
				"§c ╸ §bLa tornade explose au contact d'un portail",
				"§c ╸ §bLa tornade allume les TNTs",
				"§c ╸ §bLorsqu'elle vous emporte, vous pouvez controller la tornade"
			}
		};
		return desc;
	}

	static private ItemStack[] items;

	static
	{
		// Register tornado recipe
		tornadoItem = new ItemStack(Material.HEART_OF_THE_SEA);
		{
			ItemMeta meta = tornadoItem.getItemMeta();
			meta.setDisplayName("§bTornade");
			meta.setLore(Arrays.asList(
				"§7Click droit pour lancer la tornade",
				"§7Sneak+Click droit pour lancer la tornade et voyager avec"
			));
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			tornadoItem.setItemMeta(meta);
			tornadoItem.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
			tornadoItem.setAmount(2);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_tornado");
			ShapedRecipe recipe = new ShapedRecipe(key, tornadoItem);
			recipe.shape("DID", "D D", "DID");
			recipe.setIngredient('D', Material.DIRT);
			recipe.setIngredient('I', Material.IRON_INGOT);
			Bukkit.addRecipe(recipe);
		}

		// Register potion recipe
		// FIXME: Potion crafting accepts any potion (even for splash...)
		potionItem = new ItemStack(Material.POTION);
		{
			PotionMeta meta = (PotionMeta)potionItem.getItemMeta();
			meta.setDisplayName("§bPotion de Luck");
			meta.setLore(Arrays.asList(
				"§7Donne un effet positif aux joueurs d'Estéban",
				"§7Donne un effet négatif aux autre joueurs"
			));
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			meta.setColor(Color.fromRGB(0xCE537A));
			potionItem.setItemMeta(meta);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_potion");
			ShapedRecipe recipe = new ShapedRecipe(key, potionItem);
			recipe.shape("SSS", "SWS", "SSS");
			recipe.setIngredient('S', Material.SOUL_SAND);
			ItemStack waterBottle = new ItemStack(Material.POTION);
			{
				PotionMeta bottleMeta = (PotionMeta)waterBottle.getItemMeta();
				bottleMeta.setBasePotionData(new PotionData(PotionType.WATER));
				waterBottle.setItemMeta(bottleMeta);
			}
			recipe.setIngredient('W', new RecipeChoice.ExactChoice(waterBottle));
			Bukkit.addRecipe(recipe);
		}

		splashPotionItem = new ItemStack(Material.SPLASH_POTION);
		{
			PotionMeta meta = (PotionMeta)splashPotionItem.getItemMeta();
			meta.setDisplayName("§bPotion de Luck");
			meta.setLore(Arrays.asList(
				"§7Donne un effet positif aux joueurs d'Estéban",
				"§7Donne un effet négatif aux autre joueurs"
			));
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			meta.setColor(Color.fromRGB(0xCE537A));
			splashPotionItem.setItemMeta(meta);

			NamespacedKey key = new NamespacedKey(Game.getPlugin(), "esteban_splash_potion");
			ShapelessRecipe recipe = new ShapelessRecipe(key, splashPotionItem);
			recipe.addIngredient(Material.GUNPOWDER);
			recipe.addIngredient(new RecipeChoice.ExactChoice(potionItem));
			Bukkit.addRecipe(recipe);
		}

		items = new ItemStack[]
		{
			tornadoItem, potionItem, splashPotionItem
		};

	}

	@Override
	public ItemStack[] getItems()
	{
		return items;
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !Util.containsItem(getItems(), item);
	}

	public KitEsteban()
	{
		tornadoCount = 0;
	}

	int tornadoCount; // Keep track of how many tornadoes are active

	public static class Events implements Listener
	{
		/**
		 * Prevents other kist from crafting KitEsteban's items
		 * @param ev Event
		 */
		@EventHandler
		public void onCraftItem(CraftItemEvent ev)
		{
			HuntPlayer hp = Game.getPlayer(ev.getViewers().get(0).getName());
			if (!hp.isAlive())
				return;
			if (hp.getKit() != null && hp.getKit() instanceof KitEsteban)
				return;
			if (!Util.containsItem(KitEsteban.items, ev.getRecipe().getResult()))
				return;

			ev.setCancelled(true);
		}

		/**
		 * Allows Esteban to place dirt infinitely
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockPlace(final BlockPlaceEvent ev)
		{
			if (ev.getItemInHand().getType() != Material.DIRT)
				return;
			HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (!hp.isAlive() || hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))
				return;

			new BukkitRunnable()
			{
				public void run()
				{
					if (hp.isOnline())
						hp.getPlayer().getInventory().addItem(new ItemStack(Material.DIRT));
				}
			}.runTaskLater(Game.getPlugin(), 1);
		}

		/**
		 * Regenerates Esteban's food bar when he breaks dirt
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockBreak(BlockBreakEvent ev)
		{
			if (ev.getBlock().getType() != Material.DIRT)
				return;
			HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (!hp.isAlive() || hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))
				return;

			hp.getPlayer().setFoodLevel(hp.getPlayer().getFoodLevel()+1);
			hp.getPlayer().setSaturation(hp.getPlayer().getSaturation()+0.5f);
		}

		/**
		 * Apply Luck effect to players
		 * @param hp Player to apply effects to
		 * @param distance Distance from potion to player (0 if drinking)
		 */
		private void luckEffect(HuntPlayer hp, double distance)
		{
			double factor = 1.0 / (1.0 + distance);

			if (hp.getKit() != null && hp.getKit() instanceof KitEsteban) // Positive
				switch (Game.nextPosInt() % 13) // Positive
				{
					case 0:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int)(30*20*factor), 0));
						break;
					case 1:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, (int)(15*20*factor), 1));
						break;
					case 2:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, (int)(30*20*factor), 0));
						break;
					case 3:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HEAL, (int)(1*20*factor), 2));
						break;
					case 4:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int)(120*20*factor), 1));
						break;
					case 5:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int)(45*20*factor), 1));
						break;
					case 6:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int)(10*20*factor), 1));
						break;
					case 7:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int)(20*20*factor), 0));
						break;
					case 8:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, (int)(10*20*factor), 1));
						break;
					case 9:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int)(60*20*factor), 0));
						break;
					case 10:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int)(30*20*factor), 1));
						break;
					case 11:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, (int)(15*20*factor), 0));
						break;
					case 12:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, (int)(180*20*factor), 0));
						break;
				}
			else if (Game.inHunt()) // Negative (Won't apply if not in hunt)
				switch (Game.nextPosInt() % 10) // Negative
				{
					case 0:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, (int)(20*20*factor), 0));
						break;
					case 1:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, (int)(5*20*factor), 2));
						break;
					case 2:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.HARM, (int)(1*20*factor), 0));
						break;
					case 3:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int)(5*20*factor), 0));
						break;
					case 4:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int)(15*20*factor), 0));
						break;
					case 5:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int)(5*20*factor), 0));
						break;
					case 6:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int)(10*20*factor), 0));
						break;
					case 7:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int)(20*20*factor), 0));
						break;
					case 8:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int)(10*20*factor), 1));
						break;
					case 9:
						hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int)(10*20*factor), 0));
						break;
				}
		}

		/**
		 * Applies luck effect when drinking potion
		 * @param ev Event
		 */
		@EventHandler
		public void onPlayerItemConsume(final PlayerItemConsumeEvent ev)
		{
			if (!ev.getItem().isSimilar(KitEsteban.potionItem))
				return;

			HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			luckEffect(hp, 0.0);
		}

		/**
		 * Applies luck effect to players around splash potion explosion ares
		 * @param ev Event
		 */
		@EventHandler
		public void onProjectileHit(final ProjectileHitEvent ev)
		{
			if (!(ev.getEntity() instanceof ThrownPotion))
				return;

			final ThrownPotion pot = (ThrownPotion)ev.getEntity();
			if (!pot.getItem().isSimilar(KitEsteban.splashPotionItem))
				return;
			if (!(pot.getShooter() instanceof Player)) // Dispensers
				return;
			final HuntPlayer thrower = Game.getPlayer(((Player)pot.getShooter()).getName());

			final Location loc = ev.getEntity().getLocation();
			loc.getWorld().spawnParticle(Particle.GLOW, loc.getX(), loc.getY(), loc.getZ(), 150, 0.8, 0.4, 0.8);

			for (HashMap.Entry<String, HuntPlayer> set : Game.getPlayerList().entrySet())
			{
				HuntPlayer hp = set.getValue();
				if (!hp.isAlive() || hp.getPlayer().getWorld() != pot.getWorld())
					continue;
				if (!thrower.canDamage(hp) && thrower != hp)
					continue;
				double dist = hp.getPlayer().getLocation().distanceSquared(pot.getLocation());
				if (dist <= 16.0) // 4.0
				{
					luckEffect(hp, Math.sqrt(dist));
					if (thrower.canDamage(hp) && (hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))) // Register as attacked
						hp.registerAttack(thrower);
				}
			}
		}

		/**
		 * Shoots dirt when attempting to drop dirt
		 * @param ev Event
		 */
		@EventHandler
		public void onPlayerDropItem(PlayerDropItemEvent ev)
		{
			if (ev.getItemDrop().getItemStack().getType() != Material.DIRT)
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))
				return;

			World w = hp.getPlayer().getWorld();
			FallingBlock falling = w.spawnFallingBlock(hp.getPlayer().getLocation().add(0, hp.getPlayer().getEyeHeight(), 0), Material.DIRT.createBlockData());
			falling.setVelocity(hp.getPlayer()
				.getLocation()
				.getDirection()
				.normalize()
				.multiply(1.5f)
			);
			falling.setDropItem(false);

			ev.getItemDrop().remove();
		}

		/**
		 * Shoots tornado on right click
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null)
				return;
			if (!ev.getItem().isSimilar(KitEsteban.tornadoItem))
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))
				return;

			// Stop if clicking an inventory
			if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;
			// Stop if player has too many active tornadoes
			if (((KitEsteban)hp.getKit()).tornadoCount >= 3)
			{
				hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6Vous avez déjà 3 tornades!"));
				return;
			}

			ev.getItem().setAmount(ev.getItem().getAmount()-1);

			final double height = 10.0; // Height in blocks
			final double radius = 3.0; // Radius in blocks
			final Vector speed = new Vector(0.35, 0.4, 0.35); // Speed in blocks/ticks
			final int duration = 180; // Duration in ticks
			final int maxBlocks = 30; // Maximum number of blocks in the tornado at once

			final Location loc = hp.getPlayer().getLocation();
			// Tornado's position vector
			Vector position = new Vector(loc.getX(), loc.getY(), loc.getZ());
			// Tornado's direction vector
			Vector direction = loc.getDirection().normalize();

			if (hp.getPlayer().isSneaking())
				position.add((new Vector(0.0, -3.0, 0.0)).multiply(direction));
			else
				position.add((new Vector(4.0, 5.0, 4.0)).multiply(direction));

			// Spawn some falling dirt blocks
			{
				double x = position.getX(), y = position.getY(), z = position.getZ();
				float f = 0.f;

				loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x, y+height, z), Material.DIRT.createBlockData());
				for (int i = 0; i < 4; ++i)
				{
					loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x+radius*f, y+height*f, z), Material.DIRT.createBlockData());
					loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x-radius*f, y+height*f, z), Material.DIRT.createBlockData());
					loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x, y+height*f, z+radius*f), Material.DIRT.createBlockData());
					loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x, y+height*f, z-radius*f), Material.DIRT.createBlockData());
					if (f >= .5f)
					{
						loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x+radius*f/1.41, y+height*f, z+radius*f/1.41), Material.DIRT.createBlockData());
						loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x+radius*f/1.41, y+height*f, z-radius*f/1.41), Material.DIRT.createBlockData());
						loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x-radius*f/1.41, y+height*f, z+radius*f/1.41), Material.DIRT.createBlockData());
						loc.getWorld().spawnFallingBlock(new Location(loc.getWorld(), x-radius*f/1.41, y+height*f, z-radius*f/1.41), Material.DIRT.createBlockData());
					}
					f += .3f;
				}
			}


			new BukkitRunnable()
			{
				int ticks = 0;
				int blocks = 0;
				Vector lastDirection = direction;
				final HuntPlayer owner = hp; // Original owner

				// NOTE: hp Might change while the runnable is running
				KitEsteban k = (KitEsteban)hp.getKit();

				public void end()
				{
					this.cancel();
					k.tornadoCount -= 1;
				}

				public void run()
				{
					if (ticks == duration || Game.isPaused())
						end();

					Vector velocity = lastDirection.clone().multiply(speed);
					position.add(velocity);
					double x = position.getX(), y = position.getY(), z = position.getZ();

					// Particles
					{
						float f = 0.f;
						for (int i = 0; i < 5; ++i)
						{
							loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x+f*radius, y+f*height, z, 1);
							loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x-f*radius, y+f*height, z, 1);
							loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x, y+f*height, z+f*radius, 1);
							loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x, y+f*height, z-f*radius, 1);
							if (f > .5f)
							{
								loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x+f*radius/1.41, y+f*height, z+f*radius/1.41, 1);
								loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x+f*radius/1.41, y+f*height, z-f*radius/1.41, 1);
								loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x-f*radius/1.41, y+f*height, z+f*radius/1.41, 1);
								loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, x-f*radius/1.41, y+f*height, z-f*radius/1.41, 1);
							}
							f += .2f;
						}
					}

					if (ticks % 200 == 0)
						loc.getWorld().playSound(loc, Sound.ITEM_ELYTRA_FLYING, SoundCategory.MASTER, 40.f, 0.8f);

					// Block breaking
					for (double curY = -1.0; curY < height; curY += 1.0)
					{
						double curR = 1 + curY * radius / height; // Radius of current cone slice
						int rsq = (int)(curR * curR);

						for (int _x = -(int)curR; _x <= (int)curR; ++_x)
						{
							for (int _z = -(int)curR; _z <= (int)curR; ++_z)
							{
								if (_x*_x + _z*_z >= rsq)
									continue;

								final Block b = loc.getWorld().getBlockAt((int)x+_x, (int)(y+curY), (int)z+_z);
								if (b.getType() == Material.AIR)
									continue;
								else if (b.getType() == Material.NETHER_PORTAL) // Explodes
								{
									loc.getWorld().createExplosion(b.getLocation(), 5, true);
									end();
								}
								else if (!Util.containsMaterial(Util.nonMovableBlocks, b.getType()))
								{
									if (b.getType() == Material.TNT)
										loc.getWorld().spawnEntity(b.getLocation(), EntityType.PRIMED_TNT);
									if (blocks <= maxBlocks && Game.nextPosInt() % 10 == 0)
									{
										loc.getWorld().spawnFallingBlock(b.getLocation(), b.getType().createBlockData());
										++blocks;
									}
									b.setType(Material.AIR);
								}
							}
						}
					}

					// Physics
					for (Entity ent : loc.getWorld().getEntities())
					{
						x = ent.getLocation().getX() - position.getX();
						y = ent.getLocation().getY() - position.getY();
						z = ent.getLocation().getZ() - position.getZ();

						if (y < -1.5)
							continue;
						if (y > height)
							continue;

						final double rho = 1.0 + (1.5 + y) * radius / height; // Radius(y) := ... [with some additional margin]
						if (rho*rho < x*x + z*z)
							continue;

						final double phi; // in [-π, +π]
						if (x == 0.0 && z == 0.0)
							phi = 0;
						else if (z < 0.0)
							phi = -Math.atan2(z, x);
						else
							phi = Math.atan2(z, x);

						// rho * phi'
						final double rp = y * radius / height * 0.12;
						Vector v = new Vector(-rp * Math.sin(phi), 0.19 / (1.0 + y), rp * Math.cos(phi));

						if (ent instanceof Player)
						{
							HuntPlayer hp = Game.getPlayer(ent.getName());
							if (!hp.isAlive())
								continue;

							hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 2));
							hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
							if (hp == owner)
							{
								lastDirection = hp.getPlayer().getLocation().getDirection().normalize();
								v.setX(0);
								v.setY(0.09 / (1.0+y));
								v.setZ(0);
							}
						}
						v.add(velocity);
						ent.setVelocity(v);
					}
					++ticks;
				}
			}.runTaskTimer(Game.getPlugin(), 0, 1);

			((KitEsteban)hp.getKit()).tornadoCount += 1;
		}

		@EventHandler
		public void onSpawn(final HPSpawnEvent ev)
		{
			final HuntPlayer hp = ev.getPlayer();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitEsteban))
				return;

			PlayerInteractions.giveItem(hp, new ItemStack[]{tornadoItem}, true, true);
		}

		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			HuntPlayer.forEach(hp ->
			{
				PlayerInteractions.schedule(hp, (o) ->
				{
					hp.getPlayer().discoverRecipes(Arrays.asList(
						new NamespacedKey(Game.getPlugin(), "esteban_tornado"),
						new NamespacedKey(Game.getPlugin(), "esteban_potion"),
						new NamespacedKey(Game.getPlugin(), "esteban_splash_potion")
					));
				});
			});
		}
	}
}
