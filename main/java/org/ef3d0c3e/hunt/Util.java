package org.ef3d0c3e.hunt;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.ef3d0c3e.hunt.island.Island;
import org.ef3d0c3e.hunt.kits.entities.MehdiBee;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;

public class Util
{
	public static String generateUUID()
	{
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			builder.append(chars.charAt((int) (Math.random() * chars.length())));
		}
		return builder.toString();
	}

	static public boolean containsItem(ItemStack[] arr, ItemStack elem)
	{
		for (ItemStack x : arr)
		{
			if (elem.isSimilar(x))
				return true;
		}

		return false;
	}
	
	static final public Material rightClickableBlocks[] =
	{
		// Only includes blocks that can be right clicked without holding
		// any particular item.
		// Also: Does not include doors, trapdoors and redstone component
		Material.CHEST,
		Material.TRAPPED_CHEST,
		Material.ENDER_CHEST,
		Material.CRAFTING_TABLE,
		Material.FURNACE,
		Material.ENCHANTING_TABLE,
		Material.DROPPER,
		Material.DISPENSER,
		Material.ANVIL,
		Material.DAMAGED_ANVIL,
		Material.CHIPPED_ANVIL,
		Material.NOTE_BLOCK,
		Material.SHULKER_BOX,
		Material.LOOM,
		Material.BARREL,
		Material.SMOKER,
		Material.BLAST_FURNACE,
		Material.CARTOGRAPHY_TABLE,
		Material.FLETCHING_TABLE,
		Material.GRINDSTONE,
		Material.SMITHING_TABLE,
		Material.STONECUTTER,
		Material.LECTERN,
		Material.BEACON,
	};

	public final static Material nonMovableBlocks[] =
	{
		Material.ENCHANTING_TABLE,
		Material.OBSIDIAN,
		Material.BEDROCK,
		Material.COMMAND_BLOCK,
		Material.BARRIER,
		Material.LAVA,
		Material.WATER,
		Material.END_PORTAL_FRAME,
		Material.END_PORTAL,
		Material.END_GATEWAY,
	};
	
	public final static Material flowersBlocks[] =
	{
		Material.DANDELION,
		Material.POPPY,
		Material.BLUE_ORCHID,
		Material.ALLIUM,
		Material.AZURE_BLUET,
		Material.RED_TULIP,
		Material.ORANGE_TULIP,
		Material.WHITE_TULIP,
		Material.PINK_TULIP,
		Material.OXEYE_DAISY,
		Material.CORNFLOWER,
		Material.LILY_OF_THE_VALLEY,
		Material.SPORE_BLOSSOM,
		Material.SUNFLOWER,
		Material.LILAC,
		Material.ROSE_BUSH,
		Material.WITHER_ROSE,
		Material.PEONY,
		Material.FLOWERING_AZALEA,
		Material.FLOWERING_AZALEA_LEAVES,
	};
	
	public final static Material logBlocks[] =
	{
		Material.OAK_LOG,
		Material.SPRUCE_LOG,
		Material.BIRCH_LOG,
		Material.JUNGLE_LOG,
		Material.ACACIA_LOG,
		Material.DARK_OAK_LOG,
		Material.CRIMSON_STEM,
		Material.WARPED_STEM,
	};

	public final static Material leaveBlocks[] =
	{
		Material.OAK_LEAVES,
		Material.SPRUCE_LEAVES,
		Material.BIRCH_LEAVES,
		Material.JUNGLE_LEAVES,
		Material.ACACIA_LEAVES,
		Material.DARK_OAK_LEAVES,
		Material.CRIMSON_HYPHAE,
		Material.WARPED_HYPHAE,
		Material.AZALEA_LEAVES,
		Material.FLOWERING_AZALEA_LEAVES,
	};

	public final static Material plantBlocks[] =
	{
		Material.DANDELION,
		Material.POPPY,
		Material.BLUE_ORCHID,
		Material.ALLIUM,
		Material.AZURE_BLUET,
		Material.RED_TULIP,
		Material.ORANGE_TULIP,
		Material.WHITE_TULIP,
		Material.PINK_TULIP,
		Material.OXEYE_DAISY,
		Material.CORNFLOWER,
		Material.LILY_OF_THE_VALLEY,
		Material.SPORE_BLOSSOM,
		Material.SUNFLOWER,
		Material.LILAC,
		Material.ROSE_BUSH,
		Material.WITHER_ROSE,
		Material.PEONY,
		Material.GRASS,
		Material.FERN,
		Material.AZALEA,
		Material.FLOWERING_AZALEA,
		Material.OAK_SAPLING,
		Material.SPRUCE_SAPLING,
		Material.BIRCH_SAPLING,
		Material.JUNGLE_SAPLING,
		Material.ACACIA_SAPLING,
		Material.DARK_OAK_SAPLING,
		Material.SUGAR_CANE,
		Material.CACTUS,
		Material.TALL_GRASS,
		Material.LARGE_FERN,
	};

	public final static Material pickaxe[] =
	{
		Material.WOODEN_PICKAXE,
		Material.STONE_PICKAXE,
		Material.IRON_PICKAXE,
		Material.GOLDEN_PICKAXE,
		Material.DIAMOND_PICKAXE,
		Material.NETHERITE_PICKAXE,
	};

	public final static Material carpets[] =
	{
		Material.WHITE_CARPET,
		Material.ORANGE_CARPET,
		Material.MAGENTA_CARPET,
		Material.LIGHT_BLUE_CARPET,
		Material.YELLOW_CARPET,
		Material.LIME_CARPET,
		Material.PINK_CARPET,
		Material.GRAY_CARPET,
		Material.LIGHT_GRAY_CARPET,
		Material.CYAN_CARPET,
		Material.PURPLE_CARPET,
		Material.BLUE_CARPET,
		Material.BROWN_CARPET,
		Material.GREEN_CARPET,
		Material.RED_CARPET,
		Material.BLACK_CARPET,
	};

	static public boolean containsMaterial(Material[] arr, Material elem)
	{
		for (Material x : arr)
		{
			if (elem == x)
				return true;
		}

		return false;
	}

	public static boolean hasItems(Inventory inv, Material mat, int amt)
	{
		HashMap<Integer, ? extends ItemStack> map = inv.all(mat);
		if (map.isEmpty())
			return false;

		int total = 0;
		for (Map.Entry<Integer, ? extends ItemStack> set : map.entrySet())
		{
			total += set.getValue().getAmount();

			if (total >= amt)
				return true;
		}

		return false;
	}

	public static void removeSingleItem(Inventory inv, Material mat)
	{
		HashMap<Integer, ? extends ItemStack> map = inv.all(mat);
		if (map.isEmpty())
			return;
		
		for (Map.Entry<Integer, ? extends ItemStack> set : map.entrySet())
		{
			if (set.getValue().getAmount() == 0)
				continue;
			
			if (set.getValue().getAmount() == 1)
				inv.setItem(set.getKey(), null);
			else
			{
				set.getValue().setAmount(set.getValue().getAmount() - 1);
				inv.setItem(set.getKey(), set.getValue());
			}
			break;
		}
	}

	public static int removeItems(Inventory inv, Material mat, int amt)
	{
		HashMap<Integer, ? extends ItemStack> map = inv.all(mat);
		if (map.isEmpty())
			return 0;

		int removed = 0;
		for (Map.Entry<Integer, ? extends ItemStack> set : map.entrySet())
		{
			if (set.getValue().getAmount() == 0)
				continue;

			if (set.getValue().getAmount() == 1)
			{
				removed += 1;
				inv.setItem(set.getKey(), null);
			}
			else if (set.getValue().getAmount() > amt-removed)
			{
				set.getValue().setAmount(set.getValue().getAmount() + removed - amt);
				removed += amt - removed;
				inv.setItem(set.getKey(), set.getValue());
			}
			else
			{
				removed += set.getValue().getAmount();
				inv.setItem(set.getKey(), null);
			}

			if (removed == amt)
				return amt;
		}
		return removed; // Returns the number of items that could be removed
	}

	/**
	 * Gets the player attacker in a ```EntityDamageByEntityEvent```
	 * @param ev Event
	 * @return Player if found, null otherwise
	 */
	public static HuntPlayer getPlayerAttacker(EntityDamageByEntityEvent ev)
	{
		if (ev.getDamager() instanceof Player)
			return HuntPlayer.getPlayer((Player)ev.getDamager());
		else if (ev.getDamager() instanceof Projectile)
		{
			final ProjectileSource shooter = ((Projectile)ev.getDamager()).getShooter();
			if (shooter instanceof Player)
				return HuntPlayer.getPlayer(((Player)shooter).getName());
			else
				return null;
		}
		else if (ev.getDamager() instanceof Tameable)
		{
			final AnimalTamer owner = ((Tameable)ev.getDamager()).getOwner();
			if (owner instanceof Player)
				return HuntPlayer.getPlayer((Player)owner);
			else
				return null;
		}
		else
			return null;
	}

	/**
	 * Compares items by checking if they have the same display name & type
	 * @param a First item
	 * @param b Second item
	 * @return True if a and b have the same type and display name
	 *
	 * @note a == b IFF b == a
	 */
	public static boolean isSimilarBasic(final ItemStack a, final ItemStack b)
	{
		final ItemMeta ma = a.getItemMeta();
		final ItemMeta mb = b.getItemMeta();

		return a.getType() == b.getType() &&
			ma.getDisplayName().equals(mb.getDisplayName());
	}

	public static String getLoadingBar(String background, String loaded, int num, int progress)
	{
		String ret = new String();
		for (int i = 0; i < progress; ++i)
			ret += loaded;
		for (int i = progress; i < num; ++i)
			ret += background;

		return ret;
	}

	/**
	 * Spawn tomb containing player's inventory
	 * @param loc Tomb's location
	 * @param hp Player
	 */
	public static void spawnTomb(final Location loc, final HuntPlayer hp)
	{
		World w = loc.getWorld();
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		int z = (int) loc.getZ();
		{
			(new Location(w, x, y + 2, z)).getBlock().setType(Material.AIR);
			(new Location(w, x + 1, y + 2, z)).getBlock().setType(Material.AIR);

			Block b1 = (new Location(w, x, y + 1, z)).getBlock();
			Block b2 = (new Location(w, x + 1, y + 1, z)).getBlock();
			b1.setType(Material.CHEST);
			b2.setType(Material.CHEST);
			Chest c1 = (Chest) b1.getState();
			Chest c2 = (Chest) b2.getState();
			org.bukkit.block.data.type.Chest d1 = (org.bukkit.block.data.type.Chest) c1.getBlockData();
			org.bukkit.block.data.type.Chest d2 = (org.bukkit.block.data.type.Chest) c2.getBlockData();
			d1.setType(org.bukkit.block.data.type.Chest.Type.LEFT);
			b1.setBlockData(d1, true);
			d2.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
			b2.setBlockData(d2, true);
		}

		Chest chest = (Chest) (new Location(w, x, y + 1, z)).getBlock().getState();
		chest.setCustomName(MessageFormat.format("§cTombe de {0}", hp.getName()));
		chest.update();

		Inventory inv = chest.getInventory();
		final PlayerInventory pinv = hp.getPlayer().getInventory();
		for (int i = 0; i < 9 * 4; ++i)
		{
			if (pinv.getItem(i) == null)
				continue;
			if (Game.isKitMode() && hp.getKit() != null && !hp.getKit().itemFilter(pinv.getItem(i)))
				continue;
			if (Game.isIslandMode() && !Island.itemFilter(pinv.getItem(i)))
				continue;
			if (Game.isRoundMode() && !Round.itemFilter(pinv.getItem(i)))
				continue;
			inv.setItem(i, pinv.getItem(i));
		}
		inv.setItem(36, pinv.getItem(EquipmentSlot.OFF_HAND));
		inv.setItem(37, pinv.getItem(EquipmentSlot.FEET));
		inv.setItem(38, pinv.getItem(EquipmentSlot.LEGS));
		inv.setItem(39, pinv.getItem(EquipmentSlot.CHEST));
		inv.setItem(40, pinv.getItem(EquipmentSlot.HEAD));

		// Armor stands
		ArmorStand armorstands[] = {
			(ArmorStand) w.spawnEntity(new Location(w, x + 0.5, y, z + 1.5), EntityType.ARMOR_STAND),
			(ArmorStand) w.spawnEntity(new Location(w, x + 0.5, y, z - 0.5), EntityType.ARMOR_STAND),
			(ArmorStand) w.spawnEntity(new Location(w, x + 1.5, y, z + 1.5), EntityType.ARMOR_STAND),
			(ArmorStand) w.spawnEntity(new Location(w, x + 1.5, y, z - 0.5), EntityType.ARMOR_STAND),
		};
		for (ArmorStand ent : armorstands)
		{
			ent.setInvulnerable(true);
			ent.setBasePlate(false);
			ent.setMarker(true);
			ent.setRotation(90.f, 0.f);
			EntityEquipment equip = ent.getEquipment();
			equip.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
			equip.setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
			equip.setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
			equip.setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
		}
	}

	/**
	 * Replaces a recipe
	 * @param res Original recipe's result to replace
	 * @param replace Item to replace it with
	 */
	static void replaceRecipe(final Material res, final ItemStack replace)
	{
		Iterator<Recipe> it = Bukkit.getServer().recipeIterator();
		while (it.hasNext())
		{
			Recipe r = it.next();
			if (r != null && r.getResult().getType() != res)
				continue;

			if (r instanceof ShapedRecipe)
			{
				ShapedRecipe s = (ShapedRecipe)r;

				ShapedRecipe n = new ShapedRecipe(s.getKey(), replace);
				String[] shape = s.getShape();
				if (shape.length == 1)
					n.shape(shape[0]);
				else if (shape.length == 2)
					n.shape(shape[0], shape[1]);
				else if (shape.length == 3)
					n.shape(shape[0], shape[1], shape[2]);
				for (final Map.Entry<Character, RecipeChoice> set : s.getChoiceMap().entrySet())
					n.setIngredient(set.getKey(), set.getValue());


				Bukkit.removeRecipe(s.getKey());
				Bukkit.addRecipe(n);
			}
			else if (r instanceof ShapelessRecipe)
			{
				ShapelessRecipe s = (ShapelessRecipe)r;

				ShapelessRecipe n = new ShapelessRecipe(s.getKey(), replace);
				for (final RecipeChoice choice : s.getChoiceList())
					n.addIngredient(choice);

				n.setGroup(s.getGroup());

				Bukkit.removeRecipe(s.getKey());
				Bukkit.addRecipe(n);
			}

			return;
		}
	}

	/**
	 * Gets number of random drops
	 * @param min Minimum number of drops
	 * @param max Maximum number of drops
	 * @param chance Avg chance (.5 = neutral)
	 * @param fortune Fortune level to apply
	 * @return Number of drops
	 */
	public static int randomDrop(final int min, final int max, double chance, int fortune)
	{
		if (chance == 0.0)
			return min;
		else if (chance == 1.0)
			return max;
		else
		{
			final double x = (Game.nextPosInt() % 8192)/8192.0; // Do not include '1.0' as it would cause overflow

			return (int)(min + Math.pow(x, -Math.log(chance) / Math.log(2.0) / (fortune*0.65 + 1.0)) * (max-min + 1));
		}
	}

	/**
	 * Gets the player killer from killer entity
	 * @param killer Killer entity
	 * @return Player killer or null if not found
	 */
	public static HuntPlayer getPlayerKiller(final LivingEntity killer)
	{
		if (killer == null)
			return null;

		if (killer instanceof Player)
			return HuntPlayer.getPlayer((Player)killer);

		if (killer instanceof MehdiBee)
			return ((MehdiBee)killer).getOwner();

		if (killer instanceof Tameable)
		{
			final AnimalTamer owner = ((Tameable)killer).getOwner();
			if (owner == null || !(owner instanceof Player))
				return null;

			return HuntPlayer.getPlayer((Player)owner);
		}

		if (killer instanceof Projectile)
		{
			final ProjectileSource shooter = ((Projectile)killer).getShooter();
			if (shooter == null || !(shooter instanceof Player))
				return null;

			return HuntPlayer.getPlayer((Player)shooter);
		}

		return null;
	}

	public interface MessagePredicate
	{
		public boolean operation(final HuntPlayer hp);
	}

	/**
	 * Messages all online player
	 * @param msg1 First message
	 * @param msg2 Second message
	 * @param pred Predicate
	 */
	public static void messagePredicate(final String msg1, final String msg2, final MessagePredicate pred)
	{
		HuntPlayer.forEach(hp -> {
			if (!hp.isOnline())
				return;

			if (pred.operation(hp))
				hp.getPlayer().sendMessage(msg1);
			else
				hp.getPlayer().sendMessage(msg2);
		});
	}
}
