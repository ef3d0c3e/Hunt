package org.ef3d0c3e.hunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.ef3d0c3e.hunt.Messager;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;

public class CmdInv
{
	private static String m_prefix = "§9Inventaire de §l";

	/**
	 * Event Handler class that prevents players from stealing from GUI
	 */
	public static class CmdInvEvents implements Listener
	{
		@EventHandler
		public void onInventoryClick(InventoryClickEvent ev)
		{
			if (ev.getView().getTitle().length() < m_prefix.length())
				return;
			if (ev.getView().getTitle().substring(0, m_prefix.length()).equals(m_prefix))
				ev.setCancelled(true);
		}

		@EventHandler
		public void onInventoryDrag(InventoryDragEvent ev)
		{
			if (ev.getView().getTitle().length() < m_prefix.length())
				return;
			if (ev.getView().getTitle().substring(0, m_prefix.length()).equals(m_prefix))
				ev.setCancelled(true);
		}
	}

	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			Messager.ErrorMessage(sender, "Seul un joueur peut effectuer cette commande!");
			return true;
		}

		if (!Game.hasStarted())
		{
			Messager.ErrorMessage(sender, "La partie n'a pas commencé!");
			return true;
		}

		final HuntPlayer hp = Game.getPlayer(sender.getName());
		if (hp.isAlive())
		{
			Messager.ErrorMessage(sender, "Vous ne pouvez pas faire cette commande tant que vous êtes en vie!");
			return true;
		}

		if (Game.isTeamMode() && hp.getTeam() != null && hp.getTeam().isAlive())
		{

			Messager.ErrorMessage(sender, "Vous ne pouvez pas faire cette commande tant que votre équipe est en vie!");
			return true;
		}

		if (args.length == 0)
		{
			Messager.ErrorMessage(sender, "Vous devez spécifier un joueur!");
			return true;
		}
		final HuntPlayer other = Game.getPlayer(args[0]);
		if (other == null)
		{
			Messager.ErrorMessage(sender, "Ce joueur est introuvable!");
			return true;
		}

		if (!other.isAlive())
		{
			Messager.ErrorMessage(sender, "Ce joueur est mort!");
			return true;
		}

		if (!other.isOnline())
		{
			Messager.ErrorMessage(sender, "Ce joueur n'est pas connecté!");
			return true;
		}

		Inventory inv = Bukkit.createInventory(null, 45, m_prefix + other.getName());

		// Armor
		inv.setItem(0, other.getPlayer().getInventory().getItem(EquipmentSlot.FEET));
		inv.setItem(1, other.getPlayer().getInventory().getItem(EquipmentSlot.LEGS));
		inv.setItem(2, other.getPlayer().getInventory().getItem(EquipmentSlot.CHEST));
		inv.setItem(3, other.getPlayer().getInventory().getItem(EquipmentSlot.HEAD));
		inv.setItem(8, other.getPlayer().getInventory().getItem(EquipmentSlot.OFF_HAND));
		inv.setItem(7, other.getPlayer().getInventory().getItem(EquipmentSlot.HAND));

		// Statuses
		ItemStack statuses = new ItemStack(Material.POTION);
		{
			PotionMeta meta = (PotionMeta)statuses.getItemMeta();
			meta.setDisplayName("§6§lEffets");
			meta.setColor(Color.FUCHSIA);
			meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

			ArrayList<String> lore = new ArrayList<>();
			lore.add("§0");
			for (final PotionEffect e : other.getPlayer().getActivePotionEffects())
			{
				lore.add(MessageFormat.format(" §7- §b§n{0}§b {1} §6{2}:{3}",
					e.getType().getName(),
					e.getAmplifier()+1,
					(int)(e.getDuration() / 20 / 60),
				(int)((e.getDuration() / 20) % 60)
				));
			}
			meta.setLore(lore);

			statuses.setItemMeta(meta);
		}
		inv.setItem(4, statuses);

		// Info
		ItemStack info = new ItemStack(Material.BOOK);
		{
			ItemMeta meta = info.getItemMeta();
			meta.setDisplayName("§a§lInformations");

			ArrayList<String> lore = new ArrayList<>();
			lore.add("§0");
			lore.add(MessageFormat.format(" §7- Vie: §c{0}❤", other.getPlayer().getHealth()));
			lore.add(MessageFormat.format(" §7- Score: §e{0}", other.getScore()));
			if (Game.isKitMode() && hp.getKit() != null)
				lore.add(MessageFormat.format(" §7- Kit: §a{0}", other.getKit().getDisplayName()));
			if (Game.isTeamMode())
			{
				lore.add(MessageFormat.format(" §7- Équipe: {0}", other.getTeam().getColoredName()));
				if (other.getTeam().getTarget() != null)
				{
					lore.add(MessageFormat.format(" §7- Cible: {0}", other.getTeam().getTarget().getColoredName()));
					lore.add(MessageFormat.format(" §7- Chasseur: {0}", other.getTeam().getHunter().getColoredName()));
				}
			}
			else if (other.getTarget() != null)
			{
				lore.add(MessageFormat.format(" §7- Cible: §a{0}", other.getTarget().getName()));
				lore.add(MessageFormat.format(" §7- Chasseur: §a{0}", other.getHunter().getName()));
			}
			meta.setLore(lore);

			info.setItemMeta(meta);
		}
		inv.setItem(5, info);

		// Hotbar
		for (int i = 36; i < 45; ++i)
		{
			inv.setItem(i, other.getPlayer().getInventory().getItem(i-36));
		}
		// Inventory
		for (int i = 9; i < 36; ++i)
		{
			inv.setItem(i, other.getPlayer().getInventory().getItem(i));
		}

		hp.getPlayer().openInventory(inv);

		return true;
	}
}
