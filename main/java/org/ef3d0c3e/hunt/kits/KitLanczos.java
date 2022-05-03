package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import packets.MetadataHelper;

/**
 * Lanczos's kit
 */
public class KitLanczos extends Kit
{
	ArmorStand indicator = null;
	Location save = null;
	int lastUse = -60;

	@Override
	public String getName() { return "lanczos"; }
	@Override
	public String getDisplayName() { return "Lanczos"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.CLOCK, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ A un ange gardient qui le",
			Kit.itemLoreColor + " sauve avant de mourir"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bVous avez un §6Ange Gardien§b qui vous sauve lorsque vous allez mourir.::§d● Il vous soigne légèrement et\n vous téléporte vers une\n position ancienne",
				"§c ╸ §bAprès avoir été utilisé, votre §6Ange Gardien§b à un délai de §e60s§b avant de pouvoir être utilisé à nouveau.",
				"§c ╸ §bUn indicateur apparaît, vous indiquant où vous serez téléporté si votre §6Ange Gardien§b s'active.::§d● Tous les joueurs\n peuvent le voir",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.LANCZOS; }

	/**
	 * Registers event listener
	 */
	@Override
	public void start()
	{
		Bukkit.getServer().getPluginManager().registerEvents(new KitLanczosEvents(), Game.getPlugin());
	}
	/**
	 * Awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_LANCZOS.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_LANCZOS.award(hp.getPlayer(), 1);
	}

	/**
	 * Clears armor stand on death & resets saved location
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		if (indicator != null)
			indicator.remove();
		indicator = null;
		save = null;
		lastUse = -60;
	}

	@Override
	public Kit makeCopy()
	{
		return new KitLanczos();
	}

	public KitLanczos()
	{
	}
}
