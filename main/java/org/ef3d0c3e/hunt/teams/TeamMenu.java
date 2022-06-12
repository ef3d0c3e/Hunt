package org.ef3d0c3e.hunt.teams;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.IGui;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public class TeamMenu implements IGui
{
	private HuntPlayer hp;

	@Override
	public void onGuiClick(Player p, ClickType click, int slot, ItemStack item)
	{
		// p == hp.getPlayer()
		if (item == null || item.getType() == Material.AIR)
			return;
		if (!Game.isTeamMode() || Game.hasStarted())
			return;

		final Team team = Team.getTeam(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
		if (team == hp.getTeam())
			return;

		hp.setTeam(team);
		Items.ID.TEAM.replace(hp, getItem(hp));
		p.openInventory(new TeamMenu(hp).getInventory());
		hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 65536.f, 1.4f);
	}

	@Override
	public void onGuiClose(Player p) {}

	@Override
	public void onGuiDrag(Player p, InventoryDragEvent ev) {}

	@Override
	public Inventory getInventory()
	{
		final Inventory inv = Bukkit.createInventory(this, Math.max(9, (int)Math.ceil(Team.getTeamListSize() / 9.f) * 9),
			MessageFormat.format("§lÉquipes §8[{0}§8]", hp.getTeam() == null ? "§b§oAucune" : hp.getTeam().getColoredName()));

		Team.forEach(team -> {
			final ItemStack item = Items.createGuiItem(team.getColor().banner, 0, team.getColoredName());
			final ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<>();
			HuntPlayer.forEach(m -> {
				if (m.getTeam() == team)
					lore.add("§7 - §o" + m.getName());
			});
			meta.setLore(lore);
			if (team == hp.getTeam())
			{
				meta.addEnchant(Enchantment.DURABILITY, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			item.setItemMeta(meta);
			inv.addItem(item);
		});

		return inv;
	}

	/**
	 * Gets display item
	 * @param hp Player
	 * @return Display item
	 */
	public static ItemStack getItem(final HuntPlayer hp)
	{
		final Material mat = hp.getTeam() == null ? Material.WHITE_BANNER : hp.getTeam().getColor().banner;
		return Items.ID.TEAM.create(mat,
			MessageFormat.format("§dÉquipe §7: {0} §7(Click-Droit)", hp.getTeam() == null ? "§b§oAucune" : hp.getTeam().getColoredName()),
			"§7Utilisez cet objet pour", "§7choisir une équipe"
		);
	}
}
