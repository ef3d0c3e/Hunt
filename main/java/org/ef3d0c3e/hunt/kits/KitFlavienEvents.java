package org.ef3d0c3e.hunt.kits;

import com.comphenix.protocol.PacketType;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.world.item.alchemy.Potion;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.ef3d0c3e.hunt.Util;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.player.HuntPlayer;

/**
 * Flavien's kit's events
 */
public class KitFlavienEvents implements Listener
{
	/**
	 * Double jump
	 * @param ev Event
	 */
	@EventHandler
	public void onFly(PlayerToggleFlightEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.FLAVIEN)
			return;

		ev.setCancelled(true);
		hp.getPlayer().setAllowFlight(false);
		hp.getPlayer().setVelocity(hp.getPlayer().getVelocity().multiply(new Vector(2.0, 0.0, 2.0)).add(new Vector(0.0, 0.85, 0.0)));

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (!hp.getPlayer().isOnline())
					return;
				if (hp.getKit() == null || hp.getKit().getID() != KitID.FLAVIEN) // Player's kit may have changed
					return;

				hp.getPlayer().setAllowFlight(true);
			}
		}.runTaskLater(Game.getPlugin(), 40);
	}

	/**
	 * Allows flight for player when they connect
	 * @param ev Event
	 */
	@EventHandler
	public void onJoin(final PlayerJoinEvent ev)
	{
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (!hp.isAlive())
			return;
		if (hp.getKit() == null || hp.getKit().getID() != KitID.FLAVIEN)
			return;

		hp.getPlayer().setAllowFlight(true);
	}

	/**
	 * Consuming sugar gives speed
	 * @param ev Event
	 */
	@EventHandler
	public void onRightClick(PlayerInteractEvent ev)
	{
		if (ev.getAction() != Action.RIGHT_CLICK_AIR && ev.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if (ev.getItem() == null)
			return;
		if (ev.getItem().getType() != Material.SUGAR)
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getPlayer().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.FLAVIEN)
			return;

		// Stop if clicking an inventory
		if (!hp.getPlayer().isSneaking() && ev.getAction() == Action.RIGHT_CLICK_BLOCK &&
			Util.containsMaterial(Util.rightClickableBlocks, ev.getClickedBlock().getType()))
			return;

		ev.setCancelled(true);

		final PotionEffect current = hp.getPlayer().getPotionEffect(PotionEffectType.SPEED);
		int duration = 0;
		if (current != null)
		{
			if (current.getAmplifier() > 2)
				return;
			else if (current.getAmplifier() == 2)
				duration += current.getDuration();
		}

		ev.getItem().setAmount(ev.getItem().getAmount()-1);
		hp.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration + 100, 2));
	}

	/**
	 * Creepers drop firework when killed
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev)
	{
		if (!(ev.getEntity() instanceof Creeper))
			return;
		final HuntPlayer killer = Util.getPlayerKiller(ev.getEntity().getKiller());
		if (killer == null || killer.getKit() == null || killer.getKit().getID() != KitID.FLAVIEN)
			return;

		if (Game.nextPosInt() % 2 == 0)
			ev.getDrops().add(KitFlavien.fireworkItem);
		if (Game.nextPosInt() % 4 == 0)
			ev.getDrops().add(KitFlavien.fireworkItem);
	}

	/**
	 * Player is immune to fall damage
	 * @param ev Event
	 */
	@EventHandler
	public void onEntityDamage(EntityDamageEvent ev)
	{
		if (!(ev.getEntity() instanceof Player))
			return;
		if (ev.getCause() != EntityDamageEvent.DamageCause.FALL)
			return;
		final HuntPlayer hp = Game.getPlayer(ev.getEntity().getName());
		if (hp.getKit() == null || hp.getKit().getID() != KitID.FLAVIEN)
			return;

		ev.setCancelled(true);
	}
}
