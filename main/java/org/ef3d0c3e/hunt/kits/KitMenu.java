package org.ef3d0c3e.hunt.kits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.ef3d0c3e.hunt.IGui;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.skins.Skin;
import oshi.util.tuples.Pair;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;

@AllArgsConstructor
public class KitMenu implements IGui
{
	@Getter
	static private Kit[] kitList = new Kit[]{
		new KitEsteban(),
		new KitMehdi(),
		new KitJb(),
		new KitBaptiste(),
		new KitLino(),
		new KitJulien(),
		new KitKelian(),
		new KitThomas(),
		new KitEnzo(),
		new KitTom(),
		new KitFlavien(),
		new KitBk(),
		new KitLanczos(),
		//new KitHasagi(),
	};

	private static HashMap<Class<? extends Kit>, Boolean> taken;
	static
	{
		taken = new HashMap<>();
		for (final Kit k : kitList)
			taken.put(k.getClass(), false);
	}

	public static boolean isTaken(final Kit k)
	{
		return taken.get(k.getClass());
	}

	public static void setTaken(final Kit k, final boolean v)
	{
		taken.replace(k.getClass(), v);
	}

	HuntPlayer hp;

	@Override
	public void onGuiClick(Player p, ClickType click, int slot, ItemStack item)
	{
		// p == hp.getPlayer()
		if (item == null || item.getType() == Material.AIR)
			return;
		if (!Game.isKitMode() || Game.hasStarted())
			return;

		final Kit kit = kitList[slot];

		if (hp.getKit() != null && kit.getClass() == hp.getKit().getClass())
			return;

		if (Kit.singleKitOnly && isTaken(kit))
		{
			p.sendMessage("§cCe kit est déjà pris!");
			return;
		}

		Class<? extends Kit> KitClass = kit.getClass();

		try
		{
			hp.setKit(KitClass.getDeclaredConstructor().newInstance());
			Items.ID.KIT.replace(hp, getItem(hp));
		}
		catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}
		hp.getPlayer().playSound(hp.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 65536.f, 1.4f);
		hp.getPlayer().openInventory(new KitMenu(hp).getInventory());
	}

	@Override
	public void onGuiClose(Player p) {

	}
	@Override
	public void onGuiDrag(Player p, InventoryDragEvent ev) {

	}

	@Override
	public Inventory getInventory()
	{
		final Inventory inv = Bukkit.createInventory(this, (int)Math.ceil(kitList.length / 9.0) * 9,
			MessageFormat.format("§lKits §8[§b{0}§8]", hp.getKit() == null ? "§oAucun" : hp.getKit().getDisplayName()));

		for (final Kit kit : kitList)
		{
			final ItemStack item = kit.getDisplayItem();
			if (hp.getKit() != null && hp.getKit().getClass() == kit.getClass())
			{
				final ItemMeta meta = item.getItemMeta();
				meta.addEnchant(Enchantment.DURABILITY, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				item.setItemMeta(meta);
			}
			inv.addItem(item);
		}

		return inv;
	}

	/**
	 * Gets display item
	 * @param hp Player
	 * @return Display item
	 */
	public static ItemStack getItem(final HuntPlayer hp)
	{
		 return Items.ID.KIT.create(Material.CLOCK,
			MessageFormat.format("§6Kit §7: §a{0} §7(Click-Droit)", hp.getKit() == null ? "§oAucun" : hp.getKit().getDisplayName()),
			"§7Utilisez cet objet pour", "§7choisir un kit"
		);
	}
}
