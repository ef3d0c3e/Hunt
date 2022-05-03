package org.ef3d0c3e.hunt.kits;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Pair;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import packets.DestroyEntityHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * Kélian's kit
 */
public class KitKelian extends Kit
{
	static ItemStack detectorItem;

	HashMap<Location, Pair<Integer, UUID>> shulkers; // Client side entities

	@Override
	public String getName() { return "kelian"; }
	@Override
	public String getDisplayName() { return "Kélian"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.HEART_OF_THE_SEA, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Obtient des buffs quand il pleut",
			Kit.itemLoreColor + " ou qu'il est dans l'eau",
			Kit.itemLoreColor + "╸ A une chance d'obtenir un détecteur de diament",
			Kit.itemLoreColor + " en tuant des poulpes"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bLorsque'il pleut ou que vous êtes dans l'eau vous obtenez un buff.::§a↑ Speed II\n§a↑ Regeneration I\n§a↑ Dolphin's Grace I",
				"§c ╸ §bVous avez une chance d'obtenir un détecteur de diamants en tuant un poulpe.::§d● ¼ chance par poulpe tué",
				"§c ╸ §bLe détecteur de diament peut être utilisé pour afficher les diamants autour de vous.::§d● Vous êtes le seul\n à voir les diamants\n§d● Si aucun diamant\n n'est trouvé, le\n détecteur n'est pas\n consummé",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.KELIAN; }

	/**
	 * Create diamond detector & registers event listener
	 */
	@Override
	public void start()
	{
		detectorItem = new ItemStack(Material.HEART_OF_THE_SEA);
		{
			ItemMeta meta = detectorItem.getItemMeta();
			meta.setDisplayName("§bDétecteur de diamants");
			meta.setLore(Arrays.asList(
				"§7Click droit pour afficher les diamants autour de sois"
			));
			detectorItem.setItemMeta(meta);
		}

		Bukkit.getServer().getPluginManager().registerEvents(new KitKelianEvents(), Game.getPlugin());
	}
	/**
	 * Awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_KELIAN.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_KELIAN.award(hp.getPlayer(), 1);
	}

	/**
	 * Destroys entity that indicated diamonds' location
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		try
		{
			ArrayList<Integer> list = new ArrayList<>(shulkers.size());
			for (Pair<Integer, UUID> p : shulkers.values())
				list.add(p.first);
			ProtocolManager manager = Game.getProtocolManager();
			manager.sendServerPacket(hp.getPlayer(), new DestroyEntityHelper(list).getPacket());

			shulkers.clear();
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]
		{
			detectorItem
		};
	}
	@Override
	public boolean itemFilter(final ItemStack item)
	{
		return !item.isSimilar(detectorItem);
	}

	@Override
	public Kit makeCopy() { return new KitKelian(); }

	public KitKelian()
	{
		shulkers = new HashMap<Location, Pair<Integer, UUID>>();
	}
}
