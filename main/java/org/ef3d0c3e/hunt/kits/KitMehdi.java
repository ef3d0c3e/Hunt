package org.ef3d0c3e.hunt.kits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.Bee;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.events.GameStartEvent;
import org.ef3d0c3e.hunt.events.HPDeathEvent;
import org.ef3d0c3e.hunt.events.HPKilledWrongEvent;
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

	static
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

	public ArrayList<MehdiBee> bees;
	LivingEntity target;

	public static class Events implements Listener
	{
		/**
		 * Kills bees
		 * @param ev Event
		 */
		@EventHandler
		public void onDeath(final HPDeathEvent ev)
		{
			final HuntPlayer hp = ev.getVictim();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
				return;

			final KitMehdi kit = (KitMehdi)hp.getKit();
			for (MehdiBee bee : kit.bees)
				bee.setRemoved(Entity.RemovalReason.DISCARDED);
			kit.bees.clear();
		}

		/**
		 * Resets bees aggro
		 * @param ev Event
		 */
		@EventHandler
		public void onKillWrong(final HPKilledWrongEvent ev)
		{
			final HuntPlayer hp = ev.getAttacker();
			if (hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
				return;

			final KitMehdi kit = (KitMehdi)hp.getKit();
			if (kit.target == null)
				return;

			kit.target = null;
			for (MehdiBee bee : kit.bees)
				bee.stopBeingAngry();
		}

		/**
		 * Breaking flowers has a chance of dropping honey
		 * @param ev Event
		 */
		@EventHandler
		void onBlockBreak(final BlockBreakEvent ev)
		{
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
				return;
			if (!Util.containsMaterial(Util.flowersBlocks, ev.getBlock().getType()))
				return;

			ev.setDropItems(false);
			if (Game.nextPosInt() % 4 == 0)
				ev.getBlock().getLocation().getWorld().dropItem(
					ev.getBlock().getLocation(),
					KitMehdi.honeyItem
				);
		}

		/**
		 * Spawns a bee when right-clicking with honeycomb
		 * @param ev Event
		 */
		@EventHandler
		public void onRightClick(final PlayerInteractEvent ev)
		{
			if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
			if (ev.getItem() == null || !ev.getItem().isSimilar(KitMehdi.honeyItem))
				return;
			final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
			if (hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
				return;

			// Stop if clicking an inventory
			if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
				Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
				return;

			ev.setCancelled(true);
			// Stop if player has too many alive bees
			if (((KitMehdi)hp.getKit()).bees.size() >= KitMehdi.MAX_BEES)
			{
				hp.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageFormat.format("§6Vous avez déjà {0} abeilles!", KitMehdi.MAX_BEES)));
				return;
			}

			ev.getItem().setAmount(ev.getItem().getAmount()-1);

			MehdiBee bee = new MehdiBee(hp.getPlayer().getLocation(), hp);
			bee.spawn();
			KitMehdi kit = (KitMehdi)hp.getKit();
			kit.bees.add(bee);
			if (kit.target != null)
				bee.startPersistentAngerTimer();
		}

		/**
		 * Subtracts from bee counter on bee's death
		 * @param ev
		 */
		@EventHandler
		public void onEntityDeath(final EntityDeathEvent ev)
		{
			if (!(((CraftEntity)ev.getEntity()).getHandle() instanceof MehdiBee))
				return;

			final HuntPlayer owner = ((MehdiBee)((CraftEntity)ev.getEntity()).getHandle()).getOwner();
			KitMehdi kit = (KitMehdi)owner.getKit();
			kit.bees.remove(((CraftEntity)ev.getEntity()).getHandle());
		}

		/**
		 * Prevents player from attacking his own bees & Make bees target player's attacked entities
		 * @param ev
		 */
		@EventHandler
		public void onEntityDamageByEntity(final EntityDamageByEntityEvent ev)
		{
			if (!(ev.getEntity() instanceof LivingEntity))
				return;

			// Player attacks a bee
			if (((CraftEntity)ev.getEntity()).getHandle() instanceof MehdiBee)
			{
				final HuntPlayer attacker = Util.getPlayerAttacker(ev);
				if (attacker == null)
					return;
				final HuntPlayer owner = ((MehdiBee)((CraftEntity)ev.getEntity()).getHandle()).getOwner();
				if (attacker == owner)
					ev.setCancelled(true);
			}
			else // Player attacks another entity
			{
				if (ev.getDamager() == ev.getEntity())
					return;
				final HuntPlayer hp = Util.getPlayerAttacker(ev);
				if (hp == null || hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
					return;
				if (ev.getEntity() instanceof Player) // Prevents attacking wrong players
				{
					final HuntPlayer target = Game.getPlayer(ev.getEntity().getName());
					if (!hp.canDamage(target))
						return;
				}

				KitMehdi kit = (KitMehdi)hp.getKit();
				kit.target = (LivingEntity)ev.getEntity();
				for (MehdiBee bee : ((KitMehdi) hp.getKit()).bees)
					bee.startPersistentAngerTimer();
			}
		}

		/**
		 * Timer task that resets aggro if bees are too far away from their target (or if their target is not found)
		 */
		@EventHandler
		public void onGameStart(final GameStartEvent ev)
		{
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					for (final HuntPlayer hp : Game.getPlayerList().values())
					{
						if (!hp.isOnline() || !hp.isAlive())
							continue;
						if (hp.getKit() == null || !(hp.getKit() instanceof KitMehdi))
							continue;

						final LivingEntity target = ((KitMehdi)hp.getKit()).target;

						boolean v = false;
						if (target != null && (target instanceof Player))
						{
							final HuntPlayer o = Game.getPlayer(target.getName());
							v = !o.isOnline() || !o.isAlive() || o.getPlayer().getGameMode() != GameMode.SURVIVAL;
						}


						if (target == null || v || target.getLocation().distanceSquared(hp.getPlayer().getLocation()) > 625.0)
						{
							((KitMehdi)hp.getKit()).target = null;
							for (MehdiBee bee : ((KitMehdi) hp.getKit()).bees)
								bee.stopBeingAngry();
						}
					}
				}
			}.runTaskTimer(Game.getPlugin(), 0, 60);
		}
	}
}
