package org.ef3d0c3e.hunt;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface IGui extends InventoryHolder
{
	void onGuiClick(final Player p, final ClickType click, final int slot, final ItemStack item);
	void onGuiClose(final Player p);
	void onGuiDrag(final Player p, final InventoryDragEvent ev);

	public static class Events implements Listener
	{
		@EventHandler
		public void onInventoryClick(final InventoryClickEvent ev)
		{
			if (!(ev.getInventory().getHolder() instanceof IGui))
				return;

			ev.setCancelled(true);
			final IGui gui = (IGui)ev.getInventory().getHolder();
			gui.onGuiClick(
				(Player)ev.getWhoClicked(),
				ev.getClick(),
				ev.getRawSlot(),
				ev.getCurrentItem()
			);
		}

		@EventHandler
		public void onInventoryClose(final InventoryCloseEvent ev)
		{
			if (!(ev.getInventory().getHolder() instanceof IGui))
				return;

			final IGui gui = (IGui)ev.getInventory().getHolder();
			gui.onGuiClose((Player)ev.getPlayer());
		}

		@EventHandler
		public void onInventoryDrag(final InventoryDragEvent ev)
		{
			if (!(ev.getInventory().getHolder() instanceof IGui))
				return;

			ev.setCancelled(true);
			final IGui gui = (IGui)ev.getInventory().getHolder();
			gui.onGuiDrag(
				(Player)ev.getWhoClicked(),
				ev
			);
		}
	}
}
