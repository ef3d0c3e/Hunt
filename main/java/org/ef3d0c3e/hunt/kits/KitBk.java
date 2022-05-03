package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

/**
 * BlueKatss' kit
 */
public class KitBk extends Kit
{
	@Override
	public String getName() { return "bk"; }
	@Override
	public String getDisplayName() { return "BlueKatss"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.SUGAR, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Casse les arbres plus rapidement",
			Kit.itemLoreColor + "╸ Nourriture auto cuisante",
			Kit.itemLoreColor + "╸ Les minerais cuisent automatiquement"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bVous cassez instantanément le bois des arbres.",
				"§c ╸ §bLes minerais de fer et d'or sont automatiquement cuit. Vous avez une chance d'obtenir un lingot de fer et d'or supplémentaire en minant un minerais.",
				"§c ╸ §bTuer un animal vous donne sa viande déjà cuite.",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.BK; }

	/**
	 * Registers event listener
	 */
	@Override
	public void start()
	{
		Bukkit.getServer().getPluginManager().registerEvents(new KitBkEvents(), Game.getPlugin());
	}
	/**
	 * Rewards achievement
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_BK.award(hp.getPlayer(), 1);
	}
	/**
	 * Rewards achievement
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_BK.award(hp.getPlayer(), 1);
	}

	@Override
	public Kit makeCopy()
	{
		return new KitBk();
	}

	public KitBk()
	{
	}
}
