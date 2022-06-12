package org.ef3d0c3e.hunt.stats;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Trip;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.kits.Kit;
import org.ef3d0c3e.hunt.kits.KitMenu;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;

public class StatsMenu
{
	static public abstract class StatsCategory
	{
		static final ItemStack RETURN = Items.createGuiItem(Material.BARRIER, 0, "§cRetour");
		protected ItemStack icon;
		protected ArrayList<Stat> list;

		/**
		 * Gets category's name
		 * @return Category's name
		 */
		abstract String getName();

		/**
		 * Gets icon for category
		 * @return Icon
		 */
		ItemStack getIcon()
		{
			 return icon;
		}

		/**
		 * Gets the inventory for a certain player
		 * @param hp Player to get inventory for
		 * @return Inventory for player
		 */
		Inventory getInventory(final HuntPlayer hp)
		{
			final int size = 9*(list.size()/7+2);
			Inventory inv = Bukkit.createInventory(null, size, MessageFormat.format("§6Stats §8[§b{0}§8] » §a{1}", hp.getName(), getName()));
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

	static public class StatsCategoryHunt extends StatsCategory
	{
		@Override
		String getName()
		{
			return "Hunt";
		}

		private static Stat NORMAL;
		private static Stat ROUND;

		public StatsCategoryHunt()
		{
			icon = Items.createGuiItem(
				Material.GOLD_BLOCK, 0, "§bHunt",
				"", "§7Statistiques sur le hunt");

			list = new ArrayList<>();
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("played", "§7Parties jouées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("played_team", "§7Parties jouées en équipe: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won", "§7Parties gagnées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won_team", "§7Parties gagnées en équipe: §e{0}", new StatLong()));
				NORMAL = new Stat("hunt#normal", Material.COMPASS, "§aMode Normal", l);
				list.add(NORMAL);
			}
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("played", "§7Parties jouées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("played_team", "§7Parties jouées en équipe: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won", "§7Parties gagnées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won_team", "§7Parties gagnées en équipe: §e{0}", new StatLong()));
				ROUND = new Stat("hunt#round", Material.CLOCK, "§aMode Round", l);
				list.add(ROUND);
			}
		}
	}

	static public class StatsCategoryKits extends StatsCategory
	{
		@Override
		String getName()
		{
			return "Kits";
		}

		public StatsCategoryKits()
		{
			icon = Items.createGuiItem(
				Material.CLOCK, 0, "§bKits",
				"", "§7Statistiques sur les kits");

			list = new ArrayList<>();
			for (final Kit k : KitMenu.getKitList())
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("played", "§7Parties jouées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("played_team", "§7Parties jouées en équipe: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won", "§7Parties gagnées: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("won_team", "§7Parties gagnées en équipe: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("played_round", "§7Nombre de fois pris en mode Round: §e{0}", new StatLong()));
				list.add(new Stat("kits#" + k.getName(), k.getDisplayItem().getType(), "§a" + k.getDisplayName(), l));
			}
		}
	}

	static public class StatsCategoryMining extends StatsCategory
	{
		@Override
		String getName()
		{
			return "Minage";
		}

		private static Stat STONE;
		private static Stat WOOD;
		private static Stat DIRT;
		private static Stat ORE;

		public StatsCategoryMining()
		{
			icon = Items.createGuiItem(
				Material.IRON_PICKAXE, 0, "§bMinage",
				"", "§7Statistiques sur les blocs", "§7que vous avez miné");

			list = new ArrayList<>();
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("stone", "§cSTONE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("cobblestone", "§cCOBBLESTONE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("granite", "§cGRANITE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("andesite", "§cANDÉSITE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("diorite", "§cDIORITE§7 minée: §e{0}", new StatLong()));
				STONE = new Stat("mining#stone", Material.STONE, "§aStone Minée", l);
				list.add(STONE);
			}
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("oak", "§cCHÊNE§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("spruce", "§cSAPIN§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("birch", "§cBOULEAU§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("jungle", "§cACAJOU§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("acacia", "§cACACIA§7 coupée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("dark_oak", "§cCHÊNE NOIR§7 coupée: §e{0}", new StatLong()));
				WOOD = new Stat("mining#wood", Material.OAK_LOG, "§aBois Coupé", l);
				list.add(WOOD);
			}
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("dirt", "§cTERRE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("sand", "§cSABLE§7 minée: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("gravel", "§cGRAVEL§7 miné: §e{0}", new StatLong()));
				DIRT = new Stat("mining#dirt", Material.DIRT, "§aResources Minée", l);
				list.add(DIRT);
			}
			{
				ArrayList<Trip<String, String, StatValue>> l = new ArrayList<>();
				l.add(new Trip<String, String, StatValue>("coal", "§cCHARBON§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("iron", "§cFER§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("copper", "§cCUIVRE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("gold", "§cOR§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("lapis", "§cLAPIS LAZULI§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("redstone", "§cREDSTONE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("emerald", "§cÉMERAUDE§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("diamond", "§cDIAMOND§7 miné: §e{0}", new StatLong()));
				l.add(new Trip<String, String, StatValue>("netherite", "§cNETHERITE§7 miné: §e{0}", new StatLong()));
				ORE = new Stat("mining#ore", Material.IRON_ORE, "§aMinerais Minée", l);
				list.add(ORE);
			}
		}
	}

	public static final StatsCategoryHunt HUNT = new StatsCategoryHunt();
	public static final StatsCategoryKits KITS = new StatsCategoryKits();
	public static final StatsCategoryMining MINING = new StatsCategoryMining();
	private static final String name = "§0§6Stats";
	private static final String nameCat = "§6Stats";

	public static Inventory getInventory(final HuntPlayer hp)
	{
		Inventory inv = Bukkit.createInventory(null, 36, MessageFormat.format("{0} §8[§b{1}§8]", name, hp.getName()));
		inv.setItem(10, 	HUNT.getIcon());
		inv.setItem(11, 	KITS.getIcon());
		inv.setItem(13, MINING.getIcon());

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
		final HuntPlayer hp = HuntPlayer.getPlayer(invName.substring(invName.indexOf('[')+3, invName.indexOf(']')-2));

		switch (ev.getRawSlot())
		{
			case 10:
				ev.getWhoClicked().openInventory(HUNT.getInventory(hp));
				break;
			case 11:
				ev.getWhoClicked().openInventory(KITS.getInventory(hp));
				break;
			case 13:
				ev.getWhoClicked().openInventory(MINING.getInventory(hp));
				break;
			default:
				break;
		}
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
		final HuntPlayer hp = HuntPlayer.getPlayer(invName.substring(invName.indexOf('[')+3, invName.indexOf(']')-2));

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
		if (ev.getItem() == null || !ev.getItem().isSimilar(Items.getStatsMenu()))
			return;

		ev.setCancelled(true);

		ev.getPlayer().openInventory(getInventory( HuntPlayer.getPlayer(ev.getPlayer()) ));
	}
}
