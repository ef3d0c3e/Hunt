package org.ef3d0c3e.hunt.teams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TeamBeacon implements Listener
{
	private static EnderCrystal m_beacon = null;

	/**
	 * Spawn beacon on game start
	 */
	public static void onStart()
	{
		m_beacon = (EnderCrystal)Game.getOverworld().spawnEntity(Game.getOverworld().getHighestBlockAt(0, 0).getLocation().add(0.0, 1.5, 0.0), EntityType.ENDER_CRYSTAL);

		m_beacon.setInvulnerable(true);
		m_beacon.setSilent(true);
		m_beacon.setShowingBottom(false);
		m_beacon.setGravity(false);
		m_beacon.setBeamTarget(new Location(Game.getOverworld(), 0.0, 256.0, 0.0));
		m_beacon.setPortalCooldown(-1);

		m_beacon.setCustomName("§b§lBeacon");
		m_beacon.setCustomNameVisible(true);
	}

	// FIXME: Can be moved with pistons

	/**
	 * Get inventory consisting of dead players in team
	 * @param hp The player to get the inventory for
	 * @return null If no player can be revived
	 * 	       an Inventory containing a list of players that can be revived in hp's team
	 */
	private static Inventory getInventory(final HuntPlayer hp)
	{
		Inventory inv = Bukkit.createInventory(null, Math.min((hp.getTeam().size() / 9 + 1)*9, 54), hp.getTeam().getColoredName());

		AtomicBoolean empty = new AtomicBoolean(true);
		hp.getTeam().forAllPlayers(other ->  {
			if (other.isAlive())
				return;
			empty.set(false);

			ItemStack item = Items.getSkull(other);
			{
				ItemMeta meta = item.getItemMeta();

				meta.setDisplayName(other.getTeamColoredName());

				ArrayList<String> lore = new ArrayList<>();
				lore.add("§7Click pour ressusciter");
				meta.setLore(lore);

				item.setItemMeta(meta);
			}
			inv.addItem(item);
		});

		if (empty.get())
			return null;

		return inv;
	}

	@EventHandler
	public void onRightClick(PlayerInteractEntityEvent ev)
	{
		if (ev.getRightClicked() != m_beacon ||
			ev.getHand() == EquipmentSlot.OFF_HAND)
			return;
		final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
		if (!hp.isAlive())
			return;
		if (ev.getPlayer().getInventory().getItemInMainHand() == null ||
			ev.getPlayer().getInventory().getItemInMainHand().getType() != Material.PLAYER_HEAD ||
			!ev.getPlayer().getInventory().getItemInMainHand().getItemMeta().getDisplayName().startsWith("§eTête de ", 0))
		{
			ev.getPlayer().sendMessage("§7§oDonne moi une âme!");
			return;
		}
		ev.setCancelled(true);

		final Inventory inv = getInventory(hp);
		if (inv == null)
		{
			ev.getPlayer().sendMessage("§cTous les joueurs de votre équipe sont en vie!");
			return;
		}

		ev.getPlayer().getInventory().getItemInMainHand().setAmount(ev.getPlayer().getInventory().getItemInMainHand().getAmount()-1);
		hp.getPlayer().openInventory(inv);
	}

	@EventHandler
	public void onLeave(final PlayerQuitEvent ev)
	{
		final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
		if (hp.getTeam() == null ||
			!ev.getPlayer().getOpenInventory().getTitle().equals(hp.getTeam().getColoredName()) ||
			ev.getPlayer().getOpenInventory().getItem(0) == null)
			return;

		// Refund diamond
		PlayerInteractions.giveItem(hp, new ItemStack[]{ new ItemStack(Material.DIAMOND)}, true, true);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent ev) // Refund diamond
	{
		final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getPlayer());
		if (hp.getTeam() == null ||
			!ev.getView().getTitle().equals(hp.getTeam().getColoredName()) ||
			ev.getView().getItem(0) == null)
			return;

		PlayerInteractions.giveItem(hp, new ItemStack[]{ new ItemStack(Material.DIAMOND)}, true, true);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getWhoClicked());
		if (!ev.getView().getTitle().equals(hp.getTeam().getColoredName()))
			return;
		ev.setCancelled(true);
		if (ev.getCurrentItem() == null ||
			ev.getCurrentItem().getType() != Material.PLAYER_HEAD) // Make sure player is not clicking his own inventory
			return;

		final String clicked = ChatColor.stripColor(ev.getCurrentItem().getItemMeta().getDisplayName());
		final HuntPlayer other = HuntPlayer.getPlayer(clicked);

		if (PlayerInteractions.schedule(other, (h) -> { h.revive(); }) != null)
			hp.getPlayer().sendMessage(MessageFormat.format("{0}§7 sera ressuscité dés qu'il se reconnecte.", other.getTeamColoredName()));
		ev.getInventory().setItem(0, null); // Remove first item
		hp.getPlayer().closeInventory();
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent ev)
	{
		final HuntPlayer hp = HuntPlayer.getPlayer((Player)ev.getWhoClicked());
		if (!ev.getView().getTitle().equals(hp.getTeam().getColoredName()))
			return;
		ev.setCancelled(true);
	}
}