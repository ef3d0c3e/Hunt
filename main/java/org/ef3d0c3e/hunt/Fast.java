package org.ef3d0c3e.hunt;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class Fast
{
	private static ItemStack m_woodenPick;
	private static ItemStack m_stonePick;
	private static ItemStack m_ironPick;
	private static ItemStack m_diamondPick;
	private static ItemStack m_netheritePick;

	private static ItemStack m_woodenShovel;
	private static ItemStack m_stoneShovel;
	private static ItemStack m_ironShovel;
	private static ItemStack m_diamondShovel;
	private static ItemStack m_netheriteShovel;

	private static ItemStack m_woodenAxe;
	private static ItemStack m_stoneAxe;
	private static ItemStack m_ironAxe;
	private static ItemStack m_diamondAxe;
	private static ItemStack m_netheriteAxe;


	/**
	 * Initializes items
	 */
	public static void init()
	{
		m_woodenPick = new ItemStack(Material.WOODEN_PICKAXE);
		m_woodenPick.addEnchantment(Enchantment.DURABILITY, 1);
		m_woodenPick.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_stonePick = new ItemStack(Material.STONE_PICKAXE);
		m_stonePick.addEnchantment(Enchantment.DURABILITY, 1);
		m_stonePick.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_ironPick = new ItemStack(Material.IRON_PICKAXE);
		m_ironPick.addEnchantment(Enchantment.DURABILITY, 1);
		m_ironPick.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_diamondPick = new ItemStack(Material.DIAMOND_PICKAXE);
		m_diamondPick.addEnchantment(Enchantment.DURABILITY, 1);
		m_diamondPick.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_netheritePick = new ItemStack(Material.NETHERITE_PICKAXE);
		m_netheritePick.addEnchantment(Enchantment.DURABILITY, 1);
		m_netheritePick.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_woodenShovel = new ItemStack(Material.WOODEN_SHOVEL);
		m_woodenShovel.addEnchantment(Enchantment.DURABILITY, 1);
		m_woodenShovel.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_stoneShovel = new ItemStack(Material.STONE_SHOVEL);
		m_stoneShovel.addEnchantment(Enchantment.DURABILITY, 1);
		m_stoneShovel.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_ironShovel = new ItemStack(Material.IRON_SHOVEL);
		m_ironShovel.addEnchantment(Enchantment.DURABILITY, 1);
		m_ironShovel.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_diamondShovel = new ItemStack(Material.DIAMOND_SHOVEL);
		m_diamondShovel.addEnchantment(Enchantment.DURABILITY, 1);
		m_diamondShovel.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_netheriteShovel = new ItemStack(Material.NETHERITE_SHOVEL);
		m_netheriteShovel.addEnchantment(Enchantment.DURABILITY, 1);
		m_netheriteShovel.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_woodenAxe = new ItemStack(Material.WOODEN_AXE);
		m_woodenAxe.addEnchantment(Enchantment.DURABILITY, 1);
		m_woodenAxe.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_stoneAxe = new ItemStack(Material.STONE_AXE);
		m_stoneAxe.addEnchantment(Enchantment.DURABILITY, 1);
		m_stoneAxe.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_ironAxe = new ItemStack(Material.IRON_AXE);
		m_ironAxe.addEnchantment(Enchantment.DURABILITY, 1);
		m_ironAxe.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_diamondAxe = new ItemStack(Material.DIAMOND_AXE);
		m_diamondAxe.addEnchantment(Enchantment.DURABILITY, 1);
		m_diamondAxe.addEnchantment(Enchantment.DIG_SPEED, 2);

		m_netheriteAxe = new ItemStack(Material.NETHERITE_AXE);
		m_netheriteAxe.addEnchantment(Enchantment.DURABILITY, 1);
		m_netheriteAxe.addEnchantment(Enchantment.DIG_SPEED, 2);

		Util.replaceRecipe(Material.WOODEN_PICKAXE, m_woodenPick);
		Util.replaceRecipe(Material.STONE_PICKAXE, m_stonePick);
		Util.replaceRecipe(Material.IRON_PICKAXE, m_ironPick);
		Util.replaceRecipe(Material.DIAMOND_PICKAXE, m_diamondPick);
		Util.replaceRecipe(Material.NETHERITE_PICKAXE, m_netheritePick);

		Util.replaceRecipe(Material.WOODEN_SHOVEL, m_woodenShovel);
		Util.replaceRecipe(Material.STONE_SHOVEL, m_stoneShovel);
		Util.replaceRecipe(Material.IRON_SHOVEL, m_ironShovel);
		Util.replaceRecipe(Material.DIAMOND_SHOVEL, m_diamondShovel);
		Util.replaceRecipe(Material.NETHERITE_SHOVEL, m_netheriteShovel);

		Util.replaceRecipe(Material.WOODEN_AXE, m_woodenAxe);
		Util.replaceRecipe(Material.STONE_AXE, m_stoneAxe);
		Util.replaceRecipe(Material.IRON_AXE, m_ironAxe);
		Util.replaceRecipe(Material.DIAMOND_AXE, m_diamondAxe);
		Util.replaceRecipe(Material.NETHERITE_AXE, m_netheriteAxe);
	}

	/**
	 * Periodic effects
	 */
	public static void start()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (Game.inHunt())
					this.cancel();

				for (HuntPlayer hp : Game.getPlayerList().values())
				{
					if (!hp.isOnline())
						continue;

					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0));
					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 200, 1));
					hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 0));
				}
			}
		}.runTaskTimer(Game.getPlugin(), 20, 100);
	}

	/**
	 * Events for the fast mode
	 */
	public static class FastEvents implements Listener
	{
		/**
		 * Automatically adds lapis lazuli in player's enchanting table
		 * @param ev
		 */
		@EventHandler
		public void onEnchantingTableOpen(InventoryOpenEvent ev)
		{
			if (ev.getInventory().getType() != InventoryType.ENCHANTING)
				return;

			ev.getInventory().setItem(1, new ItemStack(Material.LAPIS_LAZULI, 64));
		}

		/**
		 * Automatically removes lapis lazuli in player's enchanting table
		 * @param ev
		 */
		@EventHandler
		public void onEnchantingTableClose(InventoryCloseEvent ev)
		{
			if (ev.getInventory().getType() != InventoryType.ENCHANTING)
				return;

			ev.getInventory().setItem(1, null);
		}


		/**
		 * Prevents player from stealing lapis lazuli in enchanting table
		 * @param ev Event
		 */
		@EventHandler
		public void onEnchantTableClick(InventoryClickEvent ev)
		{
			if (ev.getClickedInventory() == null || ev.getClickedInventory().getType() != InventoryType.ENCHANTING)
				return;
			if (ev.getCurrentItem() == null || ev.getCurrentItem().getType() != Material.LAPIS_LAZULI)
				return;

			ev.setCancelled(true);
		}

		/**
		 * Changes drops of mined blocks
		 * @param ev Event
		 */
		@EventHandler
		public void onMine(BlockBreakEvent ev)
		{
			int fortune = 0;
			if (ev.getPlayer().getInventory().getItemInMainHand() != null)
				fortune = ev.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);

			if (ev.getBlock().getType() == Material.COAL_ORE || ev.getBlock().getType() == Material.DEEPSLATE_COAL_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(5, 10, 0.5, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.COAL, Util.randomDrop(1, 2+fortune, 0.80, fortune)));
			}
			else if (ev.getBlock().getType() == Material.COPPER_ORE || ev.getBlock().getType() == Material.DEEPSLATE_COPPER_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(7, 11, 0.5, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.COPPER_INGOT, Util.randomDrop(1, 2+fortune, 0.70, fortune)));
			}
			else if (ev.getBlock().getType() == Material.IRON_ORE || ev.getBlock().getType() == Material.DEEPSLATE_IRON_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(7, 11, 0.5, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.IRON_INGOT, Util.randomDrop(1, 2+fortune, 0.75, fortune)));
			}
			else if (ev.getBlock().getType() == Material.REDSTONE_ORE || ev.getBlock().getType() == Material.DEEPSLATE_REDSTONE_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(6, 10, 0.6, fortune));
			}
			else if (ev.getBlock().getType() == Material.LAPIS_ORE || ev.getBlock().getType() == Material.DEEPSLATE_LAPIS_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(12, 24, 0.5, fortune));
				ev.setDropItems(false);
			}
			else if (ev.getBlock().getType() == Material.GOLD_ORE || ev.getBlock().getType() == Material.DEEPSLATE_GOLD_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(8, 12, 0.6, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.GOLD_INGOT, Util.randomDrop(1, 2+fortune, 0.5, fortune)));
			}
			else if (ev.getBlock().getType() == Material.NETHER_GOLD_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(5, 10, 0.7, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.GOLD_INGOT, Util.randomDrop(1, 2+fortune, 0.3, fortune)));
			}
			else if (ev.getBlock().getType() == Material.NETHER_QUARTZ_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(5, 10, 0.75, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.QUARTZ, Util.randomDrop(4, 8+fortune, 0.6, fortune)));
			}
			else if (ev.getBlock().getType() == Material.EMERALD_ORE || ev.getBlock().getType() == Material.DEEPSLATE_EMERALD_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(10, 16, 0.75, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.EMERALD, Util.randomDrop(1, 1+fortune, 0.6, fortune)));
			}
			else if (ev.getBlock().getType() == Material.DIAMOND_ORE || ev.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE)
			{
				ev.setExpToDrop(Util.randomDrop(10, 16, 0.75, fortune));
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.DIAMOND, Util.randomDrop(1, 1+fortune, 0.3, fortune)));
			}
			else if (ev.getBlock().getType() == Material.CARROTS)
			{
				ev.setDropItems(false);
				int amt = Util.randomDrop(0, 1+fortune, 0.3, fortune);
				if (amt != 0)
					ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
						new ItemStack(Material.GOLDEN_CARROT, amt));
			}
			else if (ev.getBlock().getType() == Material.POTATOES)
			{
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.BAKED_POTATO, Util.randomDrop(1, 3+fortune, 0.4, fortune)));
			}
			else if (ev.getBlock().getType() == Material.WHEAT)
			{
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.BREAD, Util.randomDrop(1, 2+fortune, 0.45, fortune)));
			}
			else if (ev.getBlock().getType() == Material.COCOA)
			{
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.COOKIE, Util.randomDrop(1, 3+fortune, 0.65, fortune)));
			}
			else if (ev.getBlock().getType() == Material.GRAVEL)
			{
				ev.setDropItems(false);
				int amt = Util.randomDrop(0, 1, 0.1, fortune);
				if (amt != 0)
					ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
						new ItemStack(Material.FLINT_AND_STEEL, amt));
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.ARROW, Util.randomDrop(1, 2+fortune, 0.5, fortune)));
			}
		}

		/**
		 * Drops cooked meat from killed animals & Increase spider eye drop rate
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDeath(EntityDeathEvent ev)
		{
			final HuntPlayer killer = Util.getPlayerKiller(ev.getEntity().getKiller());
			if (killer == null)
				return;

			int looting = 0; // Looting stack
			if (killer.getPlayer().getInventory().getItemInMainHand() != null)
				looting = killer.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

			if (ev.getEntity() instanceof Chicken)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.CHICKEN || stack.getType() == Material.FEATHER);
				ev.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, Util.randomDrop(1, 2+looting, 0.6, looting)));
				ev.getDrops().add(new ItemStack(Material.ARROW, Util.randomDrop(1, 2+looting, 0.7, looting)));
			}
			else if (ev.getEntity() instanceof Sheep)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.MUTTON);
				ev.getDrops().add(new ItemStack(Material.COOKED_MUTTON, Util.randomDrop(1, 2+looting, 0.6, looting)));
				ev.getDrops().add(new ItemStack(Material.STRING, Util.randomDrop(1, 2+looting, 0.3, looting)));
			}
			else if (ev.getEntity() instanceof Pig)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.PORKCHOP);
				ev.getDrops().add(new ItemStack(Material.COOKED_PORKCHOP, Util.randomDrop(1, 2+looting, 0.6, looting)));
			}
			else if (ev.getEntity() instanceof Cow)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.BEEF);
				ev.getDrops().add(new ItemStack(Material.COOKED_BEEF, Util.randomDrop(1, 2+looting, 0.6, looting)));
				int amt = Util.randomDrop(0, 1+looting, 0.65, looting);
				if (amt != 0)
					ev.getDrops().add(new ItemStack(Material.BOOK, amt));
			}
			else if (ev.getEntity() instanceof Rabbit)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.RABBIT);
				ev.getDrops().add(new ItemStack(Material.COOKED_RABBIT, Util.randomDrop(1, 2+looting, 0.6, looting)));
			}
			else if (ev.getEntity() instanceof Cod)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.COD);
				ev.getDrops().add(new ItemStack(Material.COOKED_COD, Util.randomDrop(1, 2+looting, 0.6, looting)));
			}
			else if (ev.getEntity() instanceof Salmon)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.SALMON);
				ev.getDrops().add(new ItemStack(Material.COOKED_SALMON, Util.randomDrop(1, 2+looting, 0.6, looting)));
			}
			else if (ev.getEntity() instanceof Spider)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.SPIDER_EYE);
				int amt = Util.randomDrop(0, 1+looting, 0.65, looting);
				if (amt != 0)
					ev.getDrops().add(new ItemStack(Material.SPIDER_EYE, amt));
			}
		}

		/**
		 * Instantly breaks logs & breaks obsidian faster
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockDamage(BlockDamageEvent ev)
		{
			if (Util.containsMaterial(Util.logBlocks, ev.getBlock().getType()))
				ev.setInstaBreak(true);
			else if (ev.getBlock().getType() == Material.OBSIDIAN)
				ev.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 10, 2));
		}
	}
}
