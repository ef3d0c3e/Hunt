package org.ef3d0c3e.hunt.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.ef3d0c3e.hunt.Items;
import org.ef3d0c3e.hunt.player.HuntPlayer;
import org.ef3d0c3e.hunt.skins.SkinMenu;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class Pause implements Listener
{
	@EventHandler
	public void onEntityInteract(final EntityInteractEvent ev)
	{
		if (ev.getEntity() instanceof Player && ((Player)ev.getEntity()).isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public void onFoodLevelChange(final FoodLevelChangeEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityAirChange(final EntityAirChangeEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public static void onEntityTarget(EntityTargetEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityMount(final EntityMountEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityCombust(final EntityCombustEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(final EntityDamageEvent ev)
	{
		if (ev instanceof EntityDamageByEntityEvent)
			return;
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent ev)
	{
		if (ev.getDamager() instanceof Player && (ev.getDamager().isOp()))
			return;
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityDismount(final EntityDismountEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityDropItem(final EntityDropItemEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(final PlayerDropItemEvent ev)
	{
		if (ev.getPlayer().isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityExhaust(final EntityExhaustionEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityPickupItem(final EntityPickupItemEvent ev)
	{
		if (ev.getEntity() instanceof Player && ((Player)ev.getEntity()).isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public void onEntityPlace(final EntityPlaceEvent ev)
	{
		if (ev.getEntity() instanceof Player && ((Player)ev.getEntity()).isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(final BlockBreakEvent ev)
	{
		if (((Player)ev.getPlayer()).isOp())
			return;

		ev.setCancelled(true);
	}

	@EventHandler
	public void onCraftItem(final CraftItemEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMoveEvent(final PlayerMoveEvent ev)
	{
		if (ev.getTo().getY() > 0)
			return;

		ev.setTo(Game.getOverworld().getSpawnLocation());
	}

	@EventHandler
	public void onEntityPortal(EntityPortalEvent ev)
	{
		ev.setCancelled(true);
	}

	@EventHandler
	public void onItemUse(final PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK || ev.getItem() == null)
			return;
		final HuntPlayer hp = HuntPlayer.getPlayer(ev.getPlayer());
		if (Items.ID.SKIN.is(ev.getItem()))
		{
			ev.setCancelled(true);
			hp.getPlayer().openInventory(new SkinMenu(hp, 0).getInventory());
		}
	}
}
