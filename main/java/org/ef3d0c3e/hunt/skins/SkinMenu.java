package org.ef3d0c3e.hunt.skins;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.ef3d0c3e.hunt.IGui;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.Arrays;

@AllArgsConstructor
public class SkinMenu implements IGui
{
	@Getter
	private HuntPlayer player;
	@Getter
	private int page;

	private final static int maxPage = Skin.getList().size() / 36 + ((Skin.getList().size() % 36 == 0) ? 0 : 1);

	@Override
	public void onGuiClick(final Player p, final ClickType click, final int slot, final ItemStack item)
	{
		if (item == null || item.getType() == Material.AIR)
			return;
		if (Game.hasStarted())
			return;

		final HuntPlayer hp = HuntPlayer.getPlayer(p);
		if (slot == 40) // Default
		{
			hp.setSkin(-1);
			hp.getPlayer().openInventory(getInventory());

			Items.ID.SKIN.replace(hp, getItem(player));
		}
		else if (slot == 36) // previous
			hp.getPlayer().openInventory(new SkinMenu(player, page-1).getInventory());
		else if (slot == 44) // next
			hp.getPlayer().openInventory(new SkinMenu(player, page+1).getInventory());
		else
		{
			hp.setSkin(slot + page * 36);
			Skin.updatePlayerSkin(hp);
			hp.getPlayer().openInventory(getInventory());

			Items.ID.SKIN.replace(hp, getItem(player));
		}
	}

	@Override
	public void onGuiClose(final Player p) {}
	@Override
	public void onGuiDrag(final Player p, final InventoryDragEvent ev) {}

	private final static ItemStack prevArrow = Items.createHead(
		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY5NzFkZDg4MWRiYWY0ZmQ2YmNhYTkzNjE0NDkzYzYxMmY4Njk2NDFlZDU5ZDFjOTM2M2EzNjY2YTVmYTYifX19",
		"§bPrécédent");
	private final static ItemStack nextArrow = Items.createHead(
		"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjMyY2E2NjA1NmI3Mjg2M2U5OGY3ZjMyYmQ3ZDk0YzdhMGQ3OTZhZjY5MWM5YWMzYTkxMzYzMzEzNTIyODhmOSJ9fX0=",
		"§bSuivant");

