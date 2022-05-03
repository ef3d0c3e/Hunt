package org.ef3d0c3e.hunt.teams;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import net.md_5.bungee.api.ChatColor;

public class TeamMenu implements Listener
{
	private static String m_name = "§6Équipes";
	private static Inventory m_inv = Bukkit.createInventory(null, 18, m_name);

	/**
	 * Update the inventory
	 */
	public static void updateInventory()
	{
		m_inv.clear();
		for (HashMap.Entry<String, Team> set : Game.getTeamList().entrySet())
		{
			Team team = set.getValue();

			ItemStack item = HuntItems.createGuiItem(team.getColor().banner, 0, team.getColoredName());
			ItemMeta meta = item.getItemMeta();
			List<String> players = new Vector<String>();
			for (HashMap.Entry<String, HuntPlayer> set2 : Game.getPlayerList().entrySet())
			{
				if (set2.getValue().getTeam() != team)
					continue;
				players.add("§7 - §o" + set2.getKey());
			}
			meta.setLore(players);
			item.setItemMeta(meta);
			m_inv.addItem(item);
		}
	}

	/**
	 * Get the inventory
	 * @return The inventory
	 */
	public static Inventory getInventory()
	{
		return m_inv;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		if (Game.hasStarted() || !Game.isTeamMode() || ev.getView().getTitle() != m_name)
			return;
		ItemStack clickedItem = ev.getCurrentItem();
		ev.setCancelled(true);
		if (clickedItem == null || clickedItem.getType() == Material.AIR)
			return;
		HuntPlayer hp = Game.getPlayer(ev.getWhoClicked().getName());
		Team team = Game.getTeam(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()));

		hp.setTeam(team);
		ev.getInventory().setContents(getInventory().getContents());
		hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 65536.f, 1.4f);

		updateInventory();
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent ev)
	{
		if (Game.hasStarted() || !Game.isTeamMode() || ev.getView().getTitle() != m_name)
			return;
		ev.setCancelled(true);
	}
	

	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (Game.hasStarted() || !Game.isTeamMode())
			return;
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null)
			return;
		else if (ev.getItem().isSimilar(HuntItems.getTeamSelector()))
		{
			Player player = ev.getPlayer();
			player.openInventory(getInventory());
		}
	}
}
