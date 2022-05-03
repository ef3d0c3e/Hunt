package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;
import oshi.util.tuples.Pair;

import java.util.*;

public class KitThomas extends Kit
{
	static ItemStack chalkItem;
	static ItemStack maskItem;
	static ItemStack crownItem;
	static ItemStack cautionItem;

	static HashMap<Location, HuntPlayer> furnaces = new HashMap<>(); // Furnaces

	boolean nightMode;
	int ammo;
	HashSet<Arrow> arrows;

	@Override
	public String getName() { return "thomas"; }
	@Override
	public String getDisplayName() { return "Thomas"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.ENDER_EYE, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Tire des craies topologiques",
			Kit.itemLoreColor + "╸ Ses fours lui rapportent des intérêts"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bObtenez §6La Caution§b que vous pouvez utiliser pour créer 100€ de dégâts autour de vous.",
				"§c ╸ §bObtenez la §6Boîte à Craies§b qui tire des craies.::§d● Vous avez 10 munitions",
				"§c ╸ §bSi une craie touche un bloc, l'espace Minecraft est privé d'une boule unité pour une certaine norme.",
				"§c ╸ §bSi une craie touche une entitée, l'espace Minecraft devient l'union de l'espace Minecraft et d'une boule unité pour une certaine norme.",
				"§c ╸ §bLe Jour, vous devenez le Roi des roublards.",
				"§c ╸ §bLa Nuit, vous devenez Ventou dé la noui.",
				"§c ╸ §bEn forme Roublard, placer un four le lie à vous. Lorsque qu'une ressource cuit dans un four lié, cette ressource est automatiquement transférée dans votre inventaire ainsi qu'un peu d'XP.::§d● Vous avez une chance de\n recevoir un lingot supplémentaire\n lorsque vous faite cuire\n du fer ou de l'or\n§d● Le four doit être placé\n le jour mais continue\n de fonctionner la nuit",
				"§c ╸ §bEn forme Ventou, la §6Boîte à Craies§b se recharge.",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.THOMAS; }

	/**
	 * Create items & registers event listener
	 */
	@Override
	public void start()
	{
		chalkItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = chalkItem.getItemMeta();
			meta.setDisplayName("§aBoîte à Craies");
			meta.setCustomModelData(1);
			meta.setLore(Arrays.asList(
				"§7Click droit pour tirer une craie"
			));
			chalkItem.setItemMeta(meta);
		}

		maskItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = maskItem.getItemMeta();
			meta.setDisplayName("§8Masque de Ventou dé la nouit");
			meta.setCustomModelData(3);
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
			maskItem.setItemMeta(meta);
		}

		crownItem = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = crownItem.getItemMeta();
			meta.setDisplayName("§8Couronne du roi des roublards");
			meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
			meta.setCustomModelData(4);
			crownItem.setItemMeta(meta);
		}

		cautionItem = new ItemStack(Material.PAPER, 4);
		{
			ItemMeta meta = cautionItem.getItemMeta();
			meta.setDisplayName("§bLa caution");
			meta.setLore(Arrays.asList(
				"§7Click droit pour créer 100€",
				"§7de dégâts autour de vous!"
			));
			cautionItem.setItemMeta(meta);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new KitThomasEvents(), Game.getPlugin());
	}
	/**
	 * Gives players his items & awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		hp.getPlayer().getInventory().addItem(cautionItem);
		final ItemStack item = hp.getPlayer().getInventory().getHelmet();
		if (item != null)
			PlayerInteractions.giveItem(hp, new ItemStack[]{ item.clone() }, true, true);
		if (((KitThomas)hp.getKit()).nightMode)
			hp.getPlayer().getInventory().setHelmet(maskItem);
		else
			hp.getPlayer().getInventory().setHelmet(crownItem);
		hp.getPlayer().getInventory().addItem(chalkItem);
		HuntAchievement.PLAY_THOMAS.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_THOMAS.award(hp.getPlayer(), 1);
	}
	/**
	 * Removes player's furnaces
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		Iterator<Map.Entry<Location, HuntPlayer>> it = furnaces.entrySet().iterator();
		while (it.hasNext())
		{
			final Map.Entry<Location, HuntPlayer> e = it.next();
			if (e.getValue() == hp)
				it.remove();
		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]
		{
			chalkItem,
			maskItem,
			crownItem,
			cautionItem
		};
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !Util.containsItem(getItems(), item) || item.isSimilar(cautionItem);
	}

	@Override
	public Kit makeCopy() { return new KitThomas(); }

	public KitThomas()
	{
		nightMode = false;
		ammo = 10;
		arrows = new HashSet<>();
	}
}
