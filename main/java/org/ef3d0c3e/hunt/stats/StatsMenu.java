package org.ef3d0c3e.hunt.stats;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Trip;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;

public class StatsMenu implements Listener
{
	static public abstract class StatsCategory
	{
		static final ItemStack RETURN = HuntItems.createGuiItem(Material.BARRIER, 0, "§cRetour");
		protected ArrayList<Stat> list;

		/**
		 * Gets category's name
		 * @return Category's name
		 */
		abstract String getName();

		/**
		 * Gets this category's icon
		 * @return Category icon
		 */
		abstract ItemStack getIcon();

		/**
		 * Gets the inventory for a certain player
		 * @param hp Player to get inventory for
		 * @return Inventory for player
		 */
		Inventory getInventory(final HuntPlayer hp)
		{
			Inventory inv = Bukkit.createInventory(null, 27, MessageFormat.format("§6Stats §8[§b{0}§8] » §a{1}", hp.getName(), getName()));
			inv.setItem(4, RETURN);

			for (int i = 0; i < list.size(); ++i)
				inv.setItem(9*(i/7+1) + i%7 + 1, list.get(i).getIcon(hp));

			return inv;
		}

		/**
		 * Initializes stats to default value for player
		 * @param hp Player to initialize stats for
		 */
		void init(final HuntPlayer hp)
		{
			for (final Stat s : list)
				s.init(hp);
		}

		String serialize(final HuntPlayer hp)
		{
			String r = new String();
			for (final Stat s : list)
				r += s.serialize(hp);

			return r;
		}
	}

	static public class StatsCategoryMining extends StatsCategory
	{
		static final ItemStack icon = HuntItems.createGuiItem(
			Material.IRON_PICKAXE, 0, "§bMinage",
			"", "§7Statistiques sur les blocs", "§7que vous avez miné");

		@Override
		String getName()
		{
			return "Minage";
		}

		@Override
		ItemStack getIcon()
		{
			return icon;
		}

		private static Stat STONE;
		private static Stat WOOD;

		public StatsCategoryMining()
		{
			list = new ArrayList<>();
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("stone", "§7Quantité de §cSTONE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("cobblestone", "§7Quantité de §cCOBBLESTONE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("granite", "§7Quantité de §cGRANITE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("andesite", "§7Quantité d''§cANDÉSITE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("diorite", "§7Quantité de §cDIORITE§7 minée: §e{0}", new StatLong()));
				STONE = new Stat("mining#stone", Material.STONE, "§aStone Minée", l);
				list.add(STONE);
			}
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("oak", "§7Quantité de bûches de §cBOULEAU§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("cobblestone", "§7Quantité de §cCOBBLESTONE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("granite", "§7Quantité de §cGRANITE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("andesite", "§7Quantité d''§cANDÉSITE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("diorite", "§7Quantité de §cDIORITE§7 minée: §e{0}", new StatLong()));
				STONE = new Stat("mining#wood", Material.STONE, "§aStone Minée", l);
				list.add(STONE);
			}
		}
	}

	public static final StatsCategoryMining MINING = new StatsCategoryMining();
	private static final String name = "§0§6Stats";
	private static final String nameCat = "§6Stats";

	public static Inventory getInventory(final HuntPlayer hp)
	{
		Inventory inv = Bukkit.createInventory(null, 36, MessageFormat.format("{0} §8[§b{1}§8]", name, hp.getName()));
		inv.setItem(11, MINING.getIcon());

		return inv;
	}

	/**
	 * Processes player's click in stat menu
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryClickMenu(InventoryClickEvent ev)
	{
		if (!ev.getView().getTitle().startsWith(name))
			return;
		ev.setCancelled(true);
		if (ev.getCurrentItem() == null || ev.getCurrentItem().getType() == Material.AIR)
			return;

		final String invName = ev.getView().getTitle();
		final HuntPlayer hp = Game.getPlayer(invName.substring(invName.indexOf('[')+3, invName.indexOf(']')-2));

		if (ev.getRawSlot() == 11) // Mining
			ev.getWhoClicked().openInventory(MINING.getInventory(hp));
	}

	/**
	 * Processes player's click in a category
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryClickCategory(InventoryClickEvent ev)
	{
		if (!ev.getView().getTitle().startsWith(nameCat))
			return;
		ev.setCancelled(true);
		if (ev.getCurrentItem() == null || ev.getCurrentItem().getType() == Material.AIR)
			return;

		final String invName = ev.getView().getTitle();
		final HuntPlayer hp = Game.getPlayer(invName.substring(invName.indexOf('[')+3, invName.indexOf(']')-2));

		if (ev.getRawSlot() == 4) // Back
			ev.getWhoClicked().openInventory(getInventory(hp));
	}

	/**
	 * Prevents dragging
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryDrag(InventoryInteractEvent ev)
	{
		if (!ev.getView().getTitle().startsWith(name) && !ev.getView().getTitle().startsWith(nameCat))
			return;

		ev.setCancelled(true);
	}


	/**
	 * Opens menu on right-click with item
	 * @param ev Event
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (Game.hasStarted())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(HuntItems.getStatsMenu()))
			return;

		ev.setCancelled(true);

		ev.getPlayer().openInventory(getInventory( Game.getPlayer(ev.getPlayer().getName()) ));
	}
}
