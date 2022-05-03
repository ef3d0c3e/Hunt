package org.ef3d0c3e.hunt.kits;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;
import packets.DestroyEntityHelper;

import java.util.Arrays;
import java.util.UUID;

/**
 * Baptiste's kit
 */
public class KitBaptiste extends Kit
{
	static UUID uuid = UUID.randomUUID();
	static int entityId = Game.nextPosInt();
	static DestroyEntityHelper entityDestroy = new DestroyEntityHelper(Arrays.asList(entityId));

	int hitStreak = 0;

	@Override
	public String getName() { return "baptiste"; }
	@Override
	public String getDisplayName() { return "Baptiste"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.SPECTRAL_ARROW, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Affiche la trajectoire des flèches",
			Kit.itemLoreColor + "╸ Illumine ses cibles",
			Kit.itemLoreColor + "╸ Connaît les PV de ses cibles",
			Kit.itemLoreColor + "╸ A une chance de récupérer une",
			Kit.itemLoreColor + " flèche lorsqu'il rate à l'arc"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bLe point d'aterrissage de vos flèches s'affichent avant de tirer.",
				"§c ╸ §bVos flèches donnent glowing à vos cibles.",
				"§c ╸ §bLorsque vous touchez une cible, vous connaissez ses points de vie.",
				"§c ╸ §bLorsque vous ratez à l'arc, vous avez une chance de récupérer votre flèche.",
				"§c ╸ §bLorsque vous tuez un mouton, vous pouvez dropper des plumes.",
				"§c ╸ §bToucher plusieurs flèches d'affilé augmente vos dégâts.::§d● +10% de dégâts par flèche,\n jusqu'à 150%",
			}
		};

		return desc;
	}

	@Override
	public KitID getID() { return KitID.BAPTISTE; }

	/**
	 * Registers event listener
	 */
	@Override
	public void start()
	{
		Bukkit.getServer().getPluginManager().registerEvents(new KitBaptisteEvents(), Game.getPlugin());
	}
	/**
	 * Awards achievement
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_BAPTISTE.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievement
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_BAPTISTE.award(hp.getPlayer(), 1);
	}
	/**
	 * Destroys entity that indicated arrow's trajectory prediction
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		try
		{
			ProtocolManager manager = Game.getProtocolManager();
			manager.sendServerPacket(hp.getPlayer(), entityDestroy.getPacket());
		}
		catch (Exception e)
		{

		}
	}

	@Override
	public Kit makeCopy()
	{
		return new KitBaptiste();
	}

	public KitBaptiste()
	{
	}
}