package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.Util;
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

	public KitBk() {}

	public static class Events implements Listener
	{
		/**
		 * Drops cooked meat from killed animals
		 * @param ev Event
		 */
		@EventHandler
		public void onEntityDeath(EntityDeathEvent ev)
		{
			if (ev.getEntity().getKiller() == null || !(ev.getEntity().getKiller() instanceof Player))
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getEntity().getKiller().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitBk))
				return;

			if (ev.getEntity() instanceof Chicken)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.CHICKEN);
				ev.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, 1 + Game.nextPosInt() % 2));
			}
			else if (ev.getEntity() instanceof Sheep)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.MUTTON);
				ev.getDrops().add(new ItemStack(Material.COOKED_MUTTON, 1 + Game.nextPosInt() % 2));
			}
			else if (ev.getEntity() instanceof Pig)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.PORKCHOP);
				ev.getDrops().add(new ItemStack(Material.COOKED_PORKCHOP, 1 + Game.nextPosInt() % 3));
			}
			else if (ev.getEntity() instanceof Cow)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.BEEF);
				ev.getDrops().add(new ItemStack(Material.COOKED_BEEF, 1 + Game.nextPosInt() % 3));
			}
			else if (ev.getEntity() instanceof Cod)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.COD);
				ev.getDrops().add(new ItemStack(Material.COOKED_COD, 1));
			}
			else if (ev.getEntity() instanceof Salmon)
			{
				ev.getDrops().removeIf((stack) -> stack.getType() == Material.SALMON);
				ev.getDrops().add(new ItemStack(Material.COOKED_SALMON, 1));
			}
		}

		/**
		 * Automatically smelts ores & Increases yields when mining certain blocks
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockBreak(BlockBreakEvent ev)
		{
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitBk))
				return;

			if (ev.getBlock().getType() ==  Material.IRON_ORE ||
				ev.getBlock().getType() == Material.DEEPSLATE_IRON_ORE)
			{
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.IRON_INGOT, 1 + ((Game.nextPosInt() % 10 == 0) ? 1 : 0)));
			}
			else if (ev.getBlock().getType() ==  Material.GOLD_ORE ||
				ev.getBlock().getType() == Material.DEEPSLATE_GOLD_ORE)
			{
				ev.setDropItems(false);
				ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(),
					new ItemStack(Material.GOLD_INGOT, 1 + ((Game.nextPosInt() % 10 == 0) ? 1 : 0)));
			}
		}

		/**
		 * Instantly breaks logs
		 * @param ev Event
		 */
		@EventHandler
		public void onBlockDamage(BlockDamageEvent ev)
		{
			if (!Util.containsMaterial(Util.logBlocks, ev.getBlock().getType()))
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitBk))
				return;

			ev.setInstaBreak(true);
		}
	}
}
