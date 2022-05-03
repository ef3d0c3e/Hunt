package org.ef3d0c3e.hunt.skins;

import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class SkinMenu implements Listener
{
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
			m_pages[page].setItem(36, HuntItems.getPrevArrow());
		if (page + 1 < sz)
			m_pages[page].setItem(44, HuntItems.getNextArrow());
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
		cloned.setItem(40, HuntItems.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
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
			ev.getInventory().setItem(40, HuntItems.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
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

			ev.getInventory().setItem(40, HuntItems.createGuiItem(Material.NETHER_STAR, 0, "§a         Votre skin",
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
		if (ev.getItem() == null || !ev.getItem().isSimilar(HuntItems.getSkinSelector()))
			return;
		ev.setCancelled(true);
		Player player = ev.getPlayer();
		player.openInventory(getInventory(Game.getPlayer(player.getName()), 0));
	}
}
