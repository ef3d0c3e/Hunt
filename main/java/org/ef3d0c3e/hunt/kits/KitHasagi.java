package org.ef3d0c3e.hunt.kits;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

/**
 * Hasagi's kit
 */
public class KitHasagi extends Kit {
	static ItemStack bladeItem;

	int charge = 0;
	int lastWall = -20;

	@Override
	public String getName() { return "hasagi"; }
	@Override
	public String getDisplayName() { return "Hasagi"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.NETHERITE_SWORD, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Place de la Dirt à l'infini",
			Kit.itemLoreColor + "╸ Récupère de la nourriture en cassant de la terre",
			Kit.itemLoreColor + "╸ Tire de la Dirt en la droppant",
			Kit.itemLoreColor + "╸ Peut crafter des potions de 'Luck'",
			Kit.itemLoreColor + " qui lui donnent un effet positif et",
			Kit.itemLoreColor + " négatif aux autres",
			Kit.itemLoreColor + "╸ Peut crafter une tornade"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bPeut tirer des blocs de terre en droppanet de la terre.",
				"§c ╸ §bQuand il place de la terre, elle reste dans son inventaire.",
				"§c ╸ §bCasser de la terre lui redonne un peu de nourriture.",
				"§c ╸ §bPeut crafter des potions de luck §e(voir p2-3)§b qui lui donnent un effet positif et négatif à ses adversaires.",
				"§c ╸ §bPeut crafter une tornade de terre §e(voir p4)§b qui lui permet de se déplacer, de creuser ou de repousser les joueurs."
			}
		};
		return desc;
	}

	@Override
	public KitID getID() { return KitID.HASAGI; }

	/**
	 * Registers blade & event listener
	 */
	@Override
	public void start()
	{
		bladeItem = new ItemStack(Material.NETHERITE_SWORD);
		{
			ItemMeta meta = bladeItem.getItemMeta();
			meta.setDisplayName("§cÉpaie");
			meta.setLore(Arrays.asList(
				"",
				"§9-30 Q.I"
			));
			meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			meta.setUnbreakable(true);
			bladeItem.setItemMeta(meta);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new KitHasagiEvents(), Game.getPlugin());
	}
	/**
	 * Gives blade to player & awards achievement for player this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		PlayerInteractions.giveItem(hp, new ItemStack[] { bladeItem }, true, true);
		HuntAchievement.PLAY_HASAGI.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievement for winning with this kit
	 * @param hp Player
	 */
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_HASAGI.award(hp.getPlayer(), 1);
	}

	@Override
	public ItemStack[] getItems()
	{
		final ItemStack items[] = new ItemStack[]
		{
			bladeItem
		};
		return items;
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !item.isSimilar(bladeItem);
	}

	@Override
	public Kit makeCopy()
	{
		return new KitHasagi();
	}

	public KitHasagi()
	{
	}
}
