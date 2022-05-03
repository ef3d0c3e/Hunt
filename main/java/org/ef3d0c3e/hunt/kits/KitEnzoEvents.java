package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.*;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Enzo's kit's events
 */
public class KitEnzoEvents implements Listener
{
	/**
	 * Opens reward menu to player & consumes key
	 * @param ev Event
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(KitEnzo.keyItem))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.ENZO)
			return;

		ev.setCancelled(true);
		ev.getItem().setAmount(ev.getItem().getAmount()-1);

		ArrayList<EnzoReward> rewards = new ArrayList<>();
		final EnzoReward[] possible = KitEnzo.getRewards();
		for (int i = 0; i < 50; ++i)
			rewards.add(possible[Game.nextPosInt() % possible.length]);

		// Opens an inventory to player with a 'spinning wheel' animation, player can close this inventory and still get their reward
		new BukkitRunnable()
		{
			int selector = 0;
			int time = 0;
			Inventory inv = Bukkit.createInventory(null, 27, "§2§lRoue de la Fortune");

			@Override
			public void run()
			{
				if (time == 0)
				{
					for (int i = 0; i < 9; ++i)
						inv.setItem(i, HuntItems.createGuiItem(Material.GREEN_STAINED_GLASS_PANE, 0, "§0"));
					for (int i = 18; i < 27; ++i)
						inv.setItem(i, HuntItems.createGuiItem(Material.GREEN_STAINED_GLASS_PANE, 0, "§0"));
					inv.setItem(22, HuntItems.createGuiItem(Material.END_ROD, 0, "§0"));

					hp.getPlayer().openInventory(inv);
				}

				for (int i = 0; i < 9; ++i)
				{
					int id = i + selector - 4;
					if (id < 0) id += rewards.size();
					if (id >= rewards.size()) id -= rewards.size();
					inv.setItem(9+i, rewards.get(id).display);
				}


				if (time == 65)
				{
					rewards.get(selector).get(hp);
					hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1.f, 1.6f);
					hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§eRécompense: {0}", rewards.get(selector).display.getItemMeta().getDisplayName())));
					this.cancel();
				}

				if (time % 2 == 0)
				{
					hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.ENTITY_CHICKEN_STEP, SoundCategory.MASTER, 1.f, 1.f);
					++selector;
				}
				++time;
			}

		}.runTaskTimer(Game.getPlugin(), 0, 1);
	}


	/**
	 * Prevents stealing from spinning wheel gui
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent ev)
	{
		if (!ev.getWhoClicked().getOpenInventory().getTitle().equals("§2§lRoue de la Fortune"))
			return;

		ev.setCancelled(true);
	}

	/**
	 * Prevents stealing from spinning wheel gui
	 * @param ev Event
	 */
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent ev)
	{
		if (!ev.getWhoClicked().getOpenInventory().getTitle().equals("§2§lRoue de la Fortune"))
			return;

		ev.setCancelled(true);
	}

	/**
	 * Periodically gives keys to players
	 */
	public KitEnzoEvents()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (Game.isPaused())
					return;

				int mins, secs;
				if (!Game.isRoundMode())
				{
					final Pair<Integer, Integer> p = Normal.getElapsedTime();
					mins = p.first;
					secs = p.second;
				}
				else
				{
					final Pair<Integer, Integer> p = Round.getRoundElapsedTime();
					mins = p.first;
					secs = p.second;
				}

				// Increase reward
				if (secs == 0 && mins != 0 && mins % 4 == 0)
				{
					for (HuntPlayer hp : Game.getPlayerList().values())
					{
						if (!hp.isAlive() || hp.getKit() == null || hp.getKit().getID() != KitID.ENZO)
							continue;

						++((KitEnzo)hp.getKit()).pendingRewards;
					}
				}

				// Give keys
				for (HuntPlayer hp : Game.getPlayerList().values())
				{
					if (!hp.isOnline() || !hp.isAlive() || hp.getKit() == null || hp.getKit().getID() != KitID.ENZO)
						continue;

					final KitEnzo kit = (KitEnzo) hp.getKit();
					if (kit.pendingRewards != 0)
						hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 65536.f, 1.f);
					while (kit.pendingRewards != 0)
					{
						PlayerInteractions.giveItem(hp, new ItemStack[] { KitEnzo.keyItem }, true, true);
						--kit.pendingRewards;
					}
				}
			}
		}.runTaskTimer(Game.getPlugin(), 0, 200);
	}
}