	@Override
	public Inventory getInventory()
	{
		final Inventory inv = Bukkit.createInventory(this, 45, MessageFormat.format("§6Skins - {0}/{1}", page+1, maxPage));
		final int end = Math.min(Skin.getList().size(), page * 36 + 36);
		for (int i = page * 36; i < end; ++i)
		{
			inv.addItem(Skin.getList().get(i).getDisplayItem());
		}

		if (page != 0)
			inv.setItem(36, prevArrow);
		if (page+1 != maxPage)
			inv.setItem(44, nextArrow);

		final ItemStack indicator = new ItemStack(Material.BOOK);
		final ItemMeta meta = indicator.getItemMeta();
		meta.setDisplayName("        §bInformations");
		String skinName = "§6§oDéfaut";
		if (player.getSkin() != -1)
		{
			skinName = Skin.getList().get(player.getSkin()).getDisplayItem().getItemMeta().getDisplayName();
		}
		else
		{
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		meta.setLore(Arrays.asList(
			"§7§oCliquer ici pour remettre",
			"§7§ovotre skin de base",
			"",
			"§7Skin actuel: " + skinName));
		indicator.setItemMeta(meta);
		inv.setItem(40, indicator);

		return inv;
	}

	public static ItemStack getItem(final HuntPlayer hp)
	{
		final ItemStack head = Items.ID.SKIN.create(Material.PLAYER_HEAD,
			MessageFormat.format("§dSkin §7: §b{0} §7(Click-Droit)", hp.getSkin() == -1 ? "§oAucun" : Skin.getSkinName(hp.getSkin())),
			"§7Utilisez cet objet pour", "§7changer de skin"
		);
		if (hp.getSkin() == -1)
		{
			final SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwnerProfile(hp.getPlayer().getPlayerProfile());
			head.setItemMeta(meta);
		}
		else
		{
			final SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwnerProfile(Skin.getSkinMeta(hp.getSkin()).getOwnerProfile());
			head.setItemMeta(meta);
		}

		return head;
	}

	/*
	private static Inventory[] m_pages; // Base gui
	private static String[] m_pagesName;
	
	public SkinMenu()
	{
		final int sz = ((Skin.getList().size() % 27 == 0) ? 0 : 1) + Skin.getList().size() / 27;
		m_pages = new Inventory[sz];
		m_pagesName = new String[sz];

		for (int i = 0; i < sz; ++i)
		{
			m_pages[i] = Bukkit.createInventory(null, 45, "");
			m_pagesName[i] = "§9Skins §0§l-§r §c" + String.valueOf(1 + i) + "/" + String.valueOf(sz);
			initializeItems(i, sz);
		}
	}

	public void initializeItems(final int page, final int sz)
	{
		for (int i = page * 27; i < Skin.getList().size() && i < (page + 1) * 27; ++i)
			m_pages[page].addItem(Skin.getList().get(i).getHead());

		if (page > 0)
			m_pages[page].setItem(36, Items.getPrevArrow());
		if (page + 1 < sz)
			m_pages[page].setItem(44, Items.getNextArrow());
	}

	public Inventory getInventory(HuntPlayer hp, int page)
	{
		Inventory cloned = Bukkit.createInventory(null, m_pages[page].getSize(), m_pagesName[page]);

		int slot = 0;
		for (ItemStack item : m_pages[page].getContents())
		{
			if (item != null)
				cloned.setItem(slot, item);
			++slot;
		}
		
		String skinName = "§6Défaut";
		if (hp.getSkin() != -1)
			skinName = Skin.getList().get(hp.getSkin()).getHead().getItemMeta().getDisplayName();
		cloned.setItem(40, Items.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
				"§7§oCliquer ici pour remettre", "§7§ovotre skin de départ", "", "§7Skin actuel: " + skinName));
	
		return cloned;
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		boolean cont = false;
		Player p = (Player)ev.getWhoClicked();

		int i;
		for (i = 0; i < m_pages.length; ++i)
			if (p.getOpenInventory().getTitle() == m_pagesName[i])
			{
				cont = true;
				break;
			}
		if (!cont)
			return;
		if (Game.hasStarted())
			return;

		ev.setCancelled(true);

		HuntPlayer hp = Game.getPlayer(p.getName());

		ItemStack clickedItem = ev.getCurrentItem();

		if (clickedItem == null || clickedItem.getType() == Material.AIR)
			return;

		if (ev.getRawSlot() < 27 && i * 27 + ev.getRawSlot() < Skin.getList().size())
		{
			hp.setSkin(i * 27 + ev.getRawSlot());
			hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_FLUTE, 65536.f, 1.6f);

			String kitName = "§6Défaut";
			if (hp.getSkin() != -1)
				kitName = Skin.getList().get(hp.getSkin()).getHead().getItemMeta().getDisplayName();
			ev.getInventory().setItem(40, Items.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
					"§7§oCliquer ici pour remmetre", "§7§ovotre skin de départ", "", "§7Skin actuel: " + kitName));
		}
		else if (ev.getRawSlot() == 40)
		{
			if (hp.getSkin() != -1)
			{
				hp.setSkin(-1);
				hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 65536.f, 1.6f);
				Messager.HuntMessage(p, "Faites un Déco-Reco pour actualiser votre skin!");
			}

			ev.getInventory().setItem(40, Items.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
					"§7§oCliquer ici pour remmetre", "§7§ovotre skin de départ", "", "§7Skin actuel: §6Défaut"));
		}
		else if (ev.getRawSlot() == 36 && i > 0)
			p.openInventory(getInventory(hp, i - 1));
		else if (ev.getRawSlot() == 44 && i < m_pages.length - 1)
			p.openInventory(getInventory(hp, i + 1));
	}

	@EventHandler
	public void onInventoryClick(InventoryDragEvent event)
	{
		for (String name : m_pagesName)
			if (((Player) event.getWhoClicked()).getOpenInventory().getTitle() == name)
			{
				event.setCancelled(true);
				break;
			}
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (Game.hasStarted())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(Items.getSkinSelector()))
			return;
		ev.setCancelled(true);
		Player player = ev.getPlayer();
		player.openInventory(getInventory(Game.getPlayer(player.getName()), 0));
	}
	*/
}
