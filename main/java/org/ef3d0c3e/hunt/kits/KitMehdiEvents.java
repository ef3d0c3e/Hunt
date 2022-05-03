package org.ef3d0c3e.hunt.kits;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.ef3d0c3e.hunt.Hunt;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.entities.MehdiBee;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Mehdi's kit's events
 */
public class KitMehdiEvents implements Listener
{
	/**
	 * Breaking flowers has a chance of dropping honey
	 * @param ev Event
	 */
	@EventHandler
	void onBlockBreak(BlockBreakEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.MEHDI)
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
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null || !ev.getItem().isSimilar(KitMehdi.honeyItem))
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.MEHDI)
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
	public void onEntityDeath(EntityDeathEvent ev)
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
	public void onEntityDamageByEntity(EntityDamageByEntityEvent ev)
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
			if (hp == null || hp.getKit() == null || hp.getKit().getID() != KitID.MEHDI)
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
	public KitMehdiEvents()
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
					if (hp.getKit() == null || hp.getKit().getID() != KitID.MEHDI)
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
