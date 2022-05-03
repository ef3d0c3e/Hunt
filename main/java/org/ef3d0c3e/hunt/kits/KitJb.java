package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;

/**
 * Jean-Baptiste's kit
 */
public class KitJb extends Kit
{
	int movePackets;

	@Override
	public String getName() { return "jb"; }
	@Override
	public String getDisplayName() { return "Jean-Baptiste"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.WITHER_ROSE, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Corromp le monde autour de lui",
			Kit.itemLoreColor + "╸ Donne hunger en tapant",
			Kit.itemLoreColor + "╸ Donne poison à l'arc",
			Kit.itemLoreColor + "╸ Donne wither en étant tué; et si",
			Kit.itemLoreColor + " mort s'en suit, vous êtes réssuscité"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bDonne hunger lorsqu'il tape.",
				"§c ╸ §bDonne poison à l'arc.",
				"§c ╸ §bImmunisé aux éclairs.",
				"§c ╸ §bCorromp le monde autour de lui lorsqu'il se déplace.",
				"§c ╸ §bLorsqu'il est proche d'un villageois, d'un creeper ou d'un cochon il a une chance de faire s'abbatre la foudre.",
				"§c ╸ §bAprès avoir été tué, le joueur qui vous a tué est maudit, et obtient des effets de potion négatifs. Si il meurt dans les 5 secondes suivantes vous êtes réssuscié.",
				"§c ╸ §bVous n'êtes pas attaqué par les Squelettes, les Zombies et les Piglins Zombifié.",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.JB; }

	/**
	 * Registers event listener
	 */
	@Override
	public void start()
	{
		Bukkit.getServer().getPluginManager().registerEvents(new KitJbEvents(), Game.getPlugin());
	}
	/**
	 * Sends recipes to player & Awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_JB.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_JB.award(hp.getPlayer(), 1);
	}

	@Override
	public Kit makeCopy()
	{
		return new KitJb();
	}
	
	public KitJb()
	{
		movePackets = 0;
	}
}
