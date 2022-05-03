package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Ocelot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.player.PlayerInteractions;

import java.util.Arrays;


public class KitTom extends Kit
{
	static ItemStack magicCarpet;
	static ItemStack magicCarpetModel;

	@Override
	public String getName() { return "tom"; }
	@Override
	public String getDisplayName() { return "Tom"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.COMPASS, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Les battements de son coeur",
			Kit.itemLoreColor + " indiquent la distance à sa cible",
			Kit.itemLoreColor + "╸ Peut traquer son chasseur",
			Kit.itemLoreColor + "╸ Obtient un tapis volant"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bEntendez votre coeur battre lorsque vous êtes proche de votre chasseur.::§d● Plus il bat fort,\n plus vous êtes proche\n§d● Si la distance est inférieure\n à 30 blocs, vous obtenez\n Speed I",
				"§c ╸ §bVous pouvez traquer votre chasseur en faisant click gauche avec le §6Tracker§b.",
				"§c ╸ §bVous pouvez fabriquer un §6Tapis Brûlé§b en droppant un §6Bloc de Charbon§b sur un §6Tapis§b.",
				"§c ╸ §bVous pouvez utiliser le tapis pour vous envoler et vous déplacer sur de longue distances.",
				"§c ╸ §bLe tapis laisse une traînée de feu derrière lui.",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.TOM; }

	/**
	 * Create items & Registers event listener
	 */
	@Override
	public void start()
	{
		magicCarpet = new ItemStack(Material.LIME_CARPET);
		{
			ItemMeta meta = magicCarpet.getItemMeta();
			meta.setDisplayName("§aTapis Brûlé");
			meta.setLore(Arrays.asList(
				"§7Click droit pour se lancer",
				"§7dans les airs."
			));
			magicCarpet.setItemMeta(meta);
		}
		magicCarpetModel = new ItemStack(Material.FIREWORK_STAR);
		{
			ItemMeta meta = magicCarpetModel.getItemMeta();
			meta.setCustomModelData(2);
			magicCarpetModel.setItemMeta(meta);
		}
		Bukkit.getServer().getPluginManager().registerEvents(new KitTomEvents(), Game.getPlugin());
	}
	/**
	 * Give carpet & awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		PlayerInteractions.giveItem(hp, new ItemStack[] { magicCarpet }, true, true);
		HuntAchievement.PLAY_TOM.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_TOM.award(hp.getPlayer(), 1);
	}
	/**
	 * Spawns an ocelot when player dies
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		//TODO: Custom texture ?
		Ocelot pangolin = (Ocelot)hp.getPlayer().getWorld().spawnEntity(hp.getPlayer().getLocation(), EntityType.OCELOT);

		pangolin.setCustomNameVisible(true);
		pangolin.setCustomName("§7Pangolin");
		pangolin.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(40.0);
		pangolin.setHealth(40);
		pangolin.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2400, 1));
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]{ magicCarpet };
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !item.isSimilar(magicCarpet);
	}

	@Override
	public Kit makeCopy()
	{
		return new KitTom();
	}

	public KitTom()
	{
	}
}
