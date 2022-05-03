package org.ef3d0c3e.hunt.kits;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.kits.entities.MehdiBee;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.game.Game;

/**
 * Mehdi's kit
 */
public class KitMehdi extends Kit
{
	static ItemStack honeyItem;
	public static int MAX_BEES = 25;

	public ArrayList<MehdiBee> bees;
	LivingEntity target;

	@Override
	public String getName() { return "mehdi"; }
	@Override
	public String getDisplayName() { return "Mehdi"; }
	@Override
	public ItemStack getDisplayItem()
	{
		return HuntItems.createGuiItem(Material.HONEYCOMB, 0, Kit.itemColor + getDisplayName(),
			Kit.itemLoreColor + "╸ Obtient du Miel Fumé en cassant des fleurs",
			Kit.itemLoreColor + "╸ Fait apparaître des abeilles qui se",
			Kit.itemLoreColor + " battent pour lui à l'aide du miel"
		);
	}
	@Override
	public String[][] getDescription()
	{
		String[][] desc = {
			{
				"§c ╸ §bObtenez du §6Miel Fumé§b en cassant des fleurs::§d● ¼ chance d'en obtenir par fleur",
				"§c ╸ §bCe miel fumé vous permet d'invoquer des abeilles::§d● Maximum 30 abeilles en même temps",
				"§c ╸ §bLes abeilles vous suivent et attaquent les joueurs que vous désignez comme cible::§d● Désignez un joueur ou mob comme\n cible en l'attaquant\n§d● Les abeilles infligent:\n§d - §c¼❤§d aux joueurs\n§d - §c¾❤§d aux mobs"
			}
		};
		return desc;
	}

	@Override
	public KitID getID() { return KitID.MEHDI; }

	/**
	 * Registers event listener & Creates honeycomb item
	 */
	@Override
	public void start()
	{
		honeyItem = new ItemStack(Material.HONEYCOMB);
		{
			ItemMeta meta = honeyItem.getItemMeta();
			meta.setDisplayName("§6Miel Fumé");
			meta.setLore(Arrays.asList(
				"§7Click droit pour faire apparaître une abeille"
			));
			honeyItem.setItemMeta(meta);
		}
		
		Bukkit.getServer().getPluginManager().registerEvents(new KitMehdiEvents(), Game.getPlugin());
	}
	/**
	 * Awards achievements for playing with this kit
	 * @param hp Player
	 */
	@Override
	public void onStart(HuntPlayer hp)
	{
		HuntAchievement.PLAY_MEHDI.award(hp.getPlayer(), 1);
	}
	/**
	 * Awards achievements for winning with this kit
	 * @param hp Player
	 */
	@Override
	public void onWin(HuntPlayer hp)
	{
		HuntAchievement.WIN_MEHDI.award(hp.getPlayer(), 1);
	}

	/**
	 * Resets bees aggro
	 * @param hp Player attempting to kill
	 * @param killed Player that would have been killed
	 */
	@Override
	public void onKillWrong(HuntPlayer hp, HuntPlayer killed)
	{
		if (target == null)
			return;

		target = null;
		for (MehdiBee bee : bees)
			bee.stopBeingAngry();
	}
	/**
	 * Kills bees
	 * @param hp Player that died or lost his kit
	 */
	@Override
	public void onDeath(HuntPlayer hp)
	{
		for (MehdiBee bee : bees)
			bee.setRemoved(Entity.RemovalReason.DISCARDED);
	}

	/**
	 * Changes bees' owner (also clears aggro)
	 * @param prev Previous owner
	 * @param next New owner
	 * @note Currently useless because when kit ownership is transferred, bees are removed
	 */
	public void changeOwner(HuntPlayer prev, HuntPlayer next)
	{
		target = null;
		for (MehdiBee bee : bees)
		{
			bee.setOwner(next);
			bee.stopBeingAngry();
		}
	}

	@Override
	public ItemStack[] getItems()
	{
		return new ItemStack[]
		{
			honeyItem
		};
	}
	@Override
	public boolean itemFilter(ItemStack item)
	{
		return !item.isSimilar(honeyItem);
	}

	public Kit makeCopy()
	{
		return new KitMehdi();
	}

	public KitMehdi()
	{
		bees = new ArrayList<MehdiBee>();
	}

	/**
	 * Gets bees' current target
	 * @return Entity targeted by bees
	 */
	public LivingEntity getTarget()
	{
		return target;
	}
}
