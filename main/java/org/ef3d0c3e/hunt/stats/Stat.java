package org.ef3d0c3e.hunt.stats;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Trip;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Stat
{
	private String namespace; ///< Namespace name (for serialization)
	private ArrayList<Trip<String, String, StatValue>> components;
	private ItemStack icon; // Base icon

	/**
	 * Constructor
	 * @param namespace Namespace key name for serialization
	 * @param material Material to use for the icon
	 * @param name Stat's display name
	 * @param components Stat's components
	 * @param desc Description to show before components
	 */
	public Stat(final String namespace, final Material material, final String name, final ArrayList<Trip<String, String, StatValue>> components, final String... desc)
	{
		this.namespace = namespace;
		this.components = components;
		icon = HuntItems.createGuiItem(material, 0, name, desc);
	}

	/**
	 * Gets icon associated with stat
	 * @param hp Player to get icon for
	 * @return Icon as an ItemStack
	 */
	public ItemStack getIcon(final HuntPlayer hp)
	{
		ItemStack item = icon.clone();
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null)
			lore = new ArrayList<String>();
		lore.add("§0");
		for (final Trip<String, String, StatValue> t : components)
		{
			final StatValue v = hp.getStat(MessageFormat.format("{0}#{1}", namespace, t.first));
			if (v != null)
				lore.add(v.format(t.second));
			else // Error
				lore.add(MessageFormat.format("ERR: {0} is null", t.second));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	/**
	 * Initialize a stat to it's default value
	 * @param hp Player to initialize stat for
	 */
	public void init(final HuntPlayer hp)
	{
		for (final Trip<String, String, StatValue> t : components)
			hp.setStat(MessageFormat.format("{0}#{1}", namespace, t.first), t.third);
	}

	String serialize(final HuntPlayer hp)
	{
		String r = new String();
		for (final Trip<String, String, StatValue> t : components)
			r += MessageFormat.format("{0}#{1}:{2}\n", namespace, t.first, hp.getStat(MessageFormat.format("{0}#{1}", namespace, t.first)).serialize());

		return r;
	}
}